// dto/TaskRequest.java
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotNull Long projectId,
        @NotBlank String title,
        @Size(max = 1000) String description,
        @NotNull Long assignedTo,
        LocalDate startDate,                 // optional: empty = keep the current start date
        @NotNull LocalDate deadline) {
}