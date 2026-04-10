package com.classpulse.domain.learning;

import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CurriculumSkill;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private CurriculumSkill skill;

    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(length = 20)
    @Builder.Default
    private String difficulty = "MEDIUM";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "generation_reason", columnDefinition = "TEXT")
    private String generationReason;

    @Column(name = "approval_status", length = 20)
    @Builder.Default
    private String approvalStatus = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
