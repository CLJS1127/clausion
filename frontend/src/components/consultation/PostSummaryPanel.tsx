import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { consultationsApi } from '../../api/consultations';

interface ActionItem {
  id: string;
  day: string;
  task: string;
  completed: boolean;
}

const MOCK_ACTION_PLAN: ActionItem[] = [
  { id: 'a1', day: 'Day 1-2', task: 'React useState/useEffect 공식 문서 정독 및 정리', completed: false },
  { id: 'a2', day: 'Day 3', task: 'TypeScript 제네릭 기초 실습 (3문제)', completed: false },
  { id: 'a3', day: 'Day 4-5', task: '미니 프로젝트: Todo 앱 상태 관리 구현', completed: false },
  { id: 'a4', day: 'Day 6', task: '코드 리뷰 요청 및 피드백 반영', completed: false },
  { id: 'a5', day: 'Day 7', task: '1주 복습 퀴즈 완료 및 자기 평가', completed: false },
];

const SUMMARY_TEXT = '학생은 React 상태 관리와 TypeScript 타입 시스템에서 근본적인 이해 부족을 보이고 있습니다. 특히 useEffect의 라이프사이클과 의존성 배열 관리에 어려움을 겪고 있으며, 실습 과제 수행 시 자신감이 크게 저하되는 패턴이 관찰됩니다.';

export default function PostSummaryPanel({ consultationId }: { consultationId?: string }) {
  const [actionItems, setActionItems] = useState(MOCK_ACTION_PLAN);
  const [saved, setSaved] = useState(false);

  const toggleItem = (id: string) => {
    setActionItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, completed: !item.completed } : item
      )
    );
  };

  const saveMutation = useMutation({
    mutationFn: () => {
      if (!consultationId) throw new Error('No consultation ID');
      return consultationsApi.createSummary(consultationId, {
        summaryText: SUMMARY_TEXT,
        actionPlanJson: JSON.stringify(actionItems),
      });
    },
    onSuccess: () => setSaved(true),
  });

  const completedCount = actionItems.filter((i) => i.completed).length;

  return (
    <div className="h-full bg-white rounded-2xl p-5 space-y-4 overflow-y-auto">
      {/* Summary Card */}
      <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
        <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">상담 요약</h4>
        <p className="text-sm text-slate-700 leading-relaxed">{SUMMARY_TEXT}</p>
      </div>

      {/* Cause Analysis Card */}
      <div className="p-4 rounded-xl bg-amber-50 border border-amber-100">
        <h4 className="text-xs font-semibold text-amber-700 uppercase tracking-wider mb-2">원인 분석</h4>
        <ul className="space-y-1.5">
          <li className="flex items-start gap-2 text-sm text-amber-800">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400 mt-1.5 flex-shrink-0" />
            JavaScript 기초 개념(클로저, 비동기) 선행 학습 부족
          </li>
          <li className="flex items-start gap-2 text-sm text-amber-800">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400 mt-1.5 flex-shrink-0" />
            이론 학습 후 실습 연결이 부족한 학습 패턴
          </li>
          <li className="flex items-start gap-2 text-sm text-amber-800">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400 mt-1.5 flex-shrink-0" />
            에러 발생 시 디버깅 전략 부재로 인한 좌절감
          </li>
        </ul>
      </div>

      {/* 7-Day Action Plan */}
      <div className="p-4 rounded-xl bg-indigo-50 border border-indigo-200">
        <div className="flex items-center justify-between mb-3">
          <h4 className="text-xs font-semibold text-indigo-700 uppercase tracking-wider">
            7일 액션플랜
          </h4>
          <span className="text-[11px] text-indigo-500 font-medium">
            {completedCount}/{actionItems.length} 완료
          </span>
        </div>

        <div className="h-1.5 bg-indigo-100 rounded-full overflow-hidden mb-3">
          <div
            className="h-full bg-indigo-500 rounded-full transition-all duration-300"
            style={{ width: `${actionItems.length > 0 ? (completedCount / actionItems.length) * 100 : 0}%` }}
          />
        </div>

        <div className="space-y-2">
          {actionItems.map((item) => (
            <label
              key={item.id}
              className="flex items-start gap-2.5 cursor-pointer group"
            >
              <button
                type="button"
                onClick={() => toggleItem(item.id)}
                className={`w-4 h-4 mt-0.5 rounded border-2 flex items-center justify-center flex-shrink-0 transition-all ${
                  item.completed
                    ? 'bg-indigo-600 border-indigo-600 text-white'
                    : 'border-indigo-300 group-hover:border-indigo-500'
                }`}
              >
                {item.completed && (
                  <svg className="w-2.5 h-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </button>
              <div>
                <span className="text-[11px] font-semibold text-indigo-600">{item.day}</span>
                <p className={`text-xs leading-relaxed transition-all ${
                  item.completed ? 'text-slate-400 line-through' : 'text-slate-700'
                }`}>
                  {item.task}
                </p>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Recommended Course Card */}
      <div className="p-4 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 text-white">
        <h4 className="text-xs font-semibold uppercase tracking-wider mb-2 text-white/80">추천 학습</h4>
        <p className="text-sm font-medium mb-1">JavaScript 핵심 개념 마스터</p>
        <p className="text-xs text-white/70 mb-3">
          클로저, 비동기 프로그래밍, 프로토타입 체인 등 React 학습을 위한 필수 선행 개념
        </p>
        <button className="text-xs font-medium px-3 py-1.5 rounded-lg bg-white/20 hover:bg-white/30 transition-colors">
          과정 보기
        </button>
      </div>

      {/* Save Button */}
      <button
        onClick={() => saveMutation.mutate()}
        disabled={saveMutation.isPending || saved}
        className={`w-full py-3 text-sm font-semibold rounded-xl transition-colors shadow-lg shadow-indigo-200 ${
          saved
            ? 'bg-emerald-600 text-white'
            : 'bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50'
        }`}
      >
        {saved ? '저장 완료' : saveMutation.isPending ? '저장 중...' : '액션플랜 Twin에 저장'}
      </button>

      {saveMutation.isError && (
        <p className="text-xs text-rose-500 text-center">저장 실패. 다시 시도해주세요.</p>
      )}
    </div>
  );
}
