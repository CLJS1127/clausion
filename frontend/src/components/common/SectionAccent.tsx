import type { ReactNode } from 'react';

interface SectionAccentProps {
  title: string;
  subtitle?: string;
  children?: ReactNode;
  className?: string;
}

export default function SectionAccent({ title, subtitle, children, className = '' }: SectionAccentProps) {
  return (
    <div className={`mb-6 ${className}`}>
      <div className="flex items-center gap-3 mb-1">
        <div className="w-1 h-6 rounded-full bg-gradient-to-b from-indigo-500 to-violet-500" />
        <h2 className="text-lg font-bold text-slate-900">{title}</h2>
        {children}
      </div>
      {subtitle && (
        <p className="text-sm text-slate-500 ml-4 pl-0.5">{subtitle}</p>
      )}
    </div>
  );
}
