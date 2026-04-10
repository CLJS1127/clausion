package com.classpulse.domain.studygroup;

import com.classpulse.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_group_members", uniqueConstraints = @UniqueConstraint(columnNames = {"study_group_id", "student_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(length = 20)
    @Builder.Default
    private String role = "MEMBER";

    @Column(name = "strength_summary")
    private String strengthSummary;

    @Column(name = "complement_note")
    private String complementNote;

    @Column(name = "match_score", precision = 3, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
