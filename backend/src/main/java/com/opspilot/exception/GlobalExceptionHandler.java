package com.opspilot.exception;

import com.opspilot.ai.exception.AiConfigurationException;
import com.opspilot.ai.exception.AiProviderResponseException;
import com.opspilot.ai.exception.AiProviderUnavailableException;
import com.opspilot.ai.exception.AiRateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiError> handleAccountNotFound(
            AccountNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidOperationalDataException.class)
    public ResponseEntity<ApiError> handleInvalidOperationalData(
            InvalidOperationalDataException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictingOrderNumberException.class)
    public ResponseEntity<ApiError> handleConflictingOrderNumber(
            ConflictingOrderNumberException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AiConfigurationException.class)
    public ResponseEntity<ApiError> handleAiConfiguration(
            AiConfigurationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AiProviderUnavailableException.class)
    public ResponseEntity<ApiError> handleAiProviderUnavailable(
            AiProviderUnavailableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AiProviderResponseException.class)
    public ResponseEntity<ApiError> handleAiProviderResponse(
            AiProviderResponseException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AiRateLimitExceededException.class)
    public ResponseEntity<ApiError> handleAiRateLimitExceeded(
            AiRateLimitExceededException exception,
            HttpServletRequest request
    ) {
        Duration retryAfter = exception.getRetryAfter();
        String message = exception.getMessage();
        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                message,
                request.getRequestURI(),
                retryAfter != null && !retryAfter.isZero() && !retryAfter.isNegative() ? retryAfter : null
        );
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, String path) {
        return buildResponse(status, message, path, null);
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Duration retryAfter
    ) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity.status(status)
                .header("Retry-After", retryAfter == null ? null : String.valueOf(Math.max(1, (int) Math.ceil(retryAfter.toSeconds()))))
                .body(body);
    }
}
