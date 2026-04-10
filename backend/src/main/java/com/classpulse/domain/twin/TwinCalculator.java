package com.classpulse.domain.twin;

import com.classpulse.domain.codeanalysis.CodeFeedback;
import com.classpulse.domain.gamification.GamificationService;
import com.classpulse.domain.learning.ReviewTask;
import com.classpulse.domain.learning.ReviewTaskRepository;
import com.classpulse.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bridges code analysis and gamification with the Twin model.
 * Updates skill mastery based on code feedback and triggers gamification events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinCalculator {

    private final StudentTwinRepository twinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final GamificationService gamificationService;
    private final NotificationService notificationService;
    private final ReviewTaskRepository reviewTaskRepository;

    private static final BigDecimal PRACTICE_INCREMENT = new BigDecimal("5.00");
    private static final BigDecimal UNDERSTANDING_DECREMENT = new BigDecimal("3.00");
    private static final BigDecimal MAX_SCORE = new BigDecimal("100.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int CODE_PRACTICE_XP = 15;

    /**
     * Called after code analysis completes.
     * Updates related skill mastery based on code feedback.
     */
    @Transactional
    public void updateFromCodeAnalysis(Long studentId, Long courseId, List<CodeFeedback> feedbacks) {
        for (CodeFeedback feedback : feedbacks) {
            if (!Boolean.TRUE.equals(feedback.getTwinLinked()) || feedback.getTwinSkill() == null) {
                continue;
            }

            Long skillId = feedback.getTwinSkill().getId();

            // Get the latest snapshot for this student+skill
            List<SkillMasterySnapshot> snapshots = snapshotRepository
                    .findByStudentIdAndSkillIdOrderByCapturedAtDesc(studentId, skillId);

            SkillMasterySnapshot snapshot;
            if (snapshots.isEmpty()) {
                // Create a new snapshot with default scores
                snapshot = SkillMasterySnapshot.builder()
                        .student(feedback.getSubmission().getStudent())
                        .course(feedback.getSubmission().getCourse())
                        .skill(feedback.getTwinSkill())
                        .understandingScore(new BigDecimal("50.00"))
                        .practiceScore(new BigDecimal("50.00"))
                        .confidenceScore(new BigDecimal("50.00"))
                        .forgettingRiskScore(new BigDecimal("30.00"))
                        .sourceType("CODE_ANALYSIS")
                        .build();
            } else {
                // Create a new snapshot based on the latest one
                SkillMasterySnapshot latest = snapshots.get(0);
                snapshot = SkillMasterySnapshot.builder()
                        .student(latest.getStudent())
                        .course(latest.getCourse())
                        .skill(latest.getSkill())
                        .understandingScore(latest.getUnderstandingScore())
                        .practiceScore(latest.getPracticeScore())
                        .confidenceScore(latest.getConfidenceScore())
                        .forgettingRiskScore(latest.getForgettingRiskScore())
                        .sourceType("CODE_ANALYSIS")
                        .build();
            }

            String severity = feedback.getSeverity();
            if ("GOOD".equals(severity)) {
                // GOOD feedback -> increase practice_score by 5
                BigDecimal newPractice = snapshot.getPracticeScore().add(PRACTICE_INCREMENT);
                snapshot.setPracticeScore(newPractice.min(MAX_SCORE));
            } else if ("WARNING".equals(severity) || "ERROR".equals(severity)) {
                // WARNING/ERROR -> decrease understanding_score by 3
                BigDecimal newUnderstanding = snapshot.getUnderstandingScore().subtract(UNDERSTANDING_DECREMENT);
                snapshot.setUnderstandingScore(newUnderstanding.max(ZERO));
            }

            snapshotRepository.save(snapshot);
            log.info("Updated skill mastery from code analysis - studentId={}, skillId={}, severity={}",
                    studentId, skillId, severity);
        }

        // Twin score recalculation is now handled by TwinInferenceEngine via debounced trigger.
        // Skill snapshots updated above will be picked up on next inference run.
        log.info("Skill snapshots updated from code analysis - studentId={}, courseId={}, awaiting inference", studentId, courseId);

        // Award gamification XP for code practice
        gamificationService.addXpEvent(studentId, courseId, "CODE_SUBMIT", CODE_PRACTICE_XP, null, "CODE_ANALYSIS");
    }

    /**
     * Called after Twin is updated.
     * Triggers gamification events based on new state.
     */
    public void onTwinUpdated(StudentTwin twin) {
        Long studentId = twin.getStudent().getId();
        Long courseId = twin.getCourse().getId();

        // Check if mastery > 80% -> notify about achievement
        if (twin.getMasteryScore().compareTo(new BigDecimal("80.00")) >= 0) {
            notificationService.createNotification(
                    studentId,
                    "ACHIEVEMENT",
                    "학습 마스터리 80% 달성!",
                    String.format("축하합니다! 전체 이해도가 %.1f%%에 도달했습니다.", twin.getMasteryScore().doubleValue()),
                    Map.of("masteryScore", twin.getMasteryScore(), "courseId", courseId)
            );
            log.info("Mastery achievement notification sent - studentId={}, mastery={}",
                    studentId, twin.getMasteryScore());
        }

        // Award XP for learning activity
        gamificationService.addXpEvent(studentId, courseId, "REVIEW_COMPLETE", null, twin.getId(), "TWIN_UPDATE");

        // Check badge eligibility
        gamificationService.checkAndAwardBadges(studentId);
    }

    /**
     * Called after code practice with AI feedback applied.
     * Connects to review scheduler.
     */
    @Transactional
    public void onCodePracticeComplete(Long studentId, Long courseId, Long skillId) {
        // Find existing pending/in-progress review tasks for this skill
        List<ReviewTask> existingTasks = reviewTaskRepository.findByStudentIdAndStatus(studentId, "PENDING");
        Optional<ReviewTask> matchingTask = existingTasks.stream()
                .filter(t -> t.getSkill() != null && t.getSkill().getId().equals(skillId))
                .findFirst();

        // Check latest snapshot to determine feedback quality
        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndSkillIdOrderByCapturedAtDesc(studentId, skillId);

        boolean hadErrors = false;
        if (!snapshots.isEmpty()) {
            SkillMasterySnapshot latest = snapshots.get(0);
            // If understanding score is below 60, consider it as having errors
            hadErrors = latest.getUnderstandingScore().compareTo(new BigDecimal("60.00")) < 0;
        }

        if (matchingTask.isPresent()) {
            ReviewTask task = matchingTask.get();
            if (hadErrors) {
                // Schedule sooner review (tomorrow)
                task.setScheduledFor(LocalDate.now().plusDays(1));
                task.setReasonSummary("코드 피드백에서 오류가 발견되어 빠른 복습이 필요합니다.");
                log.info("Review task rescheduled sooner - studentId={}, skillId={}, newDate={}",
                        studentId, skillId, task.getScheduledFor());
            } else {
                // Push review date further (5 days out)
                task.setScheduledFor(LocalDate.now().plusDays(5));
                task.setReasonSummary("코드 실습이 양호하여 복습 일정을 연장했습니다.");
                log.info("Review task pushed further - studentId={}, skillId={}, newDate={}",
                        studentId, skillId, task.getScheduledFor());
            }
            reviewTaskRepository.save(task);
        } else {
            // No existing task for this skill - create one
            // Need to get User and Course references from twin
            Optional<StudentTwin> twinOpt = twinRepository.findByStudentIdAndCourseId(studentId, courseId);
            if (twinOpt.isPresent()) {
                StudentTwin twin = twinOpt.get();
                List<SkillMasterySnapshot> skillSnapshots = snapshotRepository
                        .findByStudentIdAndSkillIdOrderByCapturedAtDesc(studentId, skillId);

                LocalDate scheduledFor = hadErrors ? LocalDate.now().plusDays(1) : LocalDate.now().plusDays(3);
                String reason = hadErrors
                        ? "코드 분석에서 이해 부족이 감지되어 복습이 필요합니다."
                        : "코드 실습 후 정기 복습 일정입니다.";

                ReviewTask newTask = ReviewTask.builder()
                        .student(twin.getStudent())
                        .course(twin.getCourse())
                        .skill(!skillSnapshots.isEmpty() ? skillSnapshots.get(0).getSkill() : null)
                        .title("코드 실습 후 복습")
                        .reasonSummary(reason)
                        .scheduledFor(scheduledFor)
                        .status("PENDING")
                        .build();
                reviewTaskRepository.save(newTask);
                log.info("New review task created from code practice - studentId={}, skillId={}, scheduledFor={}",
                        studentId, skillId, scheduledFor);
            }
        }
    }

}
