package com.classpulse.ai;

import com.classpulse.ai.TwinDataCollector.TwinInferenceContext;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.learning.Reflection;
import com.classpulse.domain.twin.*;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.consultation.Consultation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Engine 3 - 디지털 트윈 추론 엔진 (v2)
 * 멀티소스 학습 데이터를 종합하여 하이브리드(규칙 기반 + LLM 해석) 방식으로 학생 상태를 추론합니다.
 */
@Slf4j
@Service
public class TwinInferenceEngine {

    private final RestTemplate openAiRestTemplate;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TwinDataCollector dataCollector;
    private final TwinScoreHistoryRepository scoreHistoryRepository;
    private final ObjectMapper objectMapper;

    private static final double MAX_SCORE_DELTA = 25.0;

    public TwinInferenceEngine(
            @Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
            StudentTwinRepository studentTwinRepository,
            SkillMasterySnapshotRepository snapshotRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            TwinDataCollector dataCollector,
            TwinScoreHistoryRepository scoreHistoryRepository,
            ObjectMapper objectMapper) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.studentTwinRepository = studentTwinRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.dataCollector = dataCollector;
        this.scoreHistoryRepository = scoreHistoryRepository;
        this.objectMapper = objectMapper;
    }

    // ── System Prompt ───────────────────────────────────────────────────

    private static final String SYSTEM_PROMPT = """
        당신은 교육 심리학 전문 AI로, 학생의 멀티소스 학습 데이터를 분석하여 종합적인 학습 상태를 해석합니다.

        ## 점수 기준 (0-100 스케일)

        ### 이해도(mastery) - "학생이 개념을 얼마나 이해하고 있는가"
        - 80-100: 핵심 개념을 정확히 이해하고 응용 가능. 성찰일지에서 심층적 분석 능력 보임.
        - 60-79: 기본 개념 이해하나 응용에 어려움. 코드 피드백에서 간헐적 오류.
        - 40-59: 개념 혼동 빈번. 막힌점이 자주 발생하며 기초적 질문 반복.
        - 20-39: 기초 개념 미흡. 코드 오류 빈번하고 자신감 매우 낮음.
        - 0-19: 학습 시작 단계이거나 심각한 이해 부족.

        ### 수행력(execution) - "학습 계획을 실제로 얼마나 실행하는가"
        - 80-100: 복습 완료율 90%+, 코드 제출 활발, 챗봇 적극 활용.
        - 60-79: 복습 대부분 수행하나 일부 누락. 코드 제출 꾸준함.
        - 40-59: 복습 절반 수행. 코드 제출 불규칙.
        - 20-39: 복습 거의 미수행. 활동 저조.
        - 0-19: 거의 모든 학습 활동 미참여.

        ### 동기(motivation) - "학습에 대한 내적 동기와 의지"
        - 80-100: 높은 자신감, 연속 스트릭, XP 빠르게 축적, 성찰 내용 적극적.
        - 60-79: 꾸준한 참여와 보통 수준의 자신감.
        - 40-59: 참여 불규칙, 자신감 변동, 스트릭 깨짐 이력.
        - 20-39: 낮은 자신감, 활동 감소 추세, 부정적 감정 표현.
        - 0-19: 학습 포기 징후.

        ### 망각위험(retention_risk) - "학습한 내용을 잊어버릴 위험도" (높을수록 나쁨)
        - 80-100: 복습 미수행, 장기간 비활동, 이전에 알던 개념도 혼동.
        - 60-79: 복습 불규칙, 활동 간격 김, 이전 오류 반복.
        - 40-59: 복습 어느 정도 수행하나 충분하지 않음.
        - 20-39: 정기적 복습, 코드 실습으로 보강.
        - 0-19: 스페이스드 리피티션 충실히 수행, 망각 위험 낮음.

        ### 상담필요도(consultation_need) - "강사 상담이 얼마나 필요한가" (높을수록 긴급)
        - 80-100: 복합적 어려움, 학습 정체 심각, 즉각적 개입 필요.
        - 60-79: 특정 영역 어려움 지속, 상담으로 돌파 가능.
        - 40-59: 일부 약점 있으나 자기주도로 해결 가능할 수 있음.
        - 20-39: 대체로 양호하나 예방적 상담 고려.
        - 0-19: 상담 불필요, 자기주도 학습 잘 진행.

        ## 분석 시 필수 고려사항
        1. 데이터 소스 간 불일치를 반드시 식별하세요 (예: 자신감은 높은데 코드 오류가 많으면 과잉 자신감).
        2. 이전 점수 대비 변화 추세를 파악하세요 (개선/정체/악화).
        3. 코드 제출 패턴에서 실제 실력을 유추하세요 (GOOD/WARNING/ERROR 비율).
        4. 챗봇 대화에서 질문 빈도와 적극성을 고려하세요.
        5. 게이미피케이션 데이터에서 학습 일관성을 판단하세요 (스트릭, XP 속도).
        6. 감정적 톤과 수치적 데이터가 다른 경우 양쪽 모두 언급하세요.

        ## 응답 형식 (반드시 이 JSON 형식으로)
        {
          "ai_insight": "학생의 현재 학습 상태에 대한 종합 분석 (3-5문장, 한국어). 반드시 (1)현재 상태 요약, (2)주요 강점, (3)개선이 필요한 영역, (4)구체적 권고를 포함하세요.",
          "trend_analysis": {
            "direction": "IMPROVING | STABLE | DECLINING",
            "explanation": "추세 판단 근거 1-2문장"
          },
          "data_conflicts": [
            "데이터 간 불일치 사항 (있을 경우)"
          ],
          "skill_assessments": [
            {
              "skill_name": "스킬 이름",
              "understanding_score": 0.0-100.0,
              "practice_score": 0.0-100.0,
              "confidence_score": 0.0-100.0,
              "forgetting_risk_score": 0.0-100.0
            }
          ],
          "adjustment_suggestions": {
            "mastery_adjustment": -10.0 ~ 10.0,
            "execution_adjustment": -10.0 ~ 10.0,
            "motivation_adjustment": -10.0 ~ 10.0,
            "retention_risk_adjustment": -10.0 ~ 10.0,
            "consultation_need_adjustment": -10.0 ~ 10.0,
            "reasoning": "각 조정의 구체적 근거"
          }
        }
        """;

    // ── Main inference method ───────────────────────────────────────────

    @Transactional
    public Map<String, Object> infer(Long studentId, Long courseId) {
        return infer(studentId, courseId, "REFLECTION");
    }

    @Transactional
    public Map<String, Object> infer(Long studentId, Long courseId, String inferenceSource) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // ── Collect all data sources ────────────────────────────────────
        TwinInferenceContext ctx = dataCollector.collect(studentId, courseId);

        // ── Rule-based score calculation ────────────────────────────────

        // Mastery: confidence + review rate + code quality + skill understanding
        double avgSkillUnderstanding = ctx.latestSkillSnapshots().stream()
                .mapToDouble(s -> s.getUnderstandingScore().doubleValue())
                .average().orElse(50.0);
        double masteryScore = (ctx.avgConfidence() / 5.0) * 30.0
                + ctx.reviewCompletionRate() * 25.0
                + ctx.codeAnalysis().goodRate() * 25.0
                + avgSkillUnderstanding * 0.20;

        // Execution: review rate + code submission frequency + skill practice + chat engagement
        double codeSubmissionFreq = Math.min(100.0, ctx.codeAnalysis().totalSubmissions() * 10.0);
        double chatEngagementScore = Math.min(100.0, ctx.chatEngagement().totalUserMessages() * 5.0);
        double avgSkillPractice = ctx.latestSkillSnapshots().stream()
                .mapToDouble(s -> s.getPracticeScore().doubleValue())
                .average().orElse(50.0);
        double executionScore = ctx.reviewCompletionRate() * 35.0
                + codeSubmissionFreq * 0.25
                + avgSkillPractice * 0.20
                + chatEngagementScore * 0.20;

        // Motivation: confidence + activity + streak + XP velocity
        double activityScore = Math.min(100.0, ctx.recentReflections().size() * 10.0);
        double streakScore = Math.min(25.0, ctx.gamification().streakDays() * 3.0);
        double xpVelocityScore = Math.min(25.0, ctx.gamification().weeklyXp() / 4.0);
        double motivationScore = (ctx.avgConfidence() / 5.0) * 25.0
                + activityScore * 0.25
                + streakScore
                + xpVelocityScore;

        // Retention risk: penalize low review, stuck points, low code quality, inactivity
        long daysSinceLastActivity = ctx.gamification().lastActivityDate() != null
                ? ChronoUnit.DAYS.between(ctx.gamification().lastActivityDate(), LocalDate.now())
                : 30;
        double activityProximityBonus = daysSinceLastActivity < 3 ? 15.0 : daysSinceLastActivity < 7 ? 7.0 : 0.0;
        double streakBonus = ctx.gamification().streakDays() > 0 ? 10.0 : 0.0;
        double retentionRisk = Math.max(0, 100.0
                - ctx.reviewCompletionRate() * 30.0
                - (5 - ctx.stuckCount()) * 6.0
                - ctx.codeAnalysis().goodRate() * 20.0
                - activityProximityBonus
                - streakBonus);

        // Consultation need
        double consultationNeed = ctx.stuckCount() * 15.0
                + (5.0 - ctx.avgConfidence()) * 10.0
                + (ctx.hasRecentConsultation() ? 0.0 : 15.0)
                + (ctx.codeAnalysis().goodRate() < 0.4 ? 10.0 : 0.0)
                + (motivationScore < 30 ? 5.0 : 0.0);
        consultationNeed = Math.min(100.0, Math.max(0.0, consultationNeed));

        // ── Build LLM prompt ────────────────────────────────────────────

        String userPrompt = buildUserPrompt(student, course, ctx,
                masteryScore, executionScore, retentionRisk, motivationScore, consultationNeed);

        Map<String, Object> gptResponse = callGpt(SYSTEM_PROMPT, userPrompt);

        // ── Validate and apply LLM adjustments ─────────────────────────

        validateLlmResponse(gptResponse);

        @SuppressWarnings("unchecked")
        Map<String, Object> adjustments = (Map<String, Object>) gptResponse
                .getOrDefault("adjustment_suggestions", Map.of());

        double finalMastery = clamp(masteryScore + toDouble(adjustments.get("mastery_adjustment")));
        double finalExecution = clamp(executionScore + toDouble(adjustments.get("execution_adjustment")));
        double finalRetention = clamp(retentionRisk + toDouble(adjustments.get("retention_risk_adjustment")));
        double finalMotivation = clamp(motivationScore + toDouble(adjustments.get("motivation_adjustment")));
        double finalConsultation = clamp(consultationNeed + toDouble(adjustments.get("consultation_need_adjustment")));
        double overallRisk = (finalRetention * 0.3 + (100 - finalMotivation) * 0.2
                + finalConsultation * 0.2 + (100 - finalMastery) * 0.3);

        // ── Anomaly detection: cap large score swings ───────────────────

        if (ctx.previousTwin() != null) {
            finalMastery = capDelta(ctx.previousTwin().getMasteryScore().doubleValue(), finalMastery);
            finalExecution = capDelta(ctx.previousTwin().getExecutionScore().doubleValue(), finalExecution);
            finalRetention = capDelta(ctx.previousTwin().getRetentionRiskScore().doubleValue(), finalRetention);
            finalMotivation = capDelta(ctx.previousTwin().getMotivationScore().doubleValue(), finalMotivation);
            finalConsultation = capDelta(ctx.previousTwin().getConsultationNeedScore().doubleValue(), finalConsultation);
            overallRisk = (finalRetention * 0.3 + (100 - finalMotivation) * 0.2
                    + finalConsultation * 0.2 + (100 - finalMastery) * 0.3);
        }

        String aiInsight = (String) gptResponse.getOrDefault("ai_insight", "분석 결과를 생성할 수 없습니다.");

        // Extract trend analysis
        @SuppressWarnings("unchecked")
        Map<String, Object> trendAnalysis = (Map<String, Object>) gptResponse
                .getOrDefault("trend_analysis", Map.of());
        String trendDirection = (String) trendAnalysis.getOrDefault("direction", "STABLE");
        String trendExplanation = (String) trendAnalysis.getOrDefault("explanation", "");

        @SuppressWarnings("unchecked")
        List<String> dataConflicts = (List<String>) gptResponse.getOrDefault("data_conflicts", List.of());

        // ── Update StudentTwin ──────────────────────────────────────────

        StudentTwin twin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(StudentTwin.builder().student(student).course(course).build());

        twin.setMasteryScore(toBigDecimal(finalMastery));
        twin.setExecutionScore(toBigDecimal(finalExecution));
        twin.setRetentionRiskScore(toBigDecimal(finalRetention));
        twin.setMotivationScore(toBigDecimal(finalMotivation));
        twin.setConsultationNeedScore(toBigDecimal(finalConsultation));
        twin.setOverallRiskScore(toBigDecimal(overallRisk));
        twin.setAiInsight(aiInsight);
        twin.setTrendDirection(trendDirection);
        twin.setTrendExplanation(trendExplanation);
        twin.setDataConflictsJson(dataConflicts.isEmpty() ? null : dataConflicts);
        twin.setInferenceSource(inferenceSource);
        studentTwinRepository.save(twin);

        // ── Record score history ────────────────────────────────────────

        TwinScoreHistory history = TwinScoreHistory.builder()
                .student(student).course(course)
                .masteryScore(toBigDecimal(finalMastery))
                .executionScore(toBigDecimal(finalExecution))
                .retentionRiskScore(toBigDecimal(finalRetention))
                .motivationScore(toBigDecimal(finalMotivation))
                .consultationNeedScore(toBigDecimal(finalConsultation))
                .overallRiskScore(toBigDecimal(overallRisk))
                .inferenceSource(inferenceSource)
                .build();
        scoreHistoryRepository.save(history);

        // ── Create SkillMasterySnapshots ─────────────────────────────────

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skillAssessments =
                (List<Map<String, Object>>) gptResponse.getOrDefault("skill_assessments", List.of());

        Map<String, CurriculumSkill> skillNameMap = ctx.courseSkills().stream()
                .collect(Collectors.toMap(CurriculumSkill::getName, s -> s, (a, b) -> a));

        for (Map<String, Object> sa : skillAssessments) {
            String skillName = (String) sa.get("skill_name");
            CurriculumSkill skill = skillNameMap.get(skillName);
            if (skill == null) continue;

            SkillMasterySnapshot snapshot = SkillMasterySnapshot.builder()
                    .student(student)
                    .course(course)
                    .skill(skill)
                    .understandingScore(toBigDecimal(toDouble(sa.get("understanding_score"))))
                    .practiceScore(toBigDecimal(toDouble(sa.get("practice_score"))))
                    .confidenceScore(toBigDecimal(toDouble(sa.get("confidence_score"))))
                    .forgettingRiskScore(toBigDecimal(toDouble(sa.get("forgetting_risk_score"))))
                    .sourceType("TWIN_INFERENCE")
                    .build();
            snapshotRepository.save(snapshot);
        }

        log.info("트윈 추론 완료 - studentId={}, courseId={}, source={}, overallRisk={}, trend={}",
                studentId, courseId, inferenceSource, String.format("%.1f", overallRisk), trendDirection);

        return Map.ofEntries(
                Map.entry("studentId", studentId),
                Map.entry("courseId", courseId),
                Map.entry("masteryScore", finalMastery),
                Map.entry("executionScore", finalExecution),
                Map.entry("retentionRiskScore", finalRetention),
                Map.entry("motivationScore", finalMotivation),
                Map.entry("consultationNeedScore", finalConsultation),
                Map.entry("overallRiskScore", overallRisk),
                Map.entry("aiInsight", aiInsight),
                Map.entry("trendDirection", trendDirection),
                Map.entry("skillAssessments", skillAssessments)
        );
    }

    // ── Build user prompt ───────────────────────────────────────────────

    private String buildUserPrompt(User student, Course course, TwinInferenceContext ctx,
                                    double mastery, double execution, double retention,
                                    double motivation, double consultation) {

        String recentReflections = ctx.recentReflections().stream().limit(5)
                .map(r -> String.format("- 자신감: %d/5, 내용: %s, 막힌점: %s",
                        r.getSelfConfidenceScore(),
                        truncate(r.getContent(), 100),
                        r.getStuckPoint() != null ? r.getStuckPoint() : "없음"))
                .collect(Collectors.joining("\n"));

        String consultationHistory = ctx.recentConsultations().stream().limit(3)
                .map(c -> String.format("- 일자: %s, 상태: %s, 메모: %s",
                        c.getScheduledAt().toLocalDate(),
                        c.getStatus(),
                        c.getNotes() != null ? truncate(c.getNotes(), 80) : "없음"))
                .collect(Collectors.joining("\n"));

        String skillList = ctx.courseSkills().stream()
                .map(s -> s.getName() + " (" + s.getDifficulty() + ")")
                .collect(Collectors.joining(", "));

        // Previous scores for trend comparison
        String previousScores = "없음 (첫 번째 추론)";
        if (ctx.previousTwin() != null) {
            StudentTwin prev = ctx.previousTwin();
            previousScores = String.format(
                    "이해도: %.1f → 현재: %.1f, 수행력: %.1f → 현재: %.1f, 동기: %.1f → 현재: %.1f, 망각위험: %.1f → 현재: %.1f",
                    prev.getMasteryScore().doubleValue(), mastery,
                    prev.getExecutionScore().doubleValue(), execution,
                    prev.getMotivationScore().doubleValue(), motivation,
                    prev.getRetentionRiskScore().doubleValue(), retention);
        }

        return String.format("""
                ## 학생 정보
                - 이름: %s
                - 강의: %s

                ## 규칙 기반 계산 점수
                - 이해도(mastery): %.1f
                - 수행력(execution): %.1f
                - 망각위험(retention_risk): %.1f
                - 동기(motivation): %.1f
                - 상담필요도(consultation_need): %.1f

                ## 최근 성찰일지 (최대 5건)
                %s

                ## 복습 현황 (최근 30일)
                - 총 복습 과제: %d건
                - 완료: %d건
                - 완료율: %.1f%%

                ## 상담 이력 (최근 3건)
                %s

                ## 코드 제출 분석
                - 총 제출: %d건
                - 피드백 분포: GOOD %d건, WARNING %d건, ERROR %d건
                - GOOD 비율: %.1f%%

                ## 챗봇 활용 현황
                - 총 대화 세션: %d건
                - 학생 메시지 수: %d건
                - 마지막 대화: %s

                ## 게이미피케이션 데이터
                - 레벨: %d (%s)
                - 총 XP: %d
                - 현재 스트릭: %d일
                - 주간 XP 획득: %d

                ## 이전 평가 점수 (추세 비교용)
                %s

                ## 커리큘럼 스킬 목록
                %s

                위 데이터를 종합 분석하여 학생의 학습 상태 인사이트, 추세 분석, 데이터 불일치, 스킬별 평가, 점수 조정 제안을 JSON으로 반환하세요.
                """,
                student.getName(),
                course.getTitle(),
                mastery, execution, retention, motivation, consultation,
                recentReflections.isEmpty() ? "없음" : recentReflections,
                ctx.totalReviews(), ctx.completedReviews(),
                ctx.totalReviews() > 0 ? ctx.reviewCompletionRate() * 100 : 0.0,
                consultationHistory.isEmpty() ? "없음" : consultationHistory,
                ctx.codeAnalysis().totalSubmissions(),
                ctx.codeAnalysis().goodCount(), ctx.codeAnalysis().warningCount(), ctx.codeAnalysis().errorCount(),
                ctx.codeAnalysis().goodRate() * 100,
                ctx.chatEngagement().totalConversations(),
                ctx.chatEngagement().totalUserMessages(),
                ctx.chatEngagement().lastConversationDate() != null
                        ? ctx.chatEngagement().lastConversationDate().toLocalDate().toString() : "없음",
                ctx.gamification().level(), ctx.gamification().levelTitle(),
                ctx.gamification().totalXp(),
                ctx.gamification().streakDays(),
                ctx.gamification().weeklyXp(),
                previousScores,
                skillList
        );
    }

    // ── LLM call ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> callGpt(String systemPrompt, String userPrompt) {
        var messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        var body = Map.of(
                "model", "gpt-4o",
                "messages", messages,
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.3
        );
        Map<String, Object> response = openAiRestTemplate.postForObject(
                "/chat/completions", body, Map.class);

        String content = extractContent(response);
        try {
            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패: {}", content, e);
            throw new RuntimeException("GPT 응답 JSON 파싱 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    // ── Validation ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void validateLlmResponse(Map<String, Object> response) {
        // Validate adjustment_suggestions range [-10, +10]
        Map<String, Object> adjustments = (Map<String, Object>) response.get("adjustment_suggestions");
        if (adjustments != null) {
            for (String key : List.of("mastery_adjustment", "execution_adjustment",
                    "motivation_adjustment", "retention_risk_adjustment", "consultation_need_adjustment")) {
                Object val = adjustments.get(key);
                if (val != null) {
                    double d = toDouble(val);
                    if (d < -10.0 || d > 10.0) {
                        log.warn("LLM adjustment out of range: {}={}, clamping to [-10,10]", key, d);
                        adjustments.put(key, Math.min(10.0, Math.max(-10.0, d)));
                    }
                }
            }
        }

        // Validate skill_assessments scores [0, 100]
        List<Map<String, Object>> skills = (List<Map<String, Object>>) response.get("skill_assessments");
        if (skills != null) {
            for (Map<String, Object> skill : skills) {
                for (String key : List.of("understanding_score", "practice_score",
                        "confidence_score", "forgetting_risk_score")) {
                    Object val = skill.get(key);
                    if (val != null) {
                        double d = toDouble(val);
                        if (d < 0 || d > 100) {
                            skill.put(key, Math.min(100.0, Math.max(0.0, d)));
                        }
                    }
                }
            }
        }

        // Validate trend_analysis.direction
        Map<String, Object> trend = (Map<String, Object>) response.get("trend_analysis");
        if (trend != null) {
            String direction = (String) trend.get("direction");
            if (direction == null || !Set.of("IMPROVING", "STABLE", "DECLINING").contains(direction)) {
                trend.put("direction", "STABLE");
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private double capDelta(double previous, double current) {
        double delta = current - previous;
        if (Math.abs(delta) > MAX_SCORE_DELTA) {
            log.warn("Score delta capped: previous={}, current={}, delta={}", previous, current, delta);
            return clamp(previous + Math.signum(delta) * MAX_SCORE_DELTA);
        }
        return current;
    }

    private double clamp(double value) {
        return Math.min(100.0, Math.max(0.0, value));
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
