package com.classpulse.domain.course;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "async_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_payload", columnDefinition = "jsonb")
    private Map<String, Object> inputPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_payload", columnDefinition = "jsonb")
    private Map<String, Object> resultPayload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void complete(Map<String, Object> result) {
        this.status = "COMPLETED";
        this.resultPayload = result;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
