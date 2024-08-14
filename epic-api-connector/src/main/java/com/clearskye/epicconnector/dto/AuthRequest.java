package com.clearskye.epicconnector.dto;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for User credentials.
 * This class is used to transfer user login data such as username and password.
 */
@Getter
@Setter
public class AuthRequest {
    @NotEmpty(message = "userName must not be null or empty")
    private String userName;
    @NotEmpty(message = "password must not be null or empty")
    private String password;
}
