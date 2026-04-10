interface RiskIndicatorProps {
  level: 'safe' | 'caution' | 'danger';
  showLabel?: boolean;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const config = {
  safe: { label: '안전', color: 'bg-emerald-500', text: 'text-emerald-700', bg: 'bg-emerald-50', border: 'border-emerald-200' },
  caution: { label: '주의', color: 'bg-amber-500', text: 'text-amber-700', bg: 'bg-amber-50', border: 'border-amber-200' },
  danger: { label: '위험', color: 'bg-rose-500', text: 'text-rose-700', bg: 'bg-rose-50', border: 'border-rose-200' },
};

const sizeMap = {
  sm: 'w-2 h-2',
  md: 'w-2.5 h-2.5',
  lg: 'w-3 h-3',
};

export default function RiskIndicator({
  level,
  showLabel = true,
  size = 'md',
  className = '',
}: RiskIndicatorProps) {
  const c = config[level];

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium border ${c.bg} ${c.text} ${c.border} ${className}`}
    >
      <span className={`${sizeMap[size]} rounded-full ${c.color}`} />
      {showLabel && c.label}
    </span>
  );
}
