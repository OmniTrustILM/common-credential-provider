package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.exception.AlreadyExistException;
import com.otilm.api.exception.NotFoundException;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.connector.secrets.CreateSecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretContentResponseDto;
import com.otilm.api.model.connector.secrets.SecretRequestDto;
import com.otilm.api.model.connector.secrets.SecretResponseDto;
import com.otilm.api.model.connector.secrets.SecretType;
import com.otilm.api.model.connector.secrets.UpdateSecretRequestDto;

import java.util.List;

public interface SecretService {
    // CRUD
    SecretResponseDto createSecret(CreateSecretRequestDto request) throws AlreadyExistException;
    SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException;
    SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException;
    void deleteSecret(SecretRequestDto request) throws NotFoundException;

    SecretResponseDto rotateSecret(SecretRequestDto request) throws NotFoundException;
    List<BaseAttribute> getRotateAttributes() throws NotFoundException;
    List<BaseAttribute> getSecretAttributes(SecretType secretType);
}
