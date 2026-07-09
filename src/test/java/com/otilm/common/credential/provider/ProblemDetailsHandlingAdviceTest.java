package com.otilm.common.credential.provider;

import com.otilm.common.credential.provider.secret.api.v2.AttributeCallbackNotSupportedException;
import com.otilm.common.credential.provider.secret.api.v2.AttributeDefinitionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemDetailsHandlingAdviceTest {

    private final ProblemDetailsHandlingAdvice advice = new ProblemDetailsHandlingAdvice();

    @Test
    void attributeDefinitionNotFoundMapsTo404WithAttributeErrorCode() {
        UUID uuid = UUID.randomUUID();
        ProblemDetail problem = advice.handleAttributeDefinitionNotFound(
                new AttributeDefinitionNotFoundException(uuid));

        assertEquals(404, problem.getStatus());
        assertTrue(problem.getType().toString().endsWith("ATTRIBUTE_DEFINITION_NOT_FOUND"),
                "type must carry the ATTRIBUTE_DEFINITION_NOT_FOUND code, was " + problem.getType());
    }

    @Test
    void attributeCallbackNotSupportedMapsTo422WithValidationCode() {
        ProblemDetail problem = advice.handleAttributeCallbackNotSupported(
                new AttributeCallbackNotSupportedException(UUID.randomUUID()));

        assertEquals(422, problem.getStatus());
        assertTrue(problem.getType().toString().endsWith("VALIDATION_FAILED"),
                "type must carry the VALIDATION_FAILED code, was " + problem.getType());
    }
}
