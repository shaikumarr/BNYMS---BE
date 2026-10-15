// dto/ProjectResponse.java  (full file, memberCount added)
package com.mphasis.taskpulse.dto;

import java.time.LocalDate;

public record ProjectResponse(
        Long id, String name, String description,
        LocalDate startDate, LocalDate endDate,
        double progressPercent, double remainingPercent,
        int totalParts, int approvedParts, int memberCount) {
}