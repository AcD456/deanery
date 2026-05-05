package com.university.deanery.service;

import com.university.deanery.model.SecurityQuestion;
import com.university.deanery.model.User;
import com.university.deanery.repository.SecurityQuestionRepository;
import com.university.deanery.repository.UserRepository;
import com.university.deanery.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

        // Простое сравнение без BCrypt
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Неверный пароль");
        }

        return user;
    }

    // получить вопрос
    public String getSecurityQuestion(Integer userId) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);

        if (questions.isEmpty()) {
            throw new RuntimeException("Нет секретного вопроса");
        }

        return questions.get(0).getQuestion();
    }

    // проверка ответа
    public User verifySecurityAnswer(Integer userId, String answer) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);

        if (questions.isEmpty()) {
            throw new RuntimeException("Нет секретного вопроса");
        }

        SecurityQuestion question = questions.get(0);

        // Временное прямое сравнение (без BCrypt)
        if (!question.getAnswerHash().equals(answer)) {
            throw new RuntimeException("Неверный ответ");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // получение пользователя из JWT
    public User getUserFromToken(String token, JwtService jwtService) {
        String cleanToken = token.replace("Bearer ", "");
        Integer userId = jwtService.extractUserId(cleanToken);

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}