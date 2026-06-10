package com.classpulse.ai;

import com.classpulse.ai.TwinDataCollector.TwinInferenceContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 디지털 트윈 규칙 기반 점수 계산기.
 * TwinInferenceEngine의 하이브리드 추론 중 "규칙 기반" 단계를 담당하는 순수 로직으로,
 * 외부 의존성(DB, LLM) 없이 TwinInferenceContext만으로 5차원 점수를 결정적으로 계산합니다.
 * (LLM 보정과 분리되어 있어 OpenAI 장애 시에도 이 점수는 항상 계산 가능합니다.)
 */
@Slf4j
public final class TwinRuleCalculator {

    /** 한 번의 추론에서 허용하는 최대 점수 변동 폭 (이상치 방지) */
    public static final double MAX_SCORE_DELTA = 25.0;

    private TwinRuleCalculator() {
    }

    /** 규칙 기반 5차원 점수 */
    public record RuleScores(
            double mastery,
            double execution,
            double motivation,
            double retentionRisk,
            double consultationNeed
    ) {}

    /**
     * 멀티소스 학습 데이터로부터 규칙 기반 점수를 계산합니다.
     *
     * @param today 기준일 (테스트 가능성을 위해 주입)
     */
    public static RuleScores calculate(TwinInferenceContext ctx, LocalDate today) {
        // Mastery: confidence + review rate + code quality + skill understanding
        double avgSkillUnderstanding = ctx.latestSkillSnapshots().stream()
                .mapToDouble(s -> s.getUnderstandingScore().doubleValue())
                .average().orElse(50.0);
        double masteryScore = (ctx.avgConfidence() / 5.0) * 30.0
                + ctx.reviewCompletionRate() * 25.0
                + ctx.codeAnalysis().goodRate() * 25.0
                + avgSkillUnderstanding * 0.20;

        // Execution: review rate + code submission frequency + skill practice + chat engagement
        double codeSubmissionFreq = Math.min(100.0, ctx.codeAnalysis().totalSubmissions() * 10.0);
        double chatEngagementScore = Math.min(100.0, ctx.chatEngagement().totalUserMessages() * 5.0);
        double avgSkillPractice = ctx.latestSkillSnapshots().stream()
                .mapToDouble(s -> s.getPracticeScore().doubleValue())
                .average().orElse(50.0);
        double executionScore = ctx.reviewCompletionRate() * 35.0
                + codeSubmissionFreq * 0.25
                + avgSkillPractice * 0.20
                + chatEngagementScore * 0.20;

        // Motivation: confidence + activity + streak + XP velocity
        double activityScore = Math.min(100.0, ctx.recentReflections().size() * 10.0);
        double streakScore = Math.min(25.0, ctx.gamification().streakDays() * 3.0);
        double xpVelocityScore = Math.min(25.0, ctx.gamification().weeklyXp() / 4.0);
        double motivationScore = (ctx.avgConfidence() / 5.0) * 25.0
                + activityScore * 0.25
                + streakScore
                + xpVelocityScore;

        // Retention risk: penalize low review, stuck points, low code quality, inactivity
        long daysSinceLastActivity = ctx.gamification().lastActivityDate() != null
                ? ChronoUnit.DAYS.between(ctx.gamification().lastActivityDate(), today)
                : 30;
        double activityProximityBonus = daysSinceLastActivity < 3 ? 15.0 : daysSinceLastActivity < 7 ? 7.0 : 0.0;
        double streakBonus = ctx.gamification().streakDays() > 0 ? 10.0 : 0.0;
        double retentionRisk = Math.max(0, 100.0
                - ctx.reviewCompletionRate() * 30.0
                - (5 - ctx.stuckCount()) * 6.0
                - ctx.codeAnalysis().goodRate() * 20.0
                - activityProximityBonus
                - streakBonus);

        // Consultation need
        double consultationNeed = ctx.stuckCount() * 15.0
                + (5.0 - ctx.avgConfidence()) * 10.0
                + (ctx.hasRecentConsultation() ? 0.0 : 15.0)
                + (ctx.codeAnalysis().goodRate() < 0.4 ? 10.0 : 0.0)
                + (motivationScore < 30 ? 5.0 : 0.0);
        consultationNeed = clamp(consultationNeed);

        return new RuleScores(masteryScore, executionScore, motivationScore, retentionRisk, consultationNeed);
    }

    /** 5차원 점수를 종합 위험도로 환산합니다. */
    public static double overallRisk(double mastery, double motivation, double retentionRisk, double consultationNeed) {
        return retentionRisk * 0.3 + (100 - motivation) * 0.2
                + consultationNeed * 0.2 + (100 - mastery) * 0.3;
    }

    /** 이전 점수 대비 변동 폭을 MAX_SCORE_DELTA로 제한합니다 (이상치 방지). */
    public static double capDelta(double previous, double current) {
        double delta = current - previous;
        if (Math.abs(delta) > MAX_SCORE_DELTA) {
            log.warn("Score delta capped: previous={}, current={}, delta={}", previous, current, delta);
            return clamp(previous + Math.signum(delta) * MAX_SCORE_DELTA);
        }
        return current;
    }

    /** 점수를 [0, 100] 범위로 제한합니다. */
    public static double clamp(double value) {
        return Math.min(100.0, Math.max(0.0, value));
    }
}
