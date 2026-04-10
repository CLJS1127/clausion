package com.classpulse.domain.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Recommendation> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);
    List<Recommendation> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);
}
