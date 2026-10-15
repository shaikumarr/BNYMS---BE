// dto/EmployeeUpdateRequest.java  (NEW)
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        // optional: leave out to keep the current password
        @Size(min = 6, message = "Password must be at least 6 characters") String password) {
}