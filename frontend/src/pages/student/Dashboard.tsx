import React from 'react';
import { useNavigate } from 'react-router-dom';
import TodayActionPanel from '../../components/student/TodayActionPanel';
import TwinStateCard from '../../components/student/TwinStateCard';
import GamificationBar from '../../components/student/GamificationBar';
import CodeEditorPanel from '../../components/student/CodeEditorPanel';
import ReviewTimeline from '../../components/student/ReviewTimeline';
import NextStepPrescriptionCard from '../../components/student/NextStepPrescriptionCard';
import StudyGroupPanel from '../../components/student/StudyGroupPanel';
import { useAuthStore } from '../../store/authStore';
import { useCourseId, useMyEnrollments } from '../../hooks/useCourseId';
import { getDisplayInitial } from '../../utils/userDisplay';

const Dashboard: React.FC = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const courseId = useCourseId();
  const { data: enrollments, isLoading: enrollmentsLoading } = useMyEnrollments();
  const hasPendingEnrollment = (enrollments ?? []).some((e) => e.status === 'PENDING');

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Top header */}
      <header className="sticky top-[41px] lg:top-0 z-30 bg-white/80 backdrop-blur border-b border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-3 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-slate-900">
              ClassPulse Twin
            </h1>
            <p className="text-xs text-slate-500">학생 대시보드</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center text-white text-sm font-bold">
              {getDisplayInitial(user?.name)}
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-4 sm:py-6">
        {!courseId && !enrollmentsLoading ? (
          /* 수강 중인 과정이 없는 신규 학생 안내 */
          <div className="bg-white/85 backdrop-blur border border-slate-200 rounded-2xl p-10 text-center">
            <p className="text-3xl mb-3">{hasPendingEnrollment ? '⏳' : '📚'}</p>
            {hasPendingEnrollment ? (
              <>
                <h2 className="text-base font-bold text-slate-800">수강 신청이 승인 대기 중입니다</h2>
                <p className="text-sm text-slate-500 mt-1">
                  강사가 신청을 승인하면 학습을 시작할 수 있습니다. 조금만 기다려주세요!
                </p>
                <button
                  onClick={() => navigate('/student/courses')}
                  className="mt-5 px-5 py-2.5 rounded-lg bg-slate-100 text-slate-700 text-sm font-medium hover:bg-slate-200 transition-colors"
                >
                  신청 현황 보기
                </button>
              </>
            ) : (
              <>
                <h2 className="text-base font-bold text-slate-800">아직 수강 중인 과정이 없습니다</h2>
                <p className="text-sm text-slate-500 mt-1">
                  과정을 수강 신청하면 AI가 학습 상태를 분석하고 맞춤 복습을 추천해드립니다.
                </p>
                <button
                  onClick={() => navigate('/student/courses')}
                  className="mt-5 px-5 py-2.5 rounded-lg bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700 transition-colors"
                >
                  과정 둘러보기
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-7 gap-4 sm:gap-6">
            {/* Left Column (5/7) */}
            <div className="md:col-span-2 lg:col-span-5 space-y-4 sm:space-y-6">
              <TodayActionPanel />
              <CodeEditorPanel />
              <ReviewTimeline />
            </div>

            {/* Right Column (2/7) */}
            <div className="md:col-span-2 lg:col-span-2 space-y-4 sm:space-y-6">
              <TwinStateCard />
              <GamificationBar />
              <NextStepPrescriptionCard />
              <StudyGroupPanel />
            </div>
          </div>
        )}
      </main>

    </div>
  );
};

export default Dashboard;
