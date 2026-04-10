interface TagChipProps {
  label: string;
  color?: 'indigo' | 'emerald' | 'amber' | 'rose' | 'slate' | 'cyan';
  size?: 'sm' | 'md';
  className?: string;
}

const colorMap = {
  indigo: 'bg-indigo-100 text-indigo-700 border-indigo-200',
  emerald: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  amber: 'bg-amber-100 text-amber-700 border-amber-200',
  rose: 'bg-rose-100 text-rose-700 border-rose-200',
  slate: 'bg-slate-100 text-slate-700 border-slate-300',
  cyan: 'bg-cyan-100 text-cyan-700 border-cyan-200',
};

const sizeMap = {
  sm: 'px-2 py-0.5 text-[11px]',
  md: 'px-2.5 py-1 text-xs',
};

export default function TagChip({
  label,
  color = 'indigo',
  size = 'sm',
  className = '',
}: TagChipProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full border font-medium ${colorMap[color]} ${sizeMap[size]} ${className}`}
    >
      {label}
    </span>
  );
}
