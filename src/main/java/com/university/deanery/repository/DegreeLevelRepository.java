package com.university.deanery.repository;

import com.university.deanery.model.DegreeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreeLevelRepository extends JpaRepository<DegreeLevel, Integer> {
}