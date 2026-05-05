package com.university.deanery.repository;

import com.university.deanery.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByApplicantId(Integer applicantId);
    List<Application> findByStatus(String status);
}