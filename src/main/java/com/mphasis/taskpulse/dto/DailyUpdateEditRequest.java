// dto/DailyUpdateEditRequest.java  (new file)
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Change an existing update during rework (instead of creating a new one). */
public record DailyUpdateEditRequest(
        @NotBlank @Size(max = 2000) String notes,
        @NotNull @DecimalMin("0.0") @DecimalMax("24.0") Double hoursSpent,
        @NotNull @Min(0) @Max(100) Integer percentAdded) {
}