package com.classpulse.domain.consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {
    List<ActionPlan> findByStudentIdAndStatus(Long studentId, String status);
    List<ActionPlan> findByConsultationId(Long consultationId);
    List<ActionPlan> findByStudentIdAndDueDateBetween(Long studentId, LocalDate start, LocalDate end);
    long countByStudentIdAndStatus(Long studentId, String status);
}
