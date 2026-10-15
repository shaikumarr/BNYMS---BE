// dto/PolishRequest.java
package com.mphasis.taskpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolishRequest(
        @NotBlank @Size(max = 1500, message = "Text is too long (max 1500 characters)") String text) {
}