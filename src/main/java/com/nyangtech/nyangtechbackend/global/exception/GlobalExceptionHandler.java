package com.nyangtech.nyangtechbackend.global.exception;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러에서 던져진 예외를 한 곳에서 받아 ApiResponse 형식으로 바꿔준다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode));
    }

    /** @Valid 검사에 실패한 경우 (이메일 형식, 빈 값 등) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : CommonErrorCode.INVALID_INPUT.getMessage();
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT.name(), message));
    }

    /** JSON 형식이 깨졌거나 타입이 안 맞는 경우 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT.name(), "요청 본문 형식이 올바르지 않습니다."));
    }

    /** 위에서 처리하지 못한 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        // 404, 405 처럼 Spring이 상태 코드를 이미 정해둔 예외는 그 상태를 그대로 존중한다.
        if (e instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(ApiResponse.fail("HTTP_" + status, errorResponse.getBody().getTitle()));
        }
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiResponse.fail(CommonErrorCode.INTERNAL_ERROR));
    }
}
