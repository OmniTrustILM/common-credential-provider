package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.AttributeType;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.content.AttributeContentType;
import com.otilm.api.model.common.attribute.v3.DataAttributeV3;
import com.otilm.common.credential.provider.secret.api.v2.AttributeDefinitionNotFoundException;
import com.otilm.common.credential.provider.secret.service.impl.SecretProviderAttributesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecretProviderAttributesServiceTest {

    private static final UUID KNOWN = UUID.fromString("d18a014f-cfb9-4e72-8acc-4616e14fe8ad");

    private final VaultAttributeService vaultAttributeService = mock(VaultAttributeService.class);
    private SecretProviderAttributesServiceImpl service;
    private DataAttributeV3 namespaceDef;

    @BeforeEach
    void setup() {
        namespaceDef = new DataAttributeV3();
        namespaceDef.setUuid(KNOWN.toString());
        namespaceDef.setName("data_namespace");
        namespaceDef.setType(AttributeType.DATA);
        namespaceDef.setContentType(AttributeContentType.STRING);
        when(vaultAttributeService.listVaultAttributes()).thenReturn(List.of(namespaceDef));

        Properties props = new Properties();
        props.setProperty("version", "1.2.3");
        service = new SecretProviderAttributesServiceImpl(new BuildProperties(props), vaultAttributeService);
    }

    @Test
    void listDefinitionsWithoutFilterReturnsFullRegistryAndConnectorVersion() {
        AttributeDefinitionsDto dto = service.listDefinitions(null);
        assertEquals("1.2.3", dto.getConnectorVersion());
        assertEquals(1, dto.getDefinitions().size());
        assertSame(namespaceDef, dto.getDefinitions().get(0));
    }

    @Test
    void listDefinitionsFilterKeepsKnownDropsUnknown() {
        AttributeDefinitionsDto known = service.listDefinitions(List.of(KNOWN));
        assertEquals(1, known.getDefinitions().size());

        AttributeDefinitionsDto unknown = service.listDefinitions(List.of(UUID.randomUUID()));
        assertTrue(unknown.getDefinitions().isEmpty());
    }

    @Test
    void getDefinitionReturnsSameObjectForKnownUuid() {
        assertSame(namespaceDef, service.getDefinition(KNOWN));
    }

    @Test
    void getDefinitionThrowsForUnknownUuid() {
        assertThrows(AttributeDefinitionNotFoundException.class,
                () -> service.getDefinition(UUID.randomUUID()));
    }

    @Test
    void callbackAlwaysThrowsBecauseNoAttributeIsDispatchable() {
        AttributeCallbackRequestDto req = new AttributeCallbackRequestDto();
        req.setAttributeUuid(KNOWN);
        assertThrows(AttributeDefinitionNotFoundException.class, () -> service.callback(req));
    }
}
