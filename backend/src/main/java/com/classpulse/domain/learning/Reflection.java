package com.classpulse.domain.learning;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reflections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reflection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "stuck_point", columnDefinition = "TEXT")
    private String stuckPoint;

    @Column(name = "self_confidence_score")
    @Builder.Default
    private Integer selfConfidenceScore = 3;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_summary", columnDefinition = "jsonb")
    private Map<String, Object> emotionSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_analysis_json", columnDefinition = "jsonb")
    private Map<String, Object> aiAnalysisJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
