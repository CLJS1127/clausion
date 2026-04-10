package com.classpulse.domain.learning;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CurriculumSkill;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewTask {

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
    @JoinColumn(name = "skill_id")
    private CurriculumSkill skill;

    @Column(nullable = false)
    private String title;

    @Column(name = "reason_summary", columnDefinition = "TEXT")
    private String reasonSummary;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDate scheduledFor;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
}
