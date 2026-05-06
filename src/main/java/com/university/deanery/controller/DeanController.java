package com.university.deanery.controller;

import com.university.deanery.model.User;
import com.university.deanery.model.Student;
import com.university.deanery.model.Application;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.DeanService;
import com.university.deanery.service.JournalService;
import com.university.deanery.repository.ApplicationRepository;
import com.university.deanery.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private JournalService journalService;

    @PostMapping("/approve-application/{applicationId}")
    public String approveApplication(@PathVariable Integer applicationId,
                                     @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "applications", "APPROVE");

        deanService.approveApplication(applicationId, user.getId());

        // Логируем действие
        journalService.logSimple(user.getId(), "APPROVE_APPLICATION", "Application", applicationId);

        return "Заявление одобрено";
    }

    @PostMapping("/expel-student/{studentId}")
    public String expelStudent(@PathVariable Integer studentId,
                               @RequestHeader("Authorization") String token) {
        User user = getUser(token);
        aclService.checkAccess(user, "students", "EXPEL");

        deanService.expelStudent(studentId, user.getId());

        // Логируем действие
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

        // Логируем действие
        journalService.logSimple(user.getId(), "TRANSFER_STUDENT", "Student", studentId);

        return "Студент переведён";
    }
}