package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.content.AttributeContentType;
import com.otilm.api.model.common.attribute.v2.DataAttributeV2;
import com.otilm.common.credential.provider.secret.service.impl.VaultAttributeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class VaultAttributeServiceTest {

    private final VaultAttributeService vaultAttributeService = new VaultAttributeServiceImpl();

    @Test
    void listVaultAttributes_returnsOptionalNamespaceStringAttribute() {
        List<BaseAttribute> attributes = vaultAttributeService.listVaultAttributes();

        Assertions.assertEquals(1, attributes.size());
        DataAttributeV2 namespace = (DataAttributeV2) attributes.get(0);
        Assertions.assertEquals(VaultAttributeServiceImpl.ATTRIBUTE_NAMESPACE, namespace.getName());
        Assertions.assertEquals(AttributeContentType.STRING, namespace.getContentType());
        Assertions.assertFalse(namespace.getProperties().isRequired(), "namespace attribute must be optional");
    }

    @Test
    void listVaultProfileAttributes_isEmpty() {
        Assertions.assertEquals(List.of(), vaultAttributeService.listVaultProfileAttributes(List.of()));
    }
}
