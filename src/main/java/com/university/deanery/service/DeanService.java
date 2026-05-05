package com.university.deanery.service;

import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeanService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicantRepository applicantRepository;


    @Transactional
    public void approveApplication(Integer applicationId, Integer deanId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Заявление не найдено"));

        if (!application.getStatus().equals("PENDING")) {
            throw new RuntimeException("Заявление уже обработано");
        }

        application.setStatus("APPROVED");

        Integer applicantId = application.getApplicantId();

        // 👉 ВАЖНО: applicantId = ID из applicants
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));

        User user = applicant.getUser();

        if (user.getRole().equals("STUDENT")) {
            throw new RuntimeException("Уже студент");
        }

        if (studentRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new RuntimeException("Студент уже существует");
        }

        Student student = new Student();
        student.setUser(user);
        student.setFullName(applicant.getFullName());
        student.setGroupId(1);
        student.setStatus("ACTIVE");

        studentRepository.save(student);

        user.setRole("STUDENT");
        userRepository.save(user);
    }


    @Transactional
    public void expelStudent(Integer studentId, Integer deanId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        student.setStatus("EXPELLED");
        studentRepository.save(student);
    }


    @Transactional
    public void transferStudent(Integer studentId, Integer newGroupId, Integer deanId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        student.setGroupId(newGroupId);
        studentRepository.save(student);
    }
}