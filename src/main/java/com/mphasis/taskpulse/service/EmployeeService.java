// service/EmployeeService.java  (full file, update and delete added)
package com.mphasis.taskpulse.service;

import com.mphasis.taskpulse.dto.EmployeeRequest;
import com.mphasis.taskpulse.dto.EmployeeUpdateRequest;
import com.mphasis.taskpulse.dto.UserResponse;
import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.enums.Role;
import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.exception.ResourceNotFoundException;
import com.mphasis.taskpulse.mapper.Mapper;
import com.mphasis.taskpulse.repository.ProjectRepository;
import com.mphasis.taskpulse.repository.UserRepository;
import com.mphasis.taskpulse.repository.WeeklyTaskRepository;
import com.mphasis.taskpulse.security.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyTaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    public EmployeeService(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           WeeklyTaskRepository taskRepository,
                           PasswordEncoder passwordEncoder,
                           CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
    }

    public UserResponse createEmployee(EmployeeRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email is already registered");
        }
        User employee = new User();
        employee.setName(req.name());
        employee.setEmail(req.email());
        employee.setPassword(passwordEncoder.encode(req.password()));   // hash it
        employee.setRole(Role.USER);
        employee.setManager(currentUser.get());                         // map under this manager
        return Mapper.toUserResponse(userRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listMyEmployees() {
        return userRepository.findByManagerId(currentUser.get().getId())
                .stream().map(Mapper::toUserResponse).toList();
    }

    public UserResponse updateEmployee(Long id, EmployeeUpdateRequest req) {
        User employee = findOwnEmployee(id);

        // the new email must not belong to someone else
        userRepository.findByEmail(req.email())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BadRequestException("Email is already registered");
                });

        employee.setName(req.name());
        employee.setEmail(req.email());
        if (req.password() != null && !req.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(req.password()));
        }
        return Mapper.toUserResponse(employee);   // saved automatically at commit
    }

    public void deleteEmployee(Long id) {
        User employee = findOwnEmployee(id);

        if (taskRepository.existsByAssignedToId(id)) {
            throw new BadRequestException(
                    "This employee still has weekly parts. Delete or reassign them first");
        }

        // remove the employee from every project team before deleting
        for (Project project : projectRepository.findByMembersId(id)) {
            project.getMembers().remove(employee);
        }
        userRepository.delete(employee);
    }

    private User findOwnEmployee(Long id) {
        return userRepository.findByIdAndManagerId(id, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
}