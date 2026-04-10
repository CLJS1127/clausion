import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { reflectionsApi } from '../../api/reflections';
import ReflectionTimeline from '../../components/student/ReflectionTimeline';
import { useCourseId } from '../../hooks/useCourseId';

interface ReflectionForm {
  todayContent: string;
  stuckPoint: string;
  confidence: number;
  revisitConcept: string;
  freeText: string;
}

const initialForm: ReflectionForm = {
  todayContent: '',
  stuckPoint: '',
  confidence: 3,
  revisitConcept: '',
  freeText: '',
};

const AI_REASON = '망각 곡선 분석 결과, 오늘 학습한 재귀 함수 개념의 복습이 3일 후에 필요합니다.';
const PREV_COMPARISON = '지난 회고에서 자신감 2점이었던 재귀 주제가 오늘 3점으로 상승했습니다.';
const NEXT_REVIEW_DATE = '2026년 4월 11일 (토)';

const Reflection: React.FC = () => {
  const [form, setForm] = useState<ReflectionForm>(initialForm);
  const [submitted, setSubmitted] = useState(false);
  const courseId = useCourseId();
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: () => {
      if (!courseId) throw new Error('코스가 선택되지 않았습니다');
      return reflectionsApi.createReflection({
        courseId: Number(courseId),
        content: [
          `[오늘 학습] ${form.todayContent}`,
          `[다시 볼 개념] ${form.revisitConcept}`,
          `[자유 회고] ${form.freeText}`,
        ].join('\n'),
        stuckPoint: form.stuckPoint,
        selfConfidenceScore: form.confidence,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reflections', courseId] });
      setSubmitted(true);
    },
  });

  const update = (key: keyof ReflectionForm, value: string | number) =>
    setForm((prev) => ({ ...prev, [key]: value }));

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-slate-100">
        <div className="max-w-5xl mx-auto px-6 py-3">
          <h1 className="text-xl font-bold text-slate-900">학습 회고</h1>
          <p className="text-xs text-slate-500">
            오늘의 학습을 돌아보고 트윈을 업데이트하세요
          </p>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Form (3/5) */}
          <div className="lg:col-span-3 space-y-6">
            {submitted ? (
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="rounded-2xl bg-white p-8 shadow-sm border border-slate-100 text-center"
              >
                <div className="text-4xl mb-3">🎉</div>
                <h2 className="text-lg font-bold text-slate-900 mb-2">
                  회고가 저장되었습니다!
                </h2>
                <p className="text-sm text-slate-500 mb-4">
                  트윈이 업데이트되었어요. 다음 복습은{' '}
                  <span className="font-semibold text-indigo-600">
                    {NEXT_REVIEW_DATE}
                  </span>{' '}
                  에 예정되어 있습니다.
                </p>
                <button
                  onClick={() => {
                    setForm(initialForm);
                    setSubmitted(false);
                  }}
                  className="rounded-xl bg-indigo-600 px-6 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 transition-colors"
                >
                  새 회고 작성
                </button>
              </motion.div>
            ) : (
              <motion.div
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                className="rounded-2xl bg-white p-6 shadow-sm border border-slate-100 space-y-5"
              >
                {/* 오늘 학습한 내용 */}
                <div>
                  <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                    오늘 학습한 내용
                  </label>
                  <textarea
                    value={form.todayContent}
                    onChange={(e) => update('todayContent', e.target.value)}
                    rows={3}
                    placeholder="오늘 어떤 내용을 학습했나요?"
                    className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 resize-y"
                  />
                </div>

                {/* 막힌 지점 */}
                <div>
                  <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                    막힌 지점
                  </label>
                  <textarea
                    value={form.stuckPoint}
                    onChange={(e) => update('stuckPoint', e.target.value)}
                    rows={2}
                    placeholder="학습 중 이해가 안 되거나 어려웠던 부분이 있나요?"
                    className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 resize-y"
                  />
                </div>

                {/* 자신감 점수 */}
                <div>
                  <label className="block text-sm font-semibold text-slate-800 mb-2">
                    자신감 점수
                  </label>
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        onClick={() => update('confidence', star)}
                        className="focus:outline-none"
                      >
                        <span
                          className={`text-2xl transition-colors ${
                            star <= form.confidence
                              ? 'text-amber-400'
                              : 'text-slate-300 hover:text-amber-300'
                          }`}
                        >
                          ★
                        </span>
                      </button>
                    ))}
                    <span className="ml-2 text-sm text-slate-500">
                      {form.confidence}/5
                    </span>
                  </div>
                </div>

                {/* 다시 보면 좋을 개념 */}
                <div>
                  <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                    다시 보면 좋을 개념
                  </label>
                  <input
                    type="text"
                    value={form.revisitConcept}
                    onChange={(e) => update('revisitConcept', e.target.value)}
                    placeholder="복습이 필요한 개념을 적어주세요"
                    className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400"
                  />
                </div>

                {/* 자유 회고 */}
                <div>
                  <label className="block text-sm font-semibold text-slate-800 mb-1.5">
                    자유 회고
                  </label>
                  <textarea
                    value={form.freeText}
                    onChange={(e) => update('freeText', e.target.value)}
                    rows={3}
                    placeholder="자유롭게 오늘의 학습을 돌아보세요"
                    className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 resize-y"
                  />
                </div>

                <button
                  onClick={() => mutation.mutate()}
                  disabled={
                    mutation.isPending || !form.todayContent.trim() || !courseId
                  }
                  className="w-full rounded-xl bg-indigo-600 py-3 text-sm font-medium text-white hover:bg-indigo-700 active:bg-indigo-800 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  {mutation.isPending ? (
                    <>
                      <svg
                        className="animate-spin w-4 h-4"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <circle
                          className="opacity-25"
                          cx="12"
                          cy="12"
                          r="10"
                          stroke="currentColor"
                          strokeWidth="4"
                        />
                        <path
                          className="opacity-75"
                          fill="currentColor"
                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                        />
                      </svg>
                      저장 중...
                    </>
                  ) : (
                    '회고 저장 및 트윈 업데이트'
                  )}
                </button>
              </motion.div>
            )}

            {/* Previous reflections */}
            <ReflectionTimeline />
          </div>

          {/* Right sidebar (2/5) */}
          <div className="lg:col-span-2 space-y-5">
            {/* AI 추천 이유 */}
            <div className="rounded-2xl bg-indigo-50/60 border border-indigo-200 p-5">
              <h3 className="text-sm font-bold text-indigo-800 mb-2">
                AI 추천 이유
              </h3>
              <p className="text-xs text-indigo-600 leading-relaxed">
                {AI_REASON}
              </p>
            </div>

            {/* 이전 회고 비교 */}
            <div className="rounded-2xl bg-emerald-50/60 border border-emerald-100 p-5">
              <h3 className="text-sm font-bold text-emerald-800 mb-2">
                이전 회고 비교
              </h3>
              <p className="text-xs text-emerald-600 leading-relaxed">
                {PREV_COMPARISON}
              </p>
            </div>

            {/* 다음 복습 예정일 */}
            <div className="rounded-2xl bg-amber-50/60 border border-amber-100 p-5">
              <h3 className="text-sm font-bold text-amber-800 mb-2">
                다음 복습 예정일
              </h3>
              <p className="text-lg font-bold text-amber-700">
                {NEXT_REVIEW_DATE}
              </p>
              <p className="text-xs text-amber-600 mt-1">
                망각 곡선 기반 최적 타이밍
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Reflection;
