package com.university.deanery.controller;

import com.university.deanery.dto.ChangePasswordRequest;
import com.university.deanery.dto.SecurityQuestionRequest;
import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.User;
import com.university.deanery.model.Student;
import com.university.deanery.model.Application;
import com.university.deanery.model.Journal;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.DeanService;
import com.university.deanery.service.JournalService;
import com.university.deanery.repository.ApplicationRepository;
import com.university.deanery.repository.StudentRepository;
import com.university.deanery.repository.JournalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dean")
public class DeanController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private DeanService deanService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JournalService journalService;

    @Autowired
    private JournalRepository journalRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @GetMapping("/applications")
    public List<Application> getAllApplications(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "applications", "READ");
        return applicationRepository.findByStatus("PENDING");
    }

    @GetMapping("/students")
    public List<Student> getAllStudents(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");
        return studentRepository.findAll();
    }

    @GetMapping("/students-list")
    public List<Map<String, Object>> getStudentsList(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT s.id, s.full_name, g.name as group_name " +
                        "FROM students s " +
                        "LEFT JOIN groups g ON s.group_id = g.id " +
                        "ORDER BY s.full_name"
        ).getResultList();

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", row[0]);
            student.put("fullName", row[1] != null ? row[1].toString() : "—");
            student.put("groupName", row[2] != null ? row[2].toString() : "—");
            students.add(student);
        }
        return students;
    }

    @GetMapping("/student-transfers/{studentId}")
    public List<Map<String, Object>> getStudentTransfers(@PathVariable Integer studentId,
                                                         @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT sgh.from_group_id, sgh.to_group_id, sgh.changed_at, " +
                        "       fg.name as from_group_name, tg.name as to_group_name " +
                        "FROM student_group_history sgh " +
                        "LEFT JOIN groups fg ON sgh.from_group_id = fg.id " +
                        "LEFT JOIN groups tg ON sgh.to_group_id = tg.id " +
                        "WHERE sgh.student_id = ? " +
                        "ORDER BY sgh.changed_at DESC"
        ).setParameter(1, studentId).getResultList();

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

    @PostMapping("/approve-application/{applicationId}")
    public String approveApplication(@PathVariable Integer applicationId,
                                     @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "applications", "APPROVE");

        deanService.approveApplication(applicationId, user.getId());

        journalService.logSimple(user.getId(), "APPROVE_APPLICATION", "Application", applicationId);

        return "Заявление одобрено";
    }

    @PostMapping("/expel-student/{studentId}")
    public String expelStudent(@PathVariable Integer studentId,
                               @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "EXPEL");

        deanService.expelStudent(studentId, user.getId());

        journalService.logSimple(user.getId(), "EXPEL_STUDENT", "Student", studentId);

        return "Студент отчислен";
    }

    @PostMapping("/transfer-student/{studentId}/to-group/{groupId}")
    public String transferStudent(@PathVariable Integer studentId,
                                  @PathVariable Integer groupId,
                                  @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "TRANSFER");

        deanService.transferStudent(studentId, groupId, user.getId());

        journalService.logSimple(user.getId(), "TRANSFER_STUDENT", "Student", studentId);

        return "Студент переведён";
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

        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        journalService.logSimple(user.getId(), "CHANGE_PASSWORD", user.getRole(), user.getId());

        return Map.of("message", "Пароль изменён");
    }

    @PutMapping("/security-question")
    public Map<String, String> updateSecurityQuestion(@RequestHeader("Authorization") String token,
                                                      @RequestBody SecurityQuestionRequest request) {
        User user = getUser(token);

        authService.updateSecurityQuestion(user.getId(), request.getQuestion(), request.getAnswer());

        journalService.logSimple(user.getId(), "UPDATE_SECURITY_QUESTION", user.getRole(), user.getId());

        return Map.of("message", "Секретный вопрос обновлён");
    }

    // Добавьте в DeanController.java

    @GetMapping("/students/filter")
    public List<Map<String, Object>> getFilteredStudents(@RequestHeader("Authorization") String token,
                                                         @RequestParam(required = false) Integer groupId,
                                                         @RequestParam(required = false) String search) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.full_name, g.name as group_name, s.status, s.user_id " +
                        "FROM students s " +
                        "LEFT JOIN groups g ON s.group_id = g.id WHERE 1=1"
        );

        if (groupId != null && groupId > 0) {
            sql.append(" AND s.group_id = ").append(groupId);
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND LOWER(s.full_name) LIKE LOWER('%").append(search).append("%')");
        }

        sql.append(" ORDER BY s.full_name");

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(sql.toString()).getResultList();

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", row[0]);
            student.put("fullName", row[1] != null ? row[1].toString() : "—");
            student.put("groupName", row[2] != null ? row[2].toString() : "—");
            student.put("status", row[3] != null ? row[3].toString() : "—");
            student.put("userId", row[4]);
            students.add(student);
        }
        return students;
    }

    @GetMapping("/groups-list")
    public List<Map<String, Object>> getGroupsList(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT id, name FROM groups ORDER BY name"
        ).getResultList();

        List<Map<String, Object>> groups = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> group = new HashMap<>();
            group.put("id", row[0]);
            group.put("name", row[1] != null ? row[1].toString() : "—");
            groups.add(group);
        }
        return groups;
    }

    @GetMapping("/student-details/{studentId}")
    public Map<String, Object> getStudentDetails(@PathVariable Integer studentId,
                                                 @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "READ");

        Map<String, Object> details = new HashMap<>();

        // Основная информация о студенте
        @SuppressWarnings("unchecked")
        List<Object[]> studentInfo = entityManager.createNativeQuery(
                "SELECT s.id, s.full_name, g.name as group_name, s.status, " +
                        "       sc.email, sc.phone " +
                        "FROM students s " +
                        "LEFT JOIN groups g ON s.group_id = g.id " +
                        "LEFT JOIN student_contacts sc ON s.id = sc.student_id " +
                        "WHERE s.id = ?"
        ).setParameter(1, studentId).getResultList();

        if (!studentInfo.isEmpty()) {
            Object[] row = studentInfo.get(0);
            details.put("id", row[0]);
            details.put("fullName", row[1] != null ? row[1].toString() : "—");
            details.put("groupName", row[2] != null ? row[2].toString() : "—");
            details.put("status", row[3] != null ? row[3].toString() : "—");
            details.put("email", row[4] != null ? row[4].toString() : "—");
            details.put("phone", row[5] != null ? row[5].toString() : "—");
        }

        // История переводов
        @SuppressWarnings("unchecked")
        List<Object[]> transfers = entityManager.createNativeQuery(
                "SELECT sgh.from_group_id, sgh.to_group_id, sgh.changed_at, " +
                        "       fg.name as from_group_name, tg.name as to_group_name " +
                        "FROM student_group_history sgh " +
                        "LEFT JOIN groups fg ON sgh.from_group_id = fg.id " +
                        "LEFT JOIN groups tg ON sgh.to_group_id = tg.id " +
                        "WHERE sgh.student_id = ? " +
                        "ORDER BY sgh.changed_at DESC"
        ).setParameter(1, studentId).getResultList();

        List<Map<String, Object>> transfersList = new ArrayList<>();
        for (Object[] row : transfers) {
            Map<String, Object> transfer = new HashMap<>();
            transfer.put("fromGroupId", row[0]);
            transfer.put("toGroupId", row[1]);
            transfer.put("changedAt", row[2] != null ? row[2].toString() : "—");
            transfer.put("fromGroupName", row[3] != null ? row[3].toString() : "—");
            transfer.put("toGroupName", row[4] != null ? row[4].toString() : "—");
            transfersList.add(transfer);
        }
        details.put("transfers", transfersList);

        return details;
    }
}