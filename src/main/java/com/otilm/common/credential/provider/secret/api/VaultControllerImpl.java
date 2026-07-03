package com.otilm.common.credential.provider.secret.api;


import com.otilm.api.interfaces.connector.secrets.VaultController;
import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.ConnectorV2Api;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ConnectorV2Api
public class VaultControllerImpl implements VaultController {

    @Override
    public void checkVaultConnection(List<RequestAttribute> attributes) {
        // since vaults are not implemented in this provider, a connection check is not required
    }

    @Override
    public List<BaseAttribute> listVaultAttributes() {
        return List.of();
    }

    @Override
    public List<BaseAttribute> listVaultProfileAttributes(List<RequestAttribute> attributes) {
        return List.of();
    }
}
