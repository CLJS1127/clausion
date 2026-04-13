package com.classpulse.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {
    List<AsyncJob> findByJobTypeAndStatus(String jobType, String status);
}
