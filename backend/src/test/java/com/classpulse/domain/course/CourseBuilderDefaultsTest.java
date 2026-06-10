package com.classpulse.domain.course;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회귀 테스트: Lombok @Builder는 필드 초기값을 무시하므로 @Builder.Default가 없으면
 * builder로 생성한 엔티티의 기본값이 NULL로 저장된다.
 * (시드 강의가 approval_status NULL로 저장되어 학생 강의 목록에서 누락되던 버그 — V20 백필 참고)
 */
class CourseBuilderDefaultsTest {

    @Test
    @DisplayName("Course.builder()로 생성해도 상태/승인/정원 기본값이 유지된다")
    void courseBuilderKeepsDefaults() {
        Course course = Course.builder().title("테스트 과정").build();

        assertThat(course.getStatus()).isEqualTo("ACTIVE");
        assertThat(course.getApprovalStatus()).isEqualTo("PENDING");
        assertThat(course.getMaxCapacity()).isEqualTo(30);
    }

    @Test
    @DisplayName("CurriculumSkill.builder()로 생성해도 난이도 기본값이 유지된다")
    void curriculumSkillBuilderKeepsDefaults() {
        CurriculumSkill skill = CurriculumSkill.builder().name("테스트 스킬").build();

        assertThat(skill.getDifficulty()).isEqualTo("MEDIUM");
    }
}
