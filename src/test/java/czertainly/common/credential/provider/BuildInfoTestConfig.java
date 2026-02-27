package czertainly.common.credential.provider;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@TestConfiguration
public class BuildInfoTestConfig {
    @Bean
    BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "test");
        return new BuildProperties(properties);
    }
}
