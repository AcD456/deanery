package com.university.deanery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "degree_levels")
public class DegreeLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public DegreeLevel() {}

    public DegreeLevel(String name) {
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}