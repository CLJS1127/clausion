package com.classpulse.domain.codeanalysis;

import com.classpulse.domain.course.CurriculumSkill;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_feedbacks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private CodeSubmission submission;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "end_line_number")
    private Integer endLineNumber;

    @Column(nullable = false, length = 20)
    private String severity; // ERROR, WARNING, INFO, GOOD

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "twin_linked")
    @Builder.Default
    private Boolean twinLinked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_skill_id")
    private CurriculumSkill twinSkill;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
