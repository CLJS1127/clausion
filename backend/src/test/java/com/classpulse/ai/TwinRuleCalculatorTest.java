package com.classpulse.ai;

import com.classpulse.ai.TwinDataCollector.ChatEngagementSummary;
import com.classpulse.ai.TwinDataCollector.CodeAnalysisSummary;
import com.classpulse.ai.TwinDataCollector.GamificationSummary;
import com.classpulse.ai.TwinDataCollector.TwinInferenceContext;
import com.classpulse.ai.TwinRuleCalculator.RuleScores;
import com.classpulse.domain.learning.Reflection;
import com.classpulse.domain.twin.SkillMasterySnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * 디지털 트윈 규칙 기반 점수 계산 단위 테스트.
 * LLM 보정 이전 단계의 결정적(deterministic) 점수가 설계된 가중치대로 계산되는지 검증한다.
 */
class TwinRuleCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 10);

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private static SkillMasterySnapshot snapshot(double understanding, double practice) {
        return SkillMasterySnapshot.builder()
                .understandingScore(BigDecimal.valueOf(understanding))
                .practiceScore(BigDecimal.valueOf(practice))
                .build();
    }

    private static TwinInferenceContext context(
            int reflectionCount, double avgConfidence, long stuckCount,
            double reviewCompletionRate, boolean hasRecentConsultation,
            CodeAnalysisSummary code, ChatEngagementSummary chat, GamificationSummary gamification,
            List<SkillMasterySnapshot> snapshots) {
        List<Reflection> reflections = new ArrayList<>();
        for (int i = 0; i < reflectionCount; i++) reflections.add(new Reflection());
        return new TwinInferenceContext(
                reflections, avgConfidence, stuckCount,
                10, Math.round(10 * reviewCompletionRate), reviewCompletionRate,
                List.of(), hasRecentConsultation,
                code, chat, gamification,
                snapshots, List.of(), null);
    }

    // ── calculate() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("모범 학생: 모든 지표가 좋으면 숙달/실행/동기는 높고 위험/상담필요는 0이다")
    void idealStudent() {
        TwinInferenceContext ctx = context(
                3, 5.0, 0, 1.0, true,
                new CodeAnalysisSummary(10, 10, 0, 0, 1.0),
                new ChatEngagementSummary(5, 20, null),
                new GamificationSummary(3, "초보 학습자", 500, 7, 100, TODAY),
                List.of(snapshot(90, 80), snapshot(90, 80)));

        RuleScores scores = TwinRuleCalculator.calculate(ctx, TODAY);

        // mastery = 자신감 30 + 복습률 25 + 코드품질 25 + 스킬이해(90) 18
        assertThat(scores.mastery()).isCloseTo(98.0, within(1e-9));
        // execution = 복습률 35 + 제출빈도 25 + 스킬실습(80) 16 + 챗봇활용 20
        assertThat(scores.execution()).isCloseTo(96.0, within(1e-9));
        // motivation = 자신감 25 + 활동(3건) 7.5 + 스트릭(7일) 21 + XP속도 25
        assertThat(scores.motivation()).isCloseTo(78.5, within(1e-9));
        assertThat(scores.retentionRisk()).isCloseTo(0.0, within(1e-9));
        assertThat(scores.consultationNeed()).isCloseTo(0.0, within(1e-9));
    }

    @Test
    @DisplayName("위기 학생: 활동이 없고 막힌 점이 많으면 위험도가 치솟고 상담필요는 100으로 클램프된다")
    void strugglingStudent() {
        TwinInferenceContext ctx = context(
                0, 1.0, 4, 0.0, false,
                new CodeAnalysisSummary(0, 0, 0, 0, 0.0),
                new ChatEngagementSummary(0, 0, null),
                new GamificationSummary(1, "초보 학습자", 0, 0, 0, null),
                List.of());

        RuleScores scores = TwinRuleCalculator.calculate(ctx, TODAY);

        // 스냅샷이 없으면 스킬 이해/실습은 기본값 50으로 계산된다
        assertThat(scores.mastery()).isCloseTo(16.0, within(1e-9));   // 6 + 0 + 0 + 10
        assertThat(scores.execution()).isCloseTo(10.0, within(1e-9)); // 0 + 0 + 10 + 0
        assertThat(scores.motivation()).isCloseTo(5.0, within(1e-9));
        assertThat(scores.retentionRisk()).isCloseTo(94.0, within(1e-9));
        // 60(막힘) + 40(자신감) + 15(상담없음) + 10(코드품질) + 5(동기저하) = 130 → 100
        assertThat(scores.consultationNeed()).isCloseTo(100.0, within(1e-9));
    }

    @Test
    @DisplayName("막힌 점이 5개를 넘으면 망각 위험이 기본치(100)를 초과해 가산된다")
    void manyStuckPointsIncreaseRetentionRisk() {
        TwinInferenceContext base = context(
                0, 1.0, 0, 0.0, false,
                new CodeAnalysisSummary(0, 0, 0, 0, 0.0),
                new ChatEngagementSummary(0, 0, null),
                new GamificationSummary(1, "초보 학습자", 0, 0, 0, null),
                List.of());
        TwinInferenceContext stuck10 = context(
                0, 1.0, 10, 0.0, false,
                new CodeAnalysisSummary(0, 0, 0, 0, 0.0),
                new ChatEngagementSummary(0, 0, null),
                new GamificationSummary(1, "초보 학습자", 0, 0, 0, null),
                List.of());

        double riskBase = TwinRuleCalculator.calculate(base, TODAY).retentionRisk();
        double riskStuck = TwinRuleCalculator.calculate(stuck10, TODAY).retentionRisk();

        // stuckCount 0 → 100 - 5*6 = 70, stuckCount 10 → 100 + 5*6 = 130
        // (규칙 점수는 상한이 없고, 엔진이 LLM 보정 후 clamp로 [0,100]을 보장한다)
        assertThat(riskBase).isCloseTo(70.0, within(1e-9));
        assertThat(riskStuck).isCloseTo(130.0, within(1e-9));
    }

    @Test
    @DisplayName("최근 활동일이 가까울수록 망각 위험이 낮아진다 (3일 미만 -15, 7일 미만 -7)")
    void recentActivityLowersRetentionRisk() {
        GamificationSummary activeToday = new GamificationSummary(1, "초보 학습자", 0, 0, 0, TODAY);
        GamificationSummary active5DaysAgo = new GamificationSummary(1, "초보 학습자", 0, 0, 0, TODAY.minusDays(5));
        GamificationSummary inactive = new GamificationSummary(1, "초보 학습자", 0, 0, 0, TODAY.minusDays(30));

        double riskToday = calcRetention(activeToday);
        double risk5Days = calcRetention(active5DaysAgo);
        double riskInactive = calcRetention(inactive);

        assertThat(riskToday).isCloseTo(riskInactive - 15.0, within(1e-9));
        assertThat(risk5Days).isCloseTo(riskInactive - 7.0, within(1e-9));
    }

    private double calcRetention(GamificationSummary gamification) {
        TwinInferenceContext ctx = context(
                0, 3.0, 0, 0.5, true,
                new CodeAnalysisSummary(0, 0, 0, 0, 0.5),
                new ChatEngagementSummary(0, 0, null),
                gamification,
                List.of());
        return TwinRuleCalculator.calculate(ctx, TODAY).retentionRisk();
    }

    // ── overallRisk / capDelta / clamp ───────────────────────────────────

    @Nested
    class Helpers {

        @Test
        @DisplayName("종합 위험도: 최상 상태면 0, 최악 상태면 100")
        void overallRiskBounds() {
            assertThat(TwinRuleCalculator.overallRisk(100, 100, 0, 0)).isCloseTo(0.0, within(1e-9));
            assertThat(TwinRuleCalculator.overallRisk(0, 0, 100, 100)).isCloseTo(100.0, within(1e-9));
        }

        @Test
        @DisplayName("capDelta: 변동 폭이 25 이내면 그대로, 초과하면 ±25로 제한된다")
        void capDeltaLimitsSwing() {
            assertThat(TwinRuleCalculator.capDelta(50, 60)).isEqualTo(60);
            assertThat(TwinRuleCalculator.capDelta(50, 75)).isEqualTo(75);   // 경계값
            assertThat(TwinRuleCalculator.capDelta(50, 90)).isEqualTo(75);   // +40 → +25
            assertThat(TwinRuleCalculator.capDelta(50, 10)).isEqualTo(25);   // -40 → -25
        }

        @Test
        @DisplayName("capDelta 결과도 [0,100] 범위를 벗어나지 않는다")
        void capDeltaClampsResult() {
            assertThat(TwinRuleCalculator.capDelta(95, 130)).isEqualTo(100); // 95+25=120 → 100
            assertThat(TwinRuleCalculator.capDelta(5, -40)).isEqualTo(0);    // 5-25=-20 → 0
        }

        @Test
        @DisplayName("clamp: [0,100] 범위 제한")
        void clampBounds() {
            assertThat(TwinRuleCalculator.clamp(-5)).isEqualTo(0);
            assertThat(TwinRuleCalculator.clamp(105)).isEqualTo(100);
            assertThat(TwinRuleCalculator.clamp(50)).isEqualTo(50);
        }
    }
}
