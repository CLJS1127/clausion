package com.classpulse.domain.course;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"weeks", "createdBy"})
    List<Course> findByCreatedByIdAndStatus(Long instructorId, String status);

    List<Course> findByCreatedById(Long instructorId);

    @EntityGraph(attributePaths = {"weeks", "createdBy"})
    List<Course> findByStatus(String status);

    @EntityGraph(attributePaths = {"weeks", "createdBy"})
    Optional<Course> findById(Long id);
}
