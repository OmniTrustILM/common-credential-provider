package com.otilm.common.credential.provider.secret.api;


import com.otilm.api.interfaces.connector.secrets.VaultController;
import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.ConnectorV2Api;
import com.otilm.common.credential.provider.secret.service.VaultAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ConnectorV2Api
public class VaultControllerImpl implements VaultController {

    private final VaultAttributeService vaultAttributeService;

    @Autowired
    public VaultControllerImpl(VaultAttributeService vaultAttributeService) {
        this.vaultAttributeService = vaultAttributeService;
    }

    @Override
    public void checkVaultConnection(List<RequestAttribute> attributes) {
        // since vaults are not implemented in this provider, a connection check is not required
    }

    @Override
    public List<BaseAttribute> listVaultAttributes() {
        return vaultAttributeService.listVaultAttributes();
    }

    @Override
    public List<BaseAttribute> listVaultProfileAttributes(List<RequestAttribute> attributes) {
        return vaultAttributeService.listVaultProfileAttributes(attributes);
    }
}
