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
import com.university.deanery.model.Group;

import java.util.*;

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

    // Добавьте эти методы в StudentController

    @GetMapping("/my-transfers")
    public List<Map<String, Object>> getMyTransfers(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Student student = studentService.getStudentByUserId(user.getId());

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT sgh.from_group_id, sgh.to_group_id, sgh.changed_at, " +
                        "       fg.name as from_group_name, tg.name as to_group_name " +
                        "FROM student_group_history sgh " +
                        "LEFT JOIN groups fg ON sgh.from_group_id = fg.id " +
                        "LEFT JOIN groups tg ON sgh.to_group_id = tg.id " +
                        "WHERE sgh.student_id = ? " +
                        "ORDER BY sgh.changed_at DESC"
        ).setParameter(1, student.getId()).getResultList();

        List<Map<String, Object>> transfers = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> transfer = new HashMap<>();
            transfer.put("fromGroupId", row[0]);
            transfer.put("toGroupId", row[1]);
            transfer.put("changedAt", row[2] != null ? row[2].toString() : "—");
            transfer.put("fromGroupName", row[3] != null ? row[3].toString() : "—");
            transfer.put("toGroupName", row[4] != null ? row[4].toString() : "—");
            transfers.add(transfer);
        }
        return transfers;
    }

//    @GetMapping("/my-grades")
//    public List<Map<String, Object>> getMyGrades(@RequestHeader("Authorization") String token) {
//        User user = getUser(token);
//        aclService.checkAccess(user, "profile", "READ");
//
//        Student student = studentService.getStudentByUserId(user.getId());
//        Integer groupId = student.getGroupId();
//
//        @SuppressWarnings("unchecked")
//        List<Object[]> results = entityManager.createNativeQuery(
//                "SELECT c.name, cur.semester, t.full_name " +
//                        "FROM curriculum cur " +
//                        "JOIN courses c ON cur.course_id = c.id " +
//                        "LEFT JOIN teachers t ON cur.teacher_id = t.id " +
//                        "WHERE cur.group_id = ? " +
//                        "AND c.name IS NOT NULL " +
//                        "AND c.name != '' " +
//                        "ORDER BY cur.semester ASC, c.name ASC"
//        ).setParameter(1, groupId).getResultList();
//
//        List<Map<String, Object>> grades = new ArrayList<>();
//        for (Object[] row : results) {
//
//            Map<String, Object> grade = new LinkedHashMap<>();
//
//            grade.put("courseName",
//                    row[0] != null ? row[0].toString().trim() : "?");
//
//            grade.put("semester",
//                    row[1] != null ? row[1].toString() : "?");
//
//            grade.put("teacherName",
//                    row[2] != null ? row[2].toString() : "?");
//
//            grade.put("grade", "-");
//
//            grades.add(grade);
//        }
//
//        return grades;
//    }

    @GetMapping("/my-grades")
    public List<Map<String, Object>> getMyGrades(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Student student = studentService.getStudentByUserId(user.getId());

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT g.grade_value, g.grade_type, g.semester, g.comment, g.graded_at, " +
                        "       c.name as course_name, t.full_name as teacher_name " +
                        "FROM grades g " +
                        "JOIN courses c ON g.course_id = c.id " +
                        "LEFT JOIN teachers t ON g.teacher_id = t.id " +
                        "WHERE g.student_id = ? " +
                        "ORDER BY g.semester ASC, g.graded_at DESC"
        ).setParameter(1, student.getId()).getResultList();

        List<Map<String, Object>> grades = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> grade = new LinkedHashMap<>();
            grade.put("courseName", row[5] != null ? row[5].toString() : "—");
            grade.put("gradeValue", row[0] != null ? row[0].toString() : "—");
            grade.put("gradeType", getGradeTypeName(row[1] != null ? row[1].toString() : null));
            grade.put("semester", row[2] != null ? row[2].toString() : "—");
            grade.put("teacherName", row[6] != null ? row[6].toString() : "—");
            grade.put("comment", row[3] != null ? row[3].toString() : "—");
            grade.put("gradedAt", row[4] != null ? row[4].toString() : "—");
            grades.add(grade);
        }

        return grades;
    }

    private String getGradeTypeName(String type) {
        if (type == null) return "—";
        switch(type) {
            case "EXAM": return "Экзамен";
            case "TEST": return "Зачёт";
            case "COURSE_WORK": return "Курсовая работа";
            case "EXAM_SESSION": return "Экзаменационная сессия";
            default: return type;
        }
    }

    @GetMapping("/my-group-students")
    public List<Map<String, Object>> getMyGroupStudents(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        // Получаем текущего студента
        Student currentStudent = studentService.getStudentByUserId(user.getId());
        Integer myGroupId = currentStudent.getGroupId();

        if (myGroupId == null) {
            return new ArrayList<>();
        }

        // Получаем студентов только из моей группы
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT s.full_name, s.status " +
                        "FROM students s " +
                        "WHERE s.group_id = ? " +
                        "ORDER BY s.full_name"
        ).setParameter(1, myGroupId).getResultList();

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> student = new HashMap<>();
            student.put("fullName", row[0] != null ? row[0].toString() : "—");
            student.put("status", row[1] != null ? row[1].toString() : "—");
            students.add(student);
        }
        return students;
    }

    @GetMapping("/my-group-info")
    public Map<String, Object> getMyGroupInfo(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "READ");

        Map<String, Object> response = new HashMap<>();

        try {
            Student student = studentService.getStudentByUserId(user.getId());
            Integer groupId = student.getGroupId();
            response.put("groupId", groupId);

            if (groupId != null && groupId > 0) {
                // Простой запрос через entityManager
                String groupName = (String) entityManager.createNativeQuery(
                        "SELECT name FROM groups WHERE id = ?"
                ).setParameter(1, groupId).getSingleResult();

                response.put("groupName", groupName != null ? groupName : "Не указана");
            } else {
                response.put("groupName", "Не указана");
            }
        } catch (Exception e) {
            response.put("groupId", null);
            response.put("groupName", "Ошибка загрузки");
            response.put("error", e.getMessage());
        }

        return response;
    }
}