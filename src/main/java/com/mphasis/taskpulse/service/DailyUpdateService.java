// service/DailyUpdateService.java  (full file, one line added: setRawNotes)
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.DailyUpdateRequest;
import com.mphasis.taskpulse.dto.DailyUpdateResponse;
import com.mphasis.taskpulse.entity.DailyUpdate;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.mapper.Mapper;
import com.mphasis.taskpulse.repository.DailyUpdateRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DailyUpdateService {

    private final DailyUpdateRepository updateRepository;
    private final WeeklyTaskRepository taskRepository;
    private final CurrentUser currentUser;

    public DailyUpdateService(DailyUpdateRepository updateRepository,
                              WeeklyTaskRepository taskRepository,
                              CurrentUser currentUser) {
        this.updateRepository = updateRepository;
        this.taskRepository = taskRepository;
        this.currentUser = currentUser;
    }

    public DailyUpdateResponse add(Long taskId, DailyUpdateRequest req) {
        User user = currentUser.get();
        WeeklyTask task = taskRepository.findByIdAndAssignedToId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() == TaskStatus.SUBMITTED || task.getStatus() == TaskStatus.APPROVED) {
            throw new BadRequestException("Task is submitted or approved, so no more updates are allowed");
        }

        DailyUpdate update = new DailyUpdate();
        update.setTask(task);
        update.setUser(user);
        update.setUpdateDate(LocalDate.now());
        update.setNotes(req.notes().trim());
        update.setRawNotes(req.rawNotes() == null ? null : req.rawNotes().trim());
        update.setHoursSpent(req.hoursSpent());
        update.setPercentAdded(req.percentAdded());

        // progress can never go above 100%
        task.setCompletionPercent(Math.min(100, task.getCompletionPercent() + req.percentAdded()));
        task.setStatus(TaskStatus.IN_PROGRESS);

        return Mapper.toUpdateResponse(updateRepository.save(update));
    }

    @Transactional(readOnly = true)
    public List<DailyUpdateResponse> listForUser(Long taskId) {
        taskRepository.findByIdAndAssignedToId(taskId, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return mapUpdates(taskId);
    }

    @Transactional(readOnly = true)
    public List<DailyUpdateResponse> listForManager(Long taskId) {
        taskRepository.findByIdAndProjectCreatedById(taskId, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return mapUpdates(taskId);
    }

    private List<DailyUpdateResponse> mapUpdates(Long taskId) {
        return updateRepository.findByTaskIdOrderByUpdateDateDescIdDesc(taskId)
                .stream().map(Mapper::toUpdateResponse).toList();
    }
}