package com.classpulse.domain.learning;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CurriculumSkillRepository;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import com.classpulse.domain.twin.SkillMasterySnapshotRepository;
import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 복습 스케줄러
 * 학생의 트윈 상태와 스킬 숙달도를 기반으로 간격 반복(Spaced Repetition) +
 * 트윈 인식 복습 과제를 생성합니다.
 *
 * 에빙하우스 망각 곡선 기반 간격:
 * - 1일 후: 첫 번째 복습
 * - 3일 후: 두 번째 복습
 * - 7일 후: 세 번째 복습
 * - 14일 후: 네 번째 복습
 * - 30일 후: 다섯 번째 복습
 *
 * 트윈 상태에 따른 간격 조정:
 * - 망각위험이 높으면 간격을 줄임
 * - 이해도가 낮으면 추가 복습 생성
 * - 자신감이 낮으면 쉬운 복습부터 배치
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewScheduler {

    private final ReviewTaskRepository reviewTaskRepository;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final CurriculumSkillRepository curriculumSkillRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // Ebbinghaus-inspired intervals (days)
    private static final int[] BASE_INTERVALS = {0, 1, 3, 7, 14, 30};

    /**
     * 학생의 트윈 상태와 약점 스킬을 기반으로 복습 과제를 생성합니다.
     */
    @Transactional
    public List<ReviewTask> generateReviewTasks(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        StudentTwin twin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        List<CurriculumSkill> allSkills = curriculumSkillRepository.findByCourseId(courseId);
        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);

        // Get latest snapshot per skill
        Map<Long, SkillMasterySnapshot> latestPerSkill = new LinkedHashMap<>();
        for (SkillMasterySnapshot s : snapshots) {
            latestPerSkill.putIfAbsent(s.getSkill().getId(), s);
        }

        // Get existing pending tasks to avoid duplicates
        List<ReviewTask> existingPending = reviewTaskRepository
                .findByStudentIdAndStatus(studentId, "PENDING");
        Set<Long> existingSkillIds = existingPending.stream()
                .filter(t -> t.getSkill() != null)
                .map(t -> t.getSkill().getId())
                .collect(Collectors.toSet());

        List<ReviewTask> generatedTasks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Categorize skills by review urgency
        List<SkillReviewInfo> reviewInfos = new ArrayList<>();
        for (CurriculumSkill skill : allSkills) {
            if (existingSkillIds.contains(skill.getId())) continue;

            SkillMasterySnapshot snapshot = latestPerSkill.get(skill.getId());
            SkillReviewInfo info = new SkillReviewInfo(skill, snapshot);
            reviewInfos.add(info);
        }

        // Sort by priority (high forgetting risk + low understanding first)
        reviewInfos.sort(Comparator.comparingDouble(SkillReviewInfo::getPriority).reversed());

        // Generate tasks for top priority skills
        int maxTasks = calculateMaxTasks(twin);
        int taskCount = 0;

        for (SkillReviewInfo info : reviewInfos) {
            if (taskCount >= maxTasks) break;

            int[] adjustedIntervals = adjustIntervalsForTwin(twin, info);
            int intervalIndex = determineIntervalIndex(studentId, info.skill.getId());

            if (intervalIndex >= adjustedIntervals.length) continue;

            int daysFromNow = adjustedIntervals[intervalIndex];
            LocalDate scheduledFor = today.plusDays(daysFromNow);

            String reason = buildReasonSummary(info, twin);

            ReviewTask task = ReviewTask.builder()
                    .student(student)
                    .course(course)
                    .skill(info.skill)
                    .title(buildTaskTitle(info))
                    .reasonSummary(reason)
                    .scheduledFor(scheduledFor)
                    .status("PENDING")
                    .build();

            generatedTasks.add(reviewTaskRepository.save(task));
            taskCount++;
        }

        // Add forgotten concept tasks (skills with no snapshot but part of curriculum)
        for (CurriculumSkill skill : allSkills) {
            if (taskCount >= maxTasks) break;
            if (existingSkillIds.contains(skill.getId())) continue;
            if (latestPerSkill.containsKey(skill.getId())) continue;

            ReviewTask task = ReviewTask.builder()
                    .student(student)
                    .course(course)
                    .skill(skill)
                    .title("새로운 개념 학습: " + skill.getName())
                    .reasonSummary("아직 학습하지 않은 스킬입니다. 커리큘럼에 포함된 핵심 개념이므로 학습을 시작하세요.")
                    .scheduledFor(today)
                    .status("PENDING")
                    .build();

            generatedTasks.add(reviewTaskRepository.save(task));
            taskCount++;
        }

        log.info("복습 과제 생성 완료 - studentId={}, courseId={}, 생성 {}건",
                studentId, courseId, generatedTasks.size());

        return generatedTasks;
    }

    /**
     * 트윈 상태에 따라 최대 생성 과제 수를 결정합니다.
     */
    private int calculateMaxTasks(StudentTwin twin) {
        if (twin == null) return 3;

        double risk = twin.getOverallRiskScore().doubleValue();
        double motivation = twin.getMotivationScore().doubleValue();

        // High risk but low motivation: fewer tasks to avoid overwhelming
        if (risk > 60 && motivation < 40) return 2;
        // High risk and high motivation: more tasks
        if (risk > 60 && motivation >= 60) return 5;
        // Normal
        return 3;
    }

    /**
     * 트윈 상태에 따라 복습 간격을 조정합니다.
     */
    private int[] adjustIntervalsForTwin(StudentTwin twin, SkillReviewInfo info) {
        if (twin == null) return BASE_INTERVALS;

        double retentionRisk = twin.getRetentionRiskScore().doubleValue();
        double forgettingRisk = info.forgettingRisk;

        // High forgetting risk: shorten intervals
        double factor;
        if (forgettingRisk > 70 || retentionRisk > 70) {
            factor = 0.5; // Half the intervals
        } else if (forgettingRisk > 50 || retentionRisk > 50) {
            factor = 0.7;
        } else if (forgettingRisk < 20 && retentionRisk < 30) {
            factor = 1.5; // Extend intervals for well-understood skills
        } else {
            factor = 1.0;
        }

        return Arrays.stream(BASE_INTERVALS)
                .map(i -> Math.max(1, (int) Math.round(i * factor)))
                .toArray();
    }

    /**
     * 이전 복습 완료 횟수에 따라 간격 인덱스를 결정합니다.
     */
    private int determineIntervalIndex(Long studentId, Long skillId) {
        // Count completed reviews for this skill
        List<ReviewTask> allTasks = reviewTaskRepository
                .findByStudentIdAndStatus(studentId, "COMPLETED");
        long completedCount = allTasks.stream()
                .filter(t -> t.getSkill() != null && t.getSkill().getId().equals(skillId))
                .count();

        return (int) Math.min(completedCount, BASE_INTERVALS.length - 1);
    }

    private String buildTaskTitle(SkillReviewInfo info) {
        if (info.forgettingRisk > 70) {
            return "긴급 복습: " + info.skill.getName();
        } else if (info.understanding < 50) {
            return "기초 다지기: " + info.skill.getName();
        } else {
            return "정기 복습: " + info.skill.getName();
        }
    }

    private String buildReasonSummary(SkillReviewInfo info, StudentTwin twin) {
        StringBuilder reason = new StringBuilder();

        if (info.forgettingRisk > 70) {
            reason.append("망각 위험이 높습니다(%.0f%%). ".formatted(info.forgettingRisk));
        }
        if (info.understanding < 50) {
            reason.append("이해도가 낮습니다(%.0f%%). ".formatted(info.understanding));
        }
        if (info.confidence < 40) {
            reason.append("자신감이 부족합니다(%.0f%%). ".formatted(info.confidence));
        }
        if (twin != null && twin.getRetentionRiskScore().doubleValue() > 60) {
            reason.append("전반적인 학습 유지율이 낮은 상태입니다. ");
        }

        if (reason.length() == 0) {
            reason.append("주기적인 복습을 통해 장기 기억으로 전환하세요.");
        } else {
            reason.append("반복 학습으로 숙달도를 높이세요.");
        }

        return reason.toString();
    }

    // ── Inner helper class ───────────────────────────────────────────────

    private static class SkillReviewInfo {
        final CurriculumSkill skill;
        final double understanding;
        final double practice;
        final double confidence;
        final double forgettingRisk;

        SkillReviewInfo(CurriculumSkill skill, SkillMasterySnapshot snapshot) {
            this.skill = skill;
            if (snapshot != null) {
                this.understanding = snapshot.getUnderstandingScore().doubleValue();
                this.practice = snapshot.getPracticeScore().doubleValue();
                this.confidence = snapshot.getConfidenceScore().doubleValue();
                this.forgettingRisk = snapshot.getForgettingRiskScore().doubleValue();
            } else {
                this.understanding = 0;
                this.practice = 0;
                this.confidence = 0;
                this.forgettingRisk = 80; // Assume high forgetting risk for untracked skills
            }
        }

        double getPriority() {
            // Higher priority = needs review more urgently
            return forgettingRisk * 0.4
                    + (100 - understanding) * 0.3
                    + (100 - confidence) * 0.2
                    + (100 - practice) * 0.1;
        }
    }
}
