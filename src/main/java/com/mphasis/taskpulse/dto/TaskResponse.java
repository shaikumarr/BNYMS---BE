// dto/TaskResponse.java
package com.mphasis.taskpulse.dto;

import com.mphasis.taskpulse.enums.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(
        Long id, Long projectId, String projectName,
        String title, String description,
        Long assignedToId, String assignedToName,
        Integer weekNumber, LocalDate deadline,
        TaskStatus status, int completionPercent, String reviewComment) {
}