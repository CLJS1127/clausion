package com.classpulse.ai;

import com.classpulse.domain.consultation.Consultation;
import com.classpulse.domain.consultation.ConsultationRepository;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CurriculumSkillRepository;
import com.classpulse.domain.learning.Reflection;
import com.classpulse.domain.learning.ReflectionRepository;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import com.classpulse.domain.twin.SkillMasterySnapshotRepository;
import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Engine 4 - 상담 코파일럿
 * 상담 전 브리핑(학생 상태 요약, 약점 스킬, 추천 질문)과
 * 상담 후 요약(요약, 원인 분석, 7일 액션 플랜, 과목 추천)을 생성합니다.
 */
@Slf4j
@Service
public class ConsultationCopilot {

    private final RestTemplate openAiRestTemplate;
    private final ConsultationRepository consultationRepository;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final ReflectionRepository reflectionRepository;
    private final CurriculumSkillRepository curriculumSkillRepository;
    private final ObjectMapper objectMapper;

    public ConsultationCopilot(
            @Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
            ConsultationRepository consultationRepository,
            StudentTwinRepository studentTwinRepository,
            SkillMasterySnapshotRepository snapshotRepository,
            ReflectionRepository reflectionRepository,
            CurriculumSkillRepository curriculumSkillRepository,
            ObjectMapper objectMapper) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.consultationRepository = consultationRepository;
        this.studentTwinRepository = studentTwinRepository;
        this.snapshotRepository = snapshotRepository;
        this.reflectionRepository = reflectionRepository;
        this.curriculumSkillRepository = curriculumSkillRepository;
        this.objectMapper = objectMapper;
    }

    private static final String BRIEFING_SYSTEM_PROMPT = """
        당신은 대학 교수의 상담 코파일럿 AI입니다.
        학생의 디지털 트윈 데이터를 분석하여 상담 전 브리핑을 생성합니다.

        반드시 다음 JSON 형식으로 응답하세요:
        {
          "student_summary": "학생의 현재 학습 상태를 3-4문장으로 요약",
          "weak_skills": [
            {
              "skill_name": "약점 스킬 이름",
              "understanding_score": 0-100,
              "concern_reason": "이 스킬이 약점인 이유"
            }
          ],
          "recent_reflections_insight": "최근 성찰일지에서 읽을 수 있는 학생의 심리 상태와 학습 패턴",
          "recommended_questions": [
            {
              "question": "추천 상담 질문",
              "purpose": "이 질문의 의도"
            }
          ],
          "risk_alerts": ["주의해야 할 위험 신호 (해당하는 경우)"]
        }

        브리핑 작성 원칙:
        1. 학생에게 직접 전달되는 것이 아닌, 교수자를 위한 참고 자료입니다.
        2. 데이터에 근거한 객관적 분석을 제공하세요.
        3. 추천 질문은 학생의 실제 상황에 맞는 구체적인 질문이어야 합니다.
        4. 민감한 주제는 부드러운 접근을 제안하세요.
        """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
        당신은 대학 상담 기록 분석 AI입니다.
        상담 메모와 학생의 디지털 트윈 데이터를 분석하여 상담 결과를 구조화합니다.

        반드시 다음 JSON 형식으로 응답하세요:
        {
          "summary": "상담 내용 요약 (3-5문장)",
          "cause_analysis": "학습 어려움의 근본 원인 분석",
          "action_plan": [
            {
              "day": 1,
              "task": "구체적 실행 과제",
              "goal": "이 과제의 목표",
              "skill_focus": "관련 스킬"
            },
            ...7일치
          ],
          "course_recommendations": [
            {
              "title": "추천 과목/자료 제목",
              "reason": "추천 이유",
              "type": "REVIEW | PRACTICE | SUPPLEMENTARY"
            }
          ],
          "follow_up_date_suggestion": "다음 상담 추천 일자 (상담일 기준 며칠 후)",
          "priority_level": "HIGH | MEDIUM | LOW"
        }

        상담 요약 원칙:
        1. 원인 분석은 학습 측면과 동기 측면을 모두 고려하세요.
        2. 7일 액션 플랜은 점진적 난이도 상승 구조로 설계하세요.
        3. 과제는 학생이 30분 이내에 완료할 수 있는 분량이어야 합니다.
        4. 추천 과목/자료는 현실적이고 접근 가능한 것이어야 합니다.
        """;

    @Transactional
    public Map<String, Object> generateBriefing(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found: " + consultationId));

        Long studentId = consultation.getStudent().getId();
        Long courseId = consultation.getCourse().getId();

        StudentTwin twin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);

        List<Reflection> reflections = reflectionRepository
                .findByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);

        List<CurriculumSkill> skills = curriculumSkillRepository.findByCourseId(courseId);

        String twinState = twin != null ? String.format("""
                - 이해도: %.1f, 실행력: %.1f, 망각위험: %.1f
                - 동기: %.1f, 상담필요도: %.1f, 종합위험: %.1f
                - AI 인사이트: %s
                """,
                twin.getMasteryScore(), twin.getExecutionScore(), twin.getRetentionRiskScore(),
                twin.getMotivationScore(), twin.getConsultationNeedScore(), twin.getOverallRiskScore(),
                twin.getAiInsight() != null ? twin.getAiInsight() : "없음"
        ) : "트윈 데이터 없음";

        String recentReflections = reflections.stream().limit(5)
                .map(r -> String.format("  - [자신감 %d/5] %s (막힌점: %s)",
                        r.getSelfConfidenceScore(),
                        truncate(r.getContent(), 120),
                        r.getStuckPoint() != null ? r.getStuckPoint() : "없음"))
                .collect(Collectors.joining("\n"));

        String skillSnapshots = snapshots.stream().limit(10)
                .map(s -> String.format("  - %s: 이해 %.0f, 연습 %.0f, 자신감 %.0f, 망각위험 %.0f",
                        s.getSkill().getName(),
                        s.getUnderstandingScore(), s.getPracticeScore(),
                        s.getConfidenceScore(), s.getForgettingRiskScore()))
                .collect(Collectors.joining("\n"));

        String userPrompt = String.format("""
                ## 학생 정보
                - 이름: %s
                - 강의: %s

                ## 디지털 트윈 상태
                %s

                ## 최근 성찰일지 (최대 5건)
                %s

                ## 스킬별 숙달도 스냅샷
                %s

                ## 커리큘럼 스킬
                %s

                위 정보를 분석하여 상담 전 브리핑을 생성하세요. 추천 질문은 3개 이상 포함하세요.
                """,
                consultation.getStudent().getName(),
                consultation.getCourse().getTitle(),
                twinState,
                recentReflections.isEmpty() ? "없음" : recentReflections,
                skillSnapshots.isEmpty() ? "없음" : skillSnapshots,
                skills.stream().map(CurriculumSkill::getName).collect(Collectors.joining(", "))
        );

        Map<String, Object> briefing = callGpt4o(BRIEFING_SYSTEM_PROMPT, userPrompt);

        // Save briefing to consultation
        consultation.setBriefingJson(briefing);
        consultationRepository.save(consultation);

        log.info("상담 브리핑 생성 완료 - consultationId={}", consultationId);
        return Map.of("consultationId", consultationId, "briefing", briefing);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateSummary(Long consultationId, String notes) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found: " + consultationId));

        Long studentId = consultation.getStudent().getId();
        Long courseId = consultation.getCourse().getId();

        StudentTwin twin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        String twinState = twin != null ? String.format("""
                - 이해도: %.1f, 실행력: %.1f, 망각위험: %.1f
                - 동기: %.1f, 종합위험: %.1f
                """,
                twin.getMasteryScore(), twin.getExecutionScore(), twin.getRetentionRiskScore(),
                twin.getMotivationScore(), twin.getOverallRiskScore()
        ) : "트윈 데이터 없음";

        String userPrompt = String.format("""
                ## 학생 정보
                - 이름: %s
                - 강의: %s

                ## 디지털 트윈 상태
                %s

                ## 상담 메모
                %s

                ## 상담 전 브리핑 (참고용)
                %s

                위 상담 내용을 분석하여 요약, 원인 분석, 7일 액션 플랜, 과목 추천을 JSON으로 반환하세요.
                """,
                consultation.getStudent().getName(),
                consultation.getCourse().getTitle(),
                twinState,
                notes != null ? notes : "메모 없음",
                consultation.getBriefingJson() != null ? consultation.getBriefingJson().toString() : "브리핑 없음"
        );

        Map<String, Object> summaryResult = callGpt4o(SUMMARY_SYSTEM_PROMPT, userPrompt);

        // Save to consultation
        consultation.setNotes(notes);
        consultation.setSummaryText((String) summaryResult.get("summary"));
        consultation.setCauseAnalysis((String) summaryResult.get("cause_analysis"));

        Object actionPlanObj = summaryResult.get("action_plan");
        if (actionPlanObj instanceof List) {
            consultation.setActionPlanJson((List<Map<String, Object>>) actionPlanObj);
        }

        consultation.setStatus("COMPLETED");
        consultation.setCompletedAt(LocalDateTime.now());
        consultationRepository.save(consultation);

        log.info("상담 요약 생성 완료 - consultationId={}", consultationId);
        return Map.of("consultationId", consultationId, "summary", summaryResult);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> callGpt4o(String systemPrompt, String userPrompt) {
        var messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        var body = Map.of(
                "model", "gpt-4o",
                "messages", messages,
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.7
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

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
