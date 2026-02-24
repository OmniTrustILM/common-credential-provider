package czertainly.common.credential.provider;

import com.czertainly.api.model.common.error.ErrorCode;
import com.czertainly.api.model.common.error.ProblemDetailExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 1. Target only controllers in api V2 package to avoid interfering with global exception handling in other parts of the application
// 2. HIGHEST_PRECEDENCE ensures this is chosen over global ExceptionHandlingAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "czertainly.common.credential.provider.api.v2")
public class ProblemDetailsHandlingAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemDetailsHandlingAdvice.class);

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        LOG.error("General error occurred: {}", ex.getMessage(), ex);

        // needs to be updated to return ProblemDetailExtended with proper error code and details, currently just returning SERVICE_UNAVAILABLE for any unhandled exception
        return ProblemDetailExtended.fromErrorCode(ErrorCode.SERVICE_UNAVAILABLE, ex.getMessage(), null, null);
    }
}
