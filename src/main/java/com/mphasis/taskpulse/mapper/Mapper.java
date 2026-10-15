// mapper/Mapper.java  (full file, memberCount added)
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

public final class Mapper {

    private Mapper() {
    }

    public static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }

    public static ProjectResponse toProjectResponse(Project p) {
        List<WeeklyTask> tasks = p.getTasks();
        int total = tasks.size();
        double progress = total == 0 ? 0 :
                tasks.stream().mapToInt(WeeklyTask::getCompletionPercent).average().orElse(0);
        int approved = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.APPROVED).count();

        return new ProjectResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getStartDate(), p.getEndDate(),
                round1(progress), round1(100 - progress),
                total, approved, p.getMembers().size());
    }

    public static TaskResponse toTaskResponse(WeeklyTask t) {
        return new TaskResponse(
                t.getId(), t.getProject().getId(), t.getProject().getName(),
                t.getTitle(), t.getDescription(),
                t.getAssignedTo().getId(), t.getAssignedTo().getName(),
                t.getWeekNumber(), t.getDeadline(),
                t.getStatus(), t.getCompletionPercent(), t.getReviewComment());
    }

    public static DailyUpdateResponse toUpdateResponse(DailyUpdate d) {
        return new DailyUpdateResponse(
                d.getId(), d.getTask().getId(),
                d.getUser().getId(), d.getUser().getName(),
                d.getUpdateDate(), d.getNotes(), d.getRawNotes(),
                d.getHoursSpent(), d.getPercentAdded());
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}