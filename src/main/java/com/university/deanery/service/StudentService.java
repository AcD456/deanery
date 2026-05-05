package com.university.deanery.service;

import com.university.deanery.model.Student;
import com.university.deanery.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentByUserId(Integer userId) {
        return studentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));
    }

    public List<Student> getStudentsByGroup(Integer groupId) {
        return studentRepository.findByGroupId(groupId);
    }

    public Student updateStudentGroup(Integer studentId, Integer newGroupId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));
        student.setGroupId(newGroupId);
        return studentRepository.save(student);
    }

    public Student updateStudentStatus(Integer studentId, String status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));
        student.setStatus(status);
        return studentRepository.save(student);
    }
}