package com.university.deanery.controller;

import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import com.university.deanery.security.JwtService;
import com.university.deanery.service.AuthService;
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
}