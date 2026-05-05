package com.university.deanery.controller;

import com.university.deanery.model.User;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    // ЭТАП 1: логин + пароль
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String login,
                                     @RequestParam String password) {

        User user = authService.authenticate(login, password);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("requiresSecurityQuestion", true);

        return response;
    }

    // ЭТАП 2: получить вопрос
    @GetMapping("/security-question")
    public Map<String, String> getSecurityQuestion(@RequestParam Integer userId) {
        String question = authService.getSecurityQuestion(userId);

        Map<String, String> response = new HashMap<>();
        response.put("question", question);
        response.put("userId", String.valueOf(userId));

        return response;
    }

    // ЭТАП 3: ответ → ВЫДАЁМ JWT
    @PostMapping("/verify-security")
    public Map<String, String> verifySecurity(@RequestParam Integer userId,
                                              @RequestParam String answer) {

        User user = authService.verifySecurityAnswer(userId, answer);

        String token = jwtService.generateToken(user.getId(), user.getRole());

        return Map.of("token", token);
    }
}