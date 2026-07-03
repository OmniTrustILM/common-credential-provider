package com.otilm.common.credential.provider.api.v2;

import com.otilm.api.interfaces.connector.common.v2.InfoController;
import com.otilm.api.model.client.connector.v2.*;
import com.otilm.common.credential.provider.Application;
import com.otilm.common.credential.provider.ConnectorV2Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("infoControllerV2")
@ConnectorV2Api
public class InfoControllerImpl implements InfoController {

    private final BuildProperties buildProperties;

    @Autowired
    public InfoControllerImpl(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public InfoResponse getConnectorInfo() {
        ConnectorInfo connectorInfo = new ConnectorInfo();
        connectorInfo.setId(Application.class.getPackageName());
        connectorInfo.setName("Common Credential Provider");
        connectorInfo.setVersion(buildProperties.getVersion());

        ConnectorInterfaceInfo connectorInterfaceInfo = new ConnectorInterfaceInfo();
        connectorInterfaceInfo.setCode(ConnectorInterface.INFO);
        connectorInterfaceInfo.setVersion("v2");

        ConnectorInterfaceInfo connectorInterfaceInfoHealth = new ConnectorInterfaceInfo();
        connectorInterfaceInfoHealth.setCode(ConnectorInterface.HEALTH);
        connectorInterfaceInfoHealth.setVersion("v2");

        ConnectorInterfaceInfo connectorInterfaceInfoMetrics = new ConnectorInterfaceInfo();
        connectorInterfaceInfoMetrics.setCode(ConnectorInterface.METRICS);
        connectorInterfaceInfoMetrics.setVersion("v1");
        connectorInterfaceInfoMetrics.setFeatures(List.of(FeatureFlag.OPEN_METRICS));

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
