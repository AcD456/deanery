package com.university.deanery.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "academic_leave")
public class AcademicLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "order_id")
    private Integer orderId;

    public AcademicLeave() {}

    public AcademicLeave(Integer studentId, LocalDate startDate, LocalDate endDate, Integer orderId) {
        this.studentId = studentId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderId = orderId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
}