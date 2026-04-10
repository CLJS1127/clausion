import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useChatbotStore } from '../../store/chatbotStore';

const ChatbotFloatingButton: React.FC = () => {
  const { toggle, unreadCount, isOpen } = useChatbotStore();

  return (
    <motion.button
      onClick={toggle}
      className="fixed bottom-8 right-8 z-50 flex items-center justify-center w-14 h-14 rounded-full shadow-xl cursor-pointer focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:ring-offset-2"
      style={{
        background: 'linear-gradient(135deg, #4f46e5, #7c3aed)',
      }}
      whileHover={{ scale: 1.1 }}
      whileTap={{ scale: 0.95 }}
      transition={{ type: 'spring', stiffness: 400, damping: 17 }}
      aria-label={isOpen ? '챗봇 닫기' : '챗봇 열기'}
    >
      {/* Chat icon */}
      <AnimatePresence mode="wait">
        {isOpen ? (
          <motion.svg
            key="close"
            initial={{ rotate: -90, opacity: 0 }}
            animate={{ rotate: 0, opacity: 1 }}
            exit={{ rotate: 90, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="w-6 h-6 text-white"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </motion.svg>
        ) : (
          <motion.svg
            key="chat"
            initial={{ rotate: 90, opacity: 0 }}
            animate={{ rotate: 0, opacity: 1 }}
            exit={{ rotate: -90, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="w-6 h-6 text-white"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </motion.svg>
        )}
      </AnimatePresence>

      {/* Unread badge */}
      <AnimatePresence>
        {unreadCount > 0 && !isOpen && (
          <motion.span
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            exit={{ scale: 0 }}
            transition={{ type: 'spring', stiffness: 500, damping: 25 }}
            className="absolute -top-1 -right-1 flex items-center justify-center min-w-[20px] h-5 px-1 rounded-full bg-red-500 text-white text-[11px] font-bold shadow-lg"
          >
            {unreadCount > 99 ? '99+' : unreadCount}
          </motion.span>
        )}
      </AnimatePresence>

      {/* Pulse ring */}
      {!isOpen && unreadCount > 0 && (
        <span className="absolute inset-0 rounded-full animate-ping bg-indigo-400 opacity-20 pointer-events-none" />
      )}
    </motion.button>
  );
};

export default ChatbotFloatingButton;
