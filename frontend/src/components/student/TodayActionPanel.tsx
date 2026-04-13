import React, { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { recommendationsApi } from '../../api/recommendations';
import { reviewsApi } from '../../api/reviews';
import { useCourseId } from '../../hooks/useCourseId';
import { useAuthStore } from '../../store/authStore';
import type { Recommendation, ReviewTask } from '../../types';
import { getRecommendationAction, normalizeType } from '../../utils/recommendations';

type ActionItem = {
  id: string;
  title: string;
  description: string;
  badgeLabel: string;
  badgeTone: string;
  eta: string;
  ctaLabel: string;
  onClick: () => void;
};

const TODAY_BADGES = {
  review: { label: '복습 문제', tone: 'bg-indigo-100 text-indigo-700' },
  practice: { label: '실습 추천', tone: 'bg-emerald-100 text-emerald-700' },
  consultation: { label: '상담 액션', tone: 'bg-amber-100 text-amber-700' },
  resource: { label: '자료 보기', tone: 'bg-sky-100 text-sky-700' },
  reflection: { label: '성찰', tone: 'bg-violet-100 text-violet-700' },
};

const TodayActionPanel: React.FC = () => {
  const navigate = useNavigate();
  const courseId = useCourseId();
  const { user } = useAuthStore();
  const studentId = user?.id?.toString() ?? '';

  const { data: reviewTasks = [], isLoading: isReviewsLoading } = useQuery<
    ReviewTask[]
  >({
    queryKey: ['reviewsToday'],
    queryFn: () => reviewsApi.getTodayReviews(),
    staleTime: 0,
  });

  const { data: recommendations = [] } = useQuery<Recommendation[]>({
    queryKey: ['recommendations', studentId, courseId],
    queryFn: () => recommendationsApi.getRecommendations(studentId, courseId),
    enabled: !!studentId,
    staleTime: 30_000,
  });

  const actionableReviews = reviewTasks.filter(
    (task) => task.status === 'PENDING' || task.status === 'IN_PROGRESS',
  );
  const completedReviews = reviewTasks.filter(
    (task) => task.status === 'COMPLETED',
  ).length;

  const items = useMemo<ActionItem[]>(() => {
    const reviewItems = actionableReviews.slice(0, 3).map((task) => ({
      id: `review-${task.id}`,
      title: task.title,
      description: task.reasonSummary,
      badgeLabel: TODAY_BADGES.review.label,
      badgeTone: TODAY_BADGES.review.tone,
      eta: '10~15분',
      ctaLabel: '문제 풀기',
      onClick: () =>
        navigate('/student/review', {
          state: { taskId: task.id },
        }),
    }));

    const recommendationItems = recommendations
      .slice(0, Math.max(0, 3 - reviewItems.length))
      .map((recommendation) => {
        const config =
          TODAY_BADGES[
            normalizeType(recommendation.recommendationType) as keyof typeof TODAY_BADGES
          ] ?? TODAY_BADGES.resource;
        const action = getRecommendationAction(recommendation.recommendationType);
        return {
          id: `rec-${recommendation.id}`,
          title: recommendation.title,
          description: recommendation.reasonSummary,
          badgeLabel: config.label,
          badgeTone: config.tone,
          eta: '5~10분',
          ctaLabel: action.label,
          onClick: () => navigate(action.path),
        };
      });

    const baseItems = [...reviewItems, ...recommendationItems];
    if (baseItems.length >= 3) return baseItems;

    const fallbackItems: ActionItem[] = [
      {
        id: 'reflection-today',
        title: '오늘 학습 성찰 작성',
        description:
          '막힌 포인트와 자신감 점수를 남기면 다음 복습 과제가 더 정교해집니다.',
        badgeLabel: TODAY_BADGES.reflection.label,
        badgeTone: TODAY_BADGES.reflection.tone,
        eta: '3분',
        ctaLabel: '성찰 쓰기',
        onClick: () => navigate('/student/reflection'),
      },
      {
        id: 'next-step-today',
        title: 'AI 다음 단계 추천 확인',
        description:
          '복습, 실습, 상담 우선순위를 한 번에 보고 지금 해야 할 일을 고릅니다.',
        badgeLabel: TODAY_BADGES.practice.label,
        badgeTone: TODAY_BADGES.practice.tone,
        eta: '5분',
        ctaLabel: '추천 보기',
        onClick: () => navigate('/student/next-step'),
      },
      {
        id: 'dashboard-today',
        title: '대시보드 상태 점검',
        description:
          '트윈 상태, 학습 흐름, 팀 스터디 진입 상태를 빠르게 확인합니다.',
        badgeLabel: TODAY_BADGES.resource.label,
        badgeTone: TODAY_BADGES.resource.tone,
        eta: '2분',
        ctaLabel: '대시보드',
        onClick: () => navigate('/student'),
      },
    ];

    return [...baseItems, ...fallbackItems].slice(0, 3);
  }, [actionableReviews, navigate, recommendations]);

  const progress =
    reviewTasks.length > 0 ? (completedReviews / reviewTasks.length) * 100 : 0;

  return (
    <div className="rounded-2xl border border-slate-100 bg-white p-6 shadow-sm">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold text-slate-900">오늘의 할 일</h2>
          <p className="mt-0.5 text-sm text-slate-500">
            {reviewTasks.length > 0
              ? `${completedReviews}/${reviewTasks.length} 복습 완료`
              : '비어 있지 않게 AI 액션을 채워두었습니다.'}
          </p>
        </div>
        <div className="w-28 overflow-hidden rounded-full bg-slate-100">
          <div
            className="h-2 rounded-full bg-gradient-to-r from-indigo-500 to-violet-500 transition-all duration-500"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {isReviewsLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((index) => (
            <div
              key={index}
              className="h-24 animate-pulse rounded-2xl bg-slate-50"
            />
          ))}
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item, index) => (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.04 }}
              className="flex items-start gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4"
            >
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <span
                    className={`inline-flex rounded-full px-2 py-1 text-[11px] font-semibold ${item.badgeTone}`}
                  >
                    {item.badgeLabel}
                  </span>
                  <span className="text-[11px] text-slate-400">{item.eta}</span>
                </div>
                <p className="mt-3 text-sm font-semibold text-slate-900">
                  {item.title}
                </p>
                <p className="mt-1 text-xs leading-5 text-slate-500">
                  {item.description}
                </p>
              </div>

              <button
                onClick={item.onClick}
                className="shrink-0 rounded-xl bg-white px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
              >
                {item.ctaLabel}
              </button>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TodayActionPanel;
