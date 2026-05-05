package com.university.deanery.controller;

import com.university.deanery.model.User;
import com.university.deanery.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public User login(@RequestParam String login, @RequestParam String password) {
        return authService.authenticate(login, password);
    }
}