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
//        throw new RuntimeException("Not supported in this version");
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

        ConnectorInterfaceInfo connectorInterfaceInfoMetrics = new ConnectorInterfaceInfo();
        connectorInterfaceInfoHealth.setCode(ConnectorInterface.METRICS);
        connectorInterfaceInfoHealth.setVersion("v1");
        connectorInterfaceInfoHealth.setFeatures(List.of(FeatureFlag.OPEN_METRICS));

        ConnectorInterfaceInfo connectorInterfaceInfoSecret = new ConnectorInterfaceInfo();
        connectorInterfaceInfoSecret.setCode(ConnectorInterface.SECRET);
        connectorInterfaceInfoSecret.setVersion("v1");
        connectorInterfaceInfoSecret.setFeatures(List.of(FeatureFlag.STATELESS));

        InfoResponse infoResponse = new InfoResponse();
        infoResponse.setConnector(connectorInfo);
        infoResponse.setInterfaces(List.of(connectorInterfaceInfo, connectorInterfaceInfoHealth, connectorInterfaceInfoMetrics, connectorInterfaceInfoSecret));

        return infoResponse;
    }
}
