package com.classpulse.domain.twin;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "twin_score_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TwinScoreHistory {

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
    private BigDecimal masteryScore;

    @Column(name = "execution_score", precision = 5, scale = 2)
    private BigDecimal executionScore;

    @Column(name = "retention_risk_score", precision = 5, scale = 2)
    private BigDecimal retentionRiskScore;

    @Column(name = "motivation_score", precision = 5, scale = 2)
    private BigDecimal motivationScore;

    @Column(name = "consultation_need_score", precision = 5, scale = 2)
    private BigDecimal consultationNeedScore;

    @Column(name = "overall_risk_score", precision = 5, scale = 2)
    private BigDecimal overallRiskScore;

    @Column(name = "inference_source", length = 50)
    private String inferenceSource;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @PrePersist
    protected void onCreate() {
        capturedAt = LocalDateTime.now();
    }
}
