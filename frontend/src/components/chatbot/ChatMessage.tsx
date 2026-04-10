import React from 'react';
import { motion } from 'framer-motion';
import type { ChatMessage as ChatMessageType, InlineChatCard } from '../../types';

interface ChatMessageProps {
  message: ChatMessageType;
}

/* ── Inline Card Renderers ───────────────────────── */

const ReviewStepsCard: React.FC<{ data: Record<string, unknown> }> = ({ data }) => {
  const steps = (data.steps as string[]) ?? [];
  const title = (data.title as string) ?? '복습 단계';

  return (
    <div className="mt-2 rounded-lg border border-indigo-200 bg-indigo-50/60 p-3">
      <p className="text-xs font-semibold text-indigo-700 mb-2">{title}</p>
      <ol className="space-y-1.5">
        {steps.map((step, i) => (
          <li key={i} className="flex items-start gap-2 text-xs text-indigo-600">
            <span className="shrink-0 w-5 h-5 rounded-full bg-indigo-100 flex items-center justify-center text-[10px] font-bold text-indigo-700 mt-0.5">
              {i + 1}
            </span>
            <span className="leading-relaxed">{step}</span>
          </li>
        ))}
      </ol>
    </div>
  );
};

const ResourceLinkCard: React.FC<{ data: Record<string, unknown> }> = ({ data }) => {
  const title = (data.title as string) ?? '학습 리소스';
  const url = (data.url as string) ?? '#';
  const description = (data.description as string) ?? '';

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className="mt-2 flex items-center gap-3 rounded-lg border border-violet-100 bg-violet-50/60 p-3 hover:bg-violet-50 transition-colors group"
    >
      <span className="shrink-0 w-8 h-8 rounded-lg bg-violet-100 flex items-center justify-center">
        <svg className="w-4 h-4 text-violet-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
        </svg>
      </span>
      <div className="flex-1 min-w-0">
        <p className="text-xs font-semibold text-violet-700 group-hover:underline truncate">{title}</p>
        {description && (
          <p className="text-[10px] text-violet-500 truncate mt-0.5">{description}</p>
        )}
      </div>
      <svg className="w-4 h-4 text-violet-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
      </svg>
    </a>
  );
};

const ActionConfirmCard: React.FC<{ data: Record<string, unknown> }> = ({ data }) => {
  const label = (data.label as string) ?? '확인';
  const description = (data.description as string) ?? '';

  return (
    <div className="mt-2 rounded-lg border border-emerald-100 bg-emerald-50/60 p-3">
      <div className="flex items-center gap-2">
        <svg className="w-4 h-4 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span className="text-xs font-semibold text-emerald-700">{label}</span>
      </div>
      {description && (
        <p className="text-[10px] text-emerald-600 mt-1 ml-6">{description}</p>
      )}
    </div>
  );
};

const InlineCardRenderer: React.FC<{ card: InlineChatCard }> = ({ card }) => {
  switch (card.type) {
    case 'review_steps':
      return <ReviewStepsCard data={card.data} />;
    case 'resource_link':
      return <ResourceLinkCard data={card.data} />;
    case 'action_confirm':
      return <ActionConfirmCard data={card.data} />;
    default:
      return null;
  }
};

/* ── Main Component ───────────────────────────────── */

const ChatMessage: React.FC<ChatMessageProps> = ({ message }) => {
  const isUser = message.role === 'user';
  const isSystem = message.role === 'system';

  if (isSystem) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex justify-center py-2"
      >
        <span className="text-[11px] text-slate-400 bg-slate-50 rounded-full px-3 py-1">
          {message.content}
        </span>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className={`flex gap-2 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}
    >
      {/* Avatar */}
      {!isUser && (
        <div className="shrink-0 w-7 h-7 rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center mt-1">
          <svg className="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </div>
      )}

      {/* Bubble */}
      <div
        className={`max-w-[80%] rounded-2xl px-3.5 py-2.5 ${
          isUser
            ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-md'
            : 'bg-white border border-slate-100 text-slate-800 shadow-sm rounded-bl-md'
        }`}
      >
        <p className={`text-[13px] leading-relaxed whitespace-pre-wrap ${isUser ? 'text-white' : 'text-slate-700'}`}>
          {message.content}
        </p>

        {/* Inline cards */}
        {message.inlineCards?.map((card, idx) => (
          <InlineCardRenderer key={idx} card={card} />
        ))}

        {/* Timestamp */}
        <p className={`text-[10px] mt-1.5 ${isUser ? 'text-indigo-200' : 'text-slate-400'} ${isUser ? 'text-left' : 'text-right'}`}>
          {formatTime(message.timestamp)}
        </p>
      </div>
    </motion.div>
  );
};

function formatTime(ts: string): string {
  try {
    const d = new Date(ts);
    return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  } catch {
    return '';
  }
}

export default ChatMessage;
