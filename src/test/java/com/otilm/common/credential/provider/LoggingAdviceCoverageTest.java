package com.otilm.common.credential.provider;

import com.otilm.api.interfaces.connector.common.v2.AttributesController;
import com.otilm.api.interfaces.connector.secrets.SecretController;
import com.otilm.api.interfaces.connector.secrets.VaultController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Guards against a package move silently dropping controllers from LoggingAdvice's
 * {@code within(...)} pointcut: if a controller is advised it is an AOP proxy.
 */
@SpringBootTest
@Import(BuildInfoTestConfig.class)
class LoggingAdviceCoverageTest {

    @Autowired
    private SecretController secretController;

    @Autowired
    private VaultController vaultController;

    @Autowired
    private AttributesController attributesController;

    @Test
    void secretAndVaultControllersAreAdvisedByLoggingAdvice() {
        Assertions.assertTrue(AopUtils.isAopProxy(secretController),
                "SecretController must be advised by LoggingAdvice");
        Assertions.assertTrue(AopUtils.isAopProxy(vaultController),
                "VaultController must be advised by LoggingAdvice");
        Assertions.assertTrue(AopUtils.isAopProxy(attributesController),
                "AttributesController must be advised by LoggingAdvice");
    }
}
