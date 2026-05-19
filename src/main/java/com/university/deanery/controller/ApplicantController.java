package com.university.deanery.controller;

import com.university.deanery.dto.ChangePasswordRequest;
import com.university.deanery.dto.SecurityQuestionRequest;
import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import com.university.deanery.security.AclService;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
import com.university.deanery.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applicant")
public class ApplicantController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;

    @Autowired
    private JournalService journalService;

    @Autowired
    private AclService aclService;

    public ApplicantController(AuthService authService, JwtService jwtService,
                               ApplicationRepository applicationRepository,
                               ApplicantRepository applicantRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
    }

    private User getUser(String token) {
        return authService.getUserFromToken(token, jwtService);
    }

    @PostMapping("/submit-application")
    public Map<String, String> submitApplication(@RequestHeader("Authorization") String token,
                                                 @RequestBody Map<String, Integer> body) {
        User user = getUser(token);
        Applicant applicant = applicantRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));

        Application app = new Application();
        app.setApplicantId(applicant.getId());
        app.setProgramId(body.get("programId"));
        app.setStatus("PENDING");
        applicationRepository.save(app);

        return Map.of("message", "Заявление подано");
    }

    @GetMapping("/my-applications")
    public List<Application> getMyApplications(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        Applicant applicant = applicantRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));
        return applicationRepository.findByApplicantId(applicant.getId());
    }

    @GetMapping("/my-profile")
    public Applicant getMyProfile(@RequestHeader("Authorization") String token) {
        User user = getUser(token);
        return applicantRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));
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