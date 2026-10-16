// repository/UserRepository.java
package com.mphasis.taskpulse.repository;

import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findByManagerId(Long managerId);
    Optional<User> findByIdAndManagerId(Long id, Long managerId);

    // all users with a given role (used by the Excel background job to find managers)
    List<User> findByRole(Role role);

    // Same lookups, but load assigned projects in the same query (avoids N+1)
    @EntityGraph(attributePaths = "assignedProjects")
    List<User> findWithProjectsByManagerIdOrderByNameAsc(Long managerId);

    @EntityGraph(attributePaths = "assignedProjects")
    Optional<User> findWithProjectsByIdAndManagerId(Long id, Long managerId);
}