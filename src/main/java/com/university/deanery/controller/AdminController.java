package com.university.deanery.controller;
import java.util.List;

import com.university.deanery.model.User;
import com.university.deanery.repository.UserRepository;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/change-role")
    public User changeRole(@RequestHeader("Authorization") String token,
                           @RequestParam Integer userId,
                           @RequestParam String role) {

        User admin = authService.getUserFromToken(token, jwtService);

        if (!admin.getRole().equals("ADMIN")) {
            throw new RuntimeException("Нет прав");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);

        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestHeader("Authorization") String token) {
        User admin = authService.getUserFromToken(token, jwtService);
        if (!admin.getRole().equals("ADMIN")) throw new RuntimeException("Нет прав");
        return userRepository.findAll();
    }
}