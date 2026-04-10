package com.classpulse.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instructor_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InstructorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String department;
    private String bio;
}
