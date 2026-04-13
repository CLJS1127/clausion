import React, { useRef, useEffect, useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useChatbotStore } from '../../store/chatbotStore';
import { useChatbot } from '../../hooks/useChatbot';
import TwinContextBanner from './TwinContextBanner';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import ChatHistory from './ChatHistory';

const MODAL_SPRING = {
  type: 'spring' as const,
  stiffness: 500,
  damping: 35,
  mass: 0.8,
};

const ChatbotModal: React.FC = () => {
  const { isOpen, close, setUnreadCount } = useChatbotStore();
  const {
    messages,
    conversations,
    currentConversationId,
    sendMessage,
    isSending,
    createNewConversation,
    selectConversation,
    deleteConversation,
  } = useChatbot();

  const [historyOpen, setHistoryOpen] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Clear unread when opened
  useEffect(() => {
    if (isOpen) {
      setUnreadCount(0);
    }
  }, [isOpen, setUnreadCount]);

  const handleSend = useCallback(
    (content: string) => {
      sendMessage(content);
    },
    [sendMessage],
  );

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-sm"
            onClick={close}
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, y: 40, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 40, scale: 0.95 }}
            transition={MODAL_SPRING}
            className="fixed inset-3 z-50 sm:inset-auto sm:bottom-24 sm:right-8 w-auto sm:w-[420px] h-auto sm:h-[580px] flex flex-col rounded-2xl bg-white shadow-2xl border border-slate-300/50 overflow-hidden"
          >
            {/* Header */}
            <div
              className="relative shrink-0 flex items-center justify-between px-4 py-3.5"
              style={{
                background: 'linear-gradient(135deg, #4f46e5, #7c3aed)',
              }}
            >
              <div className="flex items-center gap-3">
                {/* History toggle */}
                <button
                  onClick={() => setHistoryOpen(!historyOpen)}
                  className="w-8 h-8 rounded-lg bg-white/15 hover:bg-white/25 flex items-center justify-center transition-colors"
                  aria-label="대화 기록"
                >
                  <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </button>

                <div>
                  <h2 className="text-sm font-bold text-white leading-tight">
                    Twin AI 학습 도우미
                  </h2>
                  <p className="text-[10px] text-indigo-200 mt-0.5">
                    맞춤형 학습 지원
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-1.5">
                {/* New chat */}
                <button
                  onClick={createNewConversation}
                  className="w-8 h-8 rounded-lg bg-white/15 hover:bg-white/25 flex items-center justify-center transition-colors"
                  aria-label="새 대화"
                >
                  <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                  </svg>
                </button>

                {/* Close */}
                <button
                  onClick={close}
                  className="w-8 h-8 rounded-lg bg-white/15 hover:bg-white/25 flex items-center justify-center transition-colors"
                  aria-label="챗봇 닫기"
                >
                  <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              {/* Gradient glow */}
              <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white/30 to-transparent" />
            </div>

            {/* Twin Context Banner */}
            <TwinContextBanner />

            {/* Messages area */}
            <div className="relative flex-1 overflow-y-auto px-4 py-3 space-y-3 scroll-smooth">
              {messages.length === 0 ? (
                <EmptyState />
              ) : (
                messages.map((msg) => (
                  <ChatMessage key={msg.id} message={msg} />
                ))
              )}

              {/* Typing indicator */}
              {isSending && <TypingIndicator />}

              <div ref={messagesEndRef} />
            </div>

            {/* Input */}
            <ChatInput onSend={handleSend} disabled={isSending} />

            {/* Chat History overlay */}
            <ChatHistory
              conversations={conversations}
              currentId={currentConversationId}
              onSelect={selectConversation}
              onDelete={deleteConversation}
              onNewChat={createNewConversation}
              onClose={() => setHistoryOpen(false)}
              isOpen={historyOpen}
            />
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

/* ── Sub-components ──────────────────────────────── */

const EmptyState: React.FC = () => (
  <div className="flex flex-col items-center justify-center h-full text-center px-6">
    <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-100 to-violet-100 flex items-center justify-center mb-4">
      <svg className="w-8 h-8 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
      </svg>
    </div>
    <h3 className="text-sm font-bold text-slate-700 mb-1">
      Twin AI에게 질문해보세요
    </h3>
    <p className="text-xs text-slate-400 leading-relaxed">
      디지털 트윈 분석을 기반으로<br />맞춤형 학습 도움을 드립니다.
    </p>
    <div className="mt-4 space-y-2 w-full">
      {[
        '오늘 복습할 내용이 뭐야?',
        '재귀 함수가 잘 이해가 안 돼',
        '이번 주 학습 진도가 어때?',
      ].map((q) => (
        <div
          key={q}
          className="text-[11px] text-indigo-600 bg-indigo-50/60 border border-indigo-200 rounded-lg px-3 py-2 text-left cursor-default hover:bg-indigo-50 transition-colors"
        >
          {q}
        </div>
      ))}
    </div>
  </div>
);

const TypingIndicator: React.FC = () => (
  <div className="flex items-center gap-2">
    <div className="w-7 h-7 rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center">
      <svg className="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
      </svg>
    </div>
    <div className="bg-white border border-slate-100 rounded-2xl rounded-bl-md px-3.5 py-2.5 shadow-sm">
      <div className="flex items-center gap-1">
        {[0, 1, 2].map((i) => (
          <motion.span
            key={i}
            className="w-1.5 h-1.5 rounded-full bg-slate-400"
            animate={{ y: [0, -4, 0] }}
            transition={{
              duration: 0.6,
              repeat: Infinity,
              delay: i * 0.15,
            }}
          />
        ))}
      </div>
    </div>
  </div>
);

export default ChatbotModal;
