package com.otilm.common.credential.provider.secret.service.impl;

import com.otilm.api.exception.AlreadyExistException;
import com.otilm.api.exception.NotFoundException;
import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.v3.content.StringAttributeContentV3;
import com.otilm.api.model.connector.secrets.CreateSecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretContentResponseDto;
import com.otilm.api.model.connector.secrets.SecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretResponseDto;
import com.otilm.api.model.connector.secrets.SecretType;
import com.otilm.api.model.connector.secrets.UpdateSecretRequestDto;
import com.otilm.common.credential.provider.secret.dao.entity.Secret;
import com.otilm.common.credential.provider.secret.dao.entity.SecretCompositeId;
import com.otilm.common.credential.provider.secret.dao.repository.SecretRepository;
import com.otilm.common.credential.provider.secret.service.SecretService;
import com.otilm.common.credential.provider.secret.mapper.SecretMapper;
import com.otilm.core.util.AttributeDefinitionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SecretServiceImpl implements SecretService {

    private final SecretRepository secretRepository;

    @Autowired
    public SecretServiceImpl(SecretRepository secretRepository) {
        this.secretRepository = secretRepository;
    }

    @Override
    @Transactional
    public SecretResponseDto createSecret(CreateSecretRequestDto request) throws AlreadyExistException {
        String name = request.getName();
        SecretType secretType = request.getSecret().getType();
        String namespace = resolveNamespace(request.getVaultAttributes());
        log.debug("Creating secret {}/{}/{}", namespace, name, secretType);
        ensureSecretDoesNotExist(namespace, name, secretType);

        Secret entity = SecretMapper.toEntity(request, namespace);
        try {
            // Flush here so a PK collision (concurrent create in the same scope) surfaces now as a
            // clean AlreadyExistException instead of a raw DataIntegrityViolationException at commit.
            entity = secretRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistException(Secret.class, formatSecretKey(namespace, name, secretType));
        }

        SecretResponseDto response = SecretMapper.toResponse(entity);
        log.debug("Secret {}/{}/{}/{} created", namespace, name, secretType, entity.getId().getVersion());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getType();

        String namespace = resolveNamespace(request.getVaultAttributes());

        Secret entity;
        if (version != null && !version.isEmpty()) {
            int numericVersion;
            try {
                numericVersion = Integer.parseInt(version);
            } catch (NumberFormatException e) {
                throw new NotFoundException(Secret.class, formatSecretKey(namespace, name, secretType, version));
            }
            log.debug("Getting content of secret {}/{}/{}", name, secretType, version);
            entity = secretRepository.findByNamespaceAndNameAndSecretTypeAndVersion(namespace, name, secretType, numericVersion)
                    .orElseThrow(() -> new NotFoundException(Secret.class, formatSecretKey(namespace, name, secretType, String.valueOf(numericVersion))));
        } else {
            version = "latest";
            log.debug("Getting content of secret {}/{}/latest", name, secretType);
            var allVersions = secretRepository.findByNamespaceAndNameAndSecretTypeOrderByVersionDesc(namespace, name, secretType);
            if (allVersions.isEmpty()) {
                throw new NotFoundException(Secret.class, formatSecretKey(namespace, name, secretType));
            }
            entity = allVersions.getFirst();
        }

        SecretContentResponseDto response = SecretMapper.toContentResponse(entity);
        log.debug("Retrieved content of secret {}/{}/{}", name, secretType, version);
        return response;
    }

    @Override
    @Transactional
    public SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getSecret().getType();
        log.debug("Updating secret {}/{}", name, secretType);

        // Get the latest version with pessimistic locking (SELECT FOR UPDATE) to prevent race conditions.
        // This ensures that between reading the latest version and creating the new version,
        // no other transaction can create a duplicate secret with the same name, secret type and version.
        String namespace = resolveNamespace(request.getVaultAttributes());
        Secret latestSecret = secretRepository.findLatestVersionForUpdate(namespace, name, secretType, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new NotFoundException(Secret.class, formatSecretKey(namespace, name, secretType)));

        // Create a new entity with an incremented version, in the same scope.
        Secret newEntity = Secret.builder()
                .id(new SecretCompositeId(namespace, name, secretType, latestSecret.getId().getVersion()))
                .secretContent(request.getSecret())
                .vaultAttributes(request.getVaultAttributes())
                .secretAttributes(request.getSecretAttributes())
                .build();

        newEntity.incrementVersion();
        newEntity = secretRepository.save(newEntity);

        SecretResponseDto response = SecretMapper.toResponse(newEntity);
        log.debug("Updated secret {}/{}/{}", name, secretType, newEntity.getId().getVersion());
        return response;
    }

    @Override
    @Transactional
    public void deleteSecret(SecretRequestDto request) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getType();
        log.debug("Deleting secret {}/{}", name, secretType);

        String namespace = resolveNamespace(request.getVaultAttributes());
        long deleted = secretRepository.deleteByNamespaceAndNameAndSecretType(namespace, name, secretType);
        if (deleted > 0) {
            log.debug("Deleted {} versions of secret {}/{}", deleted, name, secretType);
            return;
        }

        throw new NotFoundException(Secret.class, formatSecretKey(namespace, name, secretType));
    }

    @Override
    @Transactional
    public SecretResponseDto rotateSecret(SecretRequestDto request) throws NotFoundException {
        throw new UnsupportedOperationException("Rotation not supported in common-credential-provider");
    }

    @Override
    public List<BaseAttribute> getRotateAttributes() throws NotFoundException {
        // "Rotate secret" operation is unsupported, so we don't need any attributes for that.
        return List.of();
    }

    @Override
    public List<BaseAttribute> getSecretAttributes(SecretType secretType) {
        // This provider does not need any special attributes for managing a given secret type.
        return List.of();
    }

    /**
     * Resolves the scope from the vault-instance namespace attribute. Returns "" (the root scope)
     * when no namespace is configured; trims so incidental whitespace can't split a scope.
     */
    private static String resolveNamespace(List<RequestAttribute> vaultAttributes) {
        if (vaultAttributes == null || vaultAttributes.isEmpty()) {
            return "";
        }
        List<StringAttributeContentV3> content = AttributeDefinitionUtils.getAttributeContentValue(
                VaultAttributeServiceImpl.ATTRIBUTE_NAMESPACE, vaultAttributes, StringAttributeContentV3.class);
        if (content == null || content.isEmpty() || content.get(0) == null) {
            return "";
        }
        return StringUtils.trimToEmpty(content.get(0).getData());
    }

    private void ensureSecretDoesNotExist(String namespace, String name, SecretType secretType) throws AlreadyExistException {
        var existingVersions = secretRepository.findByNamespaceAndNameAndSecretTypeOrderByVersionDesc(namespace, name, secretType);
        if (!existingVersions.isEmpty()) {
            throw new AlreadyExistException(Secret.class, formatSecretKey(namespace, name, secretType));
        }
    }

    private String formatSecretKey(String namespace, String name, SecretType secretType) {
        return namespace + "/" + name + "/" + secretType;
    }

    private String formatSecretKey(String namespace, String name, SecretType secretType, String version) {
        return namespace + "/" + name + "/" + secretType + "/" + version;
    }
}
