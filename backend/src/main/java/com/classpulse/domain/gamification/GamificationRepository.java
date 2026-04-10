package com.classpulse.domain.gamification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GamificationRepository extends JpaRepository<StudentGamification, Long> {
    Optional<StudentGamification> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<StudentGamification> findByCourseIdOrderByTotalXpEarnedDesc(Long courseId);
}
