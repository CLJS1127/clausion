package com.classpulse.domain.gamification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface XPEventRepository extends JpaRepository<XPEvent, Long> {
    List<XPEvent> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<XPEvent> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);
    List<XPEvent> findByStudentIdAndCourseIdAndCreatedAtAfter(Long studentId, Long courseId, LocalDateTime after);
}
