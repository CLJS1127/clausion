package com.classpulse.domain.codeanalysis;

import com.classpulse.domain.user.User;
import com.classpulse.domain.course.Course;
import com.classpulse.domain.course.CurriculumSkill;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeSubmission {

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

    @Column(name = "code_content", nullable = false, columnDefinition = "TEXT")
    private String codeContent;

    @Column(length = 30)
    @Builder.Default
    private String language = "javascript";

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CodeFeedback> feedbacks = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
