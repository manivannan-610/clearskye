package com.clearskye.epicconnector.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for updating user group.
 * This class is used to transfer user ID and new group ID's data.
 */

@Getter
@Setter
public class GroupUpdateRequestDto {

    @JsonProperty("UserID")
    @NotEmpty(message = "UserID must not be empty")
    private String userId;
    @JsonProperty("UserGroups")
    @NotNull(message = "UserGroups must not be null")
    @NotEmpty(message = "UserGroups must not be null")
    private List<String> userGroups;
}
