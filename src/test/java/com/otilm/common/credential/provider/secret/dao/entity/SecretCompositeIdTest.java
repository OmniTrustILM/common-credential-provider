package com.otilm.common.credential.provider.secret.dao.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * Test class for SecretCompositeId to verify equals and hashCode contracts.
 */
class SecretCompositeIdTest {

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(SecretCompositeId.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
