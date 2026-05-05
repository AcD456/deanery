package com.university.deanery.repository;

import com.university.deanery.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByGroupId(Integer groupId);

    Optional<Student> findByUser_Id(Integer userId);
}