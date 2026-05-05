package com.university.deanery.service;

import com.university.deanery.model.SecurityQuestion;
import com.university.deanery.model.User;
import com.university.deanery.repository.SecurityQuestionRepository;
import com.university.deanery.repository.UserRepository;
import com.university.deanery.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    // 1. Проверка логина и пароля
    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Неверный логин или пароль");
        }

        return user;
    }

    // 2. Получить вопрос (если есть)
    public String getSecurityQuestion(Integer userId) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);
        if (questions.isEmpty()) {
            return null;
        }
        return questions.get(0).getQuestion();
    }

    // 3. Проверка ответа
    public User verifySecurityAnswer(Integer userId, String answer) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);

        if (questions.isEmpty()) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        }

        SecurityQuestion question = questions.get(0);
        if (!question.getAnswerHash().equals(answer)) {
            throw new RuntimeException("Неверный ответ на секретный вопрос");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // 4. Получение пользователя из JWT
    public User getUserFromToken(String token, JwtService jwtService) {
        String cleanToken = token.replace("Bearer ", "");
        Integer userId = jwtService.extractUserId(cleanToken);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}