package com.university.deanery.controller;

import com.university.deanery.model.Student;
import com.university.deanery.model.User;
import com.university.deanery.security.AclService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AclService aclService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/my-profile")
    public Student getMyProfile(@RequestHeader String login, @RequestHeader String password) {
        User user = authService.authenticate(login, password);
        aclService.checkAccess(user, "profile", "READ");
        return studentService.getStudentByUserId(user.getId());
    }

    @GetMapping("/group/{groupId}")
    public List<Student> getGroupStudents(@PathVariable Integer groupId,
                                          @RequestHeader String login,
                                          @RequestHeader String password) {
        User user = authService.authenticate(login, password);
        aclService.checkAccess(user, "students", "READ");
        return studentService.getStudentsByGroup(groupId);
    }
}