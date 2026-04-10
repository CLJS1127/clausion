interface ReasonBadgeProps {
  reason: string;
  variant?: 'forgetting' | 'weakness' | 'motivation' | 'default';
  className?: string;
}

const variantMap = {
  forgetting: 'bg-rose-50 text-rose-600 border-rose-200',
  weakness: 'bg-amber-50 text-amber-600 border-amber-200',
  motivation: 'bg-violet-50 text-violet-600 border-violet-200',
  default: 'bg-slate-50 text-slate-600 border-slate-300',
};

const iconMap = {
  forgetting: '\⏳',
  weakness: '\⚠',
  motivation: '\✨',
  default: '\u{1F3F7}',
};

export default function ReasonBadge({ reason, variant = 'default', className = '' }: ReasonBadgeProps) {
  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-semibold rounded-md border ${variantMap[variant]} ${className}`}
    >
      <span>{iconMap[variant]}</span>
      {reason}
    </span>
  );
}
