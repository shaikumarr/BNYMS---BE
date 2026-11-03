// repository/DailyUpdateRepository.java
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.DailyUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long> {
    List<DailyUpdate> findByTaskIdOrderByUpdateDateDescIdDesc(Long taskId);

    // an update, but only if it belongs to this employee
    Optional<DailyUpdate> findByIdAndUserId(Long id, Long userId);
}