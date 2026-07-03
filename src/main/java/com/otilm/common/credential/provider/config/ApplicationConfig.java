package com.otilm.common.credential.provider.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.security.Provider;
import java.security.Security;

@Configuration
@EnableJpaAuditing
@ComponentScan(basePackages = "com.otilm.common.credential.provider")
@Slf4j
public class ApplicationConfig {
    @PostConstruct
    public void registerSecurityProvider() {
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            log.info("Registering security provider {}.", BouncyCastleProvider.PROVIDER_NAME);
            Security.addProvider(new BouncyCastleProvider());
        } else {
            log.info("Security provider {} already registered.", BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}