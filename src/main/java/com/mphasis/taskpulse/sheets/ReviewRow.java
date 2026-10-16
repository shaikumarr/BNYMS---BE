// sheets/ReviewRow.java
package com.mphasis.taskpulse.sheets;

import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.Decision;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One manager decision (approve or reject) as a plain row. Built inside a transaction. */
public record ReviewRow(
        String reviewedOn,
        String project,
        Integer week,
        String task,
        String employee,
        String decision,
        String manager,
        String comment) {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static ReviewRow of(WeeklyTask task, String managerName, Decision decision,
                               String comment, LocalDateTime decidedAt) {
        return new ReviewRow(
                decidedAt == null ? "" : decidedAt.format(DATE_TIME),
                task.getProject().getName(),
                task.getWeekNumber(),
                task.getTitle(),
                task.getAssignedTo().getName(),
                decision.name(),
                managerName,
                comment == null ? "" : comment);
    }
}