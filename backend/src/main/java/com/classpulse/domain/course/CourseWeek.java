package com.classpulse.domain.course;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_weeks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "week_no", nullable = false)
    private Integer weekNo;

    private String title;
    private String summary;
}
