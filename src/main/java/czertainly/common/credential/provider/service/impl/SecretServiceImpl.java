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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecretServiceImpl implements SecretService {

    private final SecretRepository secretRepository;

    @Override
    @Transactional
    public SecretResponseDto createSecret(CreateSecretRequestDto request) throws AlreadyExistException {
        String name = request.getName();
        SecretType secretType = request.getSecret().getType();
        log.debug("Creating secret {}/{}", name, secretType);
        Optional<Secret> existing = secretRepository.findByNameAndSecretType(name, secretType);
        if (existing.isPresent()) {
            throw new AlreadyExistException(Secret.class, name + "/" + secretType);
        }

        Secret entity = Secret.builder()
                .name(request.getName())
                .secretVersion("1")
                .secretType(request.getSecret().getType())
                .secretContent(request.getSecret())
                .secretAttributes(request.getSecretAttributes())
                .vaultAttributes(request.getVaultAttributes())
                .build();
        entity = secretRepository.save(entity);

        SecretResponseDto response = SecretResponseDto.builder()
                .name(entity.getName())
                .type(entity.getSecretType())
                .version(entity.getSecretVersion())
                .build();
        log.debug("Secret {}/{} created", name, secretType);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getType();
        log.debug("Getting content of secret {}/{}", name, secretType);
        Secret entity = secretRepository.findByNameAndSecretType(name, secretType)
                .orElseThrow(() -> new NotFoundException(Secret.class, name + "/" + secretType));

        SecretContentResponseDto response = SecretContentResponseDto.builder()
                .version(entity.getSecretVersion())
                .content(entity.getSecretContent())
                .build();
        log.debug("Retrieved content of secret {}/{}", name, secretType);
        return response;
    }


    @Override
    @Transactional
    public SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException {
        String name = request.getName();
        SecretType secretType = request.getSecret().getType();
        log.debug("Updating secret {}/{}", name, secretType);
        Secret entity = secretRepository.findByNameAndSecretType(name, secretType)
                .orElseThrow(() -> new NotFoundException(Secret.class, name + "/" + secretType));

        try {
            var existingVersion = Integer.parseInt(entity.getSecretVersion());
            entity.setSecretVersion(String.valueOf(existingVersion + 1));
        } catch (NumberFormatException e) {
            // ignore version incrementing
        }
        entity.setSecretType(request.getSecret().getType());
        entity.setSecretContent(request.getSecret());
        entity.setVaultAttributes(request.getVaultAttributes());
        entity.setSecretAttributes(request.getSecretAttributes());
        entity = secretRepository.save(entity);

        SecretResponseDto response = SecretResponseDto.builder()
                .name(entity.getName())
                .type(entity.getSecretType())
                .version(entity.getSecretVersion())
                .build();
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

        throw new NotFoundException(Secret.class, name + "/" + secretType);
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
}
