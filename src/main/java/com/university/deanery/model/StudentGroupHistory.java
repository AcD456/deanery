package com.university.deanery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_group_history")
public class StudentGroupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "from_group_id")
    private Integer fromGroupId;

    @Column(name = "to_group_id")
    private Integer toGroupId;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "order_id")
    private Integer orderId;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }

    public StudentGroupHistory() {}

    public StudentGroupHistory(Integer studentId, Integer fromGroupId, Integer toGroupId, Integer orderId) {
        this.studentId = studentId;
        this.fromGroupId = fromGroupId;
        this.toGroupId = toGroupId;
        this.orderId = orderId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getFromGroupId() { return fromGroupId; }
    public void setFromGroupId(Integer fromGroupId) { this.fromGroupId = fromGroupId; }

    public Integer getToGroupId() { return toGroupId; }
    public void setToGroupId(Integer toGroupId) { this.toGroupId = toGroupId; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
}