import type { ReactNode } from 'react';
import GlassCard from './GlassCard';

interface InsightCardProps {
  icon: string;
  title: string;
  content: string;
  action?: { label: string; onClick: () => void };
  variant?: 'default' | 'warning' | 'success';
  children?: ReactNode;
  className?: string;
}

const variantBorder = {
  default: 'border-l-indigo-500',
  warning: 'border-l-amber-500',
  success: 'border-l-emerald-500',
};

export default function InsightCard({
  icon,
  title,
  content,
  action,
  variant = 'default',
  children,
  className = '',
}: InsightCardProps) {
  return (
    <GlassCard className={`p-4 border-l-4 ${variantBorder[variant]} ${className}`}>
      <div className="flex items-start gap-3">
        <span className="text-xl flex-shrink-0 mt-0.5">{icon}</span>
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-semibold text-slate-900 mb-1">{title}</h4>
          <p className="text-sm text-slate-600 leading-relaxed">{content}</p>
          {children}
          {action && (
            <button
              onClick={action.onClick}
              className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-700 transition-colors"
            >
              {action.label} &rarr;
            </button>
          )}
        </div>
      </div>
    </GlassCard>
  );
}
