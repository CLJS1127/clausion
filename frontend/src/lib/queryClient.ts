import { QueryClient, MutationCache } from '@tanstack/react-query';
import { ApiError } from '../api/client';
import { toast } from '../store/toastStore';

export const queryClient = new QueryClient({
  // 전역 mutation 에러 처리: 화면에서 자체 onError를 정의하지 않은 모든
  // mutation 실패를 에러 토스트로 사용자에게 알린다.
  mutationCache: new MutationCache({
    onError: (error, _variables, _context, mutation) => {
      if (mutation.options.onError) return; // 화면 자체 처리가 있으면 중복 알림 생략
      const detail =
        error instanceof ApiError
          ? error.message
          : '네트워크 연결을 확인한 뒤 다시 시도해주세요.';
      toast.error('요청 처리에 실패했습니다', detail);
    },
  }),
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 2,
      retry: 1,
    },
  },
});
