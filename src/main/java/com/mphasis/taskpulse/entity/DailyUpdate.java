// entity/DailyUpdate.java  (full file, rawNotes added)
package com.mphasis.taskpulse.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_updates")
public class DailyUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private WeeklyTask task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDate updateDate;

    // Final professional text (this is what goes to Excel)
    @Column(length = 2000, nullable = false)
    private String notes;

    // What the fresher originally typed. Nullable, so old rows still work.
    @Column(length = 2000)
    private String rawNotes;

    private Double hoursSpent;

    private Integer percentAdded;

    public DailyUpdate() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WeeklyTask getTask() { return task; }
    public void setTask(WeeklyTask task) { this.task = task; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDate updateDate) { this.updateDate = updateDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRawNotes() { return rawNotes; }
    public void setRawNotes(String rawNotes) { this.rawNotes = rawNotes; }

    public Double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(Double hoursSpent) { this.hoursSpent = hoursSpent; }

    public Integer getPercentAdded() { return percentAdded; }
    public void setPercentAdded(Integer percentAdded) { this.percentAdded = percentAdded; }
}