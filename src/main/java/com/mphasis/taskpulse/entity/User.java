// entity/User.java
package com.mphasis.taskpulse.entity;

import com.mphasis.taskpulse.enums.Role;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;            // BCrypt hash, never plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")    // null for managers
    private User manager;

    // Projects assigned to this employee (owning side of the relationship)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "employee_projects",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<Project> assignedProjects = new HashSet<>();

    public User() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }

    public Set<Project> getAssignedProjects() { return assignedProjects; }
    public void setAssignedProjects(Set<Project> assignedProjects) { this.assignedProjects = assignedProjects; }
}