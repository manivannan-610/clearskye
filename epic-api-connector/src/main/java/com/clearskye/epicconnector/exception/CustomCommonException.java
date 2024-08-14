package com.clearskye.epicconnector.exception;

import lombok.NoArgsConstructor;

/**
 * Custom common exception for the application.
 * This class is used as a base exception for other custom exceptions in the application.
 */
@NoArgsConstructor
public class CustomCommonException extends RuntimeException {
    /**
     * Constructs a new CustomCommonException with the specified detail message and error code.
     *
     * @param message the detail message
     */
    public CustomCommonException(String message) {
        super(message);
    }
}