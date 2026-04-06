package com.dev.LMS.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler implements ResponseBodyAdvice<Object> {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                       HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(RuntimeException ex,
                                                                       HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                messageOrFallback(ex.getMessage(), "Bad request"),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                messageOrFallback(ex.getMessage(), "Access denied"),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex,
                                                                   HttpServletRequest request) {
        log.error("Unhandled exception for path {}", request.getRequestURI(), ex);
        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected server error",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @Override
    public boolean supports(org.springframework.core.MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  org.springframework.core.MethodParameter returnType,
                                  org.springframework.http.MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(response instanceof org.springframework.http.server.ServletServerHttpResponse servletResponse)) {
            return body;
        }
        int rawStatus = servletResponse.getServletResponse().getStatus();
        HttpStatus status = HttpStatus.resolve(rawStatus);

        if (status == null || !status.isError()) {
            return body;
        }
        if (body instanceof ApiErrorResponse) {
            return body;
        }

        String path = request.getURI().getPath();
        ApiErrorResponse wrapped = wrapErrorBody(status, body, path);

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                response.getHeaders().set("Content-Type", "application/json");
                return objectMapper.writeValueAsString(wrapped);
            } catch (JsonProcessingException e) {
                return "{\"message\":\"" + wrapped.getMessage() + "\"}";
            }
        }
        return wrapped;
    }

    private ApiErrorResponse wrapErrorBody(HttpStatus status, Object body, String path) {
        if (body instanceof Map<?, ?> mapBody) {
            String message = null;
            String error = null;
            Map<String, String> validationErrors = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : mapBody.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String key = String.valueOf(entry.getKey());
                String value = entry.getValue() == null ? null : String.valueOf(entry.getValue());

                if ("message".equalsIgnoreCase(key)) {
                    message = value;
                } else if ("error".equalsIgnoreCase(key)) {
                    error = value;
                } else if (value != null) {
                    validationErrors.put(key, value);
                }
            }

            if ((message == null || message.isBlank()) && !validationErrors.isEmpty()) {
                message = "Validation failed";
            }
            if (error == null || error.isBlank()) {
                error = status.getReasonPhrase();
            }

            return ApiErrorResponse.of(
                    status.value(),
                    error,
                    messageOrFallback(message, status.getReasonPhrase()),
                    path,
                    validationErrors
            );
        }

        String message = body == null ? status.getReasonPhrase() : String.valueOf(body);
        return ApiErrorResponse.of(status.value(), status.getReasonPhrase(), messageOrFallback(message, status.getReasonPhrase()), path);
    }

    private String messageOrFallback(String message, String fallback) {
        return (message == null || message.isBlank()) ? fallback : message;
    }
}
