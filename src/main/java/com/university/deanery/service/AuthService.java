package com.university.deanery.service;

import com.university.deanery.dto.UpdateProfileRequest;
import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import com.university.deanery.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private StudentContactRepository studentContactRepository;

    // 1. Аутентификация по логину и паролю
    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Неверный логин или пароль");
        }

        return user;
    }

    // 2. Получить секретный вопрос (если есть)
    public String getSecurityQuestion(Integer userId) {
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserId(userId);
        if (questions.isEmpty()) {
            return null;
        }
        return questions.get(0).getQuestion();
    }

    // 3. Проверить ответ на секретный вопрос
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

    // 4. Получить пользователя из JWT-токена
    public User getUserFromToken(String token, JwtService jwtService) {
        String cleanToken = token.replace("Bearer ", "");
        Integer userId = jwtService.extractUserId(cleanToken);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // 5. Обновление профиля пользователя
    @Transactional
    public void updateUserProfile(Integer userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String role = user.getRole();

        if ("STUDENT".equals(role)) {
            Student student = studentRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Студент не найден"));

            // Обновляем ФИО
            if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                student.setFullName(request.getFullName());
                studentRepository.save(student);
            }

            // Обновляем контакты
            try {
                Optional<StudentContact> existingContact = studentContactRepository.findById(student.getId());
                StudentContact contact;

                if (existingContact.isPresent()) {
                    contact = existingContact.get();
                    if (request.getEmail() != null) {
                        contact.setEmail(request.getEmail().isEmpty() ? null : request.getEmail());
                    }
                    if (request.getPhone() != null) {
                        contact.setPhone(request.getPhone().isEmpty() ? null : request.getPhone());
                    }
                    studentContactRepository.save(contact);
                } else {
                    contact = new StudentContact(student.getId());
                    if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                        contact.setEmail(request.getEmail());
                    }
                    if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                        contact.setPhone(request.getPhone());
                    }
                    studentContactRepository.save(contact);
                }
            } catch (Exception e) {
                System.err.println("Error saving contacts: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else if ("TEACHER".equals(role)) {
            Teacher teacher = teacherRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
            if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                teacher.setFullName(request.getFullName());
            }
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                teacher.setPhone(request.getPhone());
            }
            if (request.getPosition() != null && !request.getPosition().isEmpty()) {
                teacher.setPosition(request.getPosition());
            }
            if (request.getAcademicDegree() != null && !request.getAcademicDegree().isEmpty()) {
                teacher.setAcademicDegree(request.getAcademicDegree());
            }
            teacherRepository.save(teacher);
        }
        else if ("APPLICANT".equals(role)) {
            Applicant applicant = applicantRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));
            if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                applicant.setFullName(request.getFullName());
                applicantRepository.save(applicant);
            }
        }
        // DEAN и ADMIN могут менять только пароль и секретный вопрос
    }

    // 6. Смена пароля
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Неверный старый пароль");
        }

        if (newPassword == null || newPassword.length() < 4) {
            throw new RuntimeException("Новый пароль должен содержать минимум 4 символа");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    // 7. Обновление секретного вопроса
    @Transactional
    public void updateSecurityQuestion(Integer userId, String question, String answer) {
        if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
            throw new RuntimeException("Вопрос и ответ не могут быть пустыми");
        }

        List<SecurityQuestion> existing = securityQuestionRepository.findByUserId(userId);

        if (existing.isEmpty()) {
            SecurityQuestion sq = new SecurityQuestion(userId, question, answer);
            securityQuestionRepository.save(sq);
        } else {
            SecurityQuestion sq = existing.get(0);
            sq.setQuestion(question);
            sq.setAnswerHash(answer);
            securityQuestionRepository.save(sq);
        }
    }

    public Applicant getApplicantProfile(Integer userId) {
        return applicantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Абитуриент не найден"));
    }
}