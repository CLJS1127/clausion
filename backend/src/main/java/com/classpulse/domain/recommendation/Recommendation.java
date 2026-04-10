package com.classpulse.domain.recommendation;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "recommendations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "recommendation_type", nullable = false, length = 50)
    private String recommendationType;

    @Column(nullable = false)
    private String title;

    @Column(name = "reason_summary", columnDefinition = "TEXT")
    private String reasonSummary;

    @Column(name = "trigger_event", length = 100)
    private String triggerEvent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_payload", columnDefinition = "jsonb")
    private Map<String, Object> evidencePayload;

    @Column(name = "expected_outcome", columnDefinition = "TEXT")
    private String expectedOutcome;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
