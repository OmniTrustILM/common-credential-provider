package com.otilm.common.credential.provider.api;

import com.otilm.api.exception.AlreadyExistException;
import com.otilm.api.exception.NotFoundException;
import com.otilm.api.interfaces.connector.secrets.SecretController;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.connector.secrets.CreateSecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretContentResponseDto;
import com.otilm.api.model.connector.secrets.SecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretResponseDto;
import com.otilm.api.model.connector.secrets.SecretType;
import com.otilm.api.model.connector.secrets.UpdateSecretRequestDto;
import com.otilm.common.credential.provider.ConnectorV2Api;
import com.otilm.common.credential.provider.service.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/secretProvider/secrets")
@ConnectorV2Api
public class SecretControllerImpl implements SecretController {


    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(SecretType.class, new SecretTypeConverter());
    }


    private final SecretService secretService;

    @Autowired
    public SecretControllerImpl(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException {
        return secretService.getSecretContent(request, version);
    }

    @Override
    public SecretResponseDto createSecret(CreateSecretRequestDto request) throws AlreadyExistException {
        return secretService.createSecret(request);
    }

    @Override
    public SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException {
        return secretService.updateSecret(request);
    }

    @Override
    public void deleteSecret(SecretRequestDto request) throws NotFoundException {
        secretService.deleteSecret(request);
    }

    @Override
    public SecretResponseDto rotateSecret(SecretRequestDto request) throws NotFoundException {
        return secretService.rotateSecret(request);
    }

    @Override
    public List<BaseAttribute> getRotateAttributes() throws NotFoundException {
        return secretService.getRotateAttributes();
    }

    @Override
    public List<BaseAttribute> getSecretAttributes(SecretType secretType) {
        return secretService.getSecretAttributes(secretType);
    }
}
