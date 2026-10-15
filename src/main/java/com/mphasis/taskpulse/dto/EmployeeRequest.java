// dto/EmployeeRequest.java
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {
}