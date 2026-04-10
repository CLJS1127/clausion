package com.classpulse.domain.twin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentTwinRepository extends JpaRepository<StudentTwin, Long> {
    Optional<StudentTwin> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<StudentTwin> findByStudentId(Long studentId);
    List<StudentTwin> findByCourseId(Long courseId);
    List<StudentTwin> findByCourseIdAndOverallRiskScoreGreaterThan(Long courseId, java.math.BigDecimal threshold);
}
