import React from 'react';

interface TwinContextBannerProps {
  weakSkills?: string[];
  confidenceLevel?: 'LOW' | 'MEDIUM' | 'HIGH';
}

const confidenceConfig = {
  LOW: { label: '낮음', color: 'text-rose-600', dot: 'bg-rose-500' },
  MEDIUM: { label: '보통', color: 'text-amber-600', dot: 'bg-amber-500' },
  HIGH: { label: '높음', color: 'text-emerald-600', dot: 'bg-emerald-500' },
} as const;

const TwinContextBanner: React.FC<TwinContextBannerProps> = ({
  weakSkills = ['재귀 함수', 'REST API'],
  confidenceLevel = 'MEDIUM',
}) => {
  const conf = confidenceConfig[confidenceLevel];

  return (
    <div className="mx-4 mt-2 mb-1 rounded-lg bg-indigo-50/70 border border-indigo-200 px-3 py-2">
      <div className="flex items-center gap-1.5 mb-1">
        <svg className="w-3.5 h-3.5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
        </svg>
        <span className="text-[11px] font-semibold text-indigo-700">
          Twin 기반 맞춤 답변
        </span>
      </div>
      <div className="flex items-center gap-3 text-[10px]">
        {weakSkills.length > 0 && (
          <span className="text-slate-500">
            약점:{' '}
            <span className="font-medium text-slate-700">
              {weakSkills.join(', ')}
            </span>
          </span>
        )}
        <span className="flex items-center gap-1">
          <span className={`inline-block w-1.5 h-1.5 rounded-full ${conf.dot}`} />
          <span className={`font-medium ${conf.color}`}>
            자신감 {conf.label}
          </span>
        </span>
      </div>
    </div>
  );
};

export default TwinContextBanner;
