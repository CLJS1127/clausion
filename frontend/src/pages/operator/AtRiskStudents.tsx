import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { operatorApi } from '../../api/operator';
import GlassCard from '../../components/common/GlassCard';

type AtRiskStudent = { id: string; name: string; courseId: string; courseTitle: string; overallRisk: number; trend: string; consecutiveAbsences: number; aiSuggestion: string };

export default function AtRiskStudents() {
  const queryClient = useQueryClient();
  const [approvedIds, setApprovedIds] = useState<Set<string>>(new Set());
  const [detailStudent, setDetailStudent] = useState<AtRiskStudent | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { data: atRisk, isLoading } = useQuery({
    queryKey: ['operator', 'at-risk'],
    queryFn: operatorApi.getAtRiskStudents,
  });

  const interventionMutation = useMutation({
    mutationFn: operatorApi.createIntervention,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['operator'] });
      setApprovedIds((prev) => new Set(prev).add(variables.studentId));
      setError(null);
    },
    onError: (err: Error) => {
      console.error('Intervention failed:', err);
      setError(err.message || '개입 승인에 실패했습니다.');
    },
  });

  const handleIntervene = (student: { id: string; courseId: string; aiSuggestion: string }) => {
    setError(null);
    interventionMutation.mutate({
      studentId: student.id,
      courseId: student.courseId,
      interventionType: 'CONSULTATION',
      description: student.aiSuggestion,
      aiSuggested: true,
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-extrabold text-slate-900">이탈 위험군 관리</h1>
        <p className="text-sm text-slate-500 mt-1">AI가 감지한 이탈 위험 수강생 및 자동 개입 제안</p>
      </div>

      {isLoading ? (
        <p className="text-sm text-slate-400">로딩 중...</p>
      ) : atRisk && atRisk.length > 0 ? (
        <div className="space-y-4">
          {atRisk.map((student) => (
            <GlassCard key={student.id} className="p-5">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h3 className="text-sm font-bold text-slate-900">{student.name}</h3>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
                      student.overallRisk >= 80 ? 'bg-rose-100 text-rose-700'
                      : student.overallRisk >= 60 ? 'bg-orange-100 text-orange-700'
                      : student.overallRisk >= 40 ? 'bg-amber-100 text-amber-700'
                      : student.overallRisk >= 20 ? 'bg-sky-100 text-sky-700'
                      : 'bg-emerald-100 text-emerald-700'
                    }`}>
                      위험도 {Math.round(student.overallRisk)}%
                    </span>
                    {student.trend === 'DECLINING' && (
                      <span className="text-rose-500 text-xs font-bold">&#9660; 하락 중</span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-1">{student.courseTitle}</p>
                  {student.consecutiveAbsences > 0 && (
                    <p className="text-xs text-rose-500 mt-1">
                      연속 결석 {student.consecutiveAbsences}일
                    </p>
                  )}

                  {/* AI suggestion */}
                  <div className="mt-3 p-3 rounded-lg bg-indigo-50 border border-indigo-100">
                    <p className="text-xs font-bold text-indigo-700 mb-1">AI 개입 제안</p>
                    <p className="text-xs text-indigo-600">{student.aiSuggestion}</p>
                  </div>
                </div>

                <div className="ml-4 flex flex-col gap-2">
                  {approvedIds.has(student.id) ? (
                    <span className="px-4 py-2 rounded-lg bg-emerald-50 text-emerald-700 text-xs font-bold text-center border border-emerald-200">
                      승인됨
                    </span>
                  ) : (
                    <button
                      onClick={() => handleIntervene(student)}
                      disabled={interventionMutation.isPending}
                      className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-xs font-bold hover:bg-indigo-700 disabled:opacity-50 transition-colors"
                    >
                      {interventionMutation.isPending ? '처리 중...' : '개입 승인'}
                    </button>
                  )}
                  <button
                    onClick={() => setDetailStudent(detailStudent?.id === student.id ? null : student)}
                    className="px-4 py-2 rounded-lg bg-slate-100 text-slate-600 text-xs font-medium hover:bg-slate-200 transition-colors"
                  >
                    {detailStudent?.id === student.id ? '접기' : '상세 보기'}
                  </button>
                </div>
              </div>
              {/* Detail panel */}
              {detailStudent?.id === student.id && (
                <div className="mt-4 pt-4 border-t border-slate-100 space-y-2">
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                    <div className="rounded-lg bg-slate-50 p-3 text-center">
                      <p className="text-lg font-bold text-slate-800">{Math.round(student.overallRisk)}%</p>
                      <p className="text-[10px] text-slate-500">종합 위험도</p>
                    </div>
                    <div className="rounded-lg bg-slate-50 p-3 text-center">
                      <p className="text-lg font-bold text-slate-800">{student.consecutiveAbsences}</p>
                      <p className="text-[10px] text-slate-500">연속 결석</p>
                    </div>
                    <div className="rounded-lg bg-slate-50 p-3 text-center">
                      <p className="text-lg font-bold text-slate-800">{student.trend === 'IMPROVING' ? '개선' : student.trend === 'DECLINING' ? '하락' : '유지'}</p>
                      <p className="text-[10px] text-slate-500">추세</p>
                    </div>
                    <div className="rounded-lg bg-slate-50 p-3 text-center">
                      <p className="text-sm font-bold text-slate-800 truncate">{student.courseTitle}</p>
                      <p className="text-[10px] text-slate-500">과정</p>
                    </div>
                  </div>
                </div>
              )}
            </GlassCard>
          ))}

          {error && (
            <div className="p-3 rounded-lg bg-rose-50 border border-rose-200">
              <p className="text-sm text-rose-700">{error}</p>
            </div>
          )}
        </div>
      ) : (
        <GlassCard className="p-8 text-center">
          <p className="text-slate-400">현재 이탈 위험 수강생이 없습니다.</p>
        </GlassCard>
      )}
    </div>
  );
}
