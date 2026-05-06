package com.university.deanery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "curriculum")
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "teacher_id")
    private Integer teacherId;

    private Integer semester;

    public Curriculum() {}

    public Curriculum(Integer groupId, Integer courseId, Integer teacherId, Integer semester) {
        this.groupId = groupId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.semester = semester;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
}