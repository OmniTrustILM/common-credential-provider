package czertainly.common.credential.provider.api;

import com.czertainly.api.interfaces.connector.secrets.VaultController;
import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import czertainly.common.credential.provider.ConnectorV2Api;
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
}
