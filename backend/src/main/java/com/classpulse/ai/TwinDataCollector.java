package com.classpulse.ai;

import com.classpulse.domain.chatbot.ChatMessageRepository;
import com.classpulse.domain.chatbot.Conversation;
import com.classpulse.domain.chatbot.ConversationRepository;
import com.classpulse.domain.codeanalysis.CodeFeedback;
import com.classpulse.domain.codeanalysis.CodeFeedbackRepository;
import com.classpulse.domain.codeanalysis.CodeSubmission;
import com.classpulse.domain.codeanalysis.CodeSubmissionRepository;
import com.classpulse.domain.consultation.Consultation;
import com.classpulse.domain.consultation.ConsultationRepository;
import com.classpulse.domain.course.CurriculumSkill;
import com.classpulse.domain.course.CurriculumSkillRepository;
import com.classpulse.domain.gamification.GamificationRepository;
import com.classpulse.domain.gamification.StudentGamification;
import com.classpulse.domain.gamification.XPEvent;
import com.classpulse.domain.gamification.XPEventRepository;
import com.classpulse.domain.learning.Reflection;
import com.classpulse.domain.learning.ReflectionRepository;
import com.classpulse.domain.learning.ReviewTaskRepository;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import com.classpulse.domain.twin.SkillMasterySnapshotRepository;
import com.classpulse.domain.twin.StudentTwin;
import com.classpulse.domain.twin.StudentTwinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TwinDataCollector {

    private final ReflectionRepository reflectionRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ConsultationRepository consultationRepository;
    private final CurriculumSkillRepository curriculumSkillRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final CodeFeedbackRepository codeFeedbackRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GamificationRepository gamificationRepository;
    private final XPEventRepository xpEventRepository;
    private final SkillMasterySnapshotRepository snapshotRepository;
    private final StudentTwinRepository studentTwinRepository;

    // ── Context record ──────────────────────────────────────────────────

    public record TwinInferenceContext(
            // Reflections
            List<Reflection> recentReflections,
            double avgConfidence,
            long stuckCount,
            // Review tasks (last 30 days)
            long totalReviews,
            long completedReviews,
            double reviewCompletionRate,
            // Consultations
            List<Consultation> recentConsultations,
            boolean hasRecentConsultation,
            // Code analysis
            CodeAnalysisSummary codeAnalysis,
            // Chatbot engagement
            ChatEngagementSummary chatEngagement,
            // Gamification
            GamificationSummary gamification,
            // Skills
            List<SkillMasterySnapshot> latestSkillSnapshots,
            List<CurriculumSkill> courseSkills,
            // Previous twin (for trend comparison)
            StudentTwin previousTwin
    ) {}

    public record CodeAnalysisSummary(
            long totalSubmissions,
            long goodCount,
            long warningCount,
            long errorCount,
            double goodRate
    ) {}

    public record ChatEngagementSummary(
            long totalConversations,
            long totalUserMessages,
            LocalDateTime lastConversationDate
    ) {}

    public record GamificationSummary(
            int level,
            String levelTitle,
            int totalXp,
            int streakDays,
            int weeklyXp,
            LocalDate lastActivityDate
    ) {}

    // ── Collect all data sources ────────────────────────────────────────

    public TwinInferenceContext collect(Long studentId, Long courseId) {
        // 1. Reflections
        List<Reflection> reflections = reflectionRepository
                .findByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
        List<Reflection> recentReflections = reflections.stream().limit(10).toList();
        double avgConfidence = reflections.stream()
                .mapToInt(Reflection::getSelfConfidenceScore)
                .average().orElse(3.0);
        long stuckCount = reflections.stream()
                .filter(r -> r.getStuckPoint() != null && !r.getStuckPoint().isBlank())
                .limit(5).count();

        // 2. Review tasks (last 30 days)
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);
        long completed = reviewTaskRepository
                .countByStudentIdAndStatusAndScheduledForBetween(studentId, "COMPLETED", thirtyDaysAgo, now);
        long pending = reviewTaskRepository
                .countByStudentIdAndStatusAndScheduledForBetween(studentId, "PENDING", thirtyDaysAgo, now);
        long totalReviews = completed + pending;
        double reviewRate = totalReviews > 0 ? (double) completed / totalReviews : 0.5;

        // 3. Consultations
        List<Consultation> consultations = consultationRepository
                .findByStudentIdAndCourseId(studentId, courseId);
        List<Consultation> recentConsultations = consultations.stream().limit(5).toList();
        boolean hasRecentConsultation = consultations.stream()
                .anyMatch(c -> c.getScheduledAt().isAfter(LocalDateTime.now().minusDays(14)));

        // 4. Code analysis
        List<CodeFeedback> allFeedbacks = codeFeedbackRepository
                .findBySubmissionStudentIdAndSubmissionCourseId(studentId, courseId);
        long goodCount = allFeedbacks.stream().filter(f -> "GOOD".equals(f.getSeverity())).count();
        long warningCount = allFeedbacks.stream().filter(f -> "WARNING".equals(f.getSeverity())).count();
        long errorCount = allFeedbacks.stream().filter(f -> "ERROR".equals(f.getSeverity())).count();
        long totalFeedbacks = goodCount + warningCount + errorCount;
        double goodRate = totalFeedbacks > 0 ? (double) goodCount / totalFeedbacks : 0.5;

        List<CodeSubmission> codeSubmissions = codeSubmissionRepository
                .findByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
        long totalSubmissions = codeSubmissions.size();

        CodeAnalysisSummary codeAnalysis = new CodeAnalysisSummary(
                totalSubmissions, goodCount, warningCount, errorCount, goodRate);

        // 5. Chatbot engagement
        List<Conversation> conversations = conversationRepository
                .findByStudentIdAndCourseIdOrderByUpdatedAtDesc(studentId, courseId);
        long totalUserMessages = chatMessageRepository.countByConversationStudentIdAndRole(studentId, "USER");
        LocalDateTime lastConvDate = conversations.isEmpty() ? null : conversations.get(0).getUpdatedAt();

        ChatEngagementSummary chatEngagement = new ChatEngagementSummary(
                conversations.size(), totalUserMessages, lastConvDate);

        // 6. Gamification
        StudentGamification gam = gamificationRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        List<XPEvent> recentXpEvents = xpEventRepository
                .findByStudentIdAndCourseIdAndCreatedAtAfter(studentId, courseId, LocalDateTime.now().minusDays(7));
        int weeklyXp = recentXpEvents.stream().mapToInt(XPEvent::getXpAmount).sum();

        GamificationSummary gamification = gam != null
                ? new GamificationSummary(gam.getLevel(), gam.getLevelTitle(),
                    gam.getTotalXpEarned(), gam.getStreakDays(), weeklyXp, gam.getLastActivityDate())
                : new GamificationSummary(1, "초보 학습자", 0, 0, 0, null);

        // 7. Skill snapshots
        List<SkillMasterySnapshot> snapshots = snapshotRepository
                .findByStudentIdAndCourseIdOrderByCapturedAtDesc(studentId, courseId);
        List<CurriculumSkill> courseSkills = curriculumSkillRepository.findByCourseId(courseId);

        // 8. Previous twin
        StudentTwin previousTwin = studentTwinRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);

        return new TwinInferenceContext(
                recentReflections, avgConfidence, stuckCount,
                totalReviews, completed, reviewRate,
                recentConsultations, hasRecentConsultation,
                codeAnalysis, chatEngagement, gamification,
                snapshots, courseSkills, previousTwin
        );
    }
}
