// repository/DailyUpdateRepository.java
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.DailyUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long> {
    List<DailyUpdate> findByTaskIdOrderByUpdateDateDescIdDesc(Long taskId);
}