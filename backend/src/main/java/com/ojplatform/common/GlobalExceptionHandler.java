package com.ojplatform.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理公共组件。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, buildValidationMessage(e.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, buildValidationMessage(e.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(OjApiException.class)
    public ResponseEntity<Result<Void>> handleOjApiException(OjApiException e) {
        log.warn("OJ API error: platform={}, status={}, message={}", e.getPlatform(), e.getHttpStatus(), e.getMessage());
        return buildResponse(HttpStatus.valueOf(e.getHttpStatus()), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后再试");
    }

    private String buildValidationMessage(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return "请求参数不合法";
        }
        return fieldErrors.stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getField() + " 不合法")
                .collect(Collectors.joining("；"));
    }

    private ResponseEntity<Result<Void>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Result.error(status.value(), message));
    }
}
