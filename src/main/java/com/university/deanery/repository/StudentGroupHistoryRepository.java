package com.university.deanery.repository;

import com.university.deanery.model.StudentGroupHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupHistoryRepository extends JpaRepository<StudentGroupHistory, Integer> {
}