package com.otilm.common.credential.provider.api;

import com.otilm.api.interfaces.connector.HealthController;
import com.otilm.api.model.common.HealthDto;
import com.otilm.api.model.common.HealthStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthControllerImpl implements HealthController {

    @Override
    public HealthDto checkHealth() {
        HealthDto health = new HealthDto();
        health.setStatus(HealthStatus.OK);
        return health;
    }
}
