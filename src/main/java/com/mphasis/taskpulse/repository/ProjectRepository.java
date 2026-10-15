// repository/ProjectRepository.java  (full file, one method added)
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedByIdOrderByIdDesc(Long managerId);
    Optional<Project> findByIdAndCreatedById(Long id, Long managerId);

    // NEW: projects where this employee is a team member
    List<Project> findByMembersId(Long userId);
}