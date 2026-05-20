package com.university.deanery.dto;

public class CreateUserRequest {
    // Общие поля
    private String login;
    private String password;
    private String role; // STUDENT, TEACHER, DEAN, ADMIN, APPLICANT

    // Общие для STUDENT и TEACHER
    private String fullName;
    private String email;
    private String phone;

    // Для STUDENT
    private Integer groupId;

    // Для TEACHER
    private String position;
    private String academicDegree;

    // Для APPLICANT
    private Integer programId;

    // Геттеры и сеттеры
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getAcademicDegree() { return academicDegree; }
    public void setAcademicDegree(String academicDegree) { this.academicDegree = academicDegree; }

    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }
}