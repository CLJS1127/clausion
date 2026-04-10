import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import type { CodeFeedback } from '../../types';

const SEVERITY_STYLES: Record<
  string,
  { bg: string; border: string; icon: string; iconColor: string; label: string }
> = {
  ERROR: {
    bg: 'bg-rose-50',
    border: 'border-rose-200',
    icon: '✕',
    iconColor: 'text-rose-600 bg-rose-100',
    label: '오류',
  },
  WARNING: {
    bg: 'bg-amber-50',
    border: 'border-amber-200',
    icon: '!',
    iconColor: 'text-amber-600 bg-amber-100',
    label: '경고',
  },
  INFO: {
    bg: 'bg-sky-50',
    border: 'border-sky-200',
    icon: 'i',
    iconColor: 'text-sky-600 bg-sky-100',
    label: '정보',
  },
  GOOD: {
    bg: 'bg-emerald-50',
    border: 'border-emerald-200',
    icon: '✓',
    iconColor: 'text-emerald-600 bg-emerald-100',
    label: '좋음',
  },
};

interface CodeAIFeedbackSidebarProps {
  feedbacks: CodeFeedback[];
  onFeedbackHover?: (lineNumber: number | null) => void;
}

const CodeAIFeedbackSidebar: React.FC<CodeAIFeedbackSidebarProps> = ({
  feedbacks,
  onFeedbackHover,
}) => {
  return (
    <div className="w-52 shrink-0 overflow-y-auto border-l border-slate-300 bg-slate-50 p-3">
      <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-3">
        AI 피드백
      </h3>

      <AnimatePresence>
        {feedbacks.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-xs text-slate-400">
              코드를 제출하면 AI가 분석합니다
            </p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {feedbacks.map((fb, i) => {
              const style =
                SEVERITY_STYLES[fb.severity] ?? SEVERITY_STYLES.INFO;
              return (
                <motion.div
                  key={fb.id}
                  initial={{ opacity: 0, x: 10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: i * 0.05 }}
                  onMouseEnter={() => onFeedbackHover?.(fb.lineNumber)}
                  onMouseLeave={() => onFeedbackHover?.(null)}
                  className={`rounded-lg border p-2.5 cursor-pointer transition-shadow hover:shadow-sm ${style.bg} ${style.border}`}
                >
                  <div className="flex items-center gap-1.5 mb-1.5">
                    <span
                      className={`w-4 h-4 rounded-full flex items-center justify-center text-[9px] font-bold ${style.iconColor}`}
                    >
                      {style.icon}
                    </span>
                    <span className="text-[10px] font-semibold text-slate-600">
                      {style.label}
                    </span>
                    <span className="text-[10px] text-slate-400 ml-auto">
                      L{fb.lineNumber}
                      {fb.endLineNumber > fb.lineNumber &&
                        `-${fb.endLineNumber}`}
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-700 leading-relaxed mb-1">
                    {fb.message}
                  </p>
                  {fb.suggestion && (
                    <p className="text-[10px] text-slate-500 italic">
                      💡 {fb.suggestion}
                    </p>
                  )}
                  {fb.twinLinked && (
                    <div className="mt-1.5 flex items-center gap-1">
                      <span className="inline-block w-1.5 h-1.5 rounded-full bg-indigo-500" />
                      <span className="text-[9px] text-indigo-600 font-medium">
                        트윈 연동
                      </span>
                    </div>
                  )}
                </motion.div>
              );
            })}
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default CodeAIFeedbackSidebar;
