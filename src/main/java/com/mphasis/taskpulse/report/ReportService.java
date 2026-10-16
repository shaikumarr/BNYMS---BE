// report/ReportService.java
package com.mphasis.taskpulse.report;

import com.mphasis.taskpulse.entity.Approval;
import com.mphasis.taskpulse.entity.DailyUpdate;
import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.Decision;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.repository.ProjectRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WeeklyTaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ExcelExportService excelExportService;
    private final CurrentUser currentUser;

    public ReportService(WeeklyTaskRepository taskRepository,
                         ProjectRepository projectRepository,
                         ExcelExportService excelExportService,
                         CurrentUser currentUser) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.excelExportService = excelExportService;
        this.currentUser = currentUser;
    }

    /**
     * All approved rows of one manager, grouped per project (one entry per project, even if empty).
     * Uses its own transaction, so it also works after another transaction has committed
     * and from the background job.
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<ProjectSheet> sheetsForManager(Long managerId) {
        Map<Long, List<ExcelRow>> rowsByProject = new HashMap<>();

        List<WeeklyTask> approvedTasks = taskRepository
                .findByStatusAndProjectCreatedByIdOrderByProjectIdAscWeekNumberAsc(TaskStatus.APPROVED, managerId);

        for (WeeklyTask task : approvedTasks) {
            // latest APPROVED decision of this task
            Approval last = null;
            for (Approval a : task.getApprovals()) {
                if (a.getDecision() == Decision.APPROVED
                        && (last == null || a.getDecidedAt().isAfter(last.getDecidedAt()))) {
                    last = a;
                }
            }
            String approvedBy = last == null ? "" : last.getManager().getName();
            LocalDateTime approvedAt = last == null ? null : last.getDecidedAt();

            Long projectId = task.getProject().getId();
            List<ExcelRow> list = rowsByProject.get(projectId);
            if (list == null) {
                list = new ArrayList<>();
                rowsByProject.put(projectId, list);
            }
            list.addAll(rowsFor(task, approvedBy, approvedAt));
        }

        List<Project> projects = new ArrayList<>(projectRepository.findByCreatedByIdOrderByIdDesc(managerId));
        projects.sort(Comparator.comparing(Project::getId));   // oldest project = first tab

        List<ProjectSheet> sheets = new ArrayList<>();
        for (Project p : projects) {
            List<ExcelRow> rows = rowsByProject.get(p.getId());
            sheets.add(new ProjectSheet(p.getId(), p.getName(), rows == null ? new ArrayList<>() : rows));
        }
        return sheets;
    }

    /** Live page data for the logged-in manager. */
    @Transactional(readOnly = true)
    public List<ProjectSheet> liveReport() {
        return sheetsForManager(currentUser.get().getId());
    }

    /** "Download Excel report" button: same workbook as the master file, built fresh. */
    @Transactional(readOnly = true)
    public byte[] buildManagerReport() throws IOException {
        return excelExportService.buildWorkbookBytes(sheetsForManager(currentUser.get().getId()));
    }

    /** One row per daily update of the task. Needs an open transaction (lazy data). */
    public List<ExcelRow> rowsFor(WeeklyTask task, String approvedBy, LocalDateTime approvedAt) {
        String approvedOn = approvedAt == null ? "" : approvedAt.format(DATE_TIME);

        List<DailyUpdate> updates = new ArrayList<>(task.getDailyUpdates());
        updates.sort(Comparator.comparing(DailyUpdate::getUpdateDate)
                .thenComparing(DailyUpdate::getId));

        List<ExcelRow> rows = new ArrayList<>();
        for (DailyUpdate u : updates) {
            rows.add(new ExcelRow(
                    u.getUpdateDate().toString(),
                    u.getUser().getName(),
                    task.getProject().getName(),
                    task.getWeekNumber(),
                    task.getTitle(),
                    u.getNotes(),
                    u.getHoursSpent(),
                    u.getPercentAdded(),
                    approvedBy,
                    approvedOn));
        }
        return rows;
    }
}