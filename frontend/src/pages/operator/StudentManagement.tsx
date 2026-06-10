import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { operatorApi } from '../../api/operator';
import GlassCard from '../../components/common/GlassCard';

function RiskBadge({ risk }: { risk: number }) {
  const color = risk >= 80 ? 'bg-rose-100 text-rose-700' : risk >= 60 ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700';
  return <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${color}`}>{Math.round(risk)}%</span>;
}

export default function StudentManagement() {
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState<'RISK' | 'NAME' | 'ATTENDANCE'>('RISK');

  const { data: students, isLoading } = useQuery({
    queryKey: ['operator', 'students'],
    queryFn: operatorApi.getStudents,
  });

  const visible = (students ?? [])
    .filter((s) => {
      const q = search.trim();
      if (!q) return true;
      return (
        (s.name ?? '').includes(q) ||
        (s.email ?? '').includes(q) ||
        (s.courseTitle ?? '').includes(q)
      );
    })
    .sort((a, b) => {
      if (sort === 'RISK') return (b.overallRisk ?? 0) - (a.overallRisk ?? 0);
      if (sort === 'ATTENDANCE') return (a.attendanceRate ?? 0) - (b.attendanceRate ?? 0);
      return (a.name ?? '').localeCompare(b.name ?? '', 'ko');
    });

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between gap-3 flex-wrap">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">수강생 관리</h1>
          <p className="text-sm text-slate-500 mt-1">전체 수강생 현황 및 Twin 위험도 모니터링</p>
        </div>
        <div className="flex gap-2 items-center">
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="이름·이메일·과정 검색"
            className="px-3 py-2 rounded-lg border border-slate-300 text-sm w-52 focus:outline-none focus:border-indigo-400 bg-white"
          />
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value as 'RISK' | 'NAME' | 'ATTENDANCE')}
            className="px-3 py-2 rounded-lg border border-slate-300 text-sm bg-white"
          >
            <option value="RISK">위험도 높은 순</option>
            <option value="ATTENDANCE">출석률 낮은 순</option>
            <option value="NAME">이름순</option>
          </select>
        </div>
      </div>

      {isLoading ? (
        <p className="text-sm text-slate-400">로딩 중...</p>
      ) : (
        <GlassCard className="overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full text-sm min-w-[640px]">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50">
                <th className="text-left p-4 font-semibold text-slate-600">이름</th>
                <th className="text-left p-4 font-semibold text-slate-600">이메일</th>
                <th className="text-left p-4 font-semibold text-slate-600">과정</th>
                <th className="text-center p-4 font-semibold text-slate-600">위험도</th>
                <th className="text-center p-4 font-semibold text-slate-600">추세</th>
                <th className="text-center p-4 font-semibold text-slate-600">출석률</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((s) => (
                <tr key={s.id} className="border-b border-slate-100 hover:bg-slate-50 transition-colors">
                  <td className="p-4 font-medium text-slate-900">{s.name}</td>
                  <td className="p-4 text-slate-500">{s.email}</td>
                  <td className="p-4 text-slate-700">{s.courseTitle}</td>
                  <td className="p-4 text-center"><RiskBadge risk={s.overallRisk} /></td>
                  <td className="p-4 text-center">
                    {s.trend === 'IMPROVING' ? <span className="text-emerald-500 font-bold">&#9650;</span>
                      : s.trend === 'DECLINING' ? <span className="text-rose-500 font-bold">&#9660;</span>
                      : <span className="text-slate-400">-</span>}
                  </td>
                  <td className="p-4 text-center">
                    <span className={s.attendanceRate < 0.8 ? 'text-amber-600 font-bold' : 'text-slate-700'}>
                      {(s.attendanceRate * 100).toFixed(0)}%
                    </span>
                  </td>
                </tr>
              ))}
              {visible.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-slate-400">
                    {search ? `'${search}' 검색 결과가 없습니다.` : '수강생 데이터가 없습니다.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          </div>
        </GlassCard>
      )}
    </div>
  );
}
