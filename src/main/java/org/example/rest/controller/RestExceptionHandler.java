package org.example.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        exception.getMessage(),
                        Instant.now().toString(),
                        List.of()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleServerError(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        exception.getMessage(),
                        Instant.now().toString(),
                        List.of()
                )
        );
    }

    @ExceptionHandler(PuzzleSolveTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleTimeout(PuzzleSolveTimeoutException exception) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(
                new ApiErrorResponse(
                        HttpStatus.REQUEST_TIMEOUT.value(),
                        "Request Timeout",
                        exception.getMessage(),
                        Instant.now().toString(),
                        List.of()
                )
        );
    }

    @io.swagger.v3.oas.annotations.media.Schema(description = "Standard error response")
    public record ApiErrorResponse(
            int status,
            String error,
            String message,
            String timestamp,
            List<String> details
    ) {
    }
}
