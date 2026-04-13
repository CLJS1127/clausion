export interface RecommendationAction {
  label: string;
  path: string;
}

/** Normalize backend recommendation types (UPPERCASE) to frontend keys (lowercase). */
export function normalizeType(type?: string | null): string {
  switch (type?.toUpperCase()) {
    case 'REVIEW':
      return 'review';
    case 'PRACTICE':
    case 'CHALLENGE':
      return 'practice';
    case 'CONSULTATION':
    case 'COURSE':
      return 'consultation';
    case 'RESOURCE':
      return 'resource';
    default:
      return type?.toLowerCase() ?? 'review';
  }
}

export function getRecommendationAction(type?: string | null): RecommendationAction {
  switch (type?.toUpperCase()) {
    case 'REVIEW':
      return { label: '복습하기', path: '/student/review' };
    case 'CONSULTATION':
    case 'COURSE':
      return { label: '상담 보기', path: '/student/consultation' };
    case 'PRACTICE':
    case 'CHALLENGE':
      return { label: '실습하기', path: '/student' };
    case 'RESOURCE':
      return { label: '대시보드 열기', path: '/student' };
    default:
      return { label: '열어보기', path: '/student/review' };
  }
}
