import type { ReactNode } from 'react';

interface GlassCardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}

export default function GlassCard({ children, className = '', onClick }: GlassCardProps) {
  return (
    <div
      onClick={onClick}
      className={`bg-white/85 backdrop-blur-[12px] border border-white/60 rounded-2xl shadow-lg ${onClick ? 'cursor-pointer hover:shadow-xl transition-shadow' : ''} ${className}`}
    >
      {children}
    </div>
  );
}
