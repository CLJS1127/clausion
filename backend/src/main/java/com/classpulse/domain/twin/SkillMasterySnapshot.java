package com.classpulse.domain.twin;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CurriculumSkill;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_mastery_snapshot")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillMasterySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private CurriculumSkill skill;

    @Column(name = "understanding_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal understandingScore = BigDecimal.ZERO;

    @Column(name = "practice_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal practiceScore = BigDecimal.ZERO;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal confidenceScore = BigDecimal.ZERO;

    @Column(name = "forgetting_risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal forgettingRiskScore = BigDecimal.ZERO;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @PrePersist
    protected void onCreate() {
        capturedAt = LocalDateTime.now();
    }
}
