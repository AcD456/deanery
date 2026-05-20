package com.university.deanery.controller;

import com.university.deanery.dto.ChangePasswordRequest;
import com.university.deanery.dto.CreateUserRequest;
import com.university.deanery.dto.SecurityQuestionRequest;
import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.JournalService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private StudentContactRepository studentContactRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private JournalService journalService;

    @PersistenceContext
    private EntityManager entityManager;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestHeader("Authorization") String token) {

        User admin = getUser(token);

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("No rights");
        }

        return userRepository.findAll();
    }

    @GetMapping("/groups-list")
    public List<Map<String, Object>> getGroupsList(
            @RequestHeader("Authorization") String token) {

        User admin = getUser(token);

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("No rights");
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT id, name FROM groups ORDER BY name"
        ).getResultList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            result.add(map);
        }

        return result;
    }

    @PostMapping("/change-role")
    public User changeRole(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer userId,
            @RequestParam String role) {

        User admin = getUser(token);

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("No rights");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldRole = user.getRole();

        user.setRole(role);

        userRepository.save(user);

        journalService.logSimple(
                admin.getId(),
                "CHANGE_ROLE",
                "User",
                userId,
                oldRole,
                role
        );

        return user;
    }

    @PutMapping("/update-profile")
    public Map<String, String> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {

        User user = getUser(token);

        aclService.checkAccess(user, "profile", "UPDATE");

        authService.updateUserProfile(user.getId(), request);

        journalService.logSimple(
                user.getId(),
                "UPDATE_PROFILE",
                user.getRole(),
                user.getId()
        );

        return Map.of("message", "Profile updated");
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request) {

        User user = getUser(token);

        authService.changePassword(
                user.getId(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        journalService.logSimple(
                user.getId(),
                "CHANGE_PASSWORD",
                user.getRole(),
                user.getId()
        );

        return Map.of("message", "Password changed");
    }

    @PutMapping("/security-question")
    public Map<String, String> updateSecurityQuestion(
            @RequestHeader("Authorization") String token,
            @RequestBody SecurityQuestionRequest request) {

        User user = getUser(token);

        authService.updateSecurityQuestion(
                user.getId(),
                request.getQuestion(),
                request.getAnswer()
        );

        journalService.logSimple(
                user.getId(),
                "UPDATE_SECURITY_QUESTION",
                user.getRole(),
                user.getId()
        );

        return Map.of("message", "Security question updated");
    }

    @Transactional
    @PostMapping("/create-user")
    public Map<String, Object> createUser(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateUserRequest request) {

        User admin = getUser(token);

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admin can create users");
        }

        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new RuntimeException("Login is empty");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is empty");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is empty");
        }

        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new RuntimeException(
                    "User with login '" + request.getLogin() + "' already exists"
            );
        }

        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setPassword(request.getPassword());
        newUser.setRole(request.getRole());

        User savedUser = userRepository.save(newUser);

        switch (request.getRole()) {

            case "STUDENT":
                createStudentProfile(savedUser, request);
                break;

            case "TEACHER":
                createTeacherProfile(savedUser, request);
                break;

            case "DEAN":
                createDeanProfile(savedUser, request);
                break;

            case "APPLICANT":
                createApplicantProfile(savedUser, request);
                break;

            case "ADMIN":
                break;

            default:
                throw new RuntimeException("Unknown role");
        }

        journalService.logSimple(
                admin.getId(),
                "CREATE_USER",
                "User",
                savedUser.getId(),
                null,
                request.getRole()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("userId", savedUser.getId());

        return response;
    }

    private void createStudentProfile(User user, CreateUserRequest request) {

        if (studentRepository.findByUser_Id(user.getId()).isPresent()) {
            return;
        }

        Student student = new Student();

        student.setUserId(user.getId());

        student.setFullName(
                request.getFullName() != null &&
                        !request.getFullName().isBlank()
                        ? request.getFullName()
                        : "Student " + user.getLogin()
        );

        student.setGroupId(
                request.getGroupId() != null
                        ? request.getGroupId()
                        : 1
        );

        student.setStatus("ACTIVE");

        Student savedStudent = studentRepository.save(student);

        System.out.println(
                "Created student: " +
                        savedStudent.getId() +
                        " user_id=" +
                        savedStudent.getUserId()
        );

        if ((request.getEmail() != null && !request.getEmail().isBlank())
                || (request.getPhone() != null && !request.getPhone().isBlank())) {

            StudentContact contact = new StudentContact();

            contact.setStudentId(savedStudent.getId());
            contact.setEmail(request.getEmail());
            contact.setPhone(request.getPhone());

            studentContactRepository.save(contact);
        }

        addStudentToCurriculum(savedStudent);
    }

    private void createTeacherProfile(User user, CreateUserRequest request) {

        if (teacherRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        Teacher teacher = new Teacher();

        teacher.setUserId(user.getId());

        teacher.setFullName(
                request.getFullName() != null &&
                        !request.getFullName().isBlank()
                        ? request.getFullName()
                        : "Teacher " + user.getLogin()
        );

        teacher.setPosition(
                request.getPosition() != null
                        ? request.getPosition()
                        : ""
        );

        teacher.setAcademicDegree(
                request.getAcademicDegree() != null
                        ? request.getAcademicDegree()
                        : ""
        );

        teacher.setPhone(
                request.getPhone() != null
                        ? request.getPhone()
                        : ""
        );

        teacherRepository.save(teacher);
    }

    private void createDeanProfile(User user, CreateUserRequest request) {

        if (teacherRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        Teacher dean = new Teacher();

        dean.setUserId(user.getId());

        dean.setFullName(
                request.getFullName() != null &&
                        !request.getFullName().isBlank()
                        ? request.getFullName()
                        : "Dean " + user.getLogin()
        );

        dean.setPosition("Dean");

        dean.setPhone(
                request.getPhone() != null
                        ? request.getPhone()
                        : ""
        );

        teacherRepository.save(dean);
    }

    private void createApplicantProfile(User user, CreateUserRequest request) {

        if (applicantRepository.findByUser_Id(user.getId()).isPresent()) {
            return;
        }

        Applicant applicant = new Applicant();

        applicant.setUser(user);

        applicant.setFullName(
                request.getFullName() != null &&
                        !request.getFullName().isBlank()
                        ? request.getFullName()
                        : "Applicant " + user.getLogin()
        );

        Applicant savedApplicant = applicantRepository.save(applicant);

        if (request.getProgramId() != null) {

            Application application = new Application();

            application.setApplicantId(savedApplicant.getId());
            application.setProgramId(request.getProgramId());
            application.setStatus("PENDING");

            applicationRepository.save(application);
        }
    }

    private void addStudentToCurriculum(Student student) {

        Integer groupId = student.getGroupId();

        if (groupId == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Object[]> courses = entityManager.createNativeQuery(
                        """
                        SELECT course_id, teacher_id, semester
                        FROM curriculum
                        WHERE group_id = ?
                        """
                )
                .setParameter(1, groupId)
                .getResultList();

        String currentYear = getCurrentAcademicYear();

        for (Object[] row : courses) {

            Integer courseId = (Integer) row[0];
            Integer teacherId = (Integer) row[1];
            Integer semester = (Integer) row[2];

            Number count = (Number) entityManager.createNativeQuery(
                            """
                            SELECT COUNT(*)
                            FROM grades
                            WHERE student_id = ?
                            AND course_id = ?
                            AND teacher_id = ?
                            AND semester = ?
                            """
                    )
                    .setParameter(1, student.getId())
                    .setParameter(2, courseId)
                    .setParameter(3, teacherId)
                    .setParameter(4, semester)
                    .getSingleResult();

            if (count.longValue() == 0) {

                entityManager.createNativeQuery(
                                """
                                INSERT INTO grades
                                (student_id, course_id, teacher_id, semester, academic_year)
                                VALUES (?, ?, ?, ?, ?)
                                """
                        )
                        .setParameter(1, student.getId())
                        .setParameter(2, courseId)
                        .setParameter(3, teacherId)
                        .setParameter(4, semester)
                        .setParameter(5, currentYear)
                        .executeUpdate();
            }
        }
    }

    private String getCurrentAcademicYear() {

        int year = Year.now().getValue();

        return year + "/" + (year + 1);
    }

    @GetMapping("/journal")
    public List<Map<String, Object>> getJournal(@RequestHeader("Authorization") String token,
                                                @RequestParam(required = false) Integer userId,
                                                @RequestParam(required = false) String action,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        User admin = getUser(token);
        if (!admin.getRole().equals("ADMIN")) {
            throw new RuntimeException("Нет прав. Только администратор может просматривать журнал");
        }

        StringBuilder sql = new StringBuilder(
                "SELECT j.id, j.user_id, u.login, j.action, j.entity_type, j.entity_id, " +
                        "j.old_value, j.new_value, j.created_at " +
                        "FROM journal j " +
                        "LEFT JOIN users u ON j.user_id = u.id " +
                        "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND j.user_id = ?");
            params.add(userId);
        }

        if (action != null && !action.isEmpty()) {
            sql.append(" AND j.action = ?");
            params.add(action);
        }

        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND j.created_at >= ?");
            params.add(startDate + " 00:00:00");
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND j.created_at <= ?");
            params.add(endDate + " 23:59:59");
        }

        sql.append(" ORDER BY j.created_at DESC");

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(sql.toString())
                .getResultList();

        List<Map<String, Object>> journalEntries = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", row[0]);
            entry.put("userId", row[1]);
            entry.put("userLogin", row[2] != null ? row[2].toString() : "Удалён");
            entry.put("action", row[3] != null ? row[3].toString() : "");
            entry.put("entityType", row[4] != null ? row[4].toString() : "");
            entry.put("entityId", row[5]);
            entry.put("oldValue", row[6] != null ? row[6].toString() : "");
            entry.put("newValue", row[7] != null ? row[7].toString() : "");
            entry.put("createdAt", row[8] != null ? row[8].toString() : "");
            journalEntries.add(entry);
        }

        return journalEntries;
    }

    @GetMapping("/journal/actions")
    public List<String> getAvailableActions(@RequestHeader("Authorization") String token) {
        User admin = getUser(token);
        if (!admin.getRole().equals("ADMIN")) {
            throw new RuntimeException("Нет прав");
        }

        @SuppressWarnings("unchecked")
        List<String> results = entityManager.createNativeQuery(
                "SELECT DISTINCT action FROM journal ORDER BY action"
        ).getResultList();

        return results;
    }
}