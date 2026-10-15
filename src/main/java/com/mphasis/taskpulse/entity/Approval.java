// entity/Approval.java
package com.mphasis.taskpulse.entity;

import com.mphasis.taskpulse.enums.Decision;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private WeeklyTask task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime decidedAt;

    public Approval() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WeeklyTask getTask() { return task; }
    public void setTask(WeeklyTask task) { this.task = task; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}