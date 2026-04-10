package com.classpulse.domain.consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    @Query("SELECT c FROM Consultation c JOIN FETCH c.student JOIN FETCH c.instructor JOIN FETCH c.course WHERE c.student.id = :studentId ORDER BY c.scheduledAt DESC")
    List<Consultation> findByStudentIdOrderByScheduledAtDesc(Long studentId);

    @Query("SELECT c FROM Consultation c JOIN FETCH c.student JOIN FETCH c.instructor JOIN FETCH c.course WHERE c.instructor.id = :instructorId ORDER BY c.scheduledAt DESC")
    List<Consultation> findByInstructorIdOrderByScheduledAtDesc(Long instructorId);

    List<Consultation> findByInstructorIdAndScheduledAtBetween(Long instructorId, LocalDateTime start, LocalDateTime end);
    List<Consultation> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Consultation> findByCourseId(Long courseId);
}
