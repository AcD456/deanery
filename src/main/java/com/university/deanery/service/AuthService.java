package com.university.deanery.service;

import com.university.deanery.model.User;
import com.university.deanery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Неверный пароль");
        }
        return user;
    }
}