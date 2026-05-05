package com.university.deanery.service;

import com.university.deanery.model.SecurityQuestion;
import com.university.deanery.model.User;
import com.university.deanery.repository.SecurityQuestionRepository;
import com.university.deanery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    // 1. Первый этап: проверка логина и пароля
    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Неверный пароль");
        }
        return user;
    }

    // 2. Получить секретный вопрос для пользователя
    public String getSecurityQuestion(Integer userId) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);
        if (questions.isEmpty()) {
            throw new RuntimeException("Секретные вопросы не настроены для пользователя");
        }
        // Берём первый вопрос (или можно случайный)
        return questions.get(0).getQuestion();
    }

    // 3. Второй этап: проверка ответа на секретный вопрос
    @Transactional
    public User verifySecurityAnswer(Integer userId, String answer) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);
        if (questions.isEmpty()) {
            throw new RuntimeException("Секретные вопросы не настроены");
        }

        SecurityQuestion question = questions.get(0);

        // В реальном проекте здесь нужно хэширование, сейчас простое сравнение
        if (!question.getAnswerHash().equals(answer)) {
            throw new RuntimeException("Неверный ответ на секретный вопрос");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}