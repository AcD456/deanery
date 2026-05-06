package com.university.deanery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_course")
public class TeacherCourse {

    @EmbeddedId
    private TeacherCourseId id;

    public TeacherCourse() {}

    public TeacherCourse(TeacherCourseId id) {
        this.id = id;
    }

    public TeacherCourseId getId() { return id; }
    public void setId(TeacherCourseId id) { this.id = id; }
}