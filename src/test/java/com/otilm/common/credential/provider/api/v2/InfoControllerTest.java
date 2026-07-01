package com.otilm.common.credential.provider.api.v2;

import com.otilm.api.model.client.connector.v2.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class InfoControllerTest {

    private InfoControllerImpl infoController;

    @BeforeEach
    void setup() {
        Properties props = new Properties();
        props.setProperty("version", "1.0.0");
        BuildProperties buildProperties = new BuildProperties(props);
        infoController = new InfoControllerImpl(buildProperties);
    }

    @Test
    void testGetConnectorInfo_ReturnsCorrectConnectorInfo() {
        InfoResponse response = infoController.getConnectorInfo();

        assertNotNull(response);
        assertNotNull(response.getConnector());
    }
}