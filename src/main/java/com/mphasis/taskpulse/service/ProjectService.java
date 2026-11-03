// service/ProjectService.java  (full file, team methods added)
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.ProjectRequest;
import com.mphasis.taskpulse.dto.ProjectResponse;
import com.mphasis.taskpulse.dto.UserResponse;
import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.mapper.Mapper;
import com.mphasis.taskpulse.repository.ProjectRepository;
import com.mphasis.taskpulse.repository.UserRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WeeklyTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public ProjectService(ProjectRepository projectRepository,
                          WeeklyTaskRepository taskRepository,
                          UserRepository userRepository,
                          CurrentUser currentUser) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    public ProjectResponse create(ProjectRequest req) {
        validateDates(req);
        Project project = new Project();
        project.setCreatedBy(currentUser.get());
        apply(project, req);
        return Mapper.toProjectResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listForManager() {

        User current = currentUser.get();

        Map<Long, Project> projects = new LinkedHashMap<>();

        projectRepository
                .findByCreatedByIdOrderByIdDesc(current.getId())
                .forEach(p -> projects.put(p.getId(), p));

        if (current.getManager() != null) {

            projectRepository
                    .findByCreatedByIdOrderByIdDesc(
                            current.getManager().getId())
                    .forEach(p -> projects.putIfAbsent(p.getId(), p));
        }

        return projects.values()
                .stream()
                .map(Mapper::toProjectResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public ProjectResponse getForManager(Long id) {
        return Mapper.toProjectResponse(findOwned(id));
    }
    public ProjectResponse update(Long id, ProjectRequest req) {
        validateDates(req);
        Project project = findOwned(id);
        apply(project, req);
        return Mapper.toProjectResponse(project);   // saved automatically at commit
    }

    public void delete(Long id) {
        projectRepository.delete(findOwned(id));
    }

    // ---------------- project team ----------------

    @Transactional(readOnly = true)
    public List<UserResponse> listMembers(Long projectId) {
        return findOwned(projectId).getMembers().stream()
                .map(Mapper::toUserResponse).toList();
    }

    /** Replaces the project's team with the given employees. */
    public List<UserResponse> updateMembers(Long projectId, List<Long> employeeIds) {
        User manager = currentUser.get();
        Project project = findOwned(projectId);

        // every id must be an employee of this manager
        Set<User> newMembers = new LinkedHashSet<>();
        for (Long employeeId : new LinkedHashSet<>(employeeIds)) {
            User employee = userRepository.findByIdAndManagerId(employeeId, manager.getId())
                    .orElseThrow(() -> new BadRequestException("Employee not found under your team"));
            newMembers.add(employee);
        }
        Set<Long> newIds = newMembers.stream().map(User::getId).collect(Collectors.toSet());

        // do not remove someone who still has a weekly part in this project
        Set<Long> assigneeIds = taskRepository.findByProjectIdOrderByWeekNumberAsc(projectId).stream()
                .map(t -> t.getAssignedTo().getId())
                .collect(Collectors.toSet());
        for (User current : project.getMembers()) {
            if (!newIds.contains(current.getId()) && assigneeIds.contains(current.getId())) {
                throw new BadRequestException(current.getName()
                        + " still has weekly parts in this project. Delete or reassign them first");
            }
        }

        project.getMembers().clear();
        project.getMembers().addAll(newMembers);
        return project.getMembers().stream().map(Mapper::toUserResponse).toList();
    }

    /** USER view: projects the user is a team member of, plus any project where they have a weekly part. */
    @Transactional(readOnly = true)
    public List<ProjectResponse> progressForUser() {

        Long userId = currentUser.get().getId();

        System.out.println("==================================");
        System.out.println("USER ID = " + userId);

        List<Project> memberProjects =
                projectRepository.findByMembersId(userId);

        System.out.println("PROJECTS FOUND = " + memberProjects.size());

        for (Project p : memberProjects) {
            System.out.println("PROJECT = " + p.getName());
        }

        Map<Long, Project> projects = new LinkedHashMap<>();

        for (Project p : memberProjects) {
            projects.put(p.getId(), p);
        }

        for (Project p : taskRepository.findProjectsByAssignee(userId)) {
            projects.putIfAbsent(p.getId(), p);
        }

        return projects.values().stream()
                .map(Mapper::toProjectResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public ProjectResponse getVisibleProject(Long id) {

        User current = currentUser.get();

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        if (project.getCreatedBy().getId().equals(current.getId())) {
            return Mapper.toProjectResponse(project);
        }

        if (current.getManager() != null &&
                project.getCreatedBy().getId().equals(current.getManager().getId())) {

            return Mapper.toProjectResponse(project);
        }

        throw new ResourceNotFoundException("Project not found");
    }

    private Project findOwned(Long id) {

        User current = currentUser.get();

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        // Project owner
        if (project.getCreatedBy().getId().equals(current.getId())) {
            return project;
        }

        // Manager under owner
        if (current.getManager() != null &&
            project.getCreatedBy().getId().equals(current.getManager().getId())) {
            return project;
        }

        throw new ResourceNotFoundException("Project not found");
    }

    private void apply(Project p, ProjectRequest req) {
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
    }

    private void validateDates(ProjectRequest req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }
}