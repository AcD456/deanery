package com.university.deanery.repository;

import com.university.deanery.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {
    Optional<Applicant> findByUser_Id(Integer userId);  // ← ДОБАВИТЬ ЭТОТ МЕТОД
}