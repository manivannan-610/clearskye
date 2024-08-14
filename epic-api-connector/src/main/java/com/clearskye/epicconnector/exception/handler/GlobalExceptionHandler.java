package com.clearskye.epicconnector.exception.handler;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.clearskye.epicconnector.exception.CustomInvalidCredentialException;
import com.clearskye.epicconnector.exception.CustomCommonException;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown by the controllers and provides appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Logger instance for logging GlobalExceptionHandler events.
     */
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles `ExpiredJwtException` exceptions.
     *
     * @param ex the exception thrown
     * @return A response entity with an error message and a 401 status code
     */
    @ExceptionHandler(value = ExpiredJwtException.class)
    public ResponseEntity<String> jwtExceptionHandle(ExpiredJwtException ex) {
        logger.error(MessageFormat.format("Token is not valid, because of error {0} : ", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not valid");
    }

    /**
     * Handles `CustomInvalidCredentialException` exceptions.
     *
     * @param ex the exception thrown
     * @return A response entity with an error message and a 401 status code
     */
    @ExceptionHandler(value = CustomInvalidCredentialException.class)
    public ResponseEntity<String> credentialExceptionHandle(CustomInvalidCredentialException ex) {
        logger.error(MessageFormat.format("Operation failed , because of error {0} : ", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Handles `CustomCommonException` exceptions.
     *
     * @param ex the exception thrown
     * @return A response entity with an error message and a 400 status code
     */
    @ExceptionHandler(value = CustomCommonException.class)
    public ResponseEntity<String> customCommonException(CustomCommonException ex) {
        logger.error(MessageFormat.format("Operation failed , because of error {0} : ", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error ->
                errors.put(((org.springframework.validation.FieldError) error).getField(), error.getDefaultMessage()));
        logger.error(MessageFormat.format("Operation failed , because of error {0} : ", errors));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles `HttpMediaTypeNotAcceptableException` exceptions.
     *
     * @param ex the exception thrown
     * @return A response entity with an error message and a 400 status code
     */
    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex) {
        logger.error(MessageFormat.format("Operation failed , because of error {0} : ", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles `HttpMessageNotReadableException` exceptions.
     *
     * @param ex the exception thrown
     * @return A response entity with an error message and a 400 status code
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error(MessageFormat.format("Operation failed , because of error {0} : ", ex.getMessage()));
        if (ex.getMessage().contains("Required request body is missing")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is missing.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


}