package com.university.deanery.controller;

import com.university.deanery.model.Teacher;
import com.university.deanery.model.User;
import com.university.deanery.repository.TeacherRepository;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private TeacherRepository teacherRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @GetMapping("/my-profile")
    public Teacher getMyProfile(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        return teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
    }

    @GetMapping("/my-courses")
    public List<Map<String, Object>> getMyCourses(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT DISTINCT c.name, c.hours, cur.semester " +
                        "FROM teacher_course tc " +
                        "JOIN courses c ON tc.course_id = c.id " +
                        "LEFT JOIN curriculum cur ON cur.course_id = c.id AND cur.teacher_id = tc.teacher_id " +
                        "WHERE tc.teacher_id = ?"
        ).setParameter(1, teacher.getId()).getResultList();

        List<Map<String, Object>> courses = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> course = new HashMap<>();
            course.put("name", row[0] != null ? row[0].toString() : "—");
            course.put("hours", row[1] != null ? row[1].toString() : "—");
            course.put("semester", row[2] != null ? row[2].toString() : "—");
            courses.add(course);
        }
        return courses;
    }

    @GetMapping("/my-schedule")
    public List<Map<String, Object>> getMySchedule(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        // Здесь можно добавить реальное расписание, пока возвращаем пустой список
        // Для демонстрации создаём тестовое расписание
        List<Map<String, Object>> schedule = new ArrayList<>();

        // Пример: расписание на основе дисциплин
        List<Object[]> courses = entityManager.createNativeQuery(
                "SELECT DISTINCT c.name, cur.group_id, g.name as group_name " +
                        "FROM teacher_course tc " +
                        "JOIN courses c ON tc.course_id = c.id " +
                        "LEFT JOIN curriculum cur ON cur.course_id = c.id AND cur.teacher_id = tc.teacher_id " +
                        "LEFT JOIN groups g ON cur.group_id = g.id " +
                        "WHERE tc.teacher_id = ?"
        ).setParameter(1, teacher.getId()).getResultList();

        for (Object[] row : courses) {
            Map<String, Object> lesson = new HashMap<>();
            lesson.put("courseName", row[0] != null ? row[0].toString() : "—");
            lesson.put("groupId", row[1] != null ? row[1].toString() : "—");
            lesson.put("groupName", row[2] != null ? row[2].toString() : "—");
            lesson.put("weekday", "—");
            lesson.put("startTime", "—");
            lesson.put("endTime", "—");
            lesson.put("classroom", "—");
            schedule.add(lesson);
        }

        return schedule;
    }

    @GetMapping("/my-students")
    public List<Map<String, Object>> getMyStudents(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT DISTINCT s.id, s.full_name, s.status, g.name as group_name " +
                        "FROM curriculum cur " +
                        "JOIN groups g ON cur.group_id = g.id " +
                        "JOIN students s ON s.group_id = g.id " +
                        "WHERE cur.teacher_id = ?"
        ).setParameter(1, teacher.getId()).getResultList();

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", row[0]);
            student.put("fullName", row[1] != null ? row[1].toString() : "—");
            student.put("status", row[2] != null ? row[2].toString() : "—");
            student.put("groupName", row[3] != null ? row[3].toString() : "—");
            students.add(student);
        }
        return students;
    }
}