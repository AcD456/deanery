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

    /**
     * ЭТАП 1: логин + пароль
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String login,
                                     @RequestParam String password) {
        User user = authService.authenticate(login, password);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());

        // Проверяем, есть ли секретный вопрос
        String question = authService.getSecurityQuestion(user.getId());
        boolean hasQuestion = (question != null && !question.isEmpty());
        response.put("requiresSecurityQuestion", hasQuestion);

        return response;
    }

    /**
     * ЭТАП 2: получить секретный вопрос (если есть)
     */
    @GetMapping("/security-question")
    public Map<String, String> getSecurityQuestion(@RequestParam Integer userId) {
        String question = authService.getSecurityQuestion(userId);

        Map<String, String> response = new HashMap<>();
        response.put("userId", String.valueOf(userId));

        if (question == null || question.isEmpty()) {
            response.put("skipped", "true");
            response.put("question", "");
        } else {
            response.put("skipped", "false");
            response.put("question", question);
        }

        return response;
    }

    /**
     * ЭТАП 3: ответ на вопрос → выдача JWT
     */
    @PostMapping("/verify-security")
    public Map<String, String> verifySecurity(@RequestParam Integer userId,
                                              @RequestParam String answer) {
        User user = authService.verifySecurityAnswer(userId, answer);
        String token = jwtService.generateToken(user.getId(), user.getRole());
        return Map.of("token", token);
    }
}