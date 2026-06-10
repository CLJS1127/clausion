package com.classpulse.domain.gamification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XP 적립과 레벨업 규칙 단위 테스트.
 * 레벨업 공식: nextLevelXp = (int)(100 * level^1.2), 초과분 XP는 다음 레벨로 이월된다.
 */
class StudentGamificationTest {

    private StudentGamification fresh() {
        return StudentGamification.builder().build();
    }

    @Test
    @DisplayName("기본 상태: 레벨 1, 다음 레벨까지 100 XP")
    void builderDefaults() {
        StudentGamification g = fresh();
        assertThat(g.getLevel()).isEqualTo(1);
        assertThat(g.getCurrentXp()).isZero();
        assertThat(g.getNextLevelXp()).isEqualTo(100);
        assertThat(g.getLevelTitle()).isEqualTo("초보 학습자");
    }

    @Test
    @DisplayName("임계치 미만 적립은 레벨이 오르지 않는다")
    void belowThresholdNoLevelUp() {
        StudentGamification g = fresh();
        g.addXp(99);
        assertThat(g.getLevel()).isEqualTo(1);
        assertThat(g.getCurrentXp()).isEqualTo(99);
        assertThat(g.getTotalXpEarned()).isEqualTo(99);
    }

    @Test
    @DisplayName("정확히 임계치만큼 적립하면 레벨업하고 잔여 XP는 0이 된다")
    void exactThresholdLevelsUp() {
        StudentGamification g = fresh();
        g.addXp(100);
        assertThat(g.getLevel()).isEqualTo(2);
        assertThat(g.getCurrentXp()).isZero();
        assertThat(g.getNextLevelXp()).isEqualTo((int) (100 * Math.pow(2, 1.2))); // 229
    }

    @Test
    @DisplayName("대량 적립 시 여러 레벨을 한 번에 통과하고 초과분이 이월된다")
    void multiLevelUpCarriesOverXp() {
        StudentGamification g = fresh();
        g.addXp(500);
        // 500 → lv1(100 소모) → lv2(229 소모) → lv3, 잔여 171
        assertThat(g.getLevel()).isEqualTo(3);
        assertThat(g.getCurrentXp()).isEqualTo(171);
        assertThat(g.getTotalXpEarned()).isEqualTo(500);
    }

    @Test
    @DisplayName("레벨 구간에 따라 칭호가 바뀐다 (lv4 → 열정적 코더)")
    void titleChangesByLevelBand() {
        StudentGamification g = fresh();
        g.addXp(100 + 229 + 373); // lv1→2→3→4 누적 임계치
        assertThat(g.getLevel()).isEqualTo(4);
        assertThat(g.getCurrentXp()).isZero();
        assertThat(g.getLevelTitle()).isEqualTo("열정적 코더");
    }
}
