package com.university.deanery.dto;

public class GradeRequest {
    private Integer studentId;
    private Integer courseId;
    private Integer gradeValue;
    private String gradeType;
    private Integer semester;
    private String comment;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getGradeType() { return gradeType; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}