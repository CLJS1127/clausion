import React from 'react';
import { motion } from 'framer-motion';
import { useGamification } from '../../hooks/useGamification';
import { useAuthStore } from '../../store/authStore';
import type { GamificationState } from '../../types';

const MOCK_STATE: GamificationState = {
  level: 7,
  currentXP: 1350,
  nextLevelXP: 2000,
  levelTitle: '성장하는 코더',
  streakDays: 5,
  badges: [
    {
      id: 'b1',
      name: '첫 복습',
      emoji: '📖',
      description: '첫 복습 완료',
      earnedAt: '2026-04-01',
    },
    {
      id: 'b2',
      name: '연속 3일',
      emoji: '🔥',
      description: '3일 연속 학습',
      earnedAt: '2026-04-03',
    },
    {
      id: 'b3',
      name: '코드 마스터',
      emoji: '💻',
      description: '코드 분석 5회',
      earnedAt: '2026-04-05',
    },
    {
      id: 'b4',
      name: '완벽한 주',
      emoji: '⭐',
      description: '주간 미션 올 클리어',
      earnedAt: '2026-04-07',
    },
  ],
};

const GamificationBar: React.FC = () => {
  const { user } = useAuthStore();
  const studentId = user?.id?.toString() ?? '';
  const { data } = useGamification(studentId);
  const state = data && data.currentXP != null ? data : MOCK_STATE;

  const xpPercent = state.nextLevelXP > 0
    ? Math.round((state.currentXP / state.nextLevelXP) * 100)
    : 0;

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl bg-gradient-to-r from-indigo-600 to-violet-700 p-5 text-white shadow-lg"
    >
      {/* Top Row: Level + Streak */}
      <div className="flex items-center justify-between mb-3">
        <div>
          <p className="text-xs text-indigo-200 font-medium">Lv.{state.level}</p>
          <p className="text-base font-bold">{state.levelTitle}</p>
        </div>
        <div className="flex items-center gap-1.5 bg-white/15 rounded-full px-3 py-1">
          <span className="text-sm">🔥</span>
          <span className="text-sm font-bold">{state.streakDays}일 연속</span>
        </div>
      </div>

      {/* XP Bar */}
      <div className="mb-3">
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs text-indigo-200">경험치</span>
          <span className="text-xs text-indigo-200">
            {state.currentXP.toLocaleString()} / {state.nextLevelXP.toLocaleString()} XP
          </span>
        </div>
        <div className="h-2 rounded-full bg-white/20 overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${xpPercent}%` }}
            transition={{ duration: 0.8, ease: 'easeOut' }}
            className="h-full rounded-full bg-gradient-to-r from-amber-300 to-yellow-200"
          />
        </div>
      </div>

      {/* Badges */}
      <div className="flex items-center gap-1.5 flex-wrap">
        <span className="text-xs text-indigo-200 mr-1">뱃지</span>
        {state.badges.map((badge) => (
          <span
            key={badge.id}
            title={badge.name}
            className="inline-flex items-center justify-center w-7 h-7 rounded-full bg-white/15 text-sm hover:bg-white/25 transition-colors cursor-default"
          >
            {badge.emoji}
          </span>
        ))}
      </div>
    </motion.div>
  );
};

export default GamificationBar;
