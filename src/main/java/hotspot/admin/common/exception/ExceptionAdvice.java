package hotspot.admin.common.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import hotspot.admin.common.exception.code.BaseErrorCode;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.exception.code.GlobalErrorCode;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    /** BaseException - 도메인 예외 (ex: ApplicationException) */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException e, HttpServletRequest request) {
        BaseErrorCode code = e.getCode();
        log.error("[BaseException] {} - {}", code.name(), code.getMessage());

        ErrorResponse response =
                new ErrorResponse(
                        code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        BaseErrorCode code = resolveValidationErrorCode(ex);

        ErrorResponse response =
                new ErrorResponse(
                        code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    /** 그 외 모든 예외 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        BaseErrorCode code = resolveTypeMismatchErrorCode(e);
        ErrorResponse response =
                new ErrorResponse(
                        code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    /** 그 외 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception e, WebRequest request) {
        log.error("[Exception] Unhandled: {}", e.getMessage(), e);

        GlobalErrorCode code = GlobalErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response =
                new ErrorResponse(
                        code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    private BaseErrorCode resolveValidationErrorCode(MethodArgumentNotValidException ex) {
        Object target = ex.getBindingResult().getTarget();
        if (target instanceof CreateTimePolicyRequest) {
            return PolicyErrorCode.INVALID_POLICY_SNAPSHOT;
        }
        return GlobalErrorCode.METHOD_ARGUMENT_NOT_VALID;
    }

    private BaseErrorCode resolveTypeMismatchErrorCode(MethodArgumentTypeMismatchException e) {
        Class<?> requiredType = e.getRequiredType();
        if (requiredType == ApplyType.class) {
            return FamilyErrorCode.INVALID_APPLY_TYPE;
        }
        if (requiredType == FamilyApplyStatus.class) {
            return FamilyErrorCode.INVALID_APPLY_STATUS;
        }
        return GlobalErrorCode.BAD_REQUEST;
    }
}
