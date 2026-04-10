import React, { useState, useRef, useCallback } from 'react';
import { motion } from 'framer-motion';

interface ChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

const ChatInput: React.FC<ChatInputProps> = ({
  onSend,
  disabled = false,
  placeholder = '질문을 입력하세요...',
}) => {
  const [text, setText] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const handleSend = useCallback(() => {
    const trimmed = text.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setText('');
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  }, [text, disabled, onSend]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleInput = () => {
    const el = textareaRef.current;
    if (el) {
      el.style.height = 'auto';
      el.style.height = `${Math.min(el.scrollHeight, 120)}px`;
    }
  };

  const canSend = text.trim().length > 0 && !disabled;

  return (
    <div className="border-t border-slate-100 bg-white px-4 py-3">
      <div className="flex items-end gap-2 rounded-xl border border-slate-300 bg-slate-50 px-3 py-2 transition-colors focus-within:border-indigo-300 focus-within:bg-white">
        <textarea
          ref={textareaRef}
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          onInput={handleInput}
          placeholder={placeholder}
          disabled={disabled}
          rows={1}
          className="flex-1 resize-none bg-transparent text-sm text-slate-800 placeholder:text-slate-400 outline-none leading-relaxed max-h-[120px] scrollbar-thin"
          style={{ minHeight: '24px' }}
        />
        <motion.button
          onClick={handleSend}
          disabled={!canSend}
          whileHover={canSend ? { scale: 1.05 } : {}}
          whileTap={canSend ? { scale: 0.95 } : {}}
          className={`shrink-0 flex items-center justify-center w-8 h-8 rounded-lg transition-all ${
            canSend
              ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md cursor-pointer'
              : 'bg-slate-200 text-slate-400 cursor-not-allowed'
          }`}
          aria-label="메시지 보내기"
        >
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
          </svg>
        </motion.button>
      </div>
      <p className="text-[10px] text-slate-400 mt-1.5 text-center">
        Shift + Enter로 줄바꿈
      </p>
    </div>
  );
};

export default ChatInput;
