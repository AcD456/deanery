package com.university.deanery.controller;

import com.university.deanery.model.User;
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

    // Этап 1: Логин (проверка логина и пароля)
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String login, @RequestParam String password) {
        User user = authService.authenticate(login, password);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("login", user.getLogin());
        response.put("role", user.getRole());
        response.put("message", "Пароль верен, требуется ответ на секретный вопрос");
        response.put("requiresSecurityQuestion", true);

        return response;
    }

    // Этап 2: Получить секретный вопрос (после успешного логина)
    @GetMapping("/security-question")
    public Map<String, String> getSecurityQuestion(@RequestParam Integer userId) {
        String question = authService.getSecurityQuestion(userId);
        Map<String, String> response = new HashMap<>();
        response.put("question", question);
        response.put("userId", String.valueOf(userId));
        return response;
    }

    // Этап 3: Ответ на секретный вопрос (завершение аутентификации)
    @PostMapping("/verify-security")
    public User verifySecurity(@RequestParam Integer userId, @RequestParam String answer) {
        return authService.verifySecurityAnswer(userId, answer);
    }
}