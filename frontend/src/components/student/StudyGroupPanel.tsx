import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { studyGroupApi } from '../../api/studyGroup';
import type { StudyGroup } from '../../types';

const StudyGroupPanel: React.FC = () => {
  const navigate = useNavigate();

  const { data: myGroups = [], isLoading } = useQuery<StudyGroup[]>({
    queryKey: ['my-study-groups'],
    queryFn: () => studyGroupApi.getMyGroups(),
  });

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl bg-white p-6 shadow-sm border border-slate-100"
    >
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-bold text-slate-900">스터디 그룹</h2>
        <button
          onClick={() => navigate('/student/study-groups')}
          className="text-xs text-indigo-600 hover:text-indigo-700 font-medium"
        >
          전체 보기
        </button>
      </div>

      {isLoading && (
        <div className="space-y-3">
          {[1, 2].map((i) => (
            <div key={i} className="h-16 rounded-xl bg-slate-50 animate-pulse" />
          ))}
        </div>
      )}

      {!isLoading && myGroups.length === 0 && (
        <div className="text-center py-6">
          <div className="w-12 h-12 mx-auto mb-3 rounded-full bg-slate-100 flex items-center justify-center">
            <svg className="w-6 h-6 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <p className="text-sm font-medium text-slate-600 mb-1">참여 중인 그룹이 없어요</p>
          <p className="text-xs text-slate-400 mb-3">함께 학습할 그룹을 찾아보세요</p>
          <button
            onClick={() => navigate('/student/study-groups')}
            className="px-4 py-2 text-xs font-medium rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors"
          >
            그룹 탐색
          </button>
        </div>
      )}

      {!isLoading && myGroups.length > 0 && (
        <div className="space-y-3">
          {myGroups.slice(0, 3).map((group, i) => (
            <motion.div
              key={group.id}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: i * 0.05 }}
              className="flex items-center gap-3 rounded-xl border border-slate-100 bg-slate-50/50 p-3 hover:bg-white hover:shadow-sm transition-all"
            >
              <div className="flex -space-x-1.5 shrink-0">
                {group.members.slice(0, 3).map((m) => {
                  const name = m.name || m.studentName || '?';
                  return (
                    <div
                      key={m.id}
                      className="w-7 h-7 rounded-full bg-gradient-to-br from-indigo-400 to-violet-500 flex items-center justify-center text-white text-[9px] font-bold border-2 border-white"
                      title={name}
                    >
                      {name.charAt(0)}
                    </div>
                  );
                })}
                {group.members.length > 3 && (
                  <div className="w-7 h-7 rounded-full bg-slate-200 flex items-center justify-center text-[9px] font-bold text-slate-500 border-2 border-white">
                    +{group.members.length - 3}
                  </div>
                )}
              </div>

              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-slate-800 truncate">{group.name}</p>
                <p className="text-[11px] text-slate-400">
                  {group.members.length}/{group.maxMembers}명
                </p>
              </div>

              <button
                onClick={() => navigate(`/student/study-groups/${group.id}/chat`)}
                className="shrink-0 px-3 py-1.5 text-xs font-medium rounded-lg bg-indigo-50 text-indigo-600 hover:bg-indigo-100 transition-colors"
              >
                채팅
              </button>
            </motion.div>
          ))}
          {myGroups.length > 3 && (
            <p className="text-center text-[11px] text-slate-400">
              +{myGroups.length - 3}개 그룹 더 보기
            </p>
          )}
        </div>
      )}
    </motion.div>
  );
};

export default StudyGroupPanel;
