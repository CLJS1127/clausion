package com.classpulse.domain.gamification;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xp_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class XPEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "xp_amount", nullable = false)
    private Integer xpAmount;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
