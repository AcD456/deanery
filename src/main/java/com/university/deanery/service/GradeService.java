package com.university.deanery.service;

import com.university.deanery.dto.GradeRequest;
import com.university.deanery.dto.GradeResponse;
import com.university.deanery.model.*;
import com.university.deanery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private JournalService journalService;

    @Transactional
    public GradeResponse setGrade(Integer teacherId, GradeRequest request) {
        // Проверяем, что преподаватель ведёт этот курс
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        // Проверяем студента
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        // Проверяем курс
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Проверяем, не выставлена ли уже оценка за этот семестр
        Grade existingGrade = gradeRepository
                .findByStudentIdAndCourseIdAndTeacherIdAndSemester(
                        request.getStudentId(), request.getCourseId(), teacherId, request.getSemester())
                .orElse(null);

        Grade grade;
        boolean isUpdate = false;

        if (existingGrade != null) {
            grade = existingGrade;
            isUpdate = true;
        } else {
            grade = new Grade();
            grade.setStudentId(request.getStudentId());
            grade.setCourseId(request.getCourseId());
            grade.setTeacherId(teacherId);
        }

        String oldValue = isUpdate ? String.valueOf(grade.getGradeValue()) : null;

        grade.setGradeValue(request.getGradeValue());
        grade.setGradeType(request.getGradeType());
        grade.setSemester(request.getSemester());
        grade.setComment(request.getComment());
        grade.setAcademicYear(getCurrentAcademicYear());

        Grade savedGrade = gradeRepository.save(grade);

        // Логируем действие
        journalService.logSimple(
                teacher.getUserId(),
                isUpdate ? "UPDATE_GRADE" : "SET_GRADE",
                "Grade",
                savedGrade.getId(),
                oldValue,
                String.valueOf(request.getGradeValue())
        );

        return convertToResponse(savedGrade);
    }

    @Transactional
    public void deleteGrade(Integer teacherId, Integer gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена"));

        if (!grade.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Нет прав на удаление этой оценки");
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        gradeRepository.delete(grade);

        journalService.logSimple(
                teacher.getUserId(),
                "DELETE_GRADE",
                "Grade",
                gradeId,
                String.valueOf(grade.getGradeValue()),
                null
        );
    }

    public List<GradeResponse> getGradesForTeacherCourse(Integer teacherId, Integer courseId, Integer semester) {
        List<Grade> grades = gradeRepository.findByTeacherId(teacherId)
                .stream()
                .filter(g -> courseId == null || g.getCourseId().equals(courseId))
                .filter(g -> semester == null || g.getSemester().equals(semester))
                .collect(Collectors.toList());

        return grades.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<GradeResponse> getStudentGrades(Integer studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private GradeResponse convertToResponse(Grade grade) {
        Student student = studentRepository.findById(grade.getStudentId()).orElse(null);
        Course course = courseRepository.findById(grade.getCourseId()).orElse(null);

        return new GradeResponse(
                grade.getId(),
                grade.getStudentId(),
                student != null ? student.getFullName() : "Неизвестно",
                grade.getCourseId(),
                course != null ? course.getName() : "Неизвестно",
                grade.getGradeValue(),
                grade.getGradeType(),
                grade.getSemester(),
                grade.getComment(),
                grade.getGradedAt()
        );
    }

    private String getCurrentAcademicYear() {
        int currentYear = java.time.Year.now().getValue();
        return currentYear + "/" + (currentYear + 1);
    }

    public List<GradeResponse> getGradesForTeacher(Integer teacherId) {
        List<Grade> grades = gradeRepository.findByTeacherId(teacherId);
        return grades.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}