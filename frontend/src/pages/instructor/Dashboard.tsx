import RiskAlertBanner from '../../components/instructor/RiskAlertBanner';
import RiskHeatmap from '../../components/instructor/RiskHeatmap';
import UpcomingConsultationPanel from '../../components/instructor/UpcomingConsultationPanel';
import QuestionReviewPanel from '../../components/instructor/QuestionReviewPanel';
import CourseProgressChart from '../../components/instructor/CourseProgressChart';

export default function InstructorDashboard() {
  const dangerCount = 4; // In production, derive from query

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50/30">
      {/* Header */}
      <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-md border-b border-slate-100">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-sm font-bold text-indigo-700">
              김
            </div>
            <div>
              <h1 className="text-base font-bold text-slate-800">김교수님의 대시보드</h1>
              <p className="text-xs text-slate-500">React 심화 과정 · 6주차</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="relative flex items-center gap-2 px-3 py-1.5 rounded-full bg-rose-50 border border-rose-200">
              <span className="relative flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-rose-500" />
              </span>
              <span className="text-xs font-semibold text-rose-700">
                위험 학생 {dangerCount}명
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="max-w-7xl mx-auto px-6 py-6 space-y-6">
        {/* Risk Alert Banner */}
        <RiskAlertBanner />

        {/* Row 1: Heatmap + Upcoming Consultations */}
        <div className="grid grid-cols-3 gap-5">
          <div className="col-span-2">
            <RiskHeatmap />
          </div>
          <div>
            <UpcomingConsultationPanel />
          </div>
        </div>

        {/* Row 2: Question Review + Course Progress */}
        <div className="grid grid-cols-2 gap-5">
          <QuestionReviewPanel />
          <CourseProgressChart />
        </div>
      </main>
    </div>
  );
}
