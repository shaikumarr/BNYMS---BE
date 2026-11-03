// dto/DailyUpdateResponse.java  (full file, locked added)
package com.mphasis.taskpulse.dto;

import java.time.LocalDate;

public record DailyUpdateResponse(
        Long id, Long taskId, Long userId, String userName,
        LocalDate updateDate, String notes, String rawNotes,
        Double hoursSpent, Integer percentAdded, boolean locked) {
}