package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotNull Long projectId,
        @NotBlank String title,
        @Size(max = 1000) String description,
        @NotNull Long assignedTo,
        @NotNull @Min(1) Integer weekNumber,
        @NotNull LocalDate deadline
) {
}