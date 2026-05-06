package com.university.deanery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_contacts")
public class StudentContact {

    @Id
    @Column(name = "student_id")
    private Integer studentId;

    private String email;
    private String phone;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    public StudentContact() {}

    public StudentContact(Integer studentId, String email, String phone) {
        this.studentId = studentId;
        this.email = email;
        this.phone = phone;
    }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}