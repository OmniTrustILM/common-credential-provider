package com.otilm.common.credential.provider.secret.api.v2;

import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.secret.service.SecretProviderAttributesService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttributesControllerImplTest {

    private final SecretProviderAttributesService service = mock(SecretProviderAttributesService.class);
    private final AttributesControllerImpl controller = new AttributesControllerImpl(service);

    @Test
    void listDefinitionsDelegatesToService() {
        AttributeDefinitionsDto dto = new AttributeDefinitionsDto();
        when(service.listDefinitions(isNull())).thenReturn(dto);

        assertSame(dto, controller.listDefinitions(null));
    }

    @Test
    void getDefinitionDelegatesToService() {
        UUID uuid = UUID.randomUUID();
        BaseAttribute def = mock(BaseAttribute.class);
        when(service.getDefinition(uuid)).thenReturn(def);

        assertSame(def, controller.getDefinition(uuid));
        verify(service).getDefinition(uuid);
    }
}
