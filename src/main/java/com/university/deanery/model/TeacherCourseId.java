package com.university.deanery.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TeacherCourseId implements Serializable {

    private Integer teacherId;
    private Integer courseId;

    public TeacherCourseId() {}

    public TeacherCourseId(Integer teacherId, Integer courseId) {
        this.teacherId = teacherId;
        this.courseId = courseId;
    }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeacherCourseId that = (TeacherCourseId) o;
        return Objects.equals(teacherId, that.teacherId) &&
                Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, courseId);
    }
}