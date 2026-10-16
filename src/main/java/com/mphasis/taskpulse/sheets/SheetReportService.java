// sheets/SheetReportService.java
package com.mphasis.taskpulse.sheets;

import com.mphasis.taskpulse.entity.Approval;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.Decision;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.report.ExcelRow;
import com.mphasis.taskpulse.report.ReportService;
import com.mphasis.taskpulse.repository.ApprovalRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SheetReportService {

    private final WeeklyTaskRepository taskRepository;
    private final ApprovalRepository approvalRepository;
    private final ReportService reportService;
    private final SheetSyncService sheetSyncService;

    public SheetReportService(WeeklyTaskRepository taskRepository,
                              ApprovalRepository approvalRepository,
                              ReportService reportService,
                              SheetSyncService sheetSyncService) {
        this.taskRepository = taskRepository;
        this.approvalRepository = approvalRepository;
        this.reportService = reportService;
        this.sheetSyncService = sheetSyncService;
    }

    /** Rebuilds both tabs from the database. Returns how many rows were written. */
    @Transactional(readOnly = true)
    public int rebuildAll() {
        List<ExcelRow> updateRows = new ArrayList<>();

        for (WeeklyTask task : taskRepository
                .findByStatusOrderByProjectIdAscWeekNumberAsc(TaskStatus.APPROVED)) {

            Approval last = task.getApprovals().stream()
                    .filter(a -> a.getDecision() == Decision.APPROVED)
                    .max(Comparator.comparing(Approval::getDecidedAt))
                    .orElse(null);

            String approvedBy = last == null ? "" : last.getManager().getName();
            LocalDateTime approvedAt = last == null ? null : last.getDecidedAt();
            updateRows.addAll(reportService.rowsFor(task, approvedBy, approvedAt));
        }

        List<ReviewRow> reviews = approvalRepository.findAllByOrderByDecidedAtAsc().stream()
                .map(a -> ReviewRow.of(a.getTask(), a.getManager().getName(),
                        a.getDecision(), a.getComment(), a.getDecidedAt()))
                .toList();

        sheetSyncService.rebuild(updateRows, reviews);
        return updateRows.size() + reviews.size();
    }
}