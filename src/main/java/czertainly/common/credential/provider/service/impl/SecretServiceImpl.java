package czertainly.common.credential.provider.service.impl;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import com.czertainly.api.model.connector.secrets.CreateSecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretContentResponseDto;
import com.czertainly.api.model.connector.secrets.SecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretResponseDto;
import com.czertainly.api.model.connector.secrets.SecretType;
import com.czertainly.api.model.connector.secrets.UpdateSecretRequestDto;
import czertainly.common.credential.provider.dao.entity.Secret;
import czertainly.common.credential.provider.dao.repository.SecretRepository;
import czertainly.common.credential.provider.service.SecretService;
import czertainly.common.credential.provider.mapper.SecretMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.debug("Creating secret {}/{}", name, secretType);
        ensureSecretDoesNotExist(name, secretType);

        Secret entity = SecretMapper.toEntity(request);
        entity = secretRepository.save(entity);

        SecretResponseDto response = SecretMapper.toResponse(entity);
        log.debug("Secret {}/{} created", name, secretType);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getType();
        log.debug("Getting content of secret {}/{}", name, secretType);
        // Versioning is not supported by this provider, so the latest is always returned.
        Secret entity = getSecretOrThrow(name, secretType);

        SecretContentResponseDto response = SecretMapper.toContentResponse(entity);
        log.debug("Retrieved content of secret {}/{}", name, secretType);
        return response;
    }

    @Override
    @Transactional
    public SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getSecret().getType();
        log.debug("Updating secret {}/{}", name, secretType);
        Secret entity = getSecretOrThrow(name, secretType);

        entity.incrementVersionIfNumeric();
        entity.setSecretType(request.getSecret().getType());
        entity.setSecretContent(request.getSecret());
        entity.setVaultAttributes(request.getVaultAttributes());
        entity.setSecretAttributes(request.getSecretAttributes());
        entity = secretRepository.save(entity);

        SecretResponseDto response = SecretMapper.toResponse(entity);
        log.debug("Updated secret {}/{}", name, secretType);
        return response;
    }

    @Override
    @Transactional
    public void deleteSecret(SecretRequestDto request) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getType();
        log.debug("Deleting secret {}/{}", name, secretType);

        long deleted = secretRepository.deleteByNameAndSecretType(name, secretType);
        if (deleted > 0) {
            log.debug("Deleted secret {}/{}", name, secretType);
            return;
        }

        throw new NotFoundException(Secret.class, formatSecretKey(name, secretType));
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

    private void ensureSecretDoesNotExist(String name, SecretType secretType) throws AlreadyExistException {
        if (secretRepository.findByNameAndSecretType(name, secretType).isPresent()) {
            throw new AlreadyExistException(Secret.class, formatSecretKey(name, secretType));
        }
    }

    private Secret getSecretOrThrow(String name, SecretType secretType) throws NotFoundException {
        return secretRepository.findByNameAndSecretType(name, secretType)
                .orElseThrow(() -> new NotFoundException(Secret.class, formatSecretKey(name, secretType)));
    }

    private String formatSecretKey(String name, SecretType secretType) {
        return name + "/" + secretType;
    }
}