package com.otilm.common.credential.provider.dao.converter;

import com.otilm.api.model.connector.secrets.content.SecretContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otilm.common.credential.provider.util.SecretEncodingVersion;
import com.otilm.common.credential.provider.util.SecretsUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter
@Slf4j
public class SecretContentEncryptionConverter implements AttributeConverter<SecretContent, String> {

    private final ObjectMapper mapper = new ObjectMapper();
    private SecretsUtil secretsUtil;

    @Autowired
    public void setSecretsUtil(SecretsUtil secretsUtil) {
        this.secretsUtil = secretsUtil;
    }

    @Override
    public String convertToDatabaseColumn(SecretContent data) {
        try {
            String converted = mapper.writeValueAsString(data);
            return secretsUtil.encryptAndEncodeSecretString(converted, SecretEncodingVersion.V1);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting SecretContent to String", e);
        }
    }

    @Override
    public SecretContent convertToEntityAttribute(String data) {
        TypeReference<SecretContent> typeRef = new TypeReference<>() {
        };

        try {
            String decrypted = secretsUtil.decodeAndDecryptSecretString(data);
            return mapper.readValue(decrypted, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting String to SecretContent", e);
        }
    }
}
