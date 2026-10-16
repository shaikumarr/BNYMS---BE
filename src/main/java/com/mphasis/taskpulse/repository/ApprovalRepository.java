// repository/ApprovalRepository.java  (full file, one method added)
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // every manager decision, oldest first (for rebuilding the sheet)
    List<Approval> findAllByOrderByDecidedAtAsc();
}