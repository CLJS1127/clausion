import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import SVGRadarChart from '../common/SVGRadarChart';
import { twinApi } from '../../api/twin';
import { useAuth } from '../../hooks/useAuth';
import { useCourseId } from '../../hooks/useCourseId';

interface WeakSkill {
  id: string;
  name: string;
  understanding: number;
  practice: number;
  confidence: number;
  forgettingRisk: number;
}

interface WeakSkillRadarProps {
  studentId?: string;
  courseId?: string;
}

const WeakSkillRadar: React.FC<WeakSkillRadarProps> = ({ studentId: studentIdProp, courseId: courseIdProp }) => {
  const { user } = useAuth();
  const studentId = studentIdProp ?? String(user?.id ?? '');
  const defaultCourseId = useCourseId();
  const courseId = courseIdProp ?? defaultCourseId;

  const { data: skills = [] } = useQuery<WeakSkill[]>({
    queryKey: ['weakSkills', studentId, courseId],
    queryFn: async () => {
      if (!studentId || !courseId) return [];
      const snapshots = await twinApi.getTwinHistory(studentId, courseId);
      // Group by skillId and take the latest snapshot per skill
      const latestBySkill = new Map<string, typeof snapshots[0]>();
      for (const s of snapshots) {
        if (!latestBySkill.has(String(s.skillId))) {
          latestBySkill.set(String(s.skillId), s);
        }
      }
      // Filter to weak skills (understanding < 60) and take top 5
      return Array.from(latestBySkill.values())
        .filter((s) => (s.understandingScore ?? 0) < 60)
        .slice(0, 5)
        .map((s) => ({
          id: String(s.skillId),
          name: s.skillName ?? `Skill ${s.skillId}`,
          understanding: s.understandingScore ?? 0,
          practice: s.practiceScore ?? 0,
          confidence: s.confidenceScore ?? 0,
          forgettingRisk: s.forgettingRiskScore ?? 0,
        }));
    },
    enabled: !!studentId && !!courseId,
  });

  const [selected, setSelected] = React.useState<string>('');

  const effectiveSelected = selected || skills[0]?.id || '';
  const current = skills.find((s) => s.id === effectiveSelected) ?? skills[0];

  const radarData = current
    ? [
        current.understanding,
        current.practice,
        current.confidence,
        100 - current.forgettingRisk,
        (current.understanding + current.practice) / 2,
        current.confidence,
      ]
    : [0, 0, 0, 0, 0, 0];

  const radarLabels = [
    '이해도',
    '실습',
    '자신감',
    '기억유지',
    '종합',
    '응용력',
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl bg-white p-6 shadow-sm border border-slate-100"
    >
      <h2 className="text-lg font-bold text-slate-900 mb-4">약점 스킬 분석</h2>

      {skills.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-sm text-slate-400">분석할 약점 스킬이 없습니다</p>
          <p className="text-xs text-slate-400 mt-1">학습을 진행하면 AI가 약점을 분석합니다</p>
        </div>
      ) : (
      <>
      {/* Skill tabs */}
      <div className="flex gap-2 mb-4 overflow-x-auto">
        {skills.map((skill) => (
          <button
            key={skill.id}
            onClick={() => setSelected(skill.id)}
            className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              effectiveSelected === skill.id
                ? 'bg-indigo-600 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            {skill.name}
          </button>
        ))}
      </div>

      {/* Radar */}
      <div className="flex justify-center">
        <SVGRadarChart
          data={radarData}
          size={180}
          showLabels
          labels={radarLabels}
        />
      </div>

      {/* Detail scores */}
      {current && (
        <div className="mt-4 space-y-2">
          {[
            {
              label: '이해도',
              value: current.understanding,
              color: 'bg-indigo-500',
            },
            {
              label: '실습 점수',
              value: current.practice,
              color: 'bg-violet-500',
            },
            {
              label: '자신감',
              value: current.confidence,
              color: 'bg-emerald-500',
            },
            {
              label: '망각 위험',
              value: current.forgettingRisk,
              color: 'bg-rose-500',
            },
          ].map((item) => (
            <div key={item.label} className="flex items-center gap-3">
              <span className="text-xs text-slate-500 w-16 shrink-0">
                {item.label}
              </span>
              <div className="flex-1 h-1.5 rounded-full bg-slate-100 overflow-hidden">
                <div
                  className={`h-full rounded-full ${item.color} transition-all duration-500`}
                  style={{ width: `${item.value}%` }}
                />
              </div>
              <span className="text-xs font-semibold text-slate-700 w-8 text-right">
                {item.value}
              </span>
            </div>
          ))}
        </div>
      )}
      </>
      )}
    </motion.div>
  );
};

export default WeakSkillRadar;
