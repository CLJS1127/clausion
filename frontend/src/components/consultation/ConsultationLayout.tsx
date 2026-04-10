import type { ReactNode } from 'react';

interface ConsultationLayoutProps {
  briefing: ReactNode;
  workspace: ReactNode;
  summary: ReactNode;
}

export default function ConsultationLayout({ briefing, workspace, summary }: ConsultationLayoutProps) {
  return (
    <div
      className="h-[calc(100vh-64px)] gap-3 p-3"
      style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1.4fr 1fr',
      }}
    >
      <div className="overflow-y-auto rounded-2xl">{briefing}</div>
      <div className="overflow-y-auto rounded-2xl">{workspace}</div>
      <div className="overflow-y-auto rounded-2xl">{summary}</div>
    </div>
  );
}
