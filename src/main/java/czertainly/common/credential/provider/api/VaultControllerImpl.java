package czertainly.common.credential.provider.api;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.connector.secrets.VaultController;
import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VaultControllerImpl implements VaultController {
    
    @Override
    public void checkVaultConnection(List<RequestAttribute> attributes) throws NotFoundException {
        // since vaults are not implemented in this provider, connection check is not required
    }

    @Override
    public List<BaseAttribute> listVaultAttributes() throws NotFoundException {
        return List.of();
    }
}
