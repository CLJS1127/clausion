package com.classpulse.domain.twin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TwinScoreHistoryRepository extends JpaRepository<TwinScoreHistory, Long> {

    List<TwinScoreHistory> findTop10ByStudentIdAndCourseIdOrderByCapturedAtDesc(Long studentId, Long courseId);
}
