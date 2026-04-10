import { useQuery } from '@tanstack/react-query';

interface WeekProgress {
  week: number;
  label: string;
  progress: number;
  status: 'completed' | 'current' | 'upcoming';
}

const MOCK_WEEKS: WeekProgress[] = [
  { week: 1, label: '1주', progress: 92, status: 'completed' },
  { week: 2, label: '2주', progress: 88, status: 'completed' },
  { week: 3, label: '3주', progress: 85, status: 'completed' },
  { week: 4, label: '4주', progress: 78, status: 'completed' },
  { week: 5, label: '5주', progress: 71, status: 'completed' },
  { week: 6, label: '6주', progress: 65, status: 'current' },
  { week: 7, label: '7주', progress: 0, status: 'upcoming' },
  { week: 8, label: '8주', progress: 0, status: 'upcoming' },
  { week: 9, label: '9주', progress: 0, status: 'upcoming' },
  { week: 10, label: '10주', progress: 0, status: 'upcoming' },
];

const barColor = (status: string) => {
  switch (status) {
    case 'completed': return '#6366f1'; // indigo-500
    case 'current': return '#818cf8';   // indigo-400 highlight
    case 'upcoming': return '#e2e8f0';  // slate-200
    default: return '#e2e8f0';
  }
};

export default function CourseProgressChart() {
  const { data: weeks = MOCK_WEEKS } = useQuery({
    queryKey: ['instructor', 'course-progress'],
    queryFn: async () => MOCK_WEEKS,
    staleTime: 30_000,
  });

  const viewBoxW = 340;
  const viewBoxH = 180;
  const paddingLeft = 30;
  const paddingBottom = 24;
  const chartW = viewBoxW - paddingLeft - 10;
  const chartH = viewBoxH - paddingBottom - 10;
  const barWidth = chartW / weeks.length * 0.6;
  const barGap = chartW / weeks.length;

  return (
    <div className="bg-white/85 backdrop-blur-[12px] border border-white/60 rounded-2xl shadow-lg p-5">
      <h3 className="text-sm font-semibold text-slate-800 mb-3">주차별 학습 진행률</h3>

      <svg viewBox={`0 0 ${viewBoxW} ${viewBoxH}`} className="w-full">
        {/* Grid lines */}
        {[25, 50, 75, 100].map((pct) => {
          const y = 10 + chartH - (pct / 100) * chartH;
          return (
            <g key={pct}>
              <line
                x1={paddingLeft}
                y1={y}
                x2={viewBoxW - 10}
                y2={y}
                stroke="#e2e8f0"
                strokeWidth={0.5}
                strokeDasharray="2,2"
              />
              <text x={paddingLeft - 4} y={y + 3} textAnchor="end" fontSize={8} fill="#94a3b8">
                {pct}%
              </text>
            </g>
          );
        })}

        {/* Bars */}
        {weeks.map((w, i) => {
          const x = paddingLeft + i * barGap + (barGap - barWidth) / 2;
          const height = w.status === 'upcoming' ? (chartH * 0.08) : (w.progress / 100) * chartH;
          const y = 10 + chartH - height;

          return (
            <g key={w.week}>
              <rect
                x={x}
                y={y}
                width={barWidth}
                height={height}
                rx={3}
                fill={barColor(w.status)}
                opacity={w.status === 'current' ? 1 : 0.85}
              />
              {w.status === 'current' && (
                <rect
                  x={x - 1}
                  y={y - 1}
                  width={barWidth + 2}
                  height={height + 2}
                  rx={4}
                  fill="none"
                  stroke="#6366f1"
                  strokeWidth={1.5}
                  strokeDasharray="3,2"
                />
              )}
              {w.status !== 'upcoming' && (
                <text
                  x={x + barWidth / 2}
                  y={y - 3}
                  textAnchor="middle"
                  fontSize={7}
                  fill="#6366f1"
                  fontWeight={600}
                >
                  {w.progress}%
                </text>
              )}
              <text
                x={x + barWidth / 2}
                y={viewBoxH - 6}
                textAnchor="middle"
                fontSize={7}
                fill={w.status === 'current' ? '#6366f1' : '#94a3b8'}
                fontWeight={w.status === 'current' ? 700 : 400}
              >
                {w.label}
              </text>
            </g>
          );
        })}
      </svg>

      <div className="flex items-center gap-4 mt-2 pt-2 border-t border-slate-100">
        <span className="flex items-center gap-1.5 text-[11px] text-slate-500">
          <span className="w-2.5 h-2.5 rounded-sm bg-indigo-500" /> 완료
        </span>
        <span className="flex items-center gap-1.5 text-[11px] text-slate-500">
          <span className="w-2.5 h-2.5 rounded-sm bg-indigo-400 border border-indigo-500 border-dashed" /> 진행 중
        </span>
        <span className="flex items-center gap-1.5 text-[11px] text-slate-500">
          <span className="w-2.5 h-2.5 rounded-sm bg-slate-200" /> 예정
        </span>
      </div>
    </div>
  );
}
