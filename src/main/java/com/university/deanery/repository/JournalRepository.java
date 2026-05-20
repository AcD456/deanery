package com.university.deanery.repository;

import com.university.deanery.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JournalRepository extends JpaRepository<Journal, Integer> {
    List<Journal> findAllByOrderByCreatedAtDesc();
}