package com.university.deanery.controller;

import com.university.deanery.dto.ChangePasswordRequest;
import com.university.deanery.dto.SecurityQuestionRequest;
import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.Student;
import com.university.deanery.model.StudentContact;
import com.university.deanery.model.User;
import com.university.deanery.repository.*;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.JournalService;
import com.university.deanery.service.StudentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

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

    @Autowired
    private JournalService journalService;

    @Autowired
    private StudentContactRepository studentContactRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @GetMapping("/my-profile")
    public Map<String, Object> getMyProfile(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Student student = studentService.getStudentByUserId(user.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("id", student.getId());
        response.put("fullName", student.getFullName() != null ? student.getFullName() : "");
        response.put("groupId", student.getGroupId());
        response.put("status", student.getStatus() != null ? student.getStatus() : "");

        // Получаем контакты из отдельной таблицы
        StudentContact contact = studentContactRepository.findById(student.getId()).orElse(null);
        if (contact != null) {
            response.put("email", contact.getEmail() != null ? contact.getEmail() : "");
            response.put("phone", contact.getPhone() != null ? contact.getPhone() : "");
        } else {
            response.put("email", "");
            response.put("phone", "");
        }

        return response;
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

        @SuppressWarnings("unchecked")
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

    @PutMapping("/update-profile")
    public Map<String, String> updateProfile(@RequestHeader("Authorization") String token,
                                             @RequestBody UpdateProfileRequest request) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "UPDATE");

        authService.updateUserProfile(user.getId(), request);

        journalService.logSimple(user.getId(), "UPDATE_PROFILE", user.getRole(), user.getId());

        return Map.of("message", "Профиль обновлён");
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@RequestHeader("Authorization") String token,
                                              @RequestBody ChangePasswordRequest request) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "UPDATE");

        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        journalService.logSimple(user.getId(), "CHANGE_PASSWORD", user.getRole(), user.getId());

        return Map.of("message", "Пароль изменён");
    }

    @PutMapping("/security-question")
    public Map<String, String> updateSecurityQuestion(@RequestHeader("Authorization") String token,
                                                      @RequestBody SecurityQuestionRequest request) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "UPDATE");

        authService.updateSecurityQuestion(user.getId(), request.getQuestion(), request.getAnswer());

        journalService.logSimple(user.getId(), "UPDATE_SECURITY_QUESTION", user.getRole(), user.getId());

        return Map.of("message", "Секретный вопрос обновлён");
    }
}