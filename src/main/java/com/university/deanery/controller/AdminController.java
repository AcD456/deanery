package com.university.deanery.controller;

import com.university.deanery.dto.ChangePasswordRequest;
import com.university.deanery.dto.SecurityQuestionRequest;
import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.User;
import com.university.deanery.repository.UserRepository;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AclService aclService;

    @Autowired
    private JournalService journalService;

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @PostMapping("/change-role")
    public User changeRole(@RequestHeader("Authorization") String token,
                           @RequestParam Integer userId,
                           @RequestParam String role) {

        User admin = getUser(token);

        if (!admin.getRole().equals("ADMIN")) {
            throw new RuntimeException("Нет прав");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);

        journalService.logSimple(admin.getId(), "CHANGE_ROLE", "User", userId, oldRole, role);

        return user;
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestHeader("Authorization") String token) {
        User admin = getUser(token);
        if (!admin.getRole().equals("ADMIN")) throw new RuntimeException("Нет прав");
        return userRepository.findAll();
    }

    @PutMapping("/update-profile")
    public Map<String, String> updateProfile(@RequestHeader("Authorization") String token,
                                             @RequestBody UpdateProfileRequest request) {
        User user = getUser(token);
        aclService.checkAccess(user, "profile", "UPDATE");

        authService.updateUserProfile(user.getId(), request);

        journalService.logSimple(user.getId(), "UPDATE_PROFILE", user.getRole(), user.getId());

        return Map.of("message", "Профиль обновлён");
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@RequestHeader("Authorization") String token,
                                              @RequestBody ChangePasswordRequest request) {
        User user = getUser(token);

        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        journalService.logSimple(user.getId(), "CHANGE_PASSWORD", user.getRole(), user.getId());

        return Map.of("message", "Пароль изменён");
    }

    @PutMapping("/security-question")
    public Map<String, String> updateSecurityQuestion(@RequestHeader("Authorization") String token,
                                                      @RequestBody SecurityQuestionRequest request) {
        User user = getUser(token);

        authService.updateSecurityQuestion(user.getId(), request.getQuestion(), request.getAnswer());

        journalService.logSimple(user.getId(), "UPDATE_SECURITY_QUESTION", user.getRole(), user.getId());

        return Map.of("message", "Секретный вопрос обновлён");
    }
}