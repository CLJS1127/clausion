package com.classpulse.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CurriculumSkillRepository extends JpaRepository<CurriculumSkill, Long> {

    @Query("SELECT DISTINCT s FROM CurriculumSkill s LEFT JOIN FETCH s.prerequisites WHERE s.course.id = :courseId")
    List<CurriculumSkill> findByCourseId(@Param("courseId") Long courseId);
}
