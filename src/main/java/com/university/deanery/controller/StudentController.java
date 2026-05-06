package com.university.deanery.controller;

import com.university.deanery.model.Student;
import com.university.deanery.model.User;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.StudentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private StudentService studentService;

    @PersistenceContext
    private EntityManager entityManager;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @GetMapping("/my-profile")
    public Student getMyProfile(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        return studentService.getStudentByUserId(user.getId());
    }

    @GetMapping("/group/{groupId}")
    public List<Student> getGroupStudents(@PathVariable Integer groupId,
                                          @RequestHeader("Authorization") String token) {

        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        return studentService.getStudentsByGroup(groupId);
    }

    @GetMapping("/my-courses")
    public List<Map<String, Object>> getMyCourses(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Student student = studentService.getStudentByUserId(user.getId());
        Integer groupId = student.getGroupId();

        // Запрос к базе данных через EntityManager
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT c.name, c.hours, cur.semester, t.full_name as teacher_name " +
                        "FROM curriculum cur " +
                        "JOIN courses c ON cur.course_id = c.id " +
                        "LEFT JOIN teachers t ON cur.teacher_id = t.id " +
                        "WHERE cur.group_id = ?"
        ).setParameter(1, groupId).getResultList();

        List<Map<String, Object>> courses = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> course = new HashMap<>();
            course.put("name", row[0] != null ? row[0].toString() : "—");
            course.put("hours", row[1] != null ? row[1].toString() : "—");
            course.put("semester", row[2] != null ? row[2].toString() : "—");
            course.put("teacherName", row[3] != null ? row[3].toString() : "—");
            courses.add(course);
        }
        return courses;
    }
}