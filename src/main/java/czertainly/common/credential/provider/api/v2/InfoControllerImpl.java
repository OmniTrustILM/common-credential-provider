package czertainly.common.credential.provider.api.v2;

import com.czertainly.api.interfaces.connector.common.v2.InfoController;
import com.czertainly.api.model.client.connector.v2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("InfoControllerV2")
public class InfoControllerImpl implements InfoController {

    @Autowired
    private BuildProperties buildProperties;

    @Override
    public InfoResponse getConnectorInfo() {
        ConnectorInfo connectorInfo = new ConnectorInfo();
        connectorInfo.setId("czertainly.common.credential.provider");
        connectorInfo.setName("Common Credential Provider");
        connectorInfo.setVersion(buildProperties.getVersion());

        ConnectorInterfaceInfo connectorInterfaceInfo = new ConnectorInterfaceInfo();
        connectorInterfaceInfo.setCode(ConnectorInterface.INFO);
        connectorInterfaceInfo.setVersion("v2");

        ConnectorInterfaceInfo connectorInterfaceInfoHealth = new ConnectorInterfaceInfo();
        connectorInterfaceInfoHealth.setCode(ConnectorInterface.HEALTH);
        connectorInterfaceInfoHealth.setVersion("v2");

        ConnectorInterfaceInfo connectorInterfaceInfoCredential = new ConnectorInterfaceInfo();
        connectorInterfaceInfoCredential.setCode(ConnectorInterface.CREDENTIAL_PROVIDER);
        connectorInterfaceInfoCredential.setVersion("v1");
        connectorInterfaceInfoCredential.setFeatures(List.of(CommonFeatureFlag.STATELESS));

        InfoResponse infoResponse = new InfoResponse();
        infoResponse.setConnectorInfo(connectorInfo);
        infoResponse.setInterfaces(List.of(connectorInterfaceInfo, connectorInterfaceInfoHealth, connectorInterfaceInfoCredential));

        return infoResponse;
    }
}
