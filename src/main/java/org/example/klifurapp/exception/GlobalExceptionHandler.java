
package org.example.klifurapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.klifurapp.DTO.responses.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiErrorResponseDTO> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponseDTO body = new ApiErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .orElse("Invalid request");

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponseDTO> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid request format or parameter",
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(
                ex.getStatusCode().value()
        );

        if (status == null) {
            return buildError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred",
                    request
            );
        }

        return buildError(
                status,
                ex.getReason() != null
                        ? ex.getReason()
                        : status.getReasonPhrase(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        // Log the exception server-side in a production application.
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "Access denied",
                request
        );
    }
}
