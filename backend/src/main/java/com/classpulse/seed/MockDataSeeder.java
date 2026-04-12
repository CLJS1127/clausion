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
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    private static final int STUDENT_COUNT = 100;

    @Override
    @Transactional
    public void run(String... args) {
        // Skip if seed data already exists (check for specific marker)
        boolean hasSeedData = userRepository.findByEmail("student001@classpulse.dev").isPresent();
        if (hasSeedData) {
            log.info("[MockDataSeeder] Seed data already exists, skipping.");
            return;
        }

        log.info("[MockDataSeeder] Seeding {} student demo data...", STUDENT_COUNT);

        String encodedPassword = passwordEncoder.encode("password123");
        Random rng = new Random(42);

        // ── Korean name pool ───────────────────────────────────────────
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍", "전"};
        String[] firstNames = {"민준", "서연", "지훈", "은서", "현우", "수빈", "도현", "하은", "태양", "서진",
                "예준", "지우", "시우", "하윤", "주원", "지아", "현서", "채원", "준서", "유나",
                "도윤", "서윤", "건우", "민서", "은우", "소율", "우진", "지민", "재현", "다은",
                "시현", "예린", "승현", "연우", "규민", "하린", "정우", "수아", "지환", "유진",
                "성민", "나윤", "찬영", "이서", "진우", "아인", "동현", "소윤", "태민", "시은"};

        // ── 1. Instructors (3) + Operator (1) ──────────────────────────
        User instructor1 = userRepository.save(User.builder().email("instructor01@classpulse.dev").passwordHash(encodedPassword).name("박지훈(강사)").role(User.Role.INSTRUCTOR).build());
        User instructor2 = userRepository.save(User.builder().email("instructor02@classpulse.dev").passwordHash(encodedPassword).name("이영수(강사)").role(User.Role.INSTRUCTOR).build());
        User instructor3 = userRepository.save(User.builder().email("instructor03@classpulse.dev").passwordHash(encodedPassword).name("김하나(강사)").role(User.Role.INSTRUCTOR).build());
        User operator = userRepository.save(User.builder().email("operator@classpulse.dev").passwordHash(encodedPassword).name("관리자").role(User.Role.OPERATOR).build());
        User[] instructors = {instructor1, instructor2, instructor3};

        // ── 2. Students (100) ──────────────────────────────────────────
        User[] students = new User[STUDENT_COUNT];
        for (int i = 0; i < STUDENT_COUNT; i++) {
            String ln = lastNames[i % lastNames.length];
            String fn = firstNames[i % firstNames.length];
            String email = String.format("student%03d@classpulse.dev", i + 1);
            students[i] = userRepository.save(User.builder().email(email).passwordHash(encodedPassword).name(ln + fn).role(User.Role.STUDENT).build());
        }
        log.info("[MockDataSeeder] Created {} students, 3 instructors, 1 operator", STUDENT_COUNT);

        // ── 3. Courses (3) ─────────────────────────────────────────────
        Course course1 = createCourse("풀스택 웹개발 부트캠프", "HTML/CSS부터 React, Spring Boot까지 16주 완성 풀스택 과정", instructor1,
                new String[][]{
                    {"HTML & CSS 기초", "시맨틱 HTML, Flexbox, Grid 레이아웃 학습"},
                    {"JavaScript 핵심", "변수, 함수, 스코프, 클로저, 비동기 프로그래밍"},
                    {"React 기초", "컴포넌트, Props, State, Hooks 기본 개념"},
                    {"React 심화", "Context API, useReducer, 커스텀 훅, 성능 최적화"},
                    {"Java & Spring 입문", "Java 문법, OOP 원칙, Spring Boot 프로젝트 구조"},
                    {"REST API 설계", "RESTful API 원칙, Spring MVC, JPA 연동"},
                    {"인증 & 보안", "JWT, Spring Security, OAuth2 소셜 로그인"},
                    {"배포 & DevOps", "Docker, CI/CD 파이프라인, 클라우드 배포"},
                });

        Course course2 = createCourse("데이터 분석 with Python", "Pandas, NumPy, Matplotlib를 활용한 데이터 분석 입문", instructor2,
                new String[][]{
                    {"Python 기초", "변수, 자료형, 조건문, 반복문, 함수"},
                    {"NumPy & Pandas", "배열 연산, DataFrame, 데이터 정제"},
                    {"데이터 시각화", "Matplotlib, Seaborn을 활용한 차트 작성"},
                    {"통계 기초", "기술통계, 확률분포, 가설검정"},
                    {"머신러닝 입문", "회귀, 분류, 클러스터링 기초"},
                    {"프로젝트", "실제 데이터셋을 활용한 분석 프로젝트"},
                });

        Course course3 = createCourse("UI/UX 디자인 실무", "Figma 활용 UI 디자인과 사용자 경험 설계", instructor3,
                new String[][]{
                    {"디자인 원칙", "색상 이론, 타이포그래피, 레이아웃 원칙"},
                    {"Figma 기초", "컴포넌트, 오토레이아웃, 스타일 시스템"},
                    {"와이어프레임", "로우/하이 피델리티 와이어프레임 설계"},
                    {"프로토타이핑", "인터랙션, 애니메이션, 사용자 플로우"},
                    {"UX 리서치", "사용성 테스트, 히트맵, A/B 테스팅"},
                });

        Course[] courses = {course1, course2, course3};

        // ── 4. Curriculum Skills per course ─────────────────────────────
        List<CurriculumSkill> skills1 = createSkills(course1, new String[][]{
            {"HTML/CSS", "시맨틱 마크업과 반응형 레이아웃 구현 능력", "EASY"},
            {"JavaScript 기초", "변수, 함수, 스코프, 비동기 처리 이해", "EASY"},
            {"React 컴포넌트", "함수형 컴포넌트, Hooks, 상태관리 패턴", "MEDIUM"},
            {"재귀 함수", "재귀적 문제 분해와 호출 스택 이해", "HARD"},
            {"REST API", "RESTful 설계 원칙과 HTTP 메서드 활용", "MEDIUM"},
            {"Spring Security", "인증/인가 메커니즘과 JWT 토큰 관리", "HARD"},
        });
        skills1.get(2).getPrerequisites().add(skills1.get(1));
        skills1.get(4).getPrerequisites().add(skills1.get(1));
        skills1.get(5).getPrerequisites().add(skills1.get(4));

        List<CurriculumSkill> skills2 = createSkills(course2, new String[][]{
            {"Python 문법", "변수, 자료형, 함수, 클래스 활용", "EASY"},
            {"Pandas", "DataFrame 조작, 그룹핑, 피벗 테이블", "MEDIUM"},
            {"데이터 시각화", "Matplotlib/Seaborn 차트 작성", "MEDIUM"},
            {"통계 분석", "기술통계, 확률분포, 가설검정", "HARD"},
            {"머신러닝 기초", "Scikit-learn을 활용한 모델 학습", "HARD"},
        });

        List<CurriculumSkill> skills3 = createSkills(course3, new String[][]{
            {"색상/타이포", "색상 이론, 서체 선택, 시각적 위계", "EASY"},
            {"Figma 도구", "컴포넌트, 오토레이아웃, 변수", "MEDIUM"},
            {"와이어프레임", "정보 구조, 네비게이션 설계", "MEDIUM"},
            {"프로토타이핑", "인터랙션, 마이크로 애니메이션", "HARD"},
        });

        em.flush();
        List<List<CurriculumSkill>> allSkills = List.of(skills1, skills2, skills3);

        // ── 5. Enrollments: 100 students across 3 courses ──────────────
        // Students 0-69: course1, Students 30-89: course2, Students 60-99: course3
        for (int i = 0; i < STUDENT_COUNT; i++) {
            if (i < 70) enroll(students[i], course1, "ACTIVE");
            if (i >= 30 && i < 90) enroll(students[i], course2, "ACTIVE");
            if (i >= 60) enroll(students[i], course3, i >= 95 ? "PENDING" : "ACTIVE");
        }
        log.info("[MockDataSeeder] Created enrollments");

        // ── 6. Student Twins for all students ──────────────────────────
        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            Course c = courses[courseIdx];
            double factor = rng.nextDouble();
            // Create realistic distribution: 20% high risk, 30% medium, 50% low
            double mastery, execution, retRisk, motivation, consultNeed, overallRisk;
            if (factor < 0.20) { // high risk
                mastery = 25 + rng.nextDouble() * 20;
                execution = 20 + rng.nextDouble() * 20;
                retRisk = 60 + rng.nextDouble() * 25;
                motivation = 25 + rng.nextDouble() * 20;
                consultNeed = 65 + rng.nextDouble() * 20;
                overallRisk = 60 + rng.nextDouble() * 20;
            } else if (factor < 0.50) { // medium
                mastery = 50 + rng.nextDouble() * 20;
                execution = 45 + rng.nextDouble() * 25;
                retRisk = 30 + rng.nextDouble() * 25;
                motivation = 50 + rng.nextDouble() * 20;
                consultNeed = 35 + rng.nextDouble() * 25;
                overallRisk = 35 + rng.nextDouble() * 20;
            } else { // low risk
                mastery = 72 + rng.nextDouble() * 25;
                execution = 70 + rng.nextDouble() * 25;
                retRisk = 5 + rng.nextDouble() * 20;
                motivation = 75 + rng.nextDouble() * 22;
                consultNeed = 5 + rng.nextDouble() * 20;
                overallRisk = 8 + rng.nextDouble() * 20;
            }

            String insight = generateInsight(students[i].getName(), mastery, overallRisk, allSkills.get(courseIdx), rng);

            studentTwinRepository.save(StudentTwin.builder()
                    .student(students[i]).course(c)
                    .masteryScore(bd(mastery)).executionScore(bd(execution))
                    .retentionRiskScore(bd(retRisk)).motivationScore(bd(motivation))
                    .consultationNeedScore(bd(consultNeed)).overallRiskScore(bd(overallRisk))
                    .aiInsight(insight)
                    .build());
        }
        log.info("[MockDataSeeder] Created {} student twins", STUDENT_COUNT);

        // ── 7. Skill Mastery Snapshots ─────────────────────────────────
        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            for (CurriculumSkill skill : sk) {
                int count = 2 + rng.nextInt(2);
                for (int s = 0; s < count; s++) {
                    double base = 30 + rng.nextDouble() * 60;
                    snapshotRepository.save(SkillMasterySnapshot.builder()
                            .student(students[i]).course(courses[courseIdx]).skill(skill)
                            .understandingScore(bd(clamp(base + rng.nextGaussian() * 10, 5, 98)))
                            .practiceScore(bd(clamp(base - 5 + rng.nextGaussian() * 12, 5, 98)))
                            .confidenceScore(bd(clamp(base + 2 + rng.nextGaussian() * 10, 5, 98)))
                            .forgettingRiskScore(bd(clamp(90 - base + rng.nextGaussian() * 10, 2, 90)))
                            .sourceType(s == 0 ? "REFLECTION_ANALYSIS" : "CODE_ANALYSIS")
                            .build());
                }
            }
        }
        log.info("[MockDataSeeder] Created skill mastery snapshots");

        // ── 8. Review Tasks (3-6 per student) ──────────────────────────
        String[][] taskTemplates = {
            {"기본 개념 복습", "이해도가 낮아 기본 개념부터 복습이 필요합니다."},
            {"실습 과제 재도전", "실습 점수가 낮아 추가 연습이 필요합니다."},
            {"심화 문제 풀이", "기초는 탄탄하지만 심화 문제 연습이 필요합니다."},
            {"코드 리팩토링 연습", "코드 품질 향상을 위한 리팩토링 연습을 해보세요."},
            {"개념 정리 노트 작성", "핵심 개념을 정리하여 장기 기억으로 전환하세요."},
            {"페어 프로그래밍 참여", "동료와 함께 문제를 풀어보세요."},
        };
        String[] taskStatuses = {"PENDING", "PENDING", "IN_PROGRESS", "COMPLETED", "COMPLETED", "SKIPPED"};

        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            int taskCount = 3 + rng.nextInt(4);
            for (int t = 0; t < taskCount; t++) {
                int tmpl = t % taskTemplates.length;
                CurriculumSkill skill = sk.get(rng.nextInt(sk.size()));
                String status = taskStatuses[t % taskStatuses.length];
                LocalDate scheduled = LocalDate.now().minusDays(rng.nextInt(14)).plusDays(rng.nextInt(7));
                ReviewTask task = ReviewTask.builder()
                        .student(students[i]).course(courses[courseIdx]).skill(skill)
                        .title(skill.getName() + " " + taskTemplates[tmpl][0])
                        .reasonSummary(taskTemplates[tmpl][1])
                        .scheduledFor(scheduled).status(status).build();
                if ("COMPLETED".equals(status)) task.setCompletedAt(LocalDateTime.now().minusDays(rng.nextInt(7)));
                reviewTaskRepository.save(task);
            }
        }
        log.info("[MockDataSeeder] Created review tasks");

        // ── 9. Reflections (2-4 per student) ───────────────────────────
        String[][] refTemplates = {
            {"오늘 %s를 배웠는데, 기본 개념은 이해했지만 응용 부분이 아직 어렵다.", "응용 문제에서 막히는 부분", "혼란", "호기심"},
            {"드디어 %s 개념을 이해했다! 실습을 통해 직접 해보니 이론과 다르게 느껴진다.", null, "성취감", "자신감"},
            {"%s 관련 과제를 제출했는데, 피드백에서 몇 가지 개선점이 있었다.", "에러 핸들링 부분", "아쉬움", "의지"},
            {"%s 수업에서 새로운 패턴을 배웠다. 기존에 작성한 코드를 리팩토링해보고 싶다.", null, "흥미", "동기부여"},
        };

        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            int refCount = 2 + rng.nextInt(3);
            for (int r = 0; r < refCount; r++) {
                int tmpl = r % refTemplates.length;
                CurriculumSkill skill = sk.get(rng.nextInt(sk.size()));
                reflectionRepository.save(Reflection.builder()
                        .student(students[i]).course(courses[courseIdx])
                        .content(String.format(refTemplates[tmpl][0], skill.getName()))
                        .stuckPoint(refTemplates[tmpl][1])
                        .selfConfidenceScore(1 + rng.nextInt(5))
                        .emotionSummary(Map.of("primary", refTemplates[tmpl][2], "secondary", refTemplates[tmpl][3]))
                        .aiAnalysisJson(Map.of("knowledgeGaps", List.of(skill.getName() + " 심화"), "strengths", List.of(skill.getName() + " 기본 이해"), "suggestion", skill.getName() + " 관련 추가 실습을 권장합니다."))
                        .build());
            }
        }
        log.info("[MockDataSeeder] Created reflections");

        // ── 10. Consultations (30 total: 10 scheduled, 20 completed) ───
        for (int c = 0; c < 30; c++) {
            User student = students[c * 3 % STUDENT_COUNT];
            User instr = instructors[c % 3];
            Course course = courses[c % 3];
            boolean completed = c >= 10;

            Consultation.ConsultationBuilder cb = Consultation.builder()
                    .student(student).instructor(instr).course(course)
                    .scheduledAt(completed ? LocalDateTime.now().minusDays(1 + c) : LocalDateTime.now().plusDays(1 + c % 7))
                    .status(completed ? "COMPLETED" : "SCHEDULED")
                    .notes(student.getName() + " 학생 학습 상담");

            if (completed) {
                cb.completedAt(LocalDateTime.now().minusDays(c));
                cb.summaryText(student.getName() + " 학생과 학습 진행 상황을 논의함. 취약 영역 보충 계획 수립.");
                cb.causeAnalysis("실습 시간 부족과 기초 개념 미흡이 주요 원인으로 파악됨.");
            } else {
                cb.briefingJson(Map.of("twinSummary", "학습 상담 예정", "suggestedTopics", List.of("학습 현황 점검", "취약 영역 보강")));
            }

            Consultation saved = consultationRepository.save(cb.build());
            if (completed) {
                actionPlanRepository.save(ActionPlan.builder()
                        .consultation(saved).student(student).course(course)
                        .title("복습 과제 수행").description("취약 영역 집중 복습을 수행하세요.")
                        .dueDate(LocalDate.now().plusDays(7)).priority("HIGH").status(c % 3 == 0 ? "COMPLETED" : "IN_PROGRESS").build());
            }
        }
        log.info("[MockDataSeeder] Created 30 consultations with action plans");

        // ── 11. Recommendations (1-3 per student) ──────────────────────
        String[][] recTemplates = {
            {"REVIEW", "%s 집중 복습 추천", "%s 이해도가 낮습니다. 집중 복습을 추천합니다."},
            {"RESOURCE", "%s 실습 자료 추천", "%s 실습 점수 향상을 위한 추가 자료를 추천합니다."},
            {"STUDY_GROUP", "%s 스터디 그룹 참여", "%s 실력 향상을 위해 스터디 그룹 참여를 추천합니다."},
        };
        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            int count = 1 + rng.nextInt(3);
            for (int r = 0; r < count; r++) {
                int tmpl = r % recTemplates.length;
                CurriculumSkill skill = sk.get(rng.nextInt(sk.size()));
                recommendationRepository.save(Recommendation.builder()
                        .student(students[i]).course(courses[courseIdx])
                        .recommendationType(recTemplates[tmpl][0])
                        .title(String.format(recTemplates[tmpl][1], skill.getName()))
                        .reasonSummary(String.format(recTemplates[tmpl][2], skill.getName()))
                        .triggerEvent("TWIN_SCORE_DROP")
                        .evidencePayload(Map.of("skillName", skill.getName(), "studentName", students[i].getName()))
                        .expectedOutcome("학습 성과 향상 예상")
                        .build());
            }
        }
        log.info("[MockDataSeeder] Created recommendations");

        // ── 12. Gamification (all students) ────────────────────────────
        String[] levelTitles = {"초보 학습자", "성장하는 학습자", "열정적 코더", "중급 개발자", "풀스택 학습자",
                                "실력자", "시니어 러너", "코드 마스터", "AI 분석가", "풀스택 마스터"};
        List<Badge> allBadges = badgeRepository.findAll();

        for (int i = 0; i < STUDENT_COUNT; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            int level = 1 + rng.nextInt(12);
            int totalXp = level * 250 + rng.nextInt(500);
            int streak = rng.nextInt(30);

            gamificationRepository.save(StudentGamification.builder()
                    .student(students[i]).course(courses[courseIdx])
                    .level(level).currentXp(rng.nextInt(200)).nextLevelXp(100 + level * 30)
                    .levelTitle(levelTitles[Math.min(level - 1, levelTitles.length - 1)])
                    .streakDays(streak).lastActivityDate(streak > 0 ? LocalDate.now() : LocalDate.now().minusDays(3 + rng.nextInt(10)))
                    .totalXpEarned(totalXp).build());

            // Badges: assign proportional to level
            int badgeCount = Math.min(level / 2, allBadges.size());
            for (int b = 0; b < badgeCount; b++) {
                studentBadgeRepository.save(StudentBadge.builder().student(students[i]).badge(allBadges.get(b)).build());
            }

            // XP Events (5-15)
            String[] eventTypes = {"REFLECTION_SUBMIT", "REVIEW_COMPLETE", "CODE_SUBMIT", "CONSULTATION_ATTEND", "CHATBOT_INTERACTION", "STUDY_GROUP_JOIN"};
            int[] eventXp = {20, 15, 25, 35, 5, 20};
            int eventCount = 5 + rng.nextInt(11);
            for (int e = 0; e < eventCount; e++) {
                int idx = rng.nextInt(eventTypes.length);
                xpEventRepository.save(XPEvent.builder()
                        .student(students[i]).course(courses[courseIdx])
                        .eventType(eventTypes[idx]).xpAmount(eventXp[idx]).sourceType(eventTypes[idx]).build());
            }
        }
        log.info("[MockDataSeeder] Created gamification data");

        // ── 13. Chatbot Conversations (first 30 students) ─────────────
        for (int i = 0; i < 30; i++) {
            int courseIdx = i < 70 ? 0 : (i < 90 ? 1 : 2);
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            CurriculumSkill skill = sk.get(rng.nextInt(sk.size()));

            Conversation conv = conversationRepository.save(Conversation.builder()
                    .student(students[i]).course(courses[courseIdx])
                    .title(skill.getName() + " 학습 도우미").status("ACTIVE")
                    .twinContextJson(Map.of("currentSkill", skill.getName()))
                    .build());

            chatMessageRepository.save(ChatMessage.builder().conversation(conv).role("USER").content(skill.getName() + "에서 이해가 안 되는 부분이 있어요.").tokenCount(30).build());
            chatMessageRepository.save(ChatMessage.builder().conversation(conv).role("ASSISTANT").content("물론이죠! " + skill.getName() + "의 어떤 부분이 어려우신가요? 기본 개념부터 차근차근 설명해드릴게요.").tokenCount(80).build());
            chatMessageRepository.save(ChatMessage.builder().conversation(conv).role("USER").content("실습에서 자꾸 에러가 나요. 어떻게 디버깅해야 할까요?").tokenCount(40).build());
            chatMessageRepository.save(ChatMessage.builder().conversation(conv).role("ASSISTANT").content("디버깅은 체계적으로 접근하면 됩니다. 먼저 에러 메시지를 꼼꼼히 읽어보세요. 그 다음 관련 코드의 입력값과 출력값을 확인해보면 원인을 찾을 수 있어요.").tokenCount(120).build());
        }
        log.info("[MockDataSeeder] Created chatbot conversations");

        // ── 14. Code Submissions (first 50 students) ───────────────────
        String[][] codeTemplates = {
            {"javascript", "function fibonacci(n) {\n  if (n <= 1) return n;\n  return fibonacci(n - 1) + fibonacci(n - 2);\n}"},
            {"javascript", "const fetchData = async (url) => {\n  const response = await fetch(url);\n  return await response.json();\n}"},
            {"python", "def merge_sort(arr):\n    if len(arr) <= 1:\n        return arr\n    mid = len(arr) // 2\n    return merge(merge_sort(arr[:mid]), merge_sort(arr[mid:]))"},
            {"java", "@GetMapping(\"/api/users\")\npublic List<User> getUsers() {\n  return userRepository.findAll();\n}"},
            {"java", "public int binarySearch(int[] arr, int target) {\n  int lo = 0, hi = arr.length - 1;\n  while (lo <= hi) {\n    int mid = (lo + hi) / 2;\n    if (arr[mid] == target) return mid;\n    else if (arr[mid] < target) lo = mid + 1;\n    else hi = mid - 1;\n  }\n  return -1;\n}"},
        };
        String[][] fbTemplates = {
            {"GOOD", "코드 구조가 깔끔합니다.", "현재 상태를 유지하세요."},
            {"WARNING", "에러 핸들링이 부족합니다.", "try-catch 블록을 추가하세요."},
            {"ERROR", "무한 재귀의 위험이 있습니다.", "기저 조건을 명확히 설정하세요."},
            {"INFO", "성능 최적화의 여지가 있습니다.", "메모이제이션을 고려해보세요."},
            {"GOOD", "RESTful 설계 원칙을 잘 따르고 있습니다.", "계속 이 패턴을 유지하세요."},
        };

        for (int i = 0; i < 50; i++) {
            int courseIdx = i < 70 ? 0 : 1;
            List<CurriculumSkill> sk = allSkills.get(courseIdx);
            int subCount = 2 + rng.nextInt(4);
            for (int s = 0; s < subCount; s++) {
                int cIdx = s % codeTemplates.length;
                CurriculumSkill skill = sk.get(rng.nextInt(sk.size()));
                CodeSubmission submission = codeSubmissionRepository.save(CodeSubmission.builder()
                        .student(students[i]).course(courses[courseIdx]).skill(skill)
                        .codeContent(codeTemplates[cIdx][1]).language(codeTemplates[cIdx][0]).status("ANALYZED").build());
                int fbCount = 1 + rng.nextInt(3);
                for (int f = 0; f < fbCount; f++) {
                    int fbIdx = (s + f) % fbTemplates.length;
                    codeFeedbackRepository.save(CodeFeedback.builder()
                            .submission(submission).lineNumber(1 + rng.nextInt(4)).endLineNumber(2 + rng.nextInt(4))
                            .severity(fbTemplates[fbIdx][0]).message(fbTemplates[fbIdx][1]).suggestion(fbTemplates[fbIdx][2])
                            .twinLinked(rng.nextBoolean()).twinSkill(rng.nextBoolean() ? skill : null).build());
                }
            }
        }
        log.info("[MockDataSeeder] Created code submissions");

        // ── 15. Study Groups (10 groups) ───────────────────────────────
        String[][] groupData = {
            {"React 마스터즈", "React 심화 학습 그룹"},
            {"Spring Boot 탐험대", "백엔드 개발 학습 그룹"},
            {"알고리즘 챌린저스", "매일 알고리즘 문제 풀기"},
            {"풀스택 프로젝트팀", "실전 프로젝트 진행"},
            {"JavaScript 딥다이브", "JS 핵심 개념 심화 학습"},
            {"Python 데이터팀", "데이터 분석 프로젝트 팀"},
            {"UI/UX 디자인 랩", "디자인 포트폴리오 제작"},
            {"코드 리뷰 클럽", "서로의 코드를 리뷰하는 모임"},
            {"취업 준비반", "포트폴리오와 면접 준비"},
            {"새벽 코딩 크루", "매일 아침 1시간 코딩 습관"},
        };

        for (int g = 0; g < groupData.length; g++) {
            int courseIdx = g < 5 ? 0 : (g < 8 ? 1 : 2);
            int leaderIdx = g * 10;
            StudyGroup group = StudyGroup.builder()
                    .course(courses[courseIdx]).name(groupData[g][0]).description(groupData[g][1])
                    .maxMembers(5).status("ACTIVE").createdBy(students[leaderIdx]).build();
            em.persist(group);

            addGroupMember(group, students[leaderIdx], "LEADER", "그룹 리더", "전반적 학습 지원", bd("0.95"));
            for (int m = 1; m <= 3; m++) {
                int memberIdx = leaderIdx + m;
                if (memberIdx < STUDENT_COUNT) {
                    addGroupMember(group, students[memberIdx], "MEMBER", "적극 참여자", "상호 보완 학습", bd(0.70 + rng.nextDouble() * 0.25));
                }
            }
        }
        em.flush();
        log.info("[MockDataSeeder] Created 10 study groups");

        log.info("[MockDataSeeder] Demo data seeding complete! ({} students, 3 courses, full data)", STUDENT_COUNT);
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private Course createCourse(String title, String desc, User instructor, String[][] weeks) {
        Course course = Course.builder().title(title).description(desc).status("ACTIVE").createdBy(instructor).build();
        for (int i = 0; i < weeks.length; i++) {
            course.getWeeks().add(CourseWeek.builder().course(course).weekNo(i + 1).title(weeks[i][0]).summary(weeks[i][1]).build());
        }
        em.persist(course);
        return course;
    }

    private List<CurriculumSkill> createSkills(Course course, String[][] data) {
        List<CurriculumSkill> skills = new ArrayList<>();
        for (String[] sd : data) {
            CurriculumSkill skill = CurriculumSkill.builder().course(course).name(sd[0]).description(sd[1]).difficulty(sd[2]).build();
            em.persist(skill);
            skills.add(skill);
        }
        return skills;
    }

    private void enroll(User student, Course course, String status) {
        courseEnrollmentRepository.save(CourseEnrollment.builder().course(course).student(student).status(status).build());
    }

    private String generateInsight(String name, double mastery, double risk, List<CurriculumSkill> skills, Random rng) {
        CurriculumSkill weak = skills.get(rng.nextInt(skills.size()));
        CurriculumSkill strong = skills.get(rng.nextInt(skills.size()));
        if (risk >= 60) {
            return String.format("%s 학생은 전반적 이해도가 %.0f%%로 낮고 위험도가 %.0f%%입니다. 특히 %s에서 어려움을 겪고 있어 즉각적인 학습 개입이 필요합니다.", name, mastery, risk, weak.getName());
        } else if (risk >= 35) {
            return String.format("%s 학생은 중간 수준의 이해도(%.0f%%)를 보입니다. %s는 양호하나 %s에서 추가 학습이 필요합니다.", name, mastery, strong.getName(), weak.getName());
        } else {
            return String.format("%s 학생은 우수한 학습 성과(이해도 %.0f%%)를 보이고 있습니다. %s에서 특히 뛰어나며, 스터디 그룹 리더 역할이 적합합니다.", name, mastery, strong.getName());
        }
    }

    private void addGroupMember(StudyGroup group, User student, String role, String strength, String complement, BigDecimal matchScore) {
        group.getMembers().add(StudyGroupMember.builder().studyGroup(group).student(student).role(role).strengthSummary(strength).complementNote(complement).matchScore(matchScore).build());
    }

    private static BigDecimal bd(String val) { return new BigDecimal(val); }
    private static BigDecimal bd(double val) { return BigDecimal.valueOf(Math.round(val * 100.0) / 100.0).setScale(2, java.math.RoundingMode.HALF_UP); }
    private static double clamp(double val, double min, double max) { return Math.max(min, Math.min(max, val)); }
}
