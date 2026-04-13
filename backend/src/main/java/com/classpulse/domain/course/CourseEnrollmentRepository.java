package com.classpulse.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByStudentId(Long studentId);
    List<CourseEnrollment> findByCourseId(Long courseId);
    List<CourseEnrollment> findByCourseIdAndStatus(Long courseId, String status);
    List<CourseEnrollment> findByStudentIdAndStatus(Long studentId, String status);
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    long countByCourseIdAndStatus(Long courseId, String status);
}
