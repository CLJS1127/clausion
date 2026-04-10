package com.classpulse.api;

import com.classpulse.domain.course.AsyncJob;
import com.classpulse.domain.course.AsyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final AsyncJobRepository asyncJobRepository;

    // --- DTOs ---

    public record JobStatusResponse(
            Long id, String jobType, String status,
            Map<String, Object> inputPayload,
            Map<String, Object> resultPayload,
            String errorMessage,
            LocalDateTime createdAt, LocalDateTime completedAt
    ) {
        public static JobStatusResponse from(AsyncJob job) {
            return new JobStatusResponse(
                    job.getId(), job.getJobType(), job.getStatus(),
                    job.getInputPayload(), job.getResultPayload(),
                    job.getErrorMessage(),
                    job.getCreatedAt(), job.getCompletedAt()
            );
        }
    }

    // --- Endpoints ---

    @GetMapping("/{jobId}/status")
    public ResponseEntity<JobStatusResponse> getStatus(@PathVariable Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        return ResponseEntity.ok(JobStatusResponse.from(job));
    }
}
