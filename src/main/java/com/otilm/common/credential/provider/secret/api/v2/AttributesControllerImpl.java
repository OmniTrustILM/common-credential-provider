package com.otilm.common.credential.provider.secret.api.v2;

import com.otilm.api.interfaces.connector.common.v2.AttributesController;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackResponseDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.ConnectorV2Api;
import com.otilm.common.credential.provider.secret.service.SecretProviderAttributesService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("attributesControllerV2")
@ConnectorV2Api
public class AttributesControllerImpl implements AttributesController {

    private final SecretProviderAttributesService attributesService;

    public AttributesControllerImpl(SecretProviderAttributesService attributesService) {
        this.attributesService = attributesService;
    }

    @Override
    public AttributeDefinitionsDto listDefinitions(List<UUID> uuids) {
        return attributesService.listDefinitions(uuids);
    }

    @Override
    public BaseAttribute getDefinition(UUID uuid) {
        return attributesService.getDefinition(uuid);
    }

    @Override
    public AttributeCallbackResponseDto callback(AttributeCallbackRequestDto request) {
        return attributesService.callback(request);
    }
}
