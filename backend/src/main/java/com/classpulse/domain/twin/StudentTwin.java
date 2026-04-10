package com.classpulse.domain.twin;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "student_twin", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentTwin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "mastery_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal masteryScore = BigDecimal.ZERO;

    @Column(name = "execution_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal executionScore = BigDecimal.ZERO;

    @Column(name = "retention_risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal retentionRiskScore = BigDecimal.ZERO;

    @Column(name = "motivation_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal motivationScore = BigDecimal.ZERO;

    @Column(name = "consultation_need_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal consultationNeedScore = BigDecimal.ZERO;

    @Column(name = "overall_risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overallRiskScore = BigDecimal.ZERO;

    @Column(name = "ai_insight", columnDefinition = "TEXT")
    private String aiInsight;

    @Column(name = "trend_direction", length = 20)
    private String trendDirection;

    @Column(name = "trend_explanation", columnDefinition = "TEXT")
    private String trendExplanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_conflicts_json", columnDefinition = "jsonb")
    private List<String> dataConflictsJson;

    @Column(name = "inference_source", length = 50)
    private String inferenceSource;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
