package com.otilm.common.credential.provider.secret.api;

import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.secret.service.impl.VaultAttributeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class VaultControllerTest {

    private final VaultControllerImpl vaultController = new VaultControllerImpl(new VaultAttributeServiceImpl());

    @Test
    void testCheckConnection() {
        Assertions.assertDoesNotThrow(() -> vaultController.checkVaultConnection(List.of()));
    }

    @Test
    void testListVaultAttributes_ReturnsNamespaceAttribute() {
        List<BaseAttribute> attributes = vaultController.listVaultAttributes();

        Assertions.assertEquals(1, attributes.size());
        Assertions.assertEquals(VaultAttributeServiceImpl.ATTRIBUTE_NAMESPACE, attributes.get(0).getName());
    }

    @Test
    void testListVaultProfileAttributes_IsEmpty() {
        Assertions.assertEquals(List.of(), vaultController.listVaultProfileAttributes(List.of()));
    }
}
