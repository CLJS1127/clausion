# Clausion (ClassPulse Twin)

> **AI 기반 직업훈련 학습관리 플랫폼** — 학생마다 "디지털 트윈"을 만들어 학습 상태를 추론하고, 강사에게는 데이터 기반 인사이트를, 학생에게는 개인화된 학습 처방을 제공합니다.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen) ![React](https://img.shields.io/badge/React-19-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178c6) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791)

## 왜 만들었나

직업훈련 현장에서 강사 1명이 20~40명의 수강생을 동시에 관리합니다. "누가 어디서 막혔는지"를 사람이 전부 추적하는 것은 불가능에 가깝습니다. Clausion은 학생의 활동 데이터(성찰일지, 코드 제출, 복습 완료율, 챗봇 대화, 출석 등 8개 소스)를 종합해 학생별 **디지털 트윈**을 만들고, 개입이 필요한 시점을 강사와 운영자에게 알려줍니다.

```
실제 학생 ←→ 디지털 트윈 (5차원 상태 모델)
              ├── 숙달도 (Mastery)
              ├── 실행력 (Execution)
              ├── 동기 (Motivation)
              ├── 망각 위험 (Retention Risk)
              └── 상담 필요도 (Consultation Need)
```

## 핵심 설계: 하이브리드 AI (규칙 + LLM)

트윈 점수는 LLM에만 맡기지 않습니다. **규칙 기반 계산(결정적, 무료, 즉시)** 이 기본 점수를 만들고, **LLM(GPT-4o)** 은 데이터 충돌 감지·점수 보정(±10 제한)·자연어 인사이트 생성만 담당합니다.

```
8개 데이터 소스 수집 (TwinDataCollector)
    ↓
규칙 기반 5차원 점수 계산 (TwinRuleCalculator — 순수 로직, 단위 테스트 완비)
    ↓
LLM 검증·보정 (±10 제한) + 인사이트 생성 (TwinInferenceEngine)
    ↓
이상치 차단 (한 번에 ±25점 이상 변동 불가)
```

- **왜?** 비용(학생 100명 × 매 활동마다 LLM 호출은 비현실적), 일관성(LLM은 같은 입력에 다른 점수를 줌), 장애 대응(OpenAI 다운 시에도 규칙 점수는 계산 가능).
- 여기에 **Redis 5분 디바운싱**으로 연속 활동(성찰 저장 → 코드 제출 → 복습 완료)을 1회의 종합 추론으로 묶어 **불필요한 API 호출 ~80% 절감**.
- 설계 결정의 전체 배경은 [docs/PROJECT_BLUEPRINT.md](docs/PROJECT_BLUEPRINT.md) 참고.

## 주요 기능 (3-Role)

| 학생 | 강사 | 운영자 |
|---|---|---|
| 트윈 대시보드 (5차원 레이더) | 학생 위험도 히트맵 | 과정 결재함 (승인/반려) |
| 간격 반복 복습 (망각곡선 기반 적응형) | AI 상담 브리핑/사후 요약 | 위험 학생 횡단 조회 + 개입 센터 |
| 코드 제출 → AI 피드백 (CodeMirror) | 커리큘럼 업로드 → AI 스킬 분석 | 강사 효과성 분석 |
| 트윈 인식 학습 챗봇 (소크라테스식) | AI 문항 생성 → 검토/승인 | What-If 시뮬레이션 |
| 스터디 그룹 (AI 매칭) + 그룹 채팅 | 출석/수강신청 관리 | 감사 로그, 주간 리포트 |
| 화상 상담 (LiveKit) + 게이미피케이션 | 화상 상담 | 초대 코드 관리 |

## 기술 스택

| 레이어 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5, Spring Security (JWT), Spring Data JPA, Flyway |
| AI | OpenAI GPT-4o (JSON Mode), 8개 독립 AI 엔진 (커리큘럼 분석 / 문항 생성 / 트윈 추론 / 상담 코파일럿 / 학습 추천 / 챗봇 / 코드 리뷰 / 스터디 매칭) |
| Frontend | React 19, TypeScript, Vite, Zustand, TanStack Query, Tailwind CSS 4, Recharts, CodeMirror 6 |
| Infra | PostgreSQL, Redis (디바운싱·캐시), RabbitMQ (알림·그룹채팅), AWS S3, LiveKit (WebRTC) |
| 실시간 | SSE (알림), WebSocket + STOMP (그룹 채팅), WebRTC (화상 상담) |

## 트러블슈팅 기록

실제로 겪고 해결한 문제들입니다. (커밋 히스토리에서 확인 가능)

1. **Lombok `@Builder`가 필드 초기값을 무시해 시드 강의가 학생 목록에서 사라진 버그** — `Course.builder()`로 만든 강의의 `approval_status`가 DB에 NULL로 저장되어, `APPROVED` 필터 쿼리에서 누락. `@Builder.Default` 추가 + V20 백필 마이그레이션 + 회귀 테스트(`CourseBuilderDefaultsTest`)로 재발 방지.
2. **스터디 그룹 삭제가 FK 제약으로 실패** — 그룹 메시지가 그룹을 참조하고 있어 삭제 불가. 연쇄 삭제 경계를 정리해 해결.
3. **수강신청 페이지 공백** — `LazyInitializationException`. 트랜잭션 경계 밖에서 LAZY 컬렉션 접근이 원인.
4. **테스트 불가능한 코어 로직** — 트윈 점수 계산이 LLM 호출·DB 저장과 한 메서드에 섞여 있어 `TwinRuleCalculator`로 순수 로직을 분리하고 단위 테스트를 작성.

## 실행 방법

### 사전 요구사항

- JDK 17, Node.js 20+, PostgreSQL 16+, Redis, RabbitMQ (호스트 설치 기준 — `docker-compose.yml`도 제공)

### 설정

```powershell
# 1. 환경변수 템플릿 복사 후 값 채우기 (OPENAI_API_KEY 등)
cp .env.example .env

# 2. 백엔드 (빌드 + 실행, Flyway 마이그레이션 자동 적용)
.\run-backend.ps1

# 3. 프론트엔드 (https://localhost:5173)
.\run-frontend.ps1
```

### 데모 계정 (시더 자동 생성, 비밀번호 공통 `password123`)

| 역할 | 이메일 |
|---|---|
| 학생 | `student001@classpulse.dev` ~ |
| 강사 | `instructor01@classpulse.dev` ~ |
| 운영자 | `operator@classpulse.dev` |

### 테스트

```powershell
.\backend\gradlew.bat -p backend test
```

## 프로젝트 구조

```
backend/src/main/java/com/classpulse/
├── ai/          # 8개 AI 엔진 (TwinInferenceEngine, TwinRuleCalculator, ChatbotAi ...)
├── api/         # REST 컨트롤러 30+ (역할별: 학생/강사/운영자)
├── config/      # Security(JWT), Redis, RabbitMQ, S3, WebSocket
├── domain/      # 15+ 도메인 모듈 (twin, course, learning, consultation ...)
├── notification/# SSE + RabbitMQ 알림 파이프라인
└── seed/        # 데모 데이터 시더

frontend/src/
├── pages/       # student / instructor / operator 역할별 페이지 40+
├── components/  # 도메인별 컴포넌트 + 공용 UI
├── api/         # 도메인별 API 클라이언트 (1 파일 = 1 도메인)
├── store/       # Zustand (auth, course, sidebar, chatbot)
└── hooks/       # useLiveKit, useAsyncJob, useStudentTwin ...
```

## 문서

- [PROJECT_BLUEPRINT.md](docs/PROJECT_BLUEPRINT.md) — "왜 이렇게 만들었는가" 설계 결정 기록
- [교육운영자 기획안](docs/교육운영자_기획안_v2.md) / [운영자 횡단관리 구현계획](docs/운영자_횡단관리_구현계획.md)
