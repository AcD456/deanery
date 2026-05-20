package com.university.deanery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(name = "grade_value")
    private Integer gradeValue; // 2, 3, 4, 5 или null если не аттестован

    @Column(name = "grade_type")
    private String gradeType; // EXAM, TEST, COURSE_WORK, EXAM_SESSION

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "academic_year")
    private String academicYear; // например "2024/2025"

    @Column(name = "comment")
    private String comment;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @PrePersist
    protected void onCreate() {
        gradedAt = LocalDateTime.now();
    }

    public Grade() {}

    public Grade(Integer studentId, Integer courseId, Integer teacherId,
                 Integer gradeValue, String gradeType, Integer semester, String academicYear) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.gradeValue = gradeValue;
        this.gradeType = gradeType;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getGradeType() { return gradeType; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}