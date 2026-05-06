package com.university.deanery.repository;

import com.university.deanery.model.AcademicLeave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicLeaveRepository extends JpaRepository<AcademicLeave, Integer> {
}