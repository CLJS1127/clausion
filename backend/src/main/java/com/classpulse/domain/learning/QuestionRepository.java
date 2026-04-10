package com.classpulse.domain.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourseId(Long courseId);
    List<Question> findByCourseIdAndApprovalStatus(Long courseId, String approvalStatus);
    List<Question> findBySkillId(Long skillId);
}
