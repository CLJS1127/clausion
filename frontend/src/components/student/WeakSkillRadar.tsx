import React from 'react';
import { motion } from 'framer-motion';
import SVGRadarChart from '../common/SVGRadarChart';

interface WeakSkill {
  id: string;
  name: string;
  understanding: number;
  practice: number;
  confidence: number;
  forgettingRisk: number;
}

const MOCK_WEAK_SKILLS: WeakSkill[] = [
  {
    id: 'sk2',
    name: '재귀 함수',
    understanding: 42,
    practice: 30,
    confidence: 25,
    forgettingRisk: 78,
  },
  {
    id: 'sk5',
    name: '클로저',
    understanding: 50,
    practice: 35,
    confidence: 40,
    forgettingRisk: 65,
  },
  {
    id: 'sk8',
    name: '트리 순회',
    understanding: 55,
    practice: 28,
    confidence: 35,
    forgettingRisk: 60,
  },
];

interface WeakSkillRadarProps {
  studentId?: string;
  courseId?: string;
}

const WeakSkillRadar: React.FC<WeakSkillRadarProps> = () => {
  const skills = MOCK_WEAK_SKILLS;
  const [selected, setSelected] = React.useState<string>(skills[0]?.id ?? '');

  const current = skills.find((s) => s.id === selected) ?? skills[0];

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

      {/* Skill tabs */}
      <div className="flex gap-2 mb-4 overflow-x-auto">
        {skills.map((skill) => (
          <button
            key={skill.id}
            onClick={() => setSelected(skill.id)}
            className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              selected === skill.id
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
    </motion.div>
  );
};

export default WeakSkillRadar;
