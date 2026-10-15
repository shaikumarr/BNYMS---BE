// dto/ProjectRequest.java
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank String name,
        @Size(max = 1000) String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}