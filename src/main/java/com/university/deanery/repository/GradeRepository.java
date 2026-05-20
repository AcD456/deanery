package com.university.deanery.repository;

import com.university.deanery.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Integer> {

    List<Grade> findByStudentId(Integer studentId);

    List<Grade> findByCourseId(Integer courseId);

    List<Grade> findByTeacherId(Integer teacherId);

    Optional<Grade> findByStudentIdAndCourseIdAndTeacherIdAndSemester(
            Integer studentId, Integer courseId, Integer teacherId, Integer semester);

    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.semester = :semester ORDER BY g.courseId")
    List<Grade> findByStudentIdAndSemester(@Param("studentId") Integer studentId, @Param("semester") Integer semester);
}