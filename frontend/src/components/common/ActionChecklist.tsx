import { useState } from 'react';

interface ActionItem {
  id: string;
  label: string;
  completed: boolean;
}

interface ActionChecklistProps {
  items: ActionItem[];
  onToggle?: (id: string, completed: boolean) => void;
  className?: string;
}

export default function ActionChecklist({ items: initialItems, onToggle, className = '' }: ActionChecklistProps) {
  const [items, setItems] = useState(initialItems);

  const handleToggle = (id: string) => {
    setItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, completed: !item.completed } : item,
      ),
    );
    const item = items.find((i) => i.id === id);
    if (item && onToggle) {
      onToggle(id, !item.completed);
    }
  };

  const completedCount = items.filter((i) => i.completed).length;

  return (
    <div className={`space-y-2 ${className}`}>
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-medium text-slate-500">
          {completedCount}/{items.length} 완료
        </span>
        <div className="h-1.5 flex-1 ml-3 bg-slate-100 rounded-full overflow-hidden">
          <div
            className="h-full bg-indigo-500 rounded-full transition-all duration-300"
            style={{ width: `${items.length > 0 ? (completedCount / items.length) * 100 : 0}%` }}
          />
        </div>
      </div>
      {items.map((item) => (
        <label
          key={item.id}
          className="flex items-center gap-3 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors group"
        >
          <button
            type="button"
            onClick={() => handleToggle(item.id)}
            className={`w-5 h-5 rounded-md border-2 flex items-center justify-center flex-shrink-0 transition-all ${
              item.completed
                ? 'bg-indigo-600 border-indigo-600 text-white'
                : 'border-slate-300 group-hover:border-indigo-400'
            }`}
          >
            {item.completed && (
              <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            )}
          </button>
          <span
            className={`text-sm transition-all ${
              item.completed ? 'text-slate-400 line-through' : 'text-slate-700'
            }`}
          >
            {item.label}
          </span>
        </label>
      ))}
    </div>
  );
}
