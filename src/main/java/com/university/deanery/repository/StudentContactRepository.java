package com.university.deanery.repository;

import com.university.deanery.model.StudentContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentContactRepository extends JpaRepository<StudentContact, Integer> {
}