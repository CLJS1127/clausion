package com.classpulse.api;

import com.classpulse.config.SecurityUtil;
import com.classpulse.domain.recommendation.Recommendation;
import com.classpulse.domain.recommendation.RecommendationRepository;
import com.classpulse.domain.user.User;
import com.classpulse.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationRepository recommendationRepository;
    private final UserService userService;

    // --- DTOs ---

    public record RecommendationResponse(
            Long id, Long studentId, Long courseId,
            String recommendationType, String title,
            String reasonSummary, String triggerEvent,
            Map<String, Object> evidencePayload,
            String expectedOutcome, LocalDateTime createdAt
    ) {
        public static RecommendationResponse from(Recommendation r) {
            return new RecommendationResponse(
                    r.getId(),
                    r.getStudent().getId(),
                    r.getCourse().getId(),
                    r.getRecommendationType(),
                    r.getTitle(),
                    r.getReasonSummary(),
                    r.getTriggerEvent(),
                    r.getEvidencePayload(),
                    r.getExpectedOutcome(),
                    r.getCreatedAt()
            );
        }
    }

    // --- Endpoints ---

    @GetMapping("/{studentId}")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId
    ) {
        verifyAccessToStudent(studentId);
        List<Recommendation> recommendations;
        if (courseId != null) {
            recommendations = recommendationRepository
                    .findByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
        } else {
            recommendations = recommendationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        }
        return ResponseEntity.ok(recommendations.stream().map(RecommendationResponse::from).toList());
    }

    private void verifyAccessToStudent(Long studentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId.equals(studentId)) return;
        User currentUser = userService.findById(userId);
        if (currentUser.getRole() == User.Role.INSTRUCTOR) return;
        throw new SecurityException("Access denied");
    }
}
