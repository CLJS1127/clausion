package com.classpulse.domain.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewTaskRepository extends JpaRepository<ReviewTask, Long> {
    List<ReviewTask> findByStudentIdAndScheduledFor(Long studentId, LocalDate date);
    List<ReviewTask> findByStudentIdAndScheduledForLessThanEqualAndStatusIn(Long studentId, LocalDate date, List<String> statuses);
    List<ReviewTask> findByStudentIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(Long studentId, LocalDateTime start, LocalDateTime end);
    List<ReviewTask> findByStudentIdAndStatus(Long studentId, String status);
    List<ReviewTask> findByStudentIdAndScheduledForBetweenOrderByScheduledFor(Long studentId, LocalDate start, LocalDate end);
    long countByStudentIdAndStatusAndScheduledForBetween(Long studentId, String status, LocalDate start, LocalDate end);
    List<ReviewTask> findByStudentIdAndCourseIdOrderByScheduledForDesc(Long studentId, Long courseId);
}
