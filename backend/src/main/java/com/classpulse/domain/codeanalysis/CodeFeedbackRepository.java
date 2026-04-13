package com.classpulse.domain.codeanalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeFeedbackRepository extends JpaRepository<CodeFeedback, Long> {
    List<CodeFeedback> findBySubmissionId(Long submissionId);
    List<CodeFeedback> findBySubmissionIdIn(List<Long> submissionIds);
    List<CodeFeedback> findBySubmissionStudentIdAndSubmissionCourseId(Long studentId, Long courseId);
}
