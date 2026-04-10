package com.classpulse.domain.course;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "curriculum_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CurriculumSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(length = 20)
    private String difficulty = "MEDIUM";

    @ManyToMany
    @JoinTable(
        name = "skill_prerequisites",
        joinColumns = @JoinColumn(name = "skill_id"),
        inverseJoinColumns = @JoinColumn(name = "prerequisite_skill_id")
    )
    @Builder.Default
    private Set<CurriculumSkill> prerequisites = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
