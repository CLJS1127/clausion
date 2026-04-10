import React from 'react';
import { motion } from 'framer-motion';
import ConsultationActionCard from '../../components/student/ConsultationActionCard';
import type { Consultation, ActionPlan } from '../../types';

const PAST_CONSULTATIONS: (Consultation & { actionPlans: ActionPlan[] })[] = [
  {
    id: 'con2',
    studentId: 's1',
    instructorId: 'i1',
    courseId: 'c1',
    scheduledAt: '2026-04-03T14:00:00Z',
    status: 'COMPLETED',
    summaryText:
      '재귀 함수 이해도 부족에 대한 상담을 진행했습니다. 기초 예제부터 단계별로 접근하는 전략을 수립했으며, 학생의 자신감 회복을 위해 작은 성공 경험을 쌓는 것이 중요하다고 조언했습니다.',
    actionPlanJson: '[]',
    createdAt: '2026-04-01T10:00:00Z',
    actionPlans: [
      {
        title: '재귀 기초 예제 5개 풀기',
        dueDate: '2026-04-07',
        linkedSkillId: 'sk2',
        priority: 'HIGH',
        status: 'COMPLETED',
      },
      {
        title: '피보나치 구현 변형 3가지',
        dueDate: '2026-04-09',
        linkedSkillId: 'sk2',
        priority: 'MEDIUM',
        status: 'IN_PROGRESS',
      },
      {
        title: '트리 순회 재귀 구현',
        dueDate: '2026-04-12',
        linkedSkillId: 'sk8',
        priority: 'MEDIUM',
        status: 'PENDING',
      },
    ],
  },
  {
    id: 'con3',
    studentId: 's1',
    instructorId: 'i1',
    courseId: 'c1',
    scheduledAt: '2026-03-27T14:00:00Z',
    status: 'COMPLETED',
    summaryText:
      '첫 상담으로 학습 스타일과 목표를 파악했습니다. 시각적 학습을 선호하며, 실습 위주의 학습이 효과적일 것으로 판단됩니다.',
    actionPlanJson: '[]',
    createdAt: '2026-03-25T10:00:00Z',
    actionPlans: [
      {
        title: '학습 루틴 설정',
        dueDate: '2026-03-30',
        linkedSkillId: '',
        priority: 'HIGH',
        status: 'COMPLETED',
      },
      {
        title: '기초 과제 3개 완료',
        dueDate: '2026-04-02',
        linkedSkillId: 'sk1',
        priority: 'MEDIUM',
        status: 'COMPLETED',
      },
    ],
  },
];

const PLAN_STATUS_STYLES: Record<
  string,
  { icon: string; color: string; bg: string }
> = {
  COMPLETED: {
    icon: '✓',
    color: 'text-emerald-600',
    bg: 'bg-emerald-50',
  },
  IN_PROGRESS: {
    icon: '◐',
    color: 'text-amber-600',
    bg: 'bg-amber-50',
  },
  PENDING: {
    icon: '○',
    color: 'text-slate-400',
    bg: 'bg-slate-50',
  },
};

const ConsultationPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-slate-100">
        <div className="max-w-5xl mx-auto px-6 py-3">
          <h1 className="text-xl font-bold text-slate-900">상담 관리</h1>
          <p className="text-xs text-slate-500">
            강사 상담 일정과 실행 계획을 확인하세요
          </p>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-6 space-y-6">
        {/* Upcoming consultations card */}
        <ConsultationActionCard />

        {/* Past consultations detail */}
        <div className="rounded-2xl bg-white p-6 shadow-sm border border-slate-100">
          <h2 className="text-lg font-bold text-slate-900 mb-5">
            지난 상담 기록
          </h2>

          <div className="space-y-6">
            {PAST_CONSULTATIONS.map((con, i) => {
              const completedPlans = con.actionPlans.filter(
                (p) => p.status === 'COMPLETED',
              ).length;
              const totalPlans = con.actionPlans.length;
              const completionRate =
                totalPlans > 0
                  ? Math.round((completedPlans / totalPlans) * 100)
                  : 0;

              return (
                <motion.div
                  key={con.id}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.08 }}
                  className="border border-slate-100 rounded-xl p-5"
                >
                  {/* Header */}
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <span className="inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold bg-emerald-100 text-emerald-700">
                        완료
                      </span>
                      <span className="text-xs text-slate-400">
                        {new Date(con.scheduledAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>
                    <span className="text-xs text-slate-400">
                      실행률 {completionRate}%
                    </span>
                  </div>

                  {/* Summary */}
                  <p className="text-sm text-slate-700 leading-relaxed mb-4">
                    {con.summaryText}
                  </p>

                  {/* Action Plan Progress */}
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-semibold text-slate-600">
                        실행 계획 ({completedPlans}/{totalPlans})
                      </span>
                    </div>
                    <div className="h-1.5 rounded-full bg-slate-100 overflow-hidden mb-3">
                      <div
                        className="h-full rounded-full bg-gradient-to-r from-indigo-500 to-emerald-500 transition-all duration-500"
                        style={{ width: `${completionRate}%` }}
                      />
                    </div>
                    <div className="space-y-2">
                      {con.actionPlans.map((plan, j) => {
                        const style =
                          PLAN_STATUS_STYLES[plan.status] ??
                          PLAN_STATUS_STYLES.PENDING;
                        return (
                          <div
                            key={j}
                            className={`flex items-center gap-3 rounded-lg p-2.5 ${style.bg}`}
                          >
                            <span
                              className={`w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold ${style.color} bg-white`}
                            >
                              {style.icon}
                            </span>
                            <div className="flex-1 min-w-0">
                              <p
                                className={`text-xs font-medium ${
                                  plan.status === 'COMPLETED'
                                    ? 'text-slate-400 line-through'
                                    : 'text-slate-700'
                                }`}
                              >
                                {plan.title}
                              </p>
                              <p className="text-[10px] text-slate-400">
                                기한:{' '}
                                {new Date(plan.dueDate).toLocaleDateString(
                                  'ko-KR',
                                  { month: 'short', day: 'numeric' },
                                )}{' '}
                                | 우선순위: {plan.priority}
                              </p>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
};

export default ConsultationPage;
