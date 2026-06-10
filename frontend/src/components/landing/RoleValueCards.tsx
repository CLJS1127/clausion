import { motion } from 'framer-motion';

const roles = [
  {
    icon: '\u{1F393}',
    title: '학생',
    subtitle: '나만의 AI 학습 분신',
    features: [
      '개인화된 복습 과제 자동 생성',
      '망각 위험 사전 경고',
      'AI 챗봇으로 24시간 학습 지원',
      '코드 분석 및 실시간 피드백',
      '게이미피케이션으로 학습 동기 유지',
    ],
    gradient: 'from-violet-500 to-indigo-600',
    bg: 'bg-violet-50',
    border: 'border-violet-100',
  },
  {
    icon: '\u{1F468}\u{200D}\u{1F3EB}',
    title: '강사',
    subtitle: '데이터 기반 교육 개입',
    features: [
      '학생별 위험 신호 실시간 모니터링',
      '최적 상담 타이밍 AI 추천',
      'AI 문제 자동 생성 및 검토',
      '상담 이력 및 액션 플랜 관리',
      '학습 성과 분석 대시보드',
    ],
    gradient: 'from-indigo-500 to-blue-600',
    bg: 'bg-indigo-50',
    border: 'border-indigo-200',
  },
  {
    icon: '\u{1F4CA}',
    title: '운영자',
    subtitle: '과정 횡단 운영 관리',
    features: [
      '과정 개설 승인·반려 결재',
      '전체 위험 학생 횡단 모니터링',
      '강사 효과성 비교 분석',
      'AI 기반 개입 지시 센터',
      '주간 운영 리포트 자동 생성',
    ],
    gradient: 'from-slate-600 to-slate-800',
    bg: 'bg-slate-50',
    border: 'border-slate-200',
  },
];

export default function RoleValueCards() {
  return (
    <section className="py-24 px-6 bg-slate-50">
      <div className="max-w-6xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="text-center mb-16"
        >
          <span className="text-sm font-bold text-indigo-600 uppercase tracking-wider">역할별 맞춤</span>
          <h2 className="text-3xl md:text-4xl font-extrabold text-slate-900 mt-3">
            역할별 핵심 가치
          </h2>
          <p className="text-slate-500 mt-3 max-w-xl mx-auto">
            학생, 강사, 운영자 모두에게 최적화된 경험을 제공합니다.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {roles.map((role, i) => (
            <motion.div
              key={role.title}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.12 }}
              className={`relative p-8 rounded-2xl ${role.bg} border ${role.border} hover:shadow-xl transition-all group`}
            >
              <div className={`inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br ${role.gradient} text-2xl mb-4`}>
                {role.icon}
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-1">{role.title}</h3>
              <p className="text-sm text-slate-500 mb-5">{role.subtitle}</p>
              <ul className="space-y-2.5">
                {role.features.map((f) => (
                  <li key={f} className="flex items-start gap-2 text-sm text-slate-700">
                    <svg className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                    {f}
                  </li>
                ))}
              </ul>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
