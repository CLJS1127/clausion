import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

interface UpcomingConsultation {
  id: string;
  time: string;
  studentName: string;
  briefingReady: boolean;
}

const MOCK_CONSULTATIONS: UpcomingConsultation[] = [
  { id: 'uc1', time: '10:00', studentName: '정예린', briefingReady: true },
  { id: 'uc2', time: '11:30', studentName: '오민서', briefingReady: true },
  { id: 'uc3', time: '14:00', studentName: '황시우', briefingReady: false },
  { id: 'uc4', time: '15:30', studentName: '백승현', briefingReady: true },
];

export default function UpcomingConsultationPanel() {
  const navigate = useNavigate();

  const { data: consultations = MOCK_CONSULTATIONS } = useQuery({
    queryKey: ['instructor', 'upcoming-consultations'],
    queryFn: async () => MOCK_CONSULTATIONS,
    staleTime: 30_000,
  });

  return (
    <div className="bg-white/85 backdrop-blur-[12px] border border-white/60 rounded-2xl shadow-lg p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-slate-800">오늘의 상담</h3>
        <span className="text-xs text-slate-500">{consultations.length}건</span>
      </div>

      <div className="space-y-2">
        {consultations.map((c) => (
          <div
            key={c.id}
            className="flex flex-col sm:flex-row sm:items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-100 hover:bg-slate-100 transition-colors gap-2"
          >
            <div className="flex items-center gap-3 min-w-0">
              <span className="text-sm font-mono font-semibold text-indigo-600 shrink-0">
                {c.time}
              </span>
              <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center text-xs font-bold text-indigo-700 shrink-0">
                {c.studentName.charAt(0)}
              </div>
              <span className="text-sm text-slate-700 font-medium truncate">{c.studentName}</span>
            </div>

            {c.briefingReady ? (
              <button
                onClick={() => navigate('/instructor/consultations', { state: { showBriefingForId: c.id } })}
                className="px-2.5 py-1 text-[11px] font-medium rounded-lg bg-indigo-50 text-indigo-600 hover:bg-indigo-100 transition-colors border border-indigo-200 shrink-0 self-end sm:self-auto"
              >
                AI 브리핑 보기
              </button>
            ) : (
              <span className="text-[11px] text-slate-400 shrink-0 self-end sm:self-auto">준비 중...</span>
            )}
          </div>
        ))}

        {consultations.length === 0 && (
          <p className="text-sm text-slate-400 text-center py-6">오늘 예정된 상담이 없습니다</p>
        )}
      </div>
    </div>
  );
}
