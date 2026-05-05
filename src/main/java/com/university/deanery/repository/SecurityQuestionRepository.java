package com.university.deanery.repository;

import com.university.deanery.model.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Integer> {
    List<SecurityQuestion> findByUserId(Integer userId);
    Optional<SecurityQuestion> findByUserIdAndQuestion(Integer userId, String question);
}