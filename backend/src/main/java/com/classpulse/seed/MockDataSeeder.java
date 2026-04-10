package com.classpulse.seed;

import com.classpulse.domain.chatbot.ChatMessage;
import com.classpulse.domain.chatbot.ChatMessageRepository;
import com.classpulse.domain.chatbot.Conversation;
import com.classpulse.domain.chatbot.ConversationRepository;
import com.classpulse.domain.codeanalysis.CodeFeedback;
import com.classpulse.domain.codeanalysis.CodeFeedbackRepository;
import com.classpulse.domain.codeanalysis.CodeSubmission;
import com.classpulse.domain.codeanalysis.CodeSubmissionRepository;
import com.classpulse.domain.consultation.ActionPlan;
import com.classpulse.domain.consultation.ActionPlanRepository;
import com.classpulse.domain.consultation.Consultation;
import com.classpulse.domain.consultation.ConsultationRepository;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseEnrollment;
import com.classpulse.domain.course.CourseEnrollmentRepository;
import com.classpulse.domain.course.CourseWeek;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.gamification.*;
import com.classpulse.domain.learning.Reflection;
import com.classpulse.domain.learning.ReflectionRepository;
import com.classpulse.domain.learning.ReviewTask;
import com.classpulse.domain.learning.ReviewTaskRepository;
import com.classpulse.domain.recommendation.Recommendation;
import com.classpulse.domain.recommendation.RecommendationRepository;
import com.classpulse.domain.studygroup.StudyGroup;
import com.classpulse.domain.studygroup.StudyGroupMember;
import com.classpulse.domain.studygroup.StudyGroupRepository;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import com.classpulse.domain.twin.SkillMasterySnapshotRepository;
import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class MockDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ReflectionRepository reflectionRepository;
    private final ConsultationRepository consultationRepository;
    private final ActionPlanRepository actionPlanRepository;
    private final RecommendationRepository recommendationRepository;
    private final GamificationRepository gamificationRepository;
    private final BadgeRepository badgeRepository;
    private final StudentBadgeRepository studentBadgeRepository;
    private final XPEventRepository xpEventRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final CodeFeedbackRepository codeFeedbackRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("[MockDataSeeder] Data already exists, skipping seed.");
            return;
        }

        log.info("[MockDataSeeder] Seeding demo data...");

        String encodedPassword = passwordEncoder.encode("password123");

        // ── 1. Users (10 students + 2 instructors) ─────────────────────
        User[] students = new User[10];
        String[][] studentData = {
                {"student01@classpulse.dev", "김민준"},
                {"student02@classpulse.dev", "이서연"},
                {"student03@classpulse.dev", "박지훈"},
                {"student04@classpulse.dev", "최은서"},
                {"student05@classpulse.dev", "정현우"},
                {"student06@classpulse.dev", "강수빈"},
                {"student07@classpulse.dev", "윤도현"},
                {"student08@classpulse.dev", "장하은"},
                {"student09@classpulse.dev", "임태양"},
                {"student10@classpulse.dev", "한서진"},
        };
        for (int i = 0; i < 10; i++) {
            students[i] = userRepository.save(User.builder()
                    .email(studentData[i][0])
                    .passwordHash(encodedPassword)
                    .name(studentData[i][1])
                    .role(User.Role.STUDENT)
                    .build());
        }

        User instructor1 = userRepository.save(User.builder()
                .email("instructor01@classpulse.dev")
                .passwordHash(encodedPassword)
                .name("박지훈(강사)")
                .role(User.Role.INSTRUCTOR)
                .build());

        User instructor2 = userRepository.save(User.builder()
                .email("instructor02@classpulse.dev")
                .passwordHash(encodedPassword)
                .name("이영수(강사)")
                .role(User.Role.INSTRUCTOR)
                .build());

        log.info("[MockDataSeeder] Created 12 users (10 students, 2 instructors)");

        // ── 2. Course with 8 weeks ──────────────────────────────────────
        Course course = Course.builder()
                .title("풀스택 웹개발 부트캠프")
                .description("HTML/CSS부터 React, Spring Boot까지 16주 완성 풀스택 과정")
                .status("ACTIVE")
                .createdBy(instructor1)
                .build();

        String[][] weekData = {
                {"HTML & CSS 기초", "시맨틱 HTML, Flexbox, Grid 레이아웃 학습"},
                {"JavaScript 핵심", "변수, 함수, 스코프, 클로저, 비동기 프로그래밍"},
                {"React 기초", "컴포넌트, Props, State, Hooks 기본 개념"},
                {"React 심화", "Context API, useReducer, 커스텀 훅, 성능 최적화"},
                {"Java & Spring 입문", "Java 문법, OOP 원칙, Spring Boot 프로젝트 구조"},
                {"REST API 설계", "RESTful API 원칙, Spring MVC, JPA 연동"},
                {"인증 & 보안", "JWT, Spring Security, OAuth2 소셜 로그인"},
                {"배포 & DevOps", "Docker, CI/CD 파이프라인, 클라우드 배포"},
        };

        for (int i = 0; i < weekData.length; i++) {
            course.getWeeks().add(CourseWeek.builder()
                    .course(course)
                    .weekNo(i + 1)
                    .title(weekData[i][0])
                    .summary(weekData[i][1])
                    .build());
        }

        em.persist(course);
        em.flush();

        log.info("[MockDataSeeder] Created course id={} with {} weeks", course.getId(), course.getWeeks().size());

        // ── 3. Curriculum Skills (6) ────────────────────────────────────
        String[][] skillData = {
                {"HTML/CSS", "시맨틱 마크업과 반응형 레이아웃 구현 능력", "EASY"},
                {"JavaScript 기초", "변수, 함수, 스코프, 비동기 처리 이해", "EASY"},
                {"React 컴포넌트", "함수형 컴포넌트, Hooks, 상태관리 패턴", "MEDIUM"},
                {"재귀 함수", "재귀적 문제 분해와 호출 스택 이해", "HARD"},
                {"REST API", "RESTful 설계 원칙과 HTTP 메서드 활용", "MEDIUM"},
                {"Spring Security", "인증/인가 메커니즘과 JWT 토큰 관리", "HARD"},
        };

        List<CurriculumSkill> skills = new ArrayList<>();
        for (String[] sd : skillData) {
            CurriculumSkill skill = CurriculumSkill.builder()
                    .course(course)
                    .name(sd[0])
                    .description(sd[1])
                    .difficulty(sd[2])
                    .build();
            em.persist(skill);
            skills.add(skill);
        }

        skills.get(2).getPrerequisites().add(skills.get(1)); // React -> JS
        skills.get(4).getPrerequisites().add(skills.get(1)); // REST API -> JS
        skills.get(5).getPrerequisites().add(skills.get(4)); // Spring Security -> REST API

        em.flush();
        log.info("[MockDataSeeder] Created {} curriculum skills", skills.size());

        // ── 4. Course Enrollments for all 10 students ───────────────────
        for (User student : students) {
            courseEnrollmentRepository.save(CourseEnrollment.builder()
                    .course(course)
                    .student(student)
                    .status("ACTIVE")
                    .build());
        }
        log.info("[MockDataSeeder] Created 10 course enrollments");

        // ── 5. Student Twins (varying risk levels) ──────────────────────
        // High risk (3): students[0], students[3], students[6]
        // Medium risk (2): students[2], students[8]
        // Low risk (5): students[1], students[4], students[5], students[7], students[9]

        BigDecimal[][] twinScores = {
                // mastery, execution, retentionRisk, motivation, consultationNeed, overallRisk
                {bd("42.50"), bd("35.00"), bd("72.00"), bd("38.00"), bd("75.00"), bd("68.00")}, // 0 김민준 - high
                {bd("85.00"), bd("88.00"), bd("12.00"), bd("90.00"), bd("10.00"), bd("15.00")}, // 1 이서연 - low
                {bd("60.00"), bd("55.00"), bd("45.00"), bd("62.00"), bd("50.00"), bd("42.00")}, // 2 박지훈 - medium
                {bd("35.00"), bd("28.00"), bd("78.00"), bd("30.00"), bd("82.00"), bd("75.00")}, // 3 최은서 - high
                {bd("78.00"), bd("80.00"), bd("18.00"), bd("82.00"), bd("15.00"), bd("20.00")}, // 4 정현우 - low
                {bd("82.00"), bd("76.00"), bd("20.00"), bd("85.00"), bd("12.00"), bd("18.00")}, // 5 강수빈 - low
                {bd("38.00"), bd("32.00"), bd("68.00"), bd("35.00"), bd("70.00"), bd("65.00")}, // 6 윤도현 - high
                {bd("75.00"), bd("72.00"), bd("22.00"), bd("78.00"), bd("20.00"), bd("25.00")}, // 7 장하은 - low
                {bd("55.00"), bd("50.00"), bd("50.00"), bd("58.00"), bd("55.00"), bd("48.00")}, // 8 임태양 - medium
                {bd("88.00"), bd("90.00"), bd("8.00"),  bd("92.00"), bd("8.00"),  bd("10.00")}, // 9 한서진 - low
        };

        String[] twinInsights = {
                "김민준 학생은 HTML/CSS 기초가 약하고 재귀 함수 이해도가 매우 낮습니다. 자신감 부족으로 학습 동기가 떨어지고 있어 즉각적인 상담이 필요합니다.",
                "이서연 학생은 전반적으로 우수한 성적을 보이고 있습니다. 특히 React와 JavaScript에서 뛰어난 실력을 발휘하고 있으며, 스터디 그룹 리더로 적합합니다.",
                "박지훈 학생은 중간 수준의 이해도를 보이고 있습니다. JavaScript 기초는 양호하나 REST API 설계에서 추가 학습이 필요합니다.",
                "최은서 학생은 전반적인 이해도가 낮고 특히 React와 Spring Security에서 큰 어려움을 겪고 있습니다. 기초부터 단계별 학습 계획이 필요합니다.",
                "정현우 학생은 안정적인 학습 성과를 보이고 있습니다. 코드 실습 참여도가 높고 꾸준한 복습으로 기억 유지율이 좋습니다.",
                "강수빈 학생은 높은 이해도와 동기부여를 보입니다. JavaScript와 React에서 특히 우수하며, 다른 학생들의 멘토 역할이 가능합니다.",
                "윤도현 학생은 학습 참여도가 저조하고 복습 이행률이 낮습니다. 재귀 함수와 Spring Security 이해도가 매우 낮아 집중 지원이 필요합니다.",
                "장하은 학생은 꾸준한 성장세를 보이고 있습니다. HTML/CSS와 JavaScript에서 좋은 성적을 유지하고 있으며, React 심화 학습 중입니다.",
                "임태양 학생은 중간 수준의 이해도를 보이며, 동기부여 점수가 다소 떨어지고 있습니다. 실습 위주 학습과 스터디 그룹 참여를 권장합니다.",
                "한서진 학생은 최상위 성적을 유지하고 있으며, 모든 영역에서 우수합니다. 심화 과제와 프로젝트 리더 역할을 맡길 수 있습니다.",
        };

        StudentTwin[] twins = new StudentTwin[10];
        for (int i = 0; i < 10; i++) {
            twins[i] = studentTwinRepository.save(StudentTwin.builder()
                    .student(students[i])
                    .course(course)
                    .masteryScore(twinScores[i][0])
                    .executionScore(twinScores[i][1])
                    .retentionRiskScore(twinScores[i][2])
                    .motivationScore(twinScores[i][3])
                    .consultationNeedScore(twinScores[i][4])
                    .overallRiskScore(twinScores[i][5])
                    .aiInsight(twinInsights[i])
                    .build());
        }
        log.info("[MockDataSeeder] Created 10 student twins");

        // ── 6. Skill Mastery Snapshots (2-3 per student per skill) ──────
        Random rng = new Random(42);
        for (int si = 0; si < 10; si++) {
            double baseFactor = twinScores[si][0].doubleValue() / 100.0; // scale based on mastery
            for (int ski = 0; ski < skills.size(); ski++) {
                int snapshotCount = 2 + rng.nextInt(2); // 2 or 3
                for (int snap = 0; snap < snapshotCount; snap++) {
                    double variance = (rng.nextDouble() - 0.5) * 20;
                    double understanding = clamp(baseFactor * 85 + variance + ski * (-3), 5, 98);
                    double practice = clamp(baseFactor * 80 + variance + ski * (-4), 5, 98);
                    double confidence = clamp(baseFactor * 82 + variance + ski * (-2), 5, 98);
                    double forgetting = clamp((1 - baseFactor) * 60 + Math.abs(variance) + ski * 3, 2, 90);

                    snapshotRepository.save(SkillMasterySnapshot.builder()
                            .student(students[si])
                            .course(course)
                            .skill(skills.get(ski))
                            .understandingScore(bd(understanding))
                            .practiceScore(bd(practice))
                            .confidenceScore(bd(confidence))
                            .forgettingRiskScore(bd(forgetting))
                            .sourceType(snap == 0 ? "REFLECTION_ANALYSIS" : "CODE_ANALYSIS")
                            .build());
                }
            }
        }
        log.info("[MockDataSeeder] Created skill mastery snapshots for all students");

        // ── 7. Review Tasks (3-5 per student) ───────────────────────────
        String[][] taskTemplates = {
                {"기본 개념 복습", "이해도가 낮아 기본 개념부터 복습이 필요합니다."},
                {"실습 과제 재도전", "실습 점수가 낮아 추가 연습이 필요합니다."},
                {"심화 문제 풀이", "기초는 탄탄하지만 심화 문제 연습이 필요합니다."},
                {"코드 리팩토링 연습", "코드 품질 향상을 위한 리팩토링 연습을 해보세요."},
                {"개념 정리 노트 작성", "핵심 개념을 정리하여 장기 기억으로 전환하세요."},
        };
        String[] taskStatuses = {"PENDING", "PENDING", "IN_PROGRESS", "COMPLETED", "SKIPPED"};

        for (int si = 0; si < 10; si++) {
            int taskCount = 3 + rng.nextInt(3); // 3-5
            for (int t = 0; t < taskCount; t++) {
                int tmplIdx = t % taskTemplates.length;
                CurriculumSkill skill = skills.get(rng.nextInt(skills.size()));
                String status = taskStatuses[t % taskStatuses.length];
                LocalDate scheduledFor = LocalDate.now().minusDays(rng.nextInt(10)).plusDays(rng.nextInt(5));

                ReviewTask task = ReviewTask.builder()
                        .student(students[si])
                        .course(course)
                        .skill(skill)
                        .title(skill.getName() + " " + taskTemplates[tmplIdx][0])
                        .reasonSummary(taskTemplates[tmplIdx][1])
                        .scheduledFor(scheduledFor)
                        .status(status)
                        .build();
                if ("COMPLETED".equals(status)) {
                    task.setCompletedAt(LocalDateTime.now().minusDays(rng.nextInt(5)));
                }
                reviewTaskRepository.save(task);
            }
        }
        log.info("[MockDataSeeder] Created review tasks for all 10 students");

        // ── 8. Reflections (2-3 per student) ────────────────────────────
        String[][] reflectionTemplates = {
                {"오늘 %s를 배웠는데, 기본 개념은 이해했지만 응용 부분이 아직 어렵다. 더 연습이 필요할 것 같다.",
                 "응용 문제에서 막히는 부분", "혼란", "호기심"},
                {"드디어 %s 개념을 이해했다! 실습을 통해 직접 해보니 이론으로만 보던 것과는 다르게 느껴진다.",
                 null, "성취감", "자신감"},
                {"%s 관련 과제를 제출했는데, 피드백에서 몇 가지 개선점이 있었다. 다음에는 더 잘할 수 있을 것 같다.",
                 "에러 핸들링 부분", "약간의 아쉬움", "의지"},
        };

        for (int si = 0; si < 10; si++) {
            int refCount = 2 + rng.nextInt(2); // 2-3
            for (int r = 0; r < refCount; r++) {
                int tmplIdx = r % reflectionTemplates.length;
                CurriculumSkill skill = skills.get(rng.nextInt(skills.size()));
                int confidence = 1 + rng.nextInt(5);
                double intensity = 0.3 + rng.nextDouble() * 0.6;

                reflectionRepository.save(Reflection.builder()
                        .student(students[si])
                        .course(course)
                        .content(String.format(reflectionTemplates[tmplIdx][0], skill.getName()))
                        .stuckPoint(reflectionTemplates[tmplIdx][1])
                        .selfConfidenceScore(confidence)
                        .emotionSummary(Map.of(
                                "primary", reflectionTemplates[tmplIdx][2],
                                "secondary", reflectionTemplates[tmplIdx][3],
                                "intensity", Math.round(intensity * 100.0) / 100.0
                        ))
                        .aiAnalysisJson(Map.of(
                                "knowledgeGaps", List.of(skill.getName() + " 심화"),
                                "strengths", List.of(skill.getName() + " 기본 이해"),
                                "suggestion", skill.getName() + " 관련 추가 실습을 권장합니다."
                        ))
                        .build());
            }
        }
        log.info("[MockDataSeeder] Created reflections for all 10 students");

        // ── 9. Consultations (8 total) ──────────────────────────────────
        // 3 scheduled, 5 completed
        Consultation[] completedConsults = new Consultation[5];

        // Scheduled consultations
        consultationRepository.save(Consultation.builder()
                .student(students[0]).instructor(instructor1).course(course)
                .scheduledAt(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .status("SCHEDULED")
                .notes("김민준 학생 재귀 함수 및 기초 학습 전략 상담")
                .briefingJson(Map.of(
                        "twinSummary", "전반적 이해도 42.5%, 위험도 68%",
                        "weakSkills", List.of("재귀 함수", "REST API"),
                        "suggestedTopics", List.of("기초 개념 재학습", "단계별 학습 계획")))
                .build());

        consultationRepository.save(Consultation.builder()
                .student(students[3]).instructor(instructor1).course(course)
                .scheduledAt(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0))
                .status("SCHEDULED")
                .notes("최은서 학생 전반적 학습 부진 상담")
                .briefingJson(Map.of(
                        "twinSummary", "전반적 이해도 35%, 위험도 75%",
                        "weakSkills", List.of("React 컴포넌트", "Spring Security"),
                        "suggestedTopics", List.of("기초 보강", "학습 동기 부여")))
                .build());

        consultationRepository.save(Consultation.builder()
                .student(students[6]).instructor(instructor2).course(course)
                .scheduledAt(LocalDateTime.now().plusDays(4).withHour(15).withMinute(30))
                .status("SCHEDULED")
                .notes("윤도현 학생 학습 참여도 개선 상담")
                .briefingJson(Map.of(
                        "twinSummary", "참여도 저조, 위험도 65%",
                        "weakSkills", List.of("재귀 함수", "Spring Security"),
                        "suggestedTopics", List.of("학습 습관 형성", "목표 설정")))
                .build());

        // Completed consultations
        completedConsults[0] = consultationRepository.save(Consultation.builder()
                .student(students[0]).instructor(instructor1).course(course)
                .scheduledAt(LocalDateTime.now().minusDays(7).withHour(10).withMinute(0))
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(7).withHour(10).withMinute(45))
                .notes("JavaScript 비동기 처리 개념 상담")
                .summaryText("Promise와 async/await의 차이점 설명. 콜백 지옥 해결 패턴 학습. 에러 핸들링 방법 논의.")
                .causeAnalysis("비동기 개념의 추상성이 높아 실습 위주 학습이 부족했음")
                .build());

        completedConsults[1] = consultationRepository.save(Consultation.builder()
                .student(students[2]).instructor(instructor1).course(course)
                .scheduledAt(LocalDateTime.now().minusDays(5).withHour(14).withMinute(0))
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(5).withHour(14).withMinute(50))
                .notes("REST API 설계 원칙 보충 상담")
                .summaryText("RESTful 설계 원칙 복습. HTTP 메서드 시맨틱 설명. 실전 API 설계 연습 진행.")
                .causeAnalysis("이론 학습은 충분하나 실전 적용 경험이 부족")
                .build());

        completedConsults[2] = consultationRepository.save(Consultation.builder()
                .student(students[3]).instructor(instructor2).course(course)
                .scheduledAt(LocalDateTime.now().minusDays(10).withHour(11).withMinute(0))
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(10).withHour(11).withMinute(40))
                .notes("React 기초 개념 보충 상담")
                .summaryText("컴포넌트 라이프사이클과 Hooks 기본 개념 재설명. useState/useEffect 실습 진행.")
                .causeAnalysis("기초 JavaScript 이해 부족이 React 학습의 장애 요인")
                .build());

        completedConsults[3] = consultationRepository.save(Consultation.builder()
                .student(students[6]).instructor(instructor1).course(course)
                .scheduledAt(LocalDateTime.now().minusDays(3).withHour(16).withMinute(0))
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(3).withHour(16).withMinute(30))
                .notes("윤도현 학생 학습 습관 형성 상담")
                .summaryText("매일 30분 코딩 습관 형성 계획. 복습 스케줄 수립. 스터디 그룹 참여 권유.")
                .causeAnalysis("학습 습관이 불규칙하고 목표가 모호함")
                .build());

        completedConsults[4] = consultationRepository.save(Consultation.builder()
                .student(students[8]).instructor(instructor2).course(course)
                .scheduledAt(LocalDateTime.now().minusDays(1).withHour(13).withMinute(0))
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(1).withHour(13).withMinute(45))
                .notes("임태양 학생 동기부여 및 학습 방향 상담")
                .summaryText("학습 목표 재설정. 실습 중심 학습 전략 수립. 스터디 그룹 합류 안내.")
                .causeAnalysis("동기부여 저하와 학습 방향 불확실성이 주요 원인")
                .build());

        log.info("[MockDataSeeder] Created 8 consultations (3 scheduled, 5 completed)");

        // ── 10. Action Plans (2-3 per completed consultation) ───────────
        // Consultation 0: 김민준 - JS async
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[0]).student(students[0]).course(course)
                .title("Promise 체이닝 연습 과제")
                .description("5개의 Promise 체이닝 문제를 풀고 결과를 제출하세요.")
                .dueDate(LocalDate.now().plusDays(3)).linkedSkill(skills.get(1))
                .priority("HIGH").status("IN_PROGRESS").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[0]).student(students[0]).course(course)
                .title("async/await 리팩토링")
                .description("기존 콜백 기반 코드를 async/await으로 리팩토링하세요.")
                .dueDate(LocalDate.now().plusDays(5)).linkedSkill(skills.get(1))
                .priority("MEDIUM").status("PENDING").build());

        // Consultation 1: 박지훈 - REST API
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[1]).student(students[2]).course(course)
                .title("REST API 설계 실습")
                .description("게시판 CRUD API를 설계하고 Swagger 문서를 작성하세요.")
                .dueDate(LocalDate.now().plusDays(4)).linkedSkill(skills.get(4))
                .priority("HIGH").status("PENDING").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[1]).student(students[2]).course(course)
                .title("HTTP 상태 코드 정리")
                .description("주요 HTTP 상태 코드를 정리하고 각 사용 사례를 작성하세요.")
                .dueDate(LocalDate.now().plusDays(2)).linkedSkill(skills.get(4))
                .priority("MEDIUM").status("COMPLETED")
                .completedAt(LocalDateTime.now().minusDays(1)).build());

        // Consultation 2: 최은서 - React
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[2]).student(students[3]).course(course)
                .title("JavaScript 기초 복습 과제")
                .description("변수, 함수, 스코프에 대한 기본 문제 30개를 풀어보세요.")
                .dueDate(LocalDate.now().plusDays(7)).linkedSkill(skills.get(1))
                .priority("HIGH").status("IN_PROGRESS").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[2]).student(students[3]).course(course)
                .title("React useState 미니 프로젝트")
                .description("간단한 Todo 앱을 만들어 useState 활용을 연습하세요.")
                .dueDate(LocalDate.now().plusDays(10)).linkedSkill(skills.get(2))
                .priority("MEDIUM").status("PENDING").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[2]).student(students[3]).course(course)
                .title("매일 코딩 챌린지 참여")
                .description("매일 1문제씩 알고리즘 문제를 풀어 기초 체력을 키우세요.")
                .dueDate(LocalDate.now().plusDays(14)).linkedSkill(null)
                .priority("LOW").status("PENDING").build());

        // Consultation 3: 윤도현 - study habits
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[3]).student(students[6]).course(course)
                .title("매일 30분 코딩 습관 형성")
                .description("매일 최소 30분 코딩 실습을 하고 활동 로그를 기록하세요.")
                .dueDate(LocalDate.now().plusDays(14)).linkedSkill(null)
                .priority("HIGH").status("IN_PROGRESS").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[3]).student(students[6]).course(course)
                .title("스터디 그룹 합류")
                .description("React 스터디 그룹에 합류하여 함께 학습하세요.")
                .dueDate(LocalDate.now().plusDays(3)).linkedSkill(skills.get(2))
                .priority("MEDIUM").status("PENDING").build());

        // Consultation 4: 임태양 - motivation
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[4]).student(students[8]).course(course)
                .title("미니 프로젝트 시작")
                .description("관심 있는 주제로 미니 프로젝트를 시작하여 학습 동기를 높이세요.")
                .dueDate(LocalDate.now().plusDays(7)).linkedSkill(null)
                .priority("HIGH").status("PENDING").build());
        actionPlanRepository.save(ActionPlan.builder()
                .consultation(completedConsults[4]).student(students[8]).course(course)
                .title("주간 학습 목표 설정")
                .description("매주 월요일에 주간 학습 목표를 설정하고 금요일에 점검하세요.")
                .dueDate(LocalDate.now().plusDays(5)).linkedSkill(null)
                .priority("MEDIUM").status("PENDING").build());

        log.info("[MockDataSeeder] Created action plans for completed consultations");

        // ── 11. Recommendations (1-3 per student) ───────────────────────
        String[][] recTypes = {{"REVIEW", "TWIN_SCORE_DROP"}, {"RESOURCE", "REVIEW_TASK_CREATED"}, {"STUDY_GROUP", "SKILL_IMPROVEMENT"}};
        String[][] recTemplates = {
                {"%s 집중 복습 추천", "%s 이해도가 %.0f%%로 낮습니다. 집중 복습을 추천합니다.", "이해도 15%% 향상 예상"},
                {"%s 실습 자료 추천", "%s 실습 점수 향상을 위한 추가 자료를 추천합니다.", "실습 점수 20%% 향상 예상"},
                {"%s 스터디 그룹 참여", "%s 실력 향상을 위해 스터디 그룹 참여를 추천합니다.", "협업 능력 및 이해도 향상"},
        };

        for (int si = 0; si < 10; si++) {
            int recCount = 1 + rng.nextInt(3); // 1-3
            for (int r = 0; r < recCount; r++) {
                int tmplIdx = r % recTemplates.length;
                CurriculumSkill skill = skills.get(rng.nextInt(skills.size()));
                double score = twinScores[si][0].doubleValue();

                recommendationRepository.save(Recommendation.builder()
                        .student(students[si]).course(course)
                        .recommendationType(recTypes[tmplIdx][0])
                        .title(String.format(recTemplates[tmplIdx][0], skill.getName()))
                        .reasonSummary(String.format(recTemplates[tmplIdx][1], skill.getName(), score))
                        .triggerEvent(recTypes[tmplIdx][1])
                        .evidencePayload(Map.of(
                                "skillName", skill.getName(),
                                "currentScore", score,
                                "studentName", students[si].getName()))
                        .expectedOutcome(recTemplates[tmplIdx][2])
                        .build());
            }
        }
        log.info("[MockDataSeeder] Created recommendations for all students");

        // ── 12. Gamification (varying levels, XP, streaks) ──────────────
        int[][] gamData = {
                // level, currentXp, nextLevelXp, streakDays, totalXpEarned
                {3,  45,  130,  2,   350},   // 0 김민준
                {10, 180, 280,  21,  3200},  // 1 이서연
                {6,  90,  200,  8,   1500},  // 2 박지훈
                {2,  30,  110,  0,   180},   // 3 최은서
                {8,  150, 250,  14,  2500},  // 4 정현우
                {9,  200, 265,  18,  2900},  // 5 강수빈
                {1,  20,  100,  0,   80},    // 6 윤도현
                {7,  140, 240,  12,  2100},  // 7 장하은
                {4,  60,  150,  3,   600},   // 8 임태양
                {12, 100, 350,  30,  4500},  // 9 한서진
        };
        String[] levelTitles = {"초보 학습자", "시니어 러너", "열정적 코더", "초보 학습자",
                                "풀스택 학습자", "풀스택 학습자", "초보 학습자", "풀스택 학습자",
                                "열정적 코더", "시니어 러너"};

        StudentGamification[] gamifications = new StudentGamification[10];
        for (int i = 0; i < 10; i++) {
            gamifications[i] = gamificationRepository.save(StudentGamification.builder()
                    .student(students[i]).course(course)
                    .level(gamData[i][0])
                    .currentXp(gamData[i][1])
                    .nextLevelXp(gamData[i][2])
                    .levelTitle(levelTitles[i])
                    .streakDays(gamData[i][3])
                    .lastActivityDate(gamData[i][3] > 0 ? LocalDate.now() : LocalDate.now().minusDays(5))
                    .totalXpEarned(gamData[i][4])
                    .build());
        }
        log.info("[MockDataSeeder] Created gamification state for all 10 students");

        // ── 13. Badges (assign from V3 seed badges) ────────────────────
        List<Badge> allBadges = badgeRepository.findAll();
        // Assign 1-5 badges per student based on their level/performance
        int[][] badgeAssignments = {
                {0},                    // 김민준: 1 badge
                {0, 1, 2, 4, 5},        // 이서연: 5 badges
                {0, 2, 3},              // 박지훈: 3 badges
                {0},                    // 최은서: 1 badge
                {0, 1, 2, 4},           // 정현우: 4 badges
                {0, 1, 2, 3, 4},        // 강수빈: 5 badges
                {},                     // 윤도현: 0 badges
                {0, 1, 2},              // 장하은: 3 badges
                {0, 2},                 // 임태양: 2 badges
                {0, 1, 2, 3, 4, 5, 7},  // 한서진: 7 badges (top performer)
        };

        for (int si = 0; si < 10; si++) {
            for (int badgeIdx : badgeAssignments[si]) {
                if (badgeIdx < allBadges.size()) {
                    studentBadgeRepository.save(StudentBadge.builder()
                            .student(students[si])
                            .badge(allBadges.get(badgeIdx))
                            .build());
                }
            }
        }
        log.info("[MockDataSeeder] Assigned V3 badges to students");

        // ── 14. XP Events (5-10 per student) ────────────────────────────
        String[] eventTypes = {"REFLECTION_SUBMIT", "REVIEW_COMPLETE", "CODE_SUBMIT",
                               "CONSULTATION_ATTEND", "CHATBOT_INTERACTION", "STUDY_GROUP_JOIN"};
        int[] eventXpAmounts = {20, 15, 25, 35, 5, 20};

        for (int si = 0; si < 10; si++) {
            int eventCount = 5 + rng.nextInt(6); // 5-10
            for (int e = 0; e < eventCount; e++) {
                int typeIdx = rng.nextInt(eventTypes.length);
                xpEventRepository.save(XPEvent.builder()
                        .student(students[si]).course(course)
                        .eventType(eventTypes[typeIdx])
                        .xpAmount(eventXpAmounts[typeIdx])
                        .sourceType(eventTypes[typeIdx])
                        .build());
            }
        }
        log.info("[MockDataSeeder] Created XP events for all students");

        // ── 15. Conversations (1-2 for first 3 students) ───────────────
        for (int si = 0; si < 3; si++) {
            int convCount = 1 + rng.nextInt(2); // 1-2
            for (int c = 0; c < convCount; c++) {
                CurriculumSkill skill = skills.get(rng.nextInt(skills.size()));
                Conversation conv = conversationRepository.save(Conversation.builder()
                        .student(students[si]).course(course)
                        .title(skill.getName() + " 학습 도우미")
                        .twinContextJson(Map.of(
                                "currentSkill", skill.getName(),
                                "masteryScore", twinScores[si][0].doubleValue(),
                                "recentReflections", List.of("최근 학습 내용 요약")))
                        .status("ACTIVE")
                        .build());

                // 3-5 messages per conversation
                String[][] msgPairs = {
                        {"USER", skill.getName() + "에서 이해가 안 되는 부분이 있어요. 도와주세요."},
                        {"ASSISTANT", "물론이죠! " + skill.getName() + "의 어떤 부분이 어려우신가요? 현재 학습 상태를 보면 기본 개념은 어느 정도 이해하고 계신 것 같아요."},
                        {"USER", "특히 실습에서 막히는 부분이 많아요. 예제를 보여주실 수 있나요?"},
                        {"ASSISTANT", "좋은 질문이에요! 간단한 예제부터 시작해볼게요. " + skill.getName() + "의 핵심은 기본 원리를 이해하는 것입니다. 먼저 이 코드를 살펴보세요..."},
                        {"USER", "아, 이렇게 하면 되는군요! 감사합니다."},
                };
                int msgCount = 3 + rng.nextInt(3); // 3-5
                for (int m = 0; m < msgCount && m < msgPairs.length; m++) {
                    chatMessageRepository.save(ChatMessage.builder()
                            .conversation(conv)
                            .role(msgPairs[m][0])
                            .content(msgPairs[m][1])
                            .tokenCount(50 + rng.nextInt(150))
                            .build());
                }
            }
        }
        log.info("[MockDataSeeder] Created chatbot conversations for first 3 students");

        // ── 16. Code Submissions (3-5 for first 5 students) ────────────
        String[][] codeTemplates = {
                {"javascript", "function fibonacci(n) {\n  if (n <= 1) return n;\n  return fibonacci(n - 1) + fibonacci(n - 2);\n}"},
                {"javascript", "const fetchData = async (url) => {\n  const response = await fetch(url);\n  const data = await response.json();\n  return data;\n}"},
                {"javascript", "function Counter() {\n  const [count, setCount] = useState(0);\n  return <button onClick={() => setCount(count + 1)}>{count}</button>;\n}"},
                {"java", "@GetMapping(\"/api/users\")\npublic List<User> getUsers() {\n  return userRepository.findAll();\n}"},
                {"java", "@PostMapping(\"/api/auth/login\")\npublic ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {\n  // authenticate\n}"},
        };
        String[][] feedbackTemplates = {
                {"GOOD", "코드 구조가 깔끔합니다.", "현재 상태를 유지하세요."},
                {"WARNING", "에러 핸들링이 부족합니다.", "try-catch 블록을 추가하세요."},
                {"ERROR", "무한 재귀의 위험이 있습니다.", "기저 조건을 명확히 설정하세요."},
                {"INFO", "성능 최적화의 여지가 있습니다.", "메모이제이션을 고려해보세요."},
                {"GOOD", "RESTful 설계 원칙을 잘 따르고 있습니다.", "계속 이 패턴을 유지하세요."},
        };

        for (int si = 0; si < 5; si++) {
            int subCount = 3 + rng.nextInt(3); // 3-5
            for (int s = 0; s < subCount; s++) {
                int codeIdx = s % codeTemplates.length;
                CurriculumSkill skill = skills.get(Math.min(codeIdx, skills.size() - 1));

                CodeSubmission submission = codeSubmissionRepository.save(CodeSubmission.builder()
                        .student(students[si]).course(course).skill(skill)
                        .codeContent(codeTemplates[codeIdx][1])
                        .language(codeTemplates[codeIdx][0])
                        .status("ANALYZED")
                        .build());

                // 1-3 feedbacks per submission
                int fbCount = 1 + rng.nextInt(3);
                for (int f = 0; f < fbCount; f++) {
                    int fbIdx = (s + f) % feedbackTemplates.length;
                    boolean twinLinked = rng.nextBoolean();
                    codeFeedbackRepository.save(CodeFeedback.builder()
                            .submission(submission)
                            .lineNumber(1 + rng.nextInt(5))
                            .endLineNumber(2 + rng.nextInt(5))
                            .severity(feedbackTemplates[fbIdx][0])
                            .message(feedbackTemplates[fbIdx][1])
                            .suggestion(feedbackTemplates[fbIdx][2])
                            .twinLinked(twinLinked)
                            .twinSkill(twinLinked ? skill : null)
                            .build());
                }
            }
        }
        log.info("[MockDataSeeder] Created code submissions with feedback for first 5 students");

        // ── 17. Study Groups (2 groups) ─────────────────────────────────
        StudyGroup group1 = StudyGroup.builder()
                .course(course)
                .name("React 마스터즈")
                .description("React 심화 학습을 위한 스터디 그룹")
                .maxMembers(5)
                .status("ACTIVE")
                .createdBy(students[1])
                .build();
        em.persist(group1);

        // Group 1 members: students[1](leader), [4], [5], [7], [9]
        addGroupMember(group1, students[1], "LEADER", "React, JavaScript 전문", "그룹 리더 및 멘토", bd("0.95"));
        addGroupMember(group1, students[4], "MEMBER", "Java, REST API 전문", "백엔드 관점 보완", bd("0.85"));
        addGroupMember(group1, students[5], "MEMBER", "CSS, React 전문", "UI/UX 관점 제공", bd("0.90"));
        addGroupMember(group1, students[7], "MEMBER", "HTML/CSS, JavaScript 기초", "프론트엔드 기초 담당", bd("0.80"));
        addGroupMember(group1, students[9], "MEMBER", "전 분야 우수", "전반적 도움 가능", bd("0.92"));

        StudyGroup group2 = StudyGroup.builder()
                .course(course)
                .name("Spring Boot 탐험대")
                .description("Spring Boot와 백엔드 개발 학습 스터디")
                .maxMembers(5)
                .status("ACTIVE")
                .createdBy(students[4])
                .build();
        em.persist(group2);

        // Group 2 members: students[4](leader), [2], [7], [8]
        addGroupMember(group2, students[4], "LEADER", "REST API, Java 전문", "그룹 리더", bd("0.88"));
        addGroupMember(group2, students[2], "MEMBER", "JavaScript, REST API 학습 중", "API 설계 함께 학습", bd("0.75"));
        addGroupMember(group2, students[7], "MEMBER", "HTML/CSS, 기초 Java", "기초부터 함께 성장", bd("0.70"));
        addGroupMember(group2, students[8], "MEMBER", "JavaScript, 동기부여 필요", "실습 파트너", bd("0.72"));

        em.flush();
        log.info("[MockDataSeeder] Created 2 study groups");

        log.info("[MockDataSeeder] Demo data seeding complete! (10 students, full data)");
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private void addGroupMember(StudyGroup group, User student, String role,
                                 String strength, String complement, BigDecimal matchScore) {
        StudyGroupMember member = StudyGroupMember.builder()
                .studyGroup(group)
                .student(student)
                .role(role)
                .strengthSummary(strength)
                .complementNote(complement)
                .matchScore(matchScore)
                .build();
        group.getMembers().add(member);
    }

    private static BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private static BigDecimal bd(double val) {
        return BigDecimal.valueOf(Math.round(val * 100.0) / 100.0).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
