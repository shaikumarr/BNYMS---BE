// controller/ManagerController.java  (full file)
package com.mphasis.taskpulse.controller;

import com.mphasis.taskpulse.dto.DailyUpdateResponse;
import com.mphasis.taskpulse.dto.EmployeeRequest;
import com.mphasis.taskpulse.dto.EmployeeUpdateRequest;
import com.mphasis.taskpulse.dto.ProjectMembersRequest;
import com.mphasis.taskpulse.dto.ProjectRequest;
import com.mphasis.taskpulse.dto.ProjectResponse;
import com.mphasis.taskpulse.dto.ReviewRequest;
import com.mphasis.taskpulse.dto.TaskRequest;
import com.mphasis.taskpulse.dto.TaskResponse;
import com.mphasis.taskpulse.dto.UserResponse;
import com.mphasis.taskpulse.enums.TaskStatus;
import com.mphasis.taskpulse.service.ApprovalService;
import com.mphasis.taskpulse.service.DailyUpdateService;
import com.mphasis.taskpulse.service.EmployeeService;
import com.mphasis.taskpulse.service.ProjectService;
import com.mphasis.taskpulse.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final DailyUpdateService dailyUpdateService;
    private final ApprovalService approvalService;

    public ManagerController(EmployeeService employeeService,
                             ProjectService projectService,
                             TaskService taskService,
                             DailyUpdateService dailyUpdateService,
                             ApprovalService approvalService) {
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.dailyUpdateService = dailyUpdateService;
        this.approvalService = approvalService;
    }

    // ---------- employees ----------
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createEmployee(@Valid @RequestBody EmployeeRequest req) {
        return employeeService.createEmployee(req);
    }

    @GetMapping("/employees")
    public List<UserResponse> employees() {
        return employeeService.listMyEmployees();
    }

    @PutMapping("/employees/{id}")
    public UserResponse updateEmployee(@PathVariable Long id,
                                       @Valid @RequestBody EmployeeUpdateRequest req) {
        return employeeService.updateEmployee(id, req);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    // ---------- projects ----------
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest req) {
        return projectService.create(req);
    }

    @GetMapping("/projects")
    public List<ProjectResponse> projects() {
        return projectService.listForManager();
    }

    @GetMapping("/projects/{id}")
    public ProjectResponse project(@PathVariable Long id) {
        return projectService.getForManager(id);
    }

    @PutMapping("/projects/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody ProjectRequest req) {
        return projectService.update(id, req);
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {
        projectService.delete(id);
    }

    // ---------- project team ----------
    @GetMapping("/projects/{id}/members")
    public List<UserResponse> projectMembers(@PathVariable Long id) {
        return projectService.listMembers(id);
    }

    @PutMapping("/projects/{id}/members")
    public List<UserResponse> setProjectMembers(@PathVariable Long id,
                                                @Valid @RequestBody ProjectMembersRequest req) {
        return projectService.updateMembers(id, req.employeeIds());
    }

    // ---------- weekly tasks ----------
    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest req) {
        return taskService.create(req);
    }

    // optional filters: /api/manager/tasks?projectId=1&status=SUBMITTED
    @GetMapping("/tasks")
    public List<TaskResponse> tasks(@RequestParam(required = false) Long projectId,
                                    @RequestParam(required = false) TaskStatus status) {
        return taskService.listForManager(projectId, status);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        return taskService.update(id, req);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/tasks/{id}/updates")
    public List<DailyUpdateResponse> taskUpdates(@PathVariable Long id) {
        return dailyUpdateService.listForManager(id);
    }

    @PostMapping("/tasks/{id}/review")
    public TaskResponse review(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        return approvalService.review(id, req);
    }
}