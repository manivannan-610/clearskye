package com.clearskye.epicconnector.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for updating user password.
 * This class is used to transfer user ID and new password data.
 */
@Getter
@Setter
public class PasswordUpdateDto {
    @JsonProperty("NewPassword")
    @NotEmpty(message = "NewPassword must not be empty")
    private String newPassword;
    @JsonProperty("UserID")
    @NotEmpty(message = "UserID must not be empty")
    private String userId;
}
