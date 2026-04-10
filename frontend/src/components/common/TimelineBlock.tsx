interface TimelineBlockProps {
  date: string;
  title: string;
  description: string;
  icon?: string;
  variant?: 'default' | 'reflection' | 'consultation' | 'review';
  className?: string;
}

const dotColor = {
  default: 'bg-slate-400',
  reflection: 'bg-violet-500',
  consultation: 'bg-indigo-500',
  review: 'bg-emerald-500',
};

export default function TimelineBlock({
  date,
  title,
  description,
  icon,
  variant = 'default',
  className = '',
}: TimelineBlockProps) {
  return (
    <div className={`relative flex gap-4 pb-6 ${className}`}>
      {/* Vertical line */}
      <div className="flex flex-col items-center">
        <div className={`w-3 h-3 rounded-full ${dotColor[variant]} ring-4 ring-white z-10 flex-shrink-0`} />
        <div className="w-px flex-1 bg-slate-200" />
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0 -mt-0.5 pb-2">
        <div className="flex items-center gap-2 mb-0.5">
          {icon && <span className="text-sm">{icon}</span>}
          <span className="text-xs text-slate-400 font-mono">{date}</span>
        </div>
        <h4 className="text-sm font-semibold text-slate-800">{title}</h4>
        <p className="text-sm text-slate-500 mt-0.5 leading-relaxed">{description}</p>
      </div>
    </div>
  );
}
