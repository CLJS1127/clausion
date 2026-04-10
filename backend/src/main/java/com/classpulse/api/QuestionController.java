package com.classpulse.api;

import com.classpulse.ai.QuestionGenerator;
import com.classpulse.domain.course.*;
import com.classpulse.domain.learning.Question;
import com.classpulse.domain.learning.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final CurriculumSkillRepository skillRepository;
    private final AsyncJobRepository asyncJobRepository;
    private final QuestionGenerationService questionGenerationService;

    // --- DTOs ---

    public record GenerateRequest(Long skillId, String difficulty, int count) {}

    public record CreateQuestionRequest(
            Long courseId, Long skillId, String questionType,
            String difficulty, String content, String answer, String explanation
    ) {}

    public record UpdateQuestionRequest(
            String questionType, String difficulty, String content,
            String answer, String explanation
    ) {}

    public record QuestionResponse(
            Long id, Long courseId, Long skillId, String questionType,
            String difficulty, String content, String answer,
            String explanation, String approvalStatus, String generationReason
    ) {
        public static QuestionResponse from(Question q) {
            return new QuestionResponse(
                    q.getId(),
                    q.getCourse().getId(),
                    q.getSkill() != null ? q.getSkill().getId() : null,
                    q.getQuestionType(),
                    q.getDifficulty(),
                    q.getContent(),
                    q.getAnswer(),
                    q.getExplanation(),
                    q.getApprovalStatus(),
                    q.getGenerationReason()
            );
        }
    }

    public record JobIdResponse(Long jobId) {}

    // --- Endpoints ---

    @PostMapping("/api/courses/{courseId}/questions/generate")
    public ResponseEntity<JobIdResponse> generate(
            @PathVariable Long courseId,
            @RequestBody GenerateRequest request
    ) {
        AsyncJob job = AsyncJob.builder()
                .jobType("QUESTION_GENERATION")
                .status("PENDING")
                .inputPayload(Map.of(
                        "courseId", courseId,
                        "skillId", request.skillId() != null ? request.skillId() : 0,
                        "difficulty", request.difficulty() != null ? request.difficulty() : "MEDIUM",
                        "count", request.count() > 0 ? request.count() : 5
                ))
                .build();
        job = asyncJobRepository.save(job);

        questionGenerationService.generateQuestions(job.getId(), courseId, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new JobIdResponse(job.getId()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/api/questions")
    public ResponseEntity<List<QuestionResponse>> list(@RequestParam Long courseId) {
        List<Question> questions = questionRepository.findByCourseId(courseId);
        return ResponseEntity.ok(questions.stream().map(QuestionResponse::from).toList());
    }

    @PutMapping("/api/questions/{id}/approve")
    public ResponseEntity<QuestionResponse> approve(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
        question.setApprovalStatus("APPROVED");
        question = questionRepository.save(question);
        return ResponseEntity.ok(QuestionResponse.from(question));
    }

    @PutMapping("/api/questions/{id}/reject")
    public ResponseEntity<QuestionResponse> reject(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
        question.setApprovalStatus("REJECTED");
        question = questionRepository.save(question);
        return ResponseEntity.ok(QuestionResponse.from(question));
    }

    @PostMapping("/api/questions")
    public ResponseEntity<QuestionResponse> create(@RequestBody CreateQuestionRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + request.courseId()));

        Question question = Question.builder()
                .course(course)
                .questionType(request.questionType() != null ? request.questionType() : "서술형")
                .difficulty(request.difficulty() != null ? request.difficulty() : "MEDIUM")
                .content(request.content())
                .answer(request.answer())
                .explanation(request.explanation())
                .generationReason("수동 등록")
                .approvalStatus("APPROVED")
                .build();

        if (request.skillId() != null) {
            CurriculumSkill skill = skillRepository.findById(request.skillId()).orElse(null);
            question.setSkill(skill);
        }

        question = questionRepository.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponse.from(question));
    }

    @PutMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateQuestionRequest request
    ) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));

        if (request.questionType() != null) question.setQuestionType(request.questionType());
        if (request.difficulty() != null) question.setDifficulty(request.difficulty());
        if (request.content() != null) question.setContent(request.content());
        if (request.answer() != null) question.setAnswer(request.answer());
        if (request.explanation() != null) question.setExplanation(request.explanation());

        question = questionRepository.save(question);
        return ResponseEntity.ok(QuestionResponse.from(question));
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
        questionRepository.delete(question);
        return ResponseEntity.noContent().build();
    }

    // --- Async Service ---

    @Slf4j
    @Service
    @RequiredArgsConstructor
    static class QuestionGenerationService {

        private final AsyncJobRepository asyncJobRepository;
        private final QuestionRepository questionRepository;
        private final CourseRepository courseRepository;
        private final CurriculumSkillRepository skillRepository;
        private final QuestionGenerator questionGenerator;

        @Async("aiTaskExecutor")
        public void generateQuestions(Long jobId, Long courseId, GenerateRequest request) {
            AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();
            try {
                job.setStatus("PROCESSING");
                asyncJobRepository.save(job);

                String difficulty = request.difficulty() != null ? request.difficulty() : "MEDIUM";
                int count = request.count() > 0 ? request.count() : 5;

                log.info("Generating {} questions for course {}, skill {}, difficulty {}",
                        count, courseId, request.skillId(), difficulty);

                Map<String, Object> result = questionGenerator.generate(courseId, difficulty, count);

                int generatedCount = (int) result.getOrDefault("generatedCount", 0);
                job.complete(Map.of(
                        "courseId", courseId,
                        "questionsGenerated", generatedCount,
                        "message", "Question generation completed"
                ));
                asyncJobRepository.save(job);

            } catch (Exception e) {
                log.error("Question generation failed for job {}", jobId, e);
                job.fail(e.getMessage());
                asyncJobRepository.save(job);
            }
        }
    }
}
