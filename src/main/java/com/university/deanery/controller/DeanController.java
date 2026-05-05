package com.university.deanery.controller;

import com.university.deanery.model.User;
import com.university.deanery.security.AclService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.DeanService;
import com.university.deanery.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dean")
public class DeanController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AclService aclService;

    @Autowired
    private DeanService deanService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/approve-application/{applicationId}")
    public String approveApplication(@PathVariable Integer applicationId,
                                     @RequestHeader String login,
                                     @RequestHeader String password) {
        User user = authService.authenticate(login, password);
        aclService.checkAccess(user, "applications", "APPROVE");
        deanService.approveApplication(applicationId, user.getId());
        return "Заявление одобрено";
    }

    @PostMapping("/expel-student/{studentId}")
    public String expelStudent(@PathVariable Integer studentId,
                               @RequestHeader String login,
                               @RequestHeader String password) {
        User user = authService.authenticate(login, password);
        aclService.checkAccess(user, "students", "EXPEL");
        deanService.expelStudent(studentId, user.getId());
        return "Студент отчислен";
    }

    @PostMapping("/transfer-student/{studentId}/to-group/{groupId}")
    public String transferStudent(@PathVariable Integer studentId,
                                  @PathVariable Integer groupId,
                                  @RequestHeader String login,
                                  @RequestHeader String password) {
        User user = authService.authenticate(login, password);
        aclService.checkAccess(user, "students", "TRANSFER");
        deanService.transferStudent(studentId, groupId, user.getId());
        return "Студент переведён";
    }
}