// repository/ApprovalRepository.java
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}