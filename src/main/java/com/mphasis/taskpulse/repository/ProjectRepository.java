// repository/ProjectRepository.java  (full file)
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedByIdOrderByIdDesc(Long managerId);
    Optional<Project> findByIdAndCreatedById(Long id, Long managerId);

    // projects where this employee is a team member
    List<Project> findByMembersId(Long userId);

    // projects this manager owns OR is a team member of (a senior manager gave them work)
    @Query("select distinct p from Project p left join p.members m " +
           "where p.createdBy.id = :userId or m.id = :userId order by p.id desc")
    List<Project> findVisibleTo(@Param("userId") Long userId);
}