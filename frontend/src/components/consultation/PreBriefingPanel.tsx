import { useQuery } from '@tanstack/react-query';
import { consultationsApi } from '../../api/consultations';

interface BriefingData {
  studentName: string;
  scores: {
    understanding: number;
    confidence: number;
    execution: number;
    forgettingRisk: number;
  };
  weakSkills: string[];
  suggestedQuestions: string[];
}

const MOCK_BRIEFING: BriefingData = {
  studentName: '정예린',
  scores: {
    understanding: 42,
    confidence: 35,
    execution: 48,
    forgettingRisk: 78,
  },
  weakSkills: [
    'React 상태 관리 패턴',
    'TypeScript 제네릭 타입 활용',
    '비동기 데이터 처리 (Promise, async/await)',
    'CSS Flexbox/Grid 레이아웃',
  ],
  suggestedQuestions: [
    '최근 과제에서 어려웠던 부분이 무엇인지 구체적으로 이야기해볼까요?',
    '학습 시 가장 자신감이 떨어지는 순간은 언제인가요?',
    '이번 주 복습 계획을 함께 세워볼까요?',
  ],
};

function ScoreCell({ label, value }: { label: string; value: number }) {
  const color =
    value >= 70 ? 'text-emerald-400' :
    value >= 40 ? 'text-amber-400' :
    'text-rose-400';

  return (
    <div className="flex flex-col items-center justify-center p-3 rounded-xl bg-slate-700/50 border border-slate-600/50">
      <span className="text-[11px] text-slate-400 mb-1">{label}</span>
      <span className={`text-xl font-bold ${color}`}>{value}</span>
    </div>
  );
}

export default function PreBriefingPanel({ consultationId }: { consultationId?: string }) {
  const { data: briefing = MOCK_BRIEFING } = useQuery({
    queryKey: ['consultation', 'briefing', consultationId],
    queryFn: async () => {
      if (!consultationId) return MOCK_BRIEFING;
      try {
        const apiData = await consultationsApi.getConsultationBriefing(consultationId);
        // API 응답을 BriefingData 인터페이스로 매핑
        return {
          studentName: MOCK_BRIEFING.studentName, // API에 studentName 없으면 mock 유지
          scores: MOCK_BRIEFING.scores, // API에 scores 없으면 mock 유지
          weakSkills: apiData.riskAreas?.length > 0 ? apiData.riskAreas : MOCK_BRIEFING.weakSkills,
          suggestedQuestions: apiData.suggestedTopics?.length > 0 ? apiData.suggestedTopics : MOCK_BRIEFING.suggestedQuestions,
        };
      } catch {
        return MOCK_BRIEFING;
      }
    },
    enabled: !!consultationId,
    placeholderData: MOCK_BRIEFING,
    staleTime: 60_000,
  });

  return (
    <div className="h-full bg-gradient-to-b from-slate-800 to-slate-900 rounded-2xl p-5 text-white">
      <div className="flex items-center gap-3 mb-5">
        <div className="w-10 h-10 rounded-full bg-indigo-500/30 flex items-center justify-center text-sm font-bold text-indigo-300">
          {briefing.studentName.charAt(0)}
        </div>
        <div>
          <h3 className="text-sm font-semibold">{briefing.studentName}</h3>
          <span className="text-[11px] text-slate-400">AI 상담 브리핑</span>
        </div>
      </div>

      {/* 2x2 Score Grid */}
      <div className="grid grid-cols-2 gap-2 mb-5">
        <ScoreCell label="이해도" value={briefing.scores.understanding} />
        <ScoreCell label="자신감" value={briefing.scores.confidence} />
        <ScoreCell label="수행력" value={briefing.scores.execution} />
        <ScoreCell label="망각위험" value={briefing.scores.forgettingRisk} />
      </div>

      {/* Weak Skills */}
      <div className="mb-5">
        <h4 className="text-xs font-semibold text-slate-300 mb-2 uppercase tracking-wider">
          취약 스킬
        </h4>
        <ul className="space-y-1.5">
          {briefing.weakSkills.map((skill) => (
            <li key={skill} className="flex items-start gap-2 text-xs text-slate-300">
              <span className="w-1.5 h-1.5 rounded-full bg-rose-400 mt-1 flex-shrink-0" />
              {skill}
            </li>
          ))}
        </ul>
      </div>

      {/* Suggested Questions */}
      <div>
        <h4 className="text-xs font-semibold text-slate-300 mb-2 uppercase tracking-wider">
          추천 질문
        </h4>
        <div className="space-y-2">
          {briefing.suggestedQuestions.map((q, i) => (
            <div
              key={i}
              className="p-2.5 rounded-lg bg-slate-700/50 border border-slate-600/40 text-xs text-slate-300 leading-relaxed cursor-pointer hover:bg-slate-700 transition-colors"
            >
              <span className="text-indigo-400 font-semibold mr-1">Q{i + 1}.</span>
              {q}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
