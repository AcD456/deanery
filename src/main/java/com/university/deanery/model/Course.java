package com.university.deanery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer hours;

    public Course() {}

    public Course(String name, Integer hours) {
        this.name = name;
        this.hours = hours;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
}