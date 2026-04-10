package com.classpulse.domain.codeanalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<CodeSubmission> findByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);
}
