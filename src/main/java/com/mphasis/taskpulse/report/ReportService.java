// report/ReportService.java
package com.mphasis.taskpulse.report;

import com.mphasis.taskpulse.entity.Approval;
import com.mphasis.taskpulse.entity.DailyUpdate;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.Decision;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WeeklyTaskRepository taskRepository;
    private final ExcelExportService excelExportService;
    private final CurrentUser currentUser;

    public ReportService(WeeklyTaskRepository taskRepository,
                         ExcelExportService excelExportService,
                         CurrentUser currentUser) {
        this.taskRepository = taskRepository;
        this.excelExportService = excelExportService;
        this.currentUser = currentUser;
    }

    /** One Excel row per daily update of the task. Must be called inside a transaction (lazy data). */
    public List<ExcelRow> rowsFor(WeeklyTask task, String approvedBy, LocalDateTime approvedAt) {
        String approvedOn = approvedAt == null ? "" : approvedAt.format(DATE_TIME);

        return task.getDailyUpdates().stream()
                .sorted(Comparator.comparing(DailyUpdate::getUpdateDate)
                        .thenComparing(DailyUpdate::getId))
                .map(u -> new ExcelRow(
                        u.getUpdateDate().toString(),
                        u.getUser().getName(),
                        task.getProject().getName(),
                        task.getWeekNumber(),
                        task.getTitle(),
                        u.getNotes(),
                        u.getHoursSpent(),
                        u.getPercentAdded(),
                        approvedBy,
                        approvedOn))
                .toList();
    }

    /** Rebuilds the whole report from the database: all approved tasks of the logged-in manager. */
    @Transactional(readOnly = true)
    public byte[] buildManagerReport() throws IOException {
        User manager = currentUser.get();

        List<ExcelRow> rows = taskRepository
                .findByStatusAndProjectCreatedByIdOrderByProjectIdAscWeekNumberAsc(
                        TaskStatus.APPROVED, manager.getId())
                .stream()
                .flatMap(task -> {
                    LocalDateTime approvedAt = task.getApprovals().stream()
                            .filter(a -> a.getDecision() == Decision.APPROVED)
                            .map(Approval::getDecidedAt)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    return rowsFor(task, manager.getName(), approvedAt).stream();
                })
                .toList();

        return excelExportService.buildWorkbookBytes(rows);
    }
}