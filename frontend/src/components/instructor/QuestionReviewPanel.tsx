import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { questionsApi } from '../../api/questions';
import { useCourseId } from '../../hooks/useCourseId';
import type { Question } from '../../types';
import TagChip from '../common/TagChip';

const MOCK_QUESTIONS: Question[] = [
  {
    id: 'q1',
    courseId: 'c1',
    skillId: 's1',
    questionType: '객관식',
    difficulty: 3,
    content: 'React에서 useEffect의 cleanup 함수가 실행되는 시점은 언제인가요?',
    answer: '컴포넌트가 언마운트되거나 의존성이 변경되기 직전',
    explanation: 'useEffect의 cleanup 함수는 다음 이펙트가 실행되기 전과 언마운트 시 호출됩니다.',
    generationReason: '망각 위험 높은 스킬 기반 자동 생성',
    approvalStatus: 'PENDING',
  },
  {
    id: 'q2',
    courseId: 'c1',
    skillId: 's2',
    questionType: '서술형',
    difficulty: 4,
    content: 'TypeScript에서 제네릭 타입을 사용하는 이유와 실제 활용 예시를 설명하세요.',
    answer: '코드 재사용성과 타입 안전성을 동시에 확보',
    explanation: '제네릭은 다양한 타입에 대해 동작하는 재사용 가능한 컴포넌트를 만들 수 있게 합니다.',
    generationReason: '이해도 낮은 스킬 보강',
    approvalStatus: 'PENDING',
  },
  {
    id: 'q3',
    courseId: 'c1',
    skillId: 's3',
    questionType: '코드작성',
    difficulty: 5,
    content: '주어진 배열에서 중복을 제거하는 함수를 Set을 사용하지 않고 구현하세요.',
    answer: 'filter + indexOf 또는 reduce 패턴',
    explanation: 'Set 없이도 다양한 방법으로 중복 제거가 가능합니다.',
    generationReason: '실행력 점수 낮은 학생 대상 보강',
    approvalStatus: 'PENDING',
  },
];

const SKILL_NAMES: Record<string, string> = {
  s1: 'React Hooks',
  s2: 'TypeScript 제네릭',
  s3: '배열 알고리즘',
};

const difficultyLabel = (d: number) => {
  if (d <= 2) return { text: '기초', color: 'emerald' as const };
  if (d <= 3) return { text: '중급', color: 'amber' as const };
  return { text: '고급', color: 'rose' as const };
};

export default function QuestionReviewPanel() {
  const queryClient = useQueryClient();
  const courseId = useCourseId();
  const [actionedIds, setActionedIds] = useState<Set<string>>(new Set());

  const { data: questions = MOCK_QUESTIONS } = useQuery({
    queryKey: ['instructor', 'questions', 'pending', courseId],
    queryFn: () => questionsApi.getQuestions(courseId!, { approvalStatus: 'PENDING' }),
    enabled: !!courseId,
    placeholderData: MOCK_QUESTIONS,
    staleTime: 30_000,
  });

  const approveMutation = useMutation({
    mutationFn: async ({ id, status }: { id: string; status: 'APPROVED' | 'REJECTED' }) => {
      if (status === 'APPROVED') return questionsApi.approveQuestion(id);
      return questionsApi.rejectQuestion(id);
    },
    onSuccess: (_, vars) => {
      setActionedIds((prev) => new Set(prev).add(vars.id));
      queryClient.invalidateQueries({ queryKey: ['instructor', 'questions'] });
    },
  });

  const pendingQuestions = questions.filter((q) => !actionedIds.has(q.id));

  return (
    <div className="bg-white/85 backdrop-blur-[12px] border border-white/60 rounded-2xl shadow-lg p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-slate-800">AI 생성 문제 검토</h3>
        <span className="text-xs text-slate-500">{pendingQuestions.length}개 대기</span>
      </div>

      <div className="space-y-3 max-h-[400px] overflow-y-auto pr-1">
        {pendingQuestions.map((q) => {
          const diff = difficultyLabel(q.difficulty);
          return (
            <div
              key={q.id}
              className="p-3 rounded-xl bg-slate-50 border border-slate-100 space-y-2"
            >
              <p className="text-sm text-slate-700 leading-relaxed">{q.content}</p>

              <div className="flex items-center gap-2 flex-wrap">
                <TagChip label={SKILL_NAMES[q.skillId] ?? q.skillId} color="indigo" size="sm" />
                <TagChip label={diff.text} color={diff.color} size="sm" />
                <TagChip label={q.questionType} color="slate" size="sm" />
              </div>

              <p className="text-[11px] text-slate-400">{q.generationReason}</p>

              <div className="flex items-center gap-2 pt-1">
                <button
                  onClick={() => approveMutation.mutate({ id: q.id, status: 'APPROVED' })}
                  className="px-3 py-1.5 text-xs font-medium rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors"
                >
                  승인
                </button>
                <button
                  onClick={() => approveMutation.mutate({ id: q.id, status: 'REJECTED' })}
                  className="px-3 py-1.5 text-xs font-medium rounded-lg bg-white border border-slate-300 text-slate-600 hover:bg-slate-50 transition-colors"
                >
                  반려
                </button>
              </div>
            </div>
          );
        })}

        {pendingQuestions.length === 0 && (
          <p className="text-sm text-slate-400 text-center py-8">
            검토 대기 중인 문제가 없습니다
          </p>
        )}
      </div>
    </div>
  );
}
