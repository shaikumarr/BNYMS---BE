// dto/DailyUpdateRequest.java  (full file)
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DailyUpdateRequest(
        @NotBlank @Size(max = 2000) String notes,       // final text
        @Size(max = 2000) String rawNotes,              // original text (optional)
        @NotNull @DecimalMin("0.0") @DecimalMax("24.0") Double hoursSpent,
        @NotNull @Min(0) @Max(100) Integer percentAdded) {
}