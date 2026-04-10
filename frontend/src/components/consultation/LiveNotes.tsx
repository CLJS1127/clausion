import { useState } from 'react';

interface NoteEntry {
  id: string;
  timestamp: string;
  content: string;
}

const MOCK_NOTES: NoteEntry[] = [
  { id: 'n1', timestamp: '10:02:15', content: '학생이 React 상태 관리에 대한 혼란 표현' },
  { id: 'n2', timestamp: '10:05:42', content: 'useEffect 의존성 배열 개념 재설명 필요' },
  { id: 'n3', timestamp: '10:08:30', content: '실습 과제 어려움 호소 - 단계별 접근 방법 안내' },
];

export default function LiveNotes() {
  const [notes, setNotes] = useState<NoteEntry[]>(MOCK_NOTES);
  const [input, setInput] = useState('');

  const addNote = () => {
    if (!input.trim()) return;
    const now = new Date();
    const timestamp = now.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
    setNotes((prev) => [
      ...prev,
      { id: `n-${Date.now()}`, timestamp, content: input.trim() },
    ]);
    setInput('');
  };

  return (
    <div className="bg-slate-800 rounded-2xl p-4 flex flex-col h-full">
      <h4 className="text-xs font-semibold text-slate-300 mb-3 uppercase tracking-wider">
        실시간 메모
      </h4>

      <div className="flex-1 overflow-y-auto space-y-2 mb-3">
        {notes.map((note) => (
          <div key={note.id} className="flex gap-2 text-xs">
            <span
              className="text-slate-500 flex-shrink-0 font-mono"
              style={{ fontFamily: "'JetBrains Mono', monospace" }}
            >
              {note.timestamp}
            </span>
            <span className="text-slate-300 leading-relaxed">{note.content}</span>
          </div>
        ))}
      </div>

      <div className="flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && addNote()}
          placeholder="메모 추가..."
          className="flex-1 bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
        />
        <button
          onClick={addNote}
          className="px-3 py-2 text-xs font-medium rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors flex-shrink-0"
        >
          추가
        </button>
      </div>
    </div>
  );
}
