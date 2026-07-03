package com.otilm.common.credential.provider.secret.dao.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * Test class for Secret entity to verify equals and hashCode contracts.
 */
class SecretTest {

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Secret.class)
                .suppress(Warning.SURROGATE_KEY)
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                .verify();
    }
}
