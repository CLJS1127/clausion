package com.classpulse.ai;

import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import com.classpulse.domain.course.CourseEnrollment;
import com.classpulse.domain.course.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 일일 배치 추론 스케줄러.
 * 24시간 내 갱신되지 않은 학생-과목 쌍에 대해 추론을 실행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinInferenceScheduler {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final StudentTwinRepository studentTwinRepository;
    private final AiJobService aiJobService;

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    public void dailyBatchInference() {
        log.info("일일 배치 추론 시작");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<CourseEnrollment> enrollments = enrollmentRepository.findAll();

        int scheduled = 0;
        for (CourseEnrollment enrollment : enrollments) {
            Long studentId = enrollment.getStudent().getId();
            Long courseId = enrollment.getCourse().getId();

            Optional<StudentTwin> twinOpt = studentTwinRepository
                    .findByStudentIdAndCourseId(studentId, courseId);

            // Skip if twin was recently updated
            if (twinOpt.isPresent() && twinOpt.get().getUpdatedAt() != null
                    && twinOpt.get().getUpdatedAt().isAfter(cutoff)) {
                continue;
            }

            aiJobService.runTwinInference(studentId, courseId);
            scheduled++;

            // Rate limit: 250ms between API calls
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("일일 배치 추론 완료 - {}명 스케줄됨", scheduled);
    }
}
