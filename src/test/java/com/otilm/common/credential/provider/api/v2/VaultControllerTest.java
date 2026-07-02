package com.otilm.common.credential.provider.api.v2;

import com.otilm.common.credential.provider.api.VaultControllerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class VaultControllerTest {

    private final VaultControllerImpl vaultController = new VaultControllerImpl();

    @Test
    void testCheckConnection() {
        Assertions.assertDoesNotThrow( () -> vaultController.checkVaultConnection(List.of()));
    }

    @Test
    void testListAttributes() {
        Assertions.assertEquals(List.of(), vaultController.listVaultAttributes());
    }
}
