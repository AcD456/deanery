package com.university.deanery.controller;

import com.university.deanery.model.Student;
import com.university.deanery.model.User;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
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
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private StudentService studentService;

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
}