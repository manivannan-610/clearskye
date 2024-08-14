package com.clearskye.epicconnector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for User details.
 * This class is used to transfer user data such as user ID.
 */
@Getter
@Setter
public class UserIdRequestDto {
    @JsonProperty("UserID")
    @NotEmpty(message = "UserID must not be empty")
    private String userId;
}
