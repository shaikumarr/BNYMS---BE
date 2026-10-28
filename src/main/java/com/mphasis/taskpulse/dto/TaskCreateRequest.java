// dto/TaskCreateRequest.java
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** Assign a project to one or more employees. One assignment = one number (T1, T2, T3...). */
public record TaskCreateRequest(
        @NotNull Long projectId,
        @NotBlank String title,
        @Size(max = 1000) String description,
        @NotEmpty(message = "Select at least one employee") List<Long> assignedToIds,
        LocalDate startDate,                 // optional: empty = today (the creation date)
        @NotNull LocalDate deadline) {
}