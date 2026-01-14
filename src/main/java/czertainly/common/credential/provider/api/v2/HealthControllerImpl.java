package czertainly.common.credential.provider.api.v2;

import com.czertainly.api.interfaces.connector.common.v2.HealthController;
import com.czertainly.api.model.client.connector.v2.*;
import org.springframework.web.bind.annotation.RestController;

@RestController("HealthControllerV2")
public class HealthControllerImpl implements HealthController {

    @Override
    public HealthInfo checkHealth() {
        HealthInfo healthInfo = new HealthInfo();
        healthInfo.setStatus(HealthStatus.UP);
        healthInfo.setDescription("Connector is operational");

        return healthInfo;
    }
}
