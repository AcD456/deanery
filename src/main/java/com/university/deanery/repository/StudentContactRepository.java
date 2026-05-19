package com.university.deanery.repository;

import com.university.deanery.model.StudentContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentContactRepository extends JpaRepository<StudentContact, Integer> {
    Optional<StudentContact> findByStudentId(Integer studentId);
}