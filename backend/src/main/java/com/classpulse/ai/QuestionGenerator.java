package com.classpulse.ai;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CourseRepository;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CurriculumSkillRepository;
import com.classpulse.domain.learning.Question;
import com.classpulse.domain.learning.QuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Engine 2 - 문제 생성기
 * 커리큘럼 스킬 기반으로 다양한 유형의 문제를 생성합니다.
 * 유형: 개념 이해, 코드 완성, 디버깅, 서술형, 시나리오 기반
 */
@Slf4j
@Service
public class QuestionGenerator {

    private final RestTemplate openAiRestTemplate;
    private final CourseRepository courseRepository;
    private final CurriculumSkillRepository curriculumSkillRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public QuestionGenerator(
            @Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate,
            CourseRepository courseRepository,
            CurriculumSkillRepository curriculumSkillRepository,
            QuestionRepository questionRepository,
            ObjectMapper objectMapper) {
        this.openAiRestTemplate = openAiRestTemplate;
        this.courseRepository = courseRepository;
        this.curriculumSkillRepository = curriculumSkillRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        당신은 프로그래밍 교육 전문 문제 출제 AI입니다.
        주어진 스킬 목록과 난이도에 맞는 고품질 문제를 생성합니다.

        반드시 다음 JSON 형식으로 응답하세요:
        {
          "questions": [
            {
              "skill_name": "해당 스킬 이름",
              "question_type": "CONCEPTUAL | CODE_COMPLETION | DEBUGGING | DESCRIPTIVE | SCENARIO",
              "difficulty": "EASY | MEDIUM | HARD",
              "content": "문제 본문 (마크다운 사용 가능, 코드 블록 포함 가능)",
              "answer": "모범 답안",
              "explanation": "해설 - 왜 이 답이 맞는지, 관련 개념 설명",
              "generation_reason": "이 문제를 생성한 교육적 의도"
            }
          ]
        }

        문제 유형별 가이드라인:
        - CONCEPTUAL: 개념 이해도를 확인하는 객관식/단답형 문제
        - CODE_COMPLETION: 빈칸 채우기 또는 함수 완성 문제
        - DEBUGGING: 버그가 있는 코드를 찾고 수정하는 문제
        - DESCRIPTIVE: 개념을 자신의 언어로 설명하게 하는 서술형 문제
        - SCENARIO: 실제 상황을 제시하고 해결 방안을 묻는 문제

        문제 출제 원칙:
        1. 각 문제는 하나의 핵심 스킬에 집중해야 합니다.
        2. 난이도가 EASY이면 기본 개념, MEDIUM이면 응용, HARD이면 복합 사고를 요구합니다.
        3. 해설은 학습자가 스스로 이해할 수 있도록 친절하게 작성합니다.
        4. 코드 예시는 실무에서 볼 수 있는 현실적인 패턴을 사용합니다.
        5. 문제 유형을 골고루 섞어서 출제합니다.
        """;

    @Transactional
    public Map<String, Object> generate(Long courseId, String difficulty, int count) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<CurriculumSkill> skills = curriculumSkillRepository.findByCourseId(courseId);
        if (skills.isEmpty()) {
            throw new IllegalStateException("해당 과목에 등록된 스킬이 없습니다. 커리큘럼 분석을 먼저 실행하세요.");
        }

        String skillList = skills.stream()
                .map(s -> String.format("- %s (%s): %s",
                        s.getName(), s.getDifficulty(),
                        s.getDescription() != null ? s.getDescription() : ""))
                .collect(Collectors.joining("\n"));

        // Build skill name -> entity map for linking
        Map<String, CurriculumSkill> skillMap = skills.stream()
                .collect(Collectors.toMap(CurriculumSkill::getName, s -> s, (a, b) -> a));

        String userPrompt = String.format("""
                ## 강의 정보
                - 강의명: %s
                - 요청 난이도: %s
                - 요청 문제 수: %d

                ## 커리큘럼 스킬 목록
                %s

                위 스킬들을 기반으로 %d개의 문제를 생성하세요.
                난이도 '%s'에 맞춰 출제하되, 5가지 문제 유형을 골고루 배분하세요.
                """,
                course.getTitle(),
                difficulty,
                count,
                skillList,
                count,
                difficulty
        );

        Map<String, Object> gptResponse = callGpt4o(SYSTEM_PROMPT, userPrompt);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questionMaps =
                (List<Map<String, Object>>) gptResponse.getOrDefault("questions", List.of());

        List<Question> savedQuestions = new ArrayList<>();
        for (Map<String, Object> qMap : questionMaps) {
            String skillName = (String) qMap.get("skill_name");
            CurriculumSkill linkedSkill = skillMap.get(skillName);

            Question question = Question.builder()
                    .course(course)
                    .skill(linkedSkill)
                    .questionType((String) qMap.get("question_type"))
                    .difficulty((String) qMap.getOrDefault("difficulty", difficulty))
                    .content((String) qMap.get("content"))
                    .answer((String) qMap.get("answer"))
                    .explanation((String) qMap.get("explanation"))
                    .generationReason((String) qMap.get("generation_reason"))
                    .approvalStatus("PENDING")
                    .build();
            savedQuestions.add(questionRepository.save(question));
        }

        log.info("문제 생성 완료 - courseId={}, 생성 수={}", courseId, savedQuestions.size());

        return Map.of(
                "courseId", courseId,
                "generatedCount", savedQuestions.size(),
                "questionIds", savedQuestions.stream().map(Question::getId).collect(Collectors.toList()),
                "questions", questionMaps
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
                "temperature", 0.8
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
