package com.classpulse.api;

import com.classpulse.ai.CurriculumAnalyzer;
import com.classpulse.domain.course.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/courses/{courseId}")
@RequiredArgsConstructor
public class CurriculumController {

    private final CourseRepository courseRepository;
    private final CurriculumSkillRepository skillRepository;
    private final AsyncJobRepository asyncJobRepository;
    private final CurriculumAsyncService curriculumAsyncService;

    // --- DTOs ---

    public record SkillResponse(
            Long id, String name, String description, String difficulty,
            List<Long> prerequisiteIds
    ) {
        public static SkillResponse from(CurriculumSkill s) {
            List<Long> prereqs = s.getPrerequisites().stream()
                    .map(CurriculumSkill::getId).toList();
            return new SkillResponse(s.getId(), s.getName(), s.getDescription(), s.getDifficulty(), prereqs);
        }
    }

    public record UpdateSkillRequest(String name, String description, String difficulty) {}

    public record CreateSkillRequest(String name, String description, String difficulty) {}

    public record JobIdResponse(Long jobId) {}

    // --- Endpoints ---

    @PostMapping("/curriculum")
    public ResponseEntity<JobIdResponse> uploadCurriculum(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "target", required = false, defaultValue = "") String target,
            @RequestParam(value = "additionalPrompt", required = false, defaultValue = "") String additionalPrompt
    ) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        AsyncJob job = AsyncJob.builder()
                .jobType("CURRICULUM_ANALYSIS")
                .status("PENDING")
                .inputPayload(Map.of(
                        "courseId", courseId,
                        "fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                        "contentLength", content.length()
                ))
                .build();
        job = asyncJobRepository.save(job);

        // target과 additionalPrompt를 objectives로 합쳐서 전달
        String objectives = "";
        if (!target.isBlank()) objectives += "대상: " + target + "\n";
        if (!additionalPrompt.isBlank()) objectives += "추가 요청: " + additionalPrompt + "\n";

        curriculumAsyncService.analyzeCurriculum(job.getId(), courseId, content, objectives);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new JobIdResponse(job.getId()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/skills")
    public ResponseEntity<List<SkillResponse>> getSkills(@PathVariable Long courseId) {
        List<CurriculumSkill> skills = skillRepository.findByCourseId(courseId);
        return ResponseEntity.ok(skills.stream().map(SkillResponse::from).toList());
    }

    @PutMapping("/skills/{skillId}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long courseId,
            @PathVariable Long skillId,
            @RequestBody UpdateSkillRequest request
    ) {
        CurriculumSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));

        if (!skill.getCourse().getId().equals(courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.name() != null) skill.setName(request.name());
        if (request.description() != null) skill.setDescription(request.description());
        if (request.difficulty() != null) skill.setDifficulty(request.difficulty());

        skill = skillRepository.save(skill);
        return ResponseEntity.ok(SkillResponse.from(skill));
    }

    @PostMapping("/skills")
    public ResponseEntity<SkillResponse> createSkill(
            @PathVariable Long courseId,
            @RequestBody CreateSkillRequest request
    ) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CurriculumSkill skill = CurriculumSkill.builder()
                .course(course)
                .name(request.name())
                .description(request.description())
                .difficulty(request.difficulty() != null ? request.difficulty() : "MEDIUM")
                .build();
        skill = skillRepository.save(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(SkillResponse.from(skill));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long courseId,
            @PathVariable Long skillId
    ) {
        CurriculumSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));

        if (!skill.getCourse().getId().equals(courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        skillRepository.delete(skill);
        return ResponseEntity.noContent().build();
    }

    // --- Async Service (inner class) ---

    @Slf4j
    @Service
    @RequiredArgsConstructor
    static class CurriculumAsyncService {

        private final AsyncJobRepository asyncJobRepository;
        private final CurriculumSkillRepository skillRepository;
        private final CourseRepository courseRepository;
        private final CurriculumAnalyzer curriculumAnalyzer;

        @Async("aiTaskExecutor")
        public void analyzeCurriculum(Long jobId, Long courseId, String content, String objectives) {
            AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();
            try {
                job.setStatus("PROCESSING");
                asyncJobRepository.save(job);

                log.info("Analyzing curriculum for course {} via CurriculumAnalyzer (content length: {})",
                        courseId, content.length());

                Map<String, Object> analysisResult = curriculumAnalyzer.analyze(courseId, content, objectives);

                // 전체 AI 분석 결과를 job resultPayload에 저장
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("courseId", courseId);
                result.put("message", "Curriculum analysis completed");
                result.put("skillsExtracted", analysisResult.getOrDefault("skillCount", 0));
                result.put("weekly_concepts", analysisResult.getOrDefault("weekly_concepts", List.of()));
                result.put("common_misconceptions", analysisResult.getOrDefault("common_misconceptions", List.of()));
                result.put("review_points", analysisResult.getOrDefault("review_points", List.of()));

                job.complete(result);
                asyncJobRepository.save(job);

            } catch (Exception e) {
                log.error("Curriculum analysis failed for job {}", jobId, e);
                job.fail(e.getMessage());
                asyncJobRepository.save(job);
            }
        }
    }
}
