package com.classpulse.domain.consultation;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public Consultation createConsultation(Long studentId, Long instructorId, Long courseId, LocalDateTime scheduledAt) {
        User student = userRepository.findById(studentId).orElseThrow();
        User instructor = userRepository.findById(instructorId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();

        Consultation consultation = Consultation.builder()
                .student(student)
                .instructor(instructor)
                .course(course)
                .scheduledAt(scheduledAt)
                .build();
        return consultationRepository.save(consultation);
    }

    public List<Consultation> getByStudentId(Long studentId) {
        return consultationRepository.findByStudentIdOrderByScheduledAtDesc(studentId);
    }

    public List<Consultation> getByInstructorId(Long instructorId) {
        return consultationRepository.findByInstructorIdOrderByScheduledAtDesc(instructorId);
    }

    public List<Consultation> getTodayConsultations(Long instructorId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return consultationRepository.findByInstructorIdAndScheduledAtBetween(instructorId, startOfDay, endOfDay);
    }

    public Consultation getById(Long id) {
        return consultationRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Consultation updateSummary(Long id, String summaryText, String causeAnalysis, List<java.util.Map<String, Object>> actionPlan) {
        Consultation c = getById(id);
        if (summaryText != null) {
            c.setSummaryText(summaryText);
        }
        if (causeAnalysis != null) {
            c.setCauseAnalysis(causeAnalysis);
        }
        if (actionPlan != null) {
            c.setActionPlanJson(actionPlan);
        }
        c.setStatus("COMPLETED");
        c.setCompletedAt(LocalDateTime.now());
        return consultationRepository.save(c);
    }
}
