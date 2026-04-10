import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import TodayActionPanel from '../../components/student/TodayActionPanel';
import TwinStateCard from '../../components/student/TwinStateCard';
import GamificationBar from '../../components/student/GamificationBar';
import CodeEditorPanel from '../../components/student/CodeEditorPanel';
import ReviewTimeline from '../../components/student/ReviewTimeline';
import NextStepPrescriptionCard from '../../components/student/NextStepPrescriptionCard';
import StudyGroupPanel from '../../components/student/StudyGroupPanel';

const Dashboard: React.FC = () => {
  const [chatOpen, setChatOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Top header */}
      <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-slate-100">
        <div className="max-w-7xl mx-auto px-6 py-3 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-slate-900">
              ClassPulse Twin
            </h1>
            <p className="text-xs text-slate-500">학생 대시보드</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center text-white text-sm font-bold">
              J
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-6 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-7 gap-6">
          {/* Left Column (5/7) */}
          <div className="lg:col-span-5 space-y-6">
            <TodayActionPanel />
            <CodeEditorPanel />
            <ReviewTimeline />
          </div>

          {/* Right Column (2/7) */}
          <div className="lg:col-span-2 space-y-6">
            <TwinStateCard />
            <GamificationBar />
            <NextStepPrescriptionCard />
            <StudyGroupPanel />
          </div>
        </div>
      </main>

      {/* Chatbot floating button */}
      <div className="fixed bottom-6 right-6 z-50">
        <AnimatePresence>
          {chatOpen && (
            <motion.div
              initial={{ opacity: 0, y: 20, scale: 0.9 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 20, scale: 0.9 }}
              className="absolute bottom-16 right-0 w-80 h-96 bg-white rounded-2xl shadow-2xl border border-slate-300 overflow-hidden"
            >
              <div className="flex items-center justify-between px-4 py-3 bg-indigo-600 text-white">
                <span className="text-sm font-semibold">AI 학습 도우미</span>
                <button
                  onClick={() => setChatOpen(false)}
                  className="text-white/80 hover:text-white"
                >
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M6 18L18 6M6 6l12 12"
                    />
                  </svg>
                </button>
              </div>
              <div className="flex-1 p-4 overflow-y-auto h-[calc(100%-88px)]">
                <div className="bg-indigo-50 rounded-xl p-3 max-w-[85%]">
                  <p className="text-xs text-indigo-700">
                    안녕하세요! 학습 중 궁금한 점이 있으면 물어보세요. 복습
                    계획이나 코드 질문도 도와드릴 수 있어요.
                  </p>
                </div>
              </div>
              <div className="px-3 py-2 border-t border-slate-100">
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    placeholder="질문을 입력하세요..."
                    className="flex-1 text-sm rounded-lg border border-slate-300 px-3 py-2 focus:outline-none focus:border-indigo-400"
                  />
                  <button className="rounded-lg bg-indigo-600 p-2 text-white hover:bg-indigo-700">
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                      />
                    </svg>
                  </button>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setChatOpen(!chatOpen)}
          className="w-14 h-14 rounded-full bg-gradient-to-br from-indigo-600 to-violet-600 text-white shadow-lg flex items-center justify-center hover:shadow-xl transition-shadow"
        >
          {chatOpen ? (
            <svg
              className="w-6 h-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          ) : (
            <svg
              className="w-6 h-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              />
            </svg>
          )}
        </motion.button>
      </div>
    </div>
  );
};

export default Dashboard;
