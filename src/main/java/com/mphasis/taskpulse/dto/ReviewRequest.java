// dto/ReviewRequest.java
package com.mphasis.taskpulse.dto;

import com.mphasis.taskpulse.enums.Decision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull Decision decision,
        @Size(max = 500) String comment) {
}