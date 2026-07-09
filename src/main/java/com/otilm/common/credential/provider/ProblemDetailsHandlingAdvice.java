package com.otilm.common.credential.provider;

import com.otilm.api.exception.*;
import com.otilm.api.model.common.error.ErrorCode;
import com.otilm.api.model.common.error.ProblemDetailExtended;
import com.otilm.common.credential.provider.secret.api.v2.AttributeDefinitionNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 1. Target only connector V2 controllers to avoid interfering with global exception handling in other parts of the application
// 2. HIGHEST_PRECEDENCE ensures this is chosen over global ExceptionHandlingAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = ConnectorV2Api.class)
public class ProblemDetailsHandlingAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemDetailsHandlingAdvice.class);

    @Override
    protected ResponseEntity<@NotNull Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Validation error: ");
        ex.getBindingResult().getFieldErrors().forEach(
                err -> messageBuilder.append(err.getField()).append(" ").append(err.getDefaultMessage()).append(", ")
        );
        // remote trailing comma and space
        messageBuilder.delete(messageBuilder.length() - 2, messageBuilder.length());
        LOG.error("Validation error occurred: {}", messageBuilder, ex);
        ProblemDetail problemDetail = ProblemDetailExtended.fromErrorCode(
                ErrorCode.VALIDATION_FAILED,
                messageBuilder.toString(),
                null,
                null
        );
        return new ResponseEntity<>(problemDetail, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        LOG.error("Validation error occurred: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.VALIDATION_FAILED, ex.getMessage(), null, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex) {
        LOG.error("Resource not found: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), null, null);
    }
    
    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleNotSupportedException(UnsupportedOperationException ex) {
        LOG.error("Not Supported error occurred: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.OPERATION_NOT_SUPPORTED, ex.getMessage(), null, null);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ProblemDetail handleAlreadyExistException(AlreadyExistException ex) {
        LOG.error("Already exist error occurred: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.RESOURCE_ALREADY_EXISTS, ex.getMessage(), null, null);
    }

    @ExceptionHandler(AttributeDefinitionNotFoundException.class)
    public ProblemDetail handleAttributeDefinitionNotFound(AttributeDefinitionNotFoundException ex) {
        LOG.error("Attribute definition not found: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.ATTRIBUTE_DEFINITION_NOT_FOUND, ex.getMessage(), null, null);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        LOG.error("General error occurred: {}", ex.getMessage(), ex);
        return ProblemDetailExtended.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), null, null);
    }

}
