package com.university.deanery.dto;

import java.time.LocalDateTime;

public class GradeResponse {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private Integer courseId;
    private String courseName;
    private Integer gradeValue;
    private String gradeType;
    private Integer semester;
    private String comment;
    private LocalDateTime gradedAt;

    // Constructors
    public GradeResponse() {}

    public GradeResponse(Integer id, Integer studentId, String studentName,
                         Integer courseId, String courseName, Integer gradeValue,
                         String gradeType, Integer semester, String comment, LocalDateTime gradedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.gradeValue = gradeValue;
        this.gradeType = gradeType;
        this.semester = semester;
        this.comment = comment;
        this.gradedAt = gradedAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getGradeType() { return gradeType; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}