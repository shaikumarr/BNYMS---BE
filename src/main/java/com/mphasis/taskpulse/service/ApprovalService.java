// service/ApprovalService.java  (full file)
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.ReviewRequest;
import com.mphasis.taskpulse.dto.TaskResponse;
import com.mphasis.taskpulse.entity.Approval;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.Decision;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.mapper.Mapper;
import com.mphasis.taskpulse.report.ExcelRow;
import com.mphasis.taskpulse.report.ReportService;
import com.mphasis.taskpulse.report.TaskApprovedEvent;
import com.mphasis.taskpulse.repository.ApprovalRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ApprovalService {

    private final WeeklyTaskRepository taskRepository;
    private final ApprovalRepository approvalRepository;
    private final CurrentUser currentUser;
    private final ReportService reportService;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalService(WeeklyTaskRepository taskRepository,
                           ApprovalRepository approvalRepository,
                           CurrentUser currentUser,
                           ReportService reportService,
                           ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.approvalRepository = approvalRepository;
        this.currentUser = currentUser;
        this.reportService = reportService;
        this.eventPublisher = eventPublisher;
    }

    public TaskResponse review(Long taskId, ReviewRequest req) {
        User manager = currentUser.get();
        WeeklyTask task = taskRepository.findByIdAndProjectCreatedById(taskId, manager.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() != TaskStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted tasks can be reviewed");
        }
        if (req.decision() == Decision.REJECTED
                && (req.comment() == null || req.comment().isBlank())) {
            throw new BadRequestException("A comment is required when rejecting a task");
        }

        LocalDateTime now = LocalDateTime.now();

        // keep the full review history in the approvals table
        Approval approval = new Approval();
        approval.setTask(task);
        approval.setManager(manager);
        approval.setDecision(req.decision());
        approval.setComment(req.comment());
        approval.setDecidedAt(now);
        approvalRepository.save(approval);

        task.setReviewComment(req.comment());

        if (req.decision() == Decision.APPROVED) {
            task.setStatus(TaskStatus.APPROVED);
            task.setCompletionPercent(100);

            // Build the Excel rows now (lazy data is only readable inside this transaction).
            // The listener writes them AFTER this transaction commits successfully.
            List<ExcelRow> rows = reportService.rowsFor(task, manager.getName(), now);
            eventPublisher.publishEvent(new TaskApprovedEvent(rows));
        } else {
            task.setStatus(TaskStatus.REJECTED);   // user can update again and re-submit
        }
        return Mapper.toTaskResponse(task);
    }
}