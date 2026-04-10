package com.classpulse.domain.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    List<Reflection> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);
    List<Reflection> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Reflection> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);
}
