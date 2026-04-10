package com.classpulse.ai;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CurriculumSkillRepository;
import com.classpulse.domain.recommendation.Recommendation;
import com.classpulse.domain.recommendation.RecommendationRepository;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import com.classpulse.domain.twin.SkillMasterySnapshotRepository;
import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Engine 5 - 학습 추천 AI
 * 학생 트윈 상태, 약점 스킬, 학습 목표를 기반으로
 * 추천 과목/과제, 추천 이유, 기대 효과를 생성합니다.
 */
@Slf4j
@Service
public class RecommendationAi {

    private final RestTemplate openAiRestTemplate;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final CurriculumSkillRepository curriculumSkillRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public RecommendationAi(
            @Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
            StudentTwinRepository studentTwinRepository,
            SkillMasterySnapshotRepository snapshotRepository,
            CurriculumSkillRepository curriculumSkillRepository,
            RecommendationRepository recommendationRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            ObjectMapper objectMapper) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.studentTwinRepository = studentTwinRepository;
        this.snapshotRepository = snapshotRepository;
        this.curriculumSkillRepository = curriculumSkillRepository;
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        당신은 개인화 학습 추천 전문 AI입니다.
        학생의 디지털 트윈 상태와 스킬 숙달도를 분석하여 최적의 학습 경로를 추천합니다.

        반드시 다음 JSON 형식으로 응답하세요:
        {
          "recommendations": [
            {
              "type": "REVIEW | PRACTICE | COURSE | RESOURCE | CHALLENGE",
              "title": "추천 항목 제목",
              "reason_summary": "이 항목을 추천하는 구체적 이유 (2-3문장)",
              "expected_outcome": "이 추천을 따랐을 때 기대되는 효과",
              "trigger_event": "이 추천이 발생한 트리거 (예: '반복문 이해도 40% 미만')",
              "priority": "HIGH | MEDIUM | LOW",
              "evidence": {
                "weak_skill": "관련 약점 스킬",
                "current_score": 0-100,
                "target_score": 0-100
              }
            }
          ]
        }

        추천 원칙:
        1. 가장 시급한 약점 스킬부터 우선 추천합니다.
        2. 학생의 현재 수준에 맞는 단계적 추천을 합니다.
        3. 동기가 낮은 학생에게는 성취감을 줄 수 있는 쉬운 과제도 포함합니다.
        4. 추천은 3-5개 범위로 합니다 (너무 많으면 학습자가 부담을 느낍니다).
        5. 각 추천의 근거를 데이터로 명확히 설명합니다.
        """;

    @Transactional
    public Map<String, Object> generateRecommendations(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        StudentTwin twin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalStateException("트윈 데이터가 없습니다. 트윈 추론을 먼저 실행하세요."));

        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);
        List<CurriculumSkill> skills = curriculumSkillRepository.findByCourseId(courseId);

        // Identify weak skills (understanding < 50 or forgetting risk > 60)
        String skillAnalysis = snapshots.stream()
                .collect(Collectors.groupingBy(s -> s.getSkill().getName()))
                .entrySet().stream()
                .map(e -> {
                    SkillMasterySnapshot latest = e.getValue().get(0);
                    return String.format("- %s: 이해 %.0f, 연습 %.0f, 자신감 %.0f, 망각위험 %.0f",
                            e.getKey(),
                            latest.getUnderstandingScore(), latest.getPracticeScore(),
                            latest.getConfidenceScore(), latest.getForgettingRiskScore());
                })
                .collect(Collectors.joining("\n"));

        String userPrompt = String.format("""
                ## 학생 정보
                - 이름: %s
                - 강의: %s

                ## 디지털 트윈 상태
                - 이해도: %.1f
                - 실행력: %.1f
                - 망각위험: %.1f
                - 동기: %.1f
                - 종합위험: %.1f
                - AI 인사이트: %s

                ## 스킬별 최신 숙달도
                %s

                ## 전체 스킬 목록
                %s

                위 데이터를 분석하여 이 학생에게 가장 효과적인 학습 추천을 생성하세요.
                """,
                student.getName(),
                course.getTitle(),
                twin.getMasteryScore(), twin.getExecutionScore(),
                twin.getRetentionRiskScore(), twin.getMotivationScore(),
                twin.getOverallRiskScore(),
                twin.getAiInsight() != null ? twin.getAiInsight() : "없음",
                skillAnalysis.isEmpty() ? "스냅샷 없음" : skillAnalysis,
                skills.stream().map(s -> s.getName() + " (" + s.getDifficulty() + ")")
                        .collect(Collectors.joining(", "))
        );

        Map<String, Object> gptResponse = callGpt4o(SYSTEM_PROMPT, userPrompt);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendations =
                (List<Map<String, Object>>) gptResponse.getOrDefault("recommendations", List.of());

        List<Long> savedIds = new ArrayList<>();
        for (Map<String, Object> recMap : recommendations) {
            @SuppressWarnings("unchecked")
            Map<String, Object> evidence = (Map<String, Object>) recMap.getOrDefault("evidence", Map.of());

            Recommendation rec = Recommendation.builder()
                    .student(student)
                    .course(course)
                    .recommendationType((String) recMap.get("type"))
                    .title((String) recMap.get("title"))
                    .reasonSummary((String) recMap.get("reason_summary"))
                    .expectedOutcome((String) recMap.get("expected_outcome"))
                    .triggerEvent((String) recMap.get("trigger_event"))
                    .evidencePayload(evidence)
                    .build();
            Recommendation saved = recommendationRepository.save(rec);
            savedIds.add(saved.getId());
        }

        log.info("추천 생성 완료 - studentId={}, courseId={}, 추천 {}건", studentId, courseId, savedIds.size());

        return Map.of(
                "studentId", studentId,
                "courseId", courseId,
                "recommendationCount", savedIds.size(),
                "recommendationIds", savedIds,
                "recommendations", recommendations
        );
    }

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
}
