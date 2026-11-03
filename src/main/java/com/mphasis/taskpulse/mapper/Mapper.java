// mapper/Mapper.java  (full file: project progress counts approved work only)
package com.mphasis.taskpulse.mapper;

import com.mphasis.taskpulse.dto.DailyUpdateResponse;
import com.mphasis.taskpulse.dto.ProjectResponse;
import com.mphasis.taskpulse.dto.TaskResponse;
import com.mphasis.taskpulse.dto.UserResponse;
import com.mphasis.taskpulse.entity.DailyUpdate;
import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.TaskStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Mapper {

    private Mapper() {
    }

    public static UserResponse toUserResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole()
        );
    }

    /** How much of one person's task is approved: all of it when completed, otherwise the approved part. */
    private static int approvedOf(WeeklyTask t) {
        return t.getStatus() == TaskStatus.APPROVED ? 100 : t.getApprovedPercent();
    }

    public static ProjectResponse toProjectResponse(Project p) {

        // tasks assigned together share one number (T1, T2...): that is ONE assignment
        Map<Integer, List<WeeklyTask>> assignments = p.getTasks().stream()
                .collect(Collectors.groupingBy(WeeklyTask::getWeekNumber));

        // only APPROVED work counts. Assignment progress = average of its people;
        // project progress = average of its assignments
        double progress = assignments.isEmpty() ? 0 :
                assignments.values().stream()
                        .mapToDouble(group -> group.stream()
                                .mapToInt(Mapper::approvedOf).average().orElse(0))
                        .average().orElse(0);

        int totalParts = assignments.size();
        int approvedParts = (int) assignments.values().stream()
                .filter(group -> group.stream().allMatch(t -> t.getStatus() == TaskStatus.APPROVED))
                .count();

        int memberCount = p.getMembers() == null
                ? 0
                : p.getMembers().size();

        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getStartDate(),
                p.getEndDate(),
                round1(progress),
                round1(100 - progress),
                totalParts,
                approvedParts,
                memberCount,
                p.getCreatedBy().getId(),       // owner: the manager who created the project
                p.getCreatedBy().getName()
        );
    }

    public static TaskResponse toTaskResponse(WeeklyTask t) {
        return new TaskResponse(
                t.getId(),
                t.getProject().getId(),
                t.getProject().getName(),
                t.getTitle(),
                t.getDescription(),
                t.getAssignedTo().getId(),
                t.getAssignedTo().getName(),
                t.getWeekNumber(),
                t.getStartDate(),
                t.getDeadline(),
                t.getStatus(),
                t.getCompletionPercent(),
                approvedOf(t),
                t.getReviewComment()
        );
    }

    public static DailyUpdateResponse toUpdateResponse(DailyUpdate d) {
        return new DailyUpdateResponse(
                d.getId(),
                d.getTask().getId(),
                d.getUser().getId(),
                d.getUser().getName(),
                d.getUpdateDate(),
                d.getNotes(),
                d.getRawNotes(),
                d.getHoursSpent(),
                d.getPercentAdded()
        );
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}