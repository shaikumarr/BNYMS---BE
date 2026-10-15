// controller/UserController.java  (full file, polish endpoint added)
package com.mphasis.taskpulse.controller;

import com.mphasis.taskpulse.ai.TextPolishService;
import com.mphasis.taskpulse.dto.DailyUpdateRequest;
import com.mphasis.taskpulse.dto.DailyUpdateResponse;
import com.mphasis.taskpulse.dto.PolishRequest;
import com.mphasis.taskpulse.dto.PolishResponse;
import com.mphasis.taskpulse.dto.ProjectResponse;
import com.mphasis.taskpulse.dto.TaskResponse;
import com.mphasis.taskpulse.service.DailyUpdateService;
import com.mphasis.taskpulse.service.ProjectService;
import com.mphasis.taskpulse.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final TaskService taskService;
    private final DailyUpdateService dailyUpdateService;
    private final ProjectService projectService;
    private final TextPolishService textPolishService;

    public UserController(TaskService taskService,
                          DailyUpdateService dailyUpdateService,
                          ProjectService projectService,
                          TextPolishService textPolishService) {
        this.taskService = taskService;
        this.dailyUpdateService = dailyUpdateService;
        this.projectService = projectService;
        this.textPolishService = textPolishService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> myTasks() {
        return taskService.myTasks();
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse myTask(@PathVariable Long id) {
        return taskService.myTask(id);
    }

    @PostMapping("/tasks/{id}/updates")
    @ResponseStatus(HttpStatus.CREATED)
    public DailyUpdateResponse addUpdate(@PathVariable Long id,
                                         @Valid @RequestBody DailyUpdateRequest req) {
        return dailyUpdateService.add(id, req);
    }

    @GetMapping("/tasks/{id}/updates")
    public List<DailyUpdateResponse> myUpdates(@PathVariable Long id) {
        return dailyUpdateService.listForUser(id);
    }

    @PostMapping("/tasks/{id}/submit")
    public TaskResponse submit(@PathVariable Long id) {
        return taskService.submit(id);
    }

    // projects I'm part of, with overall progress and remaining percentage
    @GetMapping("/projects")
    public List<ProjectResponse> myProjects() {
        return projectService.progressForUser();
    }

    // NEW: rewrite casual text as a professional update (preview only, nothing is saved)
    @PostMapping("/updates/polish")
    public PolishResponse polish(@Valid @RequestBody PolishRequest req) {
        return new PolishResponse(textPolishService.polish(req.text().trim()));
    }
}