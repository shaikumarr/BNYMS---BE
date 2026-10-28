// service/TaskService.java  (full file, team-member check added)
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.TaskRequest;
import com.mphasis.taskpulse.dto.TaskResponse;
import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.mapper.Mapper;
import com.mphasis.taskpulse.repository.ProjectRepository;
import com.mphasis.taskpulse.repository.UserRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final WeeklyTaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public TaskService(WeeklyTaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       CurrentUser currentUser) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    // ===================== MANAGER =====================

    public TaskResponse create(TaskRequest req) {
        User manager = currentUser.get();
        Project project = findManagerProject(req.projectId(), manager);
        User assignee = findEmployee(req.assignedTo(), manager);
        requireTeamMember(project, assignee);
        validateDeadline(req.deadline(), project);

        WeeklyTask task = new WeeklyTask();
        task.setProject(project);
        task.setStatus(TaskStatus.ASSIGNED);
        task.setCompletionPercent(0);
        applyRequest(task, req, assignee);
        return Mapper.toTaskResponse(taskRepository.save(task));
    }

    public TaskResponse update(Long id, TaskRequest req) {
        User manager = currentUser.get();
        WeeklyTask task = findManagerTask(id, manager);

        if (!task.getProject().getId().equals(req.projectId())) {
            throw new BadRequestException("A task cannot be moved to another project");
        }
        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new BadRequestException("Approved tasks cannot be edited");
        }
        User assignee = findEmployee(req.assignedTo(), manager);

        // only check the team when the task is moved to a different person
        if (!task.getAssignedTo().getId().equals(assignee.getId())) {
            requireTeamMember(task.getProject(), assignee);
        }
        validateDeadline(req.deadline(), task.getProject());
        

        applyRequest(task, req, assignee);
        return Mapper.toTaskResponse(task);          // saved automatically at commit
    }

    public void delete(Long id) {
        WeeklyTask task = findManagerTask(id, currentUser.get());
        task.getProject().getTasks().remove(task);   // orphanRemoval deletes it with its updates
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listForManager(Long projectId, TaskStatus status) {
        User manager = currentUser.get();
        List<WeeklyTask> tasks;
        if (projectId != null) {
            findManagerProject(projectId, manager);   // checks ownership
            tasks = taskRepository.findByProjectIdOrderByWeekNumberAsc(projectId);
        } else {
            tasks = taskRepository.findAllForManager(manager.getId());
        }
        return tasks.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .map(Mapper::toTaskResponse)
                .toList();
    }

    // ======================= USER ======================

    @Transactional(readOnly = true)
    public List<TaskResponse> myTasks() {
        return taskRepository.findByAssignedToIdOrderByDeadlineAsc(currentUser.get().getId())
                .stream().map(Mapper::toTaskResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse myTask(Long id) {
        return Mapper.toTaskResponse(findUserTask(id));
    }

    /** User says "I'm done with this week's part": moves it to SUBMITTED for manager review. */
    public TaskResponse submit(Long id) {
        WeeklyTask task = findUserTask(id);
        if (task.getStatus() == TaskStatus.SUBMITTED || task.getStatus() == TaskStatus.APPROVED) {
            throw new BadRequestException("This task is already submitted or approved");
        }
        if (task.getCompletionPercent() <= 0) {
            throw new BadRequestException("Add at least one progress update before submitting");
        }
        task.setStatus(TaskStatus.SUBMITTED);
        return Mapper.toTaskResponse(task);
    }

    // ===================== helpers =====================

    private Project findManagerProject(Long projectId, User manager) {
        return projectRepository.findByIdAndCreatedById(projectId, manager.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private WeeklyTask findManagerTask(Long id, User manager) {
        return taskRepository.findByIdAndProjectCreatedById(id, manager.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private WeeklyTask findUserTask(Long id) {
        return taskRepository.findByIdAndAssignedToId(id, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private User findEmployee(Long employeeId, User manager) {
        return userRepository.findByIdAndManagerId(employeeId, manager.getId())
                .orElseThrow(() -> new BadRequestException("Employee not found under your team"));
    }

    private void requireTeamMember(Project project, User employee) {
        boolean isMember = project.getMembers().stream()
                .anyMatch(m -> m.getId().equals(employee.getId()));
        if (!isMember) {
            throw new BadRequestException(employee.getName()
                    + " is not in this project's team. Add them with 'Project team' first");
        }
    }

    private void validateDeadline(LocalDate deadline, Project project) {
        if (deadline.isBefore(project.getStartDate()) || deadline.isAfter(project.getEndDate())) {
            throw new BadRequestException("Deadline must be within the project dates ("
                    + project.getStartDate() + " to " + project.getEndDate() + ")");
        }
    }

   
    private void applyRequest(WeeklyTask task, TaskRequest req, User assignee) {
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setAssignedTo(assignee);
        task.setWeekNumber(req.weekNumber());
        task.setDeadline(req.deadline());
    }
}