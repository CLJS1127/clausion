# Clausion Frontend

React 19 + TypeScript + Vite 기반 SPA. 전체 프로젝트 개요는 [루트 README](../README.md) 참고.

## 실행

```bash
npm install
npm run dev    # https://localhost:5173 (자체 서명 인증서)
```

`/api`, `/ws-chat` 요청은 vite proxy가 백엔드(`http://localhost:8080`)로 전달합니다.
프록시 대상 변경: `VITE_PROXY_TARGET` 환경변수.

## 구조

```
src/
├── pages/       # 라우트 단위 페이지 — student / instructor / operator
├── components/  # 도메인별 컴포넌트 (student, instructor, consultation, chatbot, common, layout)
├── api/         # fetch 기반 API 클라이언트 (1 파일 = 1 도메인, Bearer 토큰 자동 주입)
├── store/       # Zustand — authStore(localStorage 영속), courseStore, sidebarStore, chatbotStore
├── hooks/       # useLiveKit(WebRTC), useAsyncJob(작업 폴링), useStudentTwin 등
└── types/       # 도메인 타입 정의
```

## 상태 관리 원칙

- **서버 상태**: TanStack Query (`queryKey`에 courseId 포함 → 과정 전환 시 자동 캐시 무효화)
- **클라이언트 상태**: Zustand 스토어 4개만 사용

## 빌드/품질

```bash
npm run build    # tsc -b && vite build
npm run lint     # ESLint
```
