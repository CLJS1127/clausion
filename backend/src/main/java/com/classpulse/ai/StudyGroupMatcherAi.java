package com.classpulse.ai;

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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Engine 8 [v2] - 스터디 그룹 매칭 AI
 * 대상 학생의 트윈과 같은 과목 수강생들의 트윈을 비교하여
 * 보완 점수를 계산하고 최적의 매칭 파트너 3명을 추천합니다.
 */
@Slf4j
@Service
public class StudyGroupMatcherAi {

    private final RestTemplate openAiRestTemplate;
    private final StudentTwinRepository studentTwinRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public StudyGroupMatcherAi(
            @Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
            StudentTwinRepository studentTwinRepository,
            SkillMasterySnapshotRepository snapshotRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.studentTwinRepository = studentTwinRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        당신은 협동 학습 전문 AI입니다.
        학생들의 학습 프로필을 분석하여 서로 보완적인 스터디 그룹 파트너를 매칭합니다.

        반드시 다음 JSON 형식으로 응답하세요:
        {
          "matches": [
            {
              "student_id": 123,
              "student_name": "학생 이름",
              "complement_score": 0.0-1.0,
              "strength_description": "이 학생의 강점 설명",
              "complement_note": "대상 학생과 이 학생이 함께 공부하면 좋은 이유",
              "shared_weak_skills": ["공통 약점 스킬"],
              "complementary_skills": ["서로 보완되는 스킬"]
            }
          ],
          "group_synergy_analysis": "이 그룹 조합의 전반적인 시너지 분석 (2-3문장)"
        }

        매칭 원칙:
        1. 보완성: 한 학생의 강점이 다른 학생의 약점을 보완해야 합니다.
        2. 균형: 실력 차이가 너무 크면 학습 효과가 떨어집니다.
        3. 다양성: 서로 다른 강점을 가진 학생을 매칭합니다.
        4. 동기 시너지: 동기가 낮은 학생에게는 동기가 높은 파트너를 우선 매칭합니다.
        5. 최대 3명을 추천합니다.
        """;

    public Map<String, Object> findMatches(Long targetStudentId, Long courseId) {
        // Get target student's twin — return empty matches if no twin data yet
        var targetTwinOpt = studentTwinRepository.findByStudentIdAndCourseId(targetStudentId, courseId);
        if (targetTwinOpt.isEmpty()) {
            return Map.of(
                    "targetStudentId", targetStudentId,
                    "courseId", courseId,
                    "matches", List.of(),
                    "message", "아직 디지털 트윈 데이터가 생성되지 않았습니다. 학습 활동 후 매칭이 가능합니다."
            );
        }
        StudentTwin targetTwin = targetTwinOpt.get();
        User targetStudent = userRepository.findById(targetStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + targetStudentId));

        // Get all twins in the same course, excluding target
        List<StudentTwin> allTwins = studentTwinRepository.findByCourseId(courseId).stream()
                .filter(t -> !t.getStudent().getId().equals(targetStudentId))
                .collect(Collectors.toList());

        if (allTwins.isEmpty()) {
            return Map.of(
                    "targetStudentId", targetStudentId,
                    "courseId", courseId,
                    "matches", List.of(),
                    "message", "매칭 가능한 다른 학생이 없습니다."
            );
        }

        // Calculate rule-based complement scores first
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (StudentTwin candidateTwin : allTwins) {
            double complementScore = calculateComplementScore(targetTwin, candidateTwin);
            User candidate = candidateTwin.getStudent();

            Map<String, Object> candidateInfo = new HashMap<>();
            candidateInfo.put("student_id", candidate.getId());
            candidateInfo.put("student_name", candidate.getName());
            candidateInfo.put("complement_score_rule", complementScore);
            candidateInfo.put("mastery_score", candidateTwin.getMasteryScore());
            candidateInfo.put("execution_score", candidateTwin.getExecutionScore());
            candidateInfo.put("motivation_score", candidateTwin.getMotivationScore());
            candidateInfo.put("retention_risk_score", candidateTwin.getRetentionRiskScore());
            candidates.add(candidateInfo);
        }

        // Sort by complement score and take top 5 for LLM analysis
        candidates.sort((a, b) -> Double.compare(
                (double) b.get("complement_score_rule"),
                (double) a.get("complement_score_rule")));
        List<Map<String, Object>> topCandidates = candidates.stream()
                .limit(5).collect(Collectors.toList());

        // Get skill-level data for target and top candidates
        Map<String, List<String>> targetSkillProfile = getSkillProfile(targetStudentId, courseId);

        Map<Long, Map<String, List<String>>> candidateProfiles = new HashMap<>();
        for (Map<String, Object> c : topCandidates) {
            Long cId = ((Number) c.get("student_id")).longValue();
            candidateProfiles.put(cId, getSkillProfile(cId, courseId));
        }

        String userPrompt = buildMatchingPrompt(targetStudent, targetTwin, targetSkillProfile,
                topCandidates, candidateProfiles);

        Map<String, Object> gptResponse = callGpt4o(SYSTEM_PROMPT, userPrompt);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> matches =
                (List<Map<String, Object>>) gptResponse.getOrDefault("matches", List.of());

        // Limit to top 3
        if (matches.size() > 3) {
            matches = matches.subList(0, 3);
        }

        log.info("스터디 그룹 매칭 완료 - targetStudentId={}, courseId={}, 매칭 {}건",
                targetStudentId, courseId, matches.size());

        return Map.of(
                "targetStudentId", targetStudentId,
                "courseId", courseId,
                "matches", matches,
                "groupSynergyAnalysis", gptResponse.getOrDefault("group_synergy_analysis", "")
        );
    }

    /**
     * 규칙 기반 보완 점수 계산
     * 두 학생의 강점/약점 차이가 클수록 보완성이 높음
     */
    private double calculateComplementScore(StudentTwin target, StudentTwin candidate) {
        // Complement: where one is strong and other is weak
        double masteryComplement = Math.abs(
                target.getMasteryScore().subtract(candidate.getMasteryScore()).doubleValue()) / 100.0;
        double executionComplement = Math.abs(
                target.getExecutionScore().subtract(candidate.getExecutionScore()).doubleValue()) / 100.0;

        // Motivation synergy: prefer matching with motivated students if target motivation is low
        double motivationBonus = 0;
        if (target.getMotivationScore().doubleValue() < 50
                && candidate.getMotivationScore().doubleValue() > 70) {
            motivationBonus = 0.15;
        }

        // Avoid matching two high-risk students together
        double riskPenalty = 0;
        if (target.getOverallRiskScore().doubleValue() > 60
                && candidate.getOverallRiskScore().doubleValue() > 60) {
            riskPenalty = 0.2;
        }

        // Balanced skill difference (not too big, not too small)
        double avgDifference = (masteryComplement + executionComplement) / 2.0;
        double balanceBonus = avgDifference > 0.15 && avgDifference < 0.5 ? 0.1 : 0;

        return Math.min(1.0, Math.max(0.0,
                masteryComplement * 0.3 + executionComplement * 0.3
                        + motivationBonus + balanceBonus - riskPenalty));
    }

    private Map<String, List<String>> getSkillProfile(Long studentId, Long courseId) {
        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);

        Map<String, SkillMasterySnapshot> latest = new LinkedHashMap<>();
        for (SkillMasterySnapshot s : snapshots) {
            latest.putIfAbsent(s.getSkill().getName(), s);
        }

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        for (Map.Entry<String, SkillMasterySnapshot> e : latest.entrySet()) {
            if (e.getValue().getUnderstandingScore().doubleValue() >= 70) {
                strengths.add(e.getKey());
            } else if (e.getValue().getUnderstandingScore().doubleValue() < 50) {
                weaknesses.add(e.getKey());
            }
        }

        return Map.of("strengths", strengths, "weaknesses", weaknesses);
    }

    private String buildMatchingPrompt(User target, StudentTwin targetTwin,
                                       Map<String, List<String>> targetProfile,
                                       List<Map<String, Object>> candidates,
                                       Map<Long, Map<String, List<String>>> candidateProfiles) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                ## 대상 학생
                - 이름: %s
                - 이해도: %.1f, 실행력: %.1f, 동기: %.1f, 망각위험: %.1f
                - 강점 스킬: %s
                - 약점 스킬: %s

                ## 매칭 후보 (상위 5명)
                """,
                target.getName(),
                targetTwin.getMasteryScore(), targetTwin.getExecutionScore(),
                targetTwin.getMotivationScore(), targetTwin.getRetentionRiskScore(),
                String.join(", ", targetProfile.getOrDefault("strengths", List.of())),
                String.join(", ", targetProfile.getOrDefault("weaknesses", List.of()))
        ));

        for (Map<String, Object> c : candidates) {
            Long cId = ((Number) c.get("student_id")).longValue();
            Map<String, List<String>> profile = candidateProfiles.getOrDefault(cId, Map.of());
            sb.append(String.format("""
                    ### %s (ID: %d)
                    - 이해도: %.1f, 실행력: %.1f, 동기: %.1f, 망각위험: %.1f
                    - 규칙기반 보완점수: %.2f
                    - 강점 스킬: %s
                    - 약점 스킬: %s

                    """,
                    c.get("student_name"), c.get("student_id"),
                    ((BigDecimal) c.get("mastery_score")).doubleValue(),
                    ((BigDecimal) c.get("execution_score")).doubleValue(),
                    ((BigDecimal) c.get("motivation_score")).doubleValue(),
                    ((BigDecimal) c.get("retention_risk_score")).doubleValue(),
                    c.get("complement_score_rule"),
                    String.join(", ", profile.getOrDefault("strengths", List.of())),
                    String.join(", ", profile.getOrDefault("weaknesses", List.of()))
            ));
        }

        sb.append("위 후보 중 대상 학생에게 가장 좋은 파트너 3명을 선정하고 분석을 JSON으로 반환하세요.");
        return sb.toString();
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
                "temperature", 0.6
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
