package com.clearskye.epicconnector.exception;

import lombok.NoArgsConstructor;

/**
 * Custom invalid credential exception for the application.
 * This class is used as a base exception for other custom invalid credential exceptions in the application.
 */
@NoArgsConstructor
public class CustomInvalidCredentialException extends RuntimeException{
    /**
     * Constructs a new CustomCommonException with the specified detail message and error code.
     *
     * @param message the detail message
     */
    public CustomInvalidCredentialException(String message) {
        super(message);
    }
}