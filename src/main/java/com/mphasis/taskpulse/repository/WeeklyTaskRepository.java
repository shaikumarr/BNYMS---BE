// repository/WeeklyTaskRepository.java  (full file, one method added)
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.Project;
import com.mphasis.taskpulse.entity.WeeklyTask;
import com.mphasis.taskpulse.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {

    @Query("select t from WeeklyTask t where t.project.createdBy.id = :managerId " +
           "order by t.project.id, t.weekNumber")
    List<WeeklyTask> findAllForManager(@Param("managerId") Long managerId);

    List<WeeklyTask> findByProjectIdOrderByWeekNumberAsc(Long projectId);

    List<WeeklyTask> findByAssignedToIdOrderByDeadlineAsc(Long userId);

    // all approved tasks of a manager, for the Excel report
    List<WeeklyTask> findByStatusAndProjectCreatedByIdOrderByProjectIdAscWeekNumberAsc(
            TaskStatus status, Long managerId);

    // all tasks with a given status, for rebuilding the live sheet
    List<WeeklyTask> findByStatusOrderByProjectIdAscWeekNumberAsc(TaskStatus status);

    // does this employee still have any weekly parts?
    boolean existsByAssignedToId(Long userId);

    // "ownership" lookups: found only if the task belongs to this manager / user
    Optional<WeeklyTask> findByIdAndProjectCreatedById(Long id, Long managerId);
    Optional<WeeklyTask> findByIdAndAssignedToId(Long id, Long userId);

    Optional<WeeklyTask> findByProjectIdAndAssignedToIdAndWeekNumber(
            Long projectId, Long userId, Integer weekNumber);

    @Query("select distinct t.project from WeeklyTask t where t.assignedTo.id = :userId")
    List<Project> findProjectsByAssignee(@Param("userId") Long userId);
}