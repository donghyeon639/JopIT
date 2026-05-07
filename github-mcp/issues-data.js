export const issuesData = {
  labels: [
    { name: "epic",        color: "6f42c1", description: "최상위 목표 단위" },
    { name: "feature",     color: "0075ca", description: "기능 단위" },
    { name: "task",        color: "cfd3d7", description: "구현 작업 단위" },
    { name: "backend",     color: "e4e669", description: "Spring Boot 백엔드" },
    { name: "frontend",    color: "f9d0c4", description: "React 프런트엔드" },
    { name: "security",    color: "d93f0b", description: "인증/보안" },
    { name: "mvp",         color: "0e8a16", description: "1차 MVP 범위" },
    { name: "enhancement", color: "84b6eb", description: "기존 기능 개선" },
    { name: "bug",         color: "ee0701", description: "버그 수정" },
  ],
  issues: [
    // ══════════════════════════════════════════
    // EPIC 1: 인증 및 보안
    // ══════════════════════════════════════════
    {
      title: "[EPIC-1] 인증 및 보안 시스템",
      body: `## 📌 Epic: 인증 및 보안 시스템

회원가입, 로그인, JWT 인증, Spring Security 설정을 포함한 전체 인증/보안 체계를 완성합니다.

### 포함 Feature
- \`[FEATURE-1-1]\` 회원가입/로그인 API 완성
- \`[FEATURE-1-2]\` Spring Security + JWT 설정 정비
- \`[FEATURE-1-3]\` 프런트엔드 인증 UI 개선

### 완료 조건
- [ ] username 기반 JWT 로그인 정상 동작
- [ ] 보호된 엔드포인트 인가 처리
- [ ] 프런트 토큰 저장 및 자동 헤더 첨부`,
      labels: ["epic", "security", "mvp"],
    },
    {
      title: "[FEATURE-1-1] 회원가입/로그인 API 완성",
      body: `## 🔹 Feature: 회원가입/로그인 API

**Epic:** \`[EPIC-1] 인증 및 보안 시스템\`

### 관련 Task
- \`[TASK-1-1-1]\` username 기반 인증 전환 (email → username)
- \`[TASK-1-1-2]\` 회원가입 시 직군 대분류 선택 저장
- \`[TASK-1-1-3]\` 로그인 응답에 accessToken/refreshToken 포함
- \`[TASK-1-1-4]\` 비밀번호 BCrypt 암호화 적용 확인

### 관련 파일
- \`auth/AuthController.java\`
- \`auth/AuthService.java\`
- \`user/Users.java\``,
      labels: ["feature", "backend", "mvp"],
    },
    {
      title: "[TASK-1-1-1] username 기반 인증 전환 (email → username)",
      body: `## ✅ Task: username 기반 인증 전환

**Feature:** \`[FEATURE-1-1] 회원가입/로그인 API 완성\`

### 작업 내용
- \`Users.java\` 인증 식별자 \`email\` → \`username\` 변경
- \`CustomUserDetailsService.java\` \`findByEmail\` → \`findByUsername\`
- \`AuthService.java\` 로그인 로직 username으로 사용자 조회
- 프런트 \`Auth.jsx\` 로그인 폼 필드 \`email\` → \`username\`

### 완료 조건
- [ ] \`POST /api/auth/login\` username으로 JWT 정상 발급
- [ ] DB users 테이블 username 컬럼 UNIQUE 제약
- [ ] 프런트 로그인 폼 username 입력 필드 표시`,
      labels: ["task", "backend", "frontend", "mvp"],
    },
    {
      title: "[TASK-1-1-2] 회원가입 시 직군 대분류 선택 저장",
      body: `## ✅ Task: 직군 대분류 선택 저장

**Feature:** \`[FEATURE-1-1] 회원가입/로그인 API 완성\`

### 작업 내용
- 회원가입 DTO에 \`jobCategory\` (대분류: DEVELOPER, DESIGNER 등) 필드 추가
- \`Users.java\`에 \`jobCategory\` 컬럼 저장
- 프런트 \`Auth.jsx\` 회원가입 폼 소분류 UI → 대분류 드롭다운으로 교체

### 완료 조건
- [ ] 회원가입 요청에 대분류 포함
- [ ] DB \`users\` 테이블에 \`job_category\` 저장
- [ ] 소분류 UI 제거, 대분류 드롭다운 표시`,
      labels: ["task", "backend", "frontend", "mvp"],
    },
    {
      title: "[TASK-1-1-3] 로그인 응답에 accessToken / refreshToken 포함",
      body: `## ✅ Task: 로그인 응답 토큰 구조 완성

**Feature:** \`[FEATURE-1-1] 회원가입/로그인 API 완성\`

### 작업 내용
- \`AuthController\` 로그인 응답 DTO에 \`accessToken\`, \`refreshToken\`, \`expiresIn\` 포함
- Refresh Token DB 저장 (users 테이블 또는 별도 테이블)
- 프런트 \`AuthContext.jsx\`에서 두 토큰 모두 저장

### 완료 조건
- [ ] 로그인 성공 시 두 토큰 모두 응답에 포함
- [ ] 프런트 localStorage/sessionStorage에 저장`,
      labels: ["task", "backend", "frontend", "mvp"],
    },
    {
      title: "[FEATURE-1-2] Spring Security + JWT 설정 정비",
      body: `## 🔹 Feature: Spring Security + JWT 설정 정비

**Epic:** \`[EPIC-1] 인증 및 보안 시스템\`

### 관련 Task
- \`[TASK-1-2-1]\` SecurityConfig 공개/보호 엔드포인트 명세 정비
- \`[TASK-1-2-2]\` JwtAuthenticationFilter 예외 처리 개선
- \`[TASK-1-2-3]\` Refresh Token 재발급 엔드포인트 구현

### 관련 파일
- \`security/SecurityConfig.java\`
- \`security/JwtAuthenticationFilter.java\`
- \`security/JwtTokenProvider.java\``,
      labels: ["feature", "backend", "security", "mvp"],
    },
    {
      title: "[TASK-1-2-1] SecurityConfig 공개/보호 엔드포인트 명세 정비",
      body: `## ✅ Task: SecurityConfig 엔드포인트 명세 정비

**Feature:** \`[FEATURE-1-2] Spring Security + JWT 설정 정비\`

### 작업 내용
| 경로 | 정책 |
|------|------|
| \`/api/auth/**\` | permitAll |
| \`GET /api/questions/**\` | permitAll |
| \`GET /api/answers/**\` | permitAll |
| \`POST/PUT/DELETE /api/answers/**\` | authenticated |
| \`/api/me/**\` | authenticated |
| \`/api/admin/**\` | hasRole(ADMIN) |

- CORS: \`localhost:5173\` 허용

### 완료 조건
- [ ] 비로그인 사용자 질문 목록 조회 가능
- [ ] 비로그인 사용자 답변 작성 시 401 반환
- [ ] 관리자 엔드포인트 ROLE_ADMIN만 403 없이 접근`,
      labels: ["task", "backend", "security", "mvp"],
    },
    {
      title: "[TASK-1-2-2] JwtAuthenticationFilter 예외 처리 개선",
      body: `## ✅ Task: JWT 필터 예외 처리 개선

**Feature:** \`[FEATURE-1-2] Spring Security + JWT 설정 정비\`

### 작업 내용
- 만료된 토큰 → 401 + \`{"error":"TOKEN_EXPIRED"}\`
- 위조/유효하지 않은 토큰 → 401 + \`{"error":"INVALID_TOKEN"}\`
- SecurityContext 설정 누락 케이스 방어 코드 추가

### 완료 조건
- [ ] 만료 토큰 요청 시 명확한 에러 코드 응답
- [ ] 프런트에서 에러 코드로 refresh 시도 여부 판단 가능`,
      labels: ["task", "backend", "security"],
    },
    {
      title: "[TASK-1-2-3] Refresh Token 재발급 엔드포인트 구현",
      body: `## ✅ Task: Refresh Token 재발급

**Feature:** \`[FEATURE-1-2] Spring Security + JWT 설정 정비\`

### 작업 내용
- \`POST /api/auth/refresh\` 엔드포인트 추가
- Body: \`{ "refreshToken": "..." }\`
- 응답: \`{ "accessToken": "...", "expiresIn": 3600 }\`
- Refresh Token 만료 또는 DB 불일치 시 401

### 완료 조건
- [ ] Refresh Token으로 새 Access Token 발급
- [ ] Refresh Token 만료 시 재로그인 유도`,
      labels: ["task", "backend", "frontend"],
    },
    {
      title: "[FEATURE-1-3] 프런트엔드 인증 UI 개선",
      body: `## 🔹 Feature: 프런트엔드 인증 UI 개선

**Epic:** \`[EPIC-1] 인증 및 보안 시스템\`

### 관련 Task
- \`[TASK-1-3-1]\` Auth.jsx 왼쪽 패널 제거 + 로그인 폼 중앙 배치
- \`[TASK-1-3-2]\` 회원가입 직군 대분류 드롭다운 UI
- \`[TASK-1-3-3]\` API client.js JWT 자동 첨부 + 401 인터셉터

### 관련 파일
- \`frontend/src/screens/Auth.jsx\`
- \`frontend/src/api/client.js\`
- \`frontend/src/context/AuthContext.jsx\``,
      labels: ["feature", "frontend", "mvp"],
    },
    {
      title: "[TASK-1-3-1] Auth.jsx 왼쪽 패널 제거 + 로그인 폼 중앙 배치",
      body: `## ✅ Task: Auth.jsx UI 개선

**Feature:** \`[FEATURE-1-3] 프런트엔드 인증 UI 개선\`

### 작업 내용
- 왼쪽 브랜딩/설명 패널 완전 제거
- 로그인/회원가입 카드 화면 정중앙 배치
- 카드 크기 확대 (min-width: 480px)
- username 입력 필드로 변경

### 완료 조건
- [ ] 단일 컬럼 레이아웃
- [ ] \`display:flex; align-items:center; justify-content:center\` 적용
- [ ] 모바일 반응형 유지`,
      labels: ["task", "frontend", "mvp"],
    },
    {
      title: "[TASK-1-3-3] API client.js JWT 자동 첨부 및 401 인터셉터",
      body: `## ✅ Task: API 클라이언트 인터셉터

**Feature:** \`[FEATURE-1-3] 프런트엔드 인증 UI 개선\`

### 작업 내용
- 요청 인터셉터: \`Authorization: Bearer <token>\` 자동 첨부
- 401 응답 인터셉터: Refresh Token으로 자동 갱신 시도
- 갱신 실패 시 로그인 페이지 리다이렉트

### 완료 조건
- [ ] 로그인 후 모든 API 요청에 토큰 자동 첨부
- [ ] 토큰 만료 시 자동 갱신 또는 로그인 이동`,
      labels: ["task", "frontend", "mvp"],
    },

    // ══════════════════════════════════════════
    // EPIC 2: 질문 관리
    // ══════════════════════════════════════════
    {
      title: "[EPIC-2] 질문 관리 시스템",
      body: `## 📌 Epic: 질문 관리 시스템

직군별/카테고리별 기술면접 질문 목록 조회, 난이도 필터링, 관리자 CRUD를 포함합니다.

### 포함 Feature
- \`[FEATURE-2-1]\` 질문 목록 API 완성
- \`[FEATURE-2-2]\` 프런트엔드 QuestionList 완성
- \`[FEATURE-2-3]\` 관리자 질문 CRUD

### 완료 조건
- [ ] 직군별 + 난이도별 필터링 API 동작
- [ ] 페이지네이션 적용
- [ ] 관리자 질문 CRUD 동작`,
      labels: ["epic", "mvp"],
    },
    {
      title: "[FEATURE-2-1] 질문 목록 API 완성",
      body: `## 🔹 Feature: 질문 목록 API

**Epic:** \`[EPIC-2] 질문 관리 시스템\`

### 관련 Task
- \`[TASK-2-1-1]\` 직군/카테고리/난이도 복합 필터링 쿼리
- \`[TASK-2-1-2]\` 페이지네이션(Pageable) 적용
- \`[TASK-2-1-3]\` 질문 상세 조회 \`GET /api/questions/{id}\`

### 관련 파일
- \`question/QuestionController.java\`
- \`question/QuestionService.java\`
- \`question/QuestionRepository.java\``,
      labels: ["feature", "backend", "mvp"],
    },
    {
      title: "[TASK-2-1-1] 직군/카테고리/난이도 복합 필터링 쿼리 구현",
      body: `## ✅ Task: 질문 복합 필터링 쿼리

**Feature:** \`[FEATURE-2-1] 질문 목록 API 완성\`

### 작업 내용
- \`QuestionRepository\`에 복합 필터 쿼리 구현
- 파라미터: \`jobCategory\` (대분류), \`categoryId\`, \`difficulty\` (EASY/MEDIUM/HARD)
- 모두 Optional 파라미터로 처리 (없으면 전체 조회)

### 완료 조건
- [ ] \`GET /api/questions?jobCategory=DEVELOPER&difficulty=MEDIUM\` 동작
- [ ] 파라미터 없으면 전체 조회`,
      labels: ["task", "backend", "mvp"],
    },
    {
      title: "[TASK-2-1-2] 질문 목록 페이지네이션 적용",
      body: `## ✅ Task: 질문 페이지네이션

**Feature:** \`[FEATURE-2-1] 질문 목록 API 완성\`

### 작업 내용
- \`Pageable\` 파라미터 추가 (\`page\`, \`size\`, \`sort\`)
- 응답 스키마: \`{ content: [...], totalElements: N, totalPages: N, page: 0 }\`

### 완료 조건
- [ ] \`GET /api/questions?page=0&size=20\` 동작
- [ ] 프런트에서 totalPages로 페이지 네비게이션 가능`,
      labels: ["task", "backend", "mvp"],
    },
    {
      title: "[FEATURE-2-2] 프런트엔드 QuestionList 화면 완성",
      body: `## 🔹 Feature: 프런트엔드 QuestionList

**Epic:** \`[EPIC-2] 질문 관리 시스템\`

### 관련 Task
- \`[TASK-2-2-1]\` 직군/카테고리/난이도 필터 UI → API 연결
- \`[TASK-2-2-2]\` 페이지네이션 UI 구현
- \`[TASK-2-2-3]\` 질문 카드 클릭 → Solve 페이지 이동

### 관련 파일
- \`frontend/src/screens/QuestionList.jsx\`
- \`frontend/src/api/questionApi.js\``,
      labels: ["feature", "frontend", "mvp"],
    },

    // ══════════════════════════════════════════
    // EPIC 3: 답변 시스템
    // ══════════════════════════════════════════
    {
      title: "[EPIC-3] 답변 시스템",
      body: `## 📌 Epic: 답변 시스템

질문에 대한 답변 작성/저장, 다른 사람 답변 열람, 좋아요 기능을 포함합니다.

### 포함 Feature
- \`[FEATURE-3-1]\` 답변 작성/수정/삭제 API
- \`[FEATURE-3-2]\` 답변 열람 및 좋아요 API
- \`[FEATURE-3-3]\` 프런트엔드 Solve/AnswerDetail 화면

### 완료 조건
- [ ] 로그인 사용자 답변 저장
- [ ] 다른 사람 답변 목록 조회
- [ ] 좋아요 토글 동작`,
      labels: ["epic", "mvp"],
    },
    {
      title: "[FEATURE-3-1] 답변 작성/수정/삭제 API",
      body: `## 🔹 Feature: 답변 CRUD API

**Epic:** \`[EPIC-3] 답변 시스템\`

### 관련 Task
- \`[TASK-3-1-1]\` \`POST /api/answers\` 답변 저장
- \`[TASK-3-1-2]\` \`PUT /api/answers/{id}\` 답변 수정 (본인만)
- \`[TASK-3-1-3]\` \`DELETE /api/answers/{id}\` 답변 삭제 (본인 + 관리자)

### 관련 파일
- \`answer/AnswerController.java\`
- \`answer/AnswerService.java\``,
      labels: ["feature", "backend", "mvp"],
    },
    {
      title: "[TASK-3-1-1] 답변 저장 API 구현",
      body: `## ✅ Task: 답변 저장

**Feature:** \`[FEATURE-3-1] 답변 작성/수정/삭제 API\`

### 작업 내용
- \`POST /api/answers\` - Body: \`{ questionId, content }\`
- 로그인 사용자 본인 정보 SecurityContext에서 추출
- 응답: 저장된 Answer DTO

### 완료 조건
- [ ] 인증된 사용자만 답변 저장 가능
- [ ] 동일 질문에 여러 번 답변 가능 (히스토리)`,
      labels: ["task", "backend", "mvp"],
    },
    {
      title: "[FEATURE-3-2] 답변 열람 및 좋아요 API",
      body: `## 🔹 Feature: 답변 열람 및 좋아요

**Epic:** \`[EPIC-3] 답변 시스템\`

### 관련 Task
- \`[TASK-3-2-1]\` \`GET /api/questions/{id}/answers\` 답변 목록
- \`[TASK-3-2-2]\` 좋아요 토글 \`POST /api/answers/{id}/like\`
- \`[TASK-3-2-3]\` 좋아요 중복 방지 (userId + answerId UNIQUE)

### 완료 조건
- [ ] 비로그인 사용자도 답변 목록 조회 가능
- [ ] 로그인 사용자만 좋아요 가능
- [ ] 좋아요 수 응답에 포함`,
      labels: ["feature", "backend", "mvp"],
    },
    {
      title: "[TASK-3-2-2] 좋아요 토글 API 구현",
      body: `## ✅ Task: 좋아요 토글

**Feature:** \`[FEATURE-3-2] 답변 열람 및 좋아요 API\`

### 작업 내용
- \`answer_likes\` 테이블 생성: \`(user_id, answer_id, created_at)\` UNIQUE(user_id, answer_id)
- \`POST /api/answers/{id}/like\` - 이미 좋아요 시 취소 (토글)
- 응답: \`{ liked: true/false, likeCount: N }\`

### 완료 조건
- [ ] 동일 사용자 중복 좋아요 불가
- [ ] 토글 정상 동작`,
      labels: ["task", "backend", "mvp"],
    },
    {
      title: "[FEATURE-3-3] 프런트엔드 Solve / AnswerDetail 화면",
      body: `## 🔹 Feature: 답변 UI 화면

**Epic:** \`[EPIC-3] 답변 시스템\`

### 관련 Task
- \`[TASK-3-3-1]\` Solve.jsx 답변 작성 폼 → API 연결
- \`[TASK-3-3-2]\` AnswerDetail.jsx 다른 사람 답변 목록 + 좋아요 UI
- \`[TASK-3-3-3]\` MyAnswers.jsx 내 답변 히스토리 API 연결

### 관련 파일
- \`frontend/src/screens/Solve.jsx\`
- \`frontend/src/screens/AnswerDetail.jsx\`
- \`frontend/src/screens/MyAnswers.jsx\``,
      labels: ["feature", "frontend", "mvp"],
    },

    // ══════════════════════════════════════════
    // EPIC 4: 관리자 시스템
    // ══════════════════════════════════════════
    {
      title: "[EPIC-4] 관리자 시스템",
      body: `## 📌 Epic: 관리자 시스템

질문/사용자/카테고리 관리를 위한 관리자 전용 기능입니다.

### 포함 Feature
- \`[FEATURE-4-1]\` 관리자 질문 CRUD API + UI
- \`[FEATURE-4-2]\` 관리자 사용자 관리
- \`[FEATURE-4-3]\` 관리자 카테고리 관리

### 관련 파일
- \`src/main/java/.../admin/\`
- \`frontend/src/screens/admin/\``,
      labels: ["epic"],
    },
    {
      title: "[FEATURE-4-1] 관리자 질문 CRUD API 및 UI",
      body: `## 🔹 Feature: 관리자 질문 관리

**Epic:** \`[EPIC-4] 관리자 시스템\`

### 관련 Task
- \`[TASK-4-1-1]\` \`GET/POST/PUT/DELETE /api/admin/questions\` API 완성
- \`[TASK-4-1-2]\` AdminQuestions.jsx + AdminQuestionForm.jsx API 연결
- \`[TASK-4-1-3]\` AdminPreview.jsx 미리보기 기능 구현

### 완료 조건
- [ ] ROLE_ADMIN만 접근 가능 (403 처리)
- [ ] 질문 등록/수정/삭제 정상 동작
- [ ] 카테고리 연결`,
      labels: ["feature", "backend", "frontend"],
    },
    {
      title: "[TASK-4-1-1] 관리자 질문 CRUD API 구현",
      body: `## ✅ Task: 관리자 질문 CRUD

**Feature:** \`[FEATURE-4-1] 관리자 질문 CRUD API 및 UI\`

### 작업 내용
- \`POST /api/admin/questions\` - 질문 생성 (title, content, difficulty, categoryId, tags)
- \`PUT /api/admin/questions/{id}\` - 질문 수정
- \`DELETE /api/admin/questions/{id}\` - 질문 삭제 (soft delete 권장)
- \`ROLE_ADMIN\` 인가 SecurityConfig에서 설정

### 완료 조건
- [ ] 모든 CRUD 엔드포인트 동작
- [ ] 비관리자 접근 시 403 반환`,
      labels: ["task", "backend"],
    },

    // ══════════════════════════════════════════
    // EPIC 5: 대시보드 및 사용자 통계
    // ══════════════════════════════════════════
    {
      title: "[EPIC-5] 대시보드 및 사용자 통계",
      body: `## 📌 Epic: 대시보드 및 사용자 통계

사용자의 학습 현황, 답변 통계, 연속 학습 일수(streak) 등을 제공합니다.

### 포함 Feature
- \`[FEATURE-5-1]\` 내 통계 API (\`/api/me/stats\`)
- \`[FEATURE-5-2]\` Dashboard mock → 실 API 연결
- \`[FEATURE-5-3]\` LearningStatus.jsx 화면

### 관련 파일
- \`me/\` 패키지 (MeService, MeController)
- \`frontend/src/screens/Dashboard.jsx\`
- \`frontend/src/screens/LearningStatus.jsx\``,
      labels: ["epic", "mvp"],
    },
    {
      title: "[FEATURE-5-1] 내 통계 API 완성 (/api/me/stats)",
      body: `## 🔹 Feature: 내 통계 API

**Epic:** \`[EPIC-5] 대시보드 및 사용자 통계\`

### 관련 Task
- \`[TASK-5-1-1]\` \`GET /api/me/stats\` 전체 통계 집계
- \`[TASK-5-1-2]\` \`GET /api/me/answers\` 내 답변 목록
- \`[TASK-5-1-3]\` 연속 학습 일수(streak) 계산 로직

### 응답 스키마 (MyStatsResponse)
\`\`\`json
{
  "totalAnswers": 42,
  "uniqueQuestions": 30,
  "feedbackDone": 10,
  "feedbackPending": 5,
  "currentStreakDays": 7,
  "byCategory": {"CS": 15, "DB": 10},
  "byDifficulty": {"EASY": 10, "MEDIUM": 20, "HARD": 12},
  "last7Days": [{"date": "2026-05-01", "count": 3}],
  "latestAnswer": {}
}
\`\`\``,
      labels: ["feature", "backend", "mvp"],
    },
    {
      title: "[TASK-5-1-3] 연속 학습 일수(streak) 계산 로직 구현",
      body: `## ✅ Task: Streak 계산

**Feature:** \`[FEATURE-5-1] 내 통계 API 완성\`

### 작업 내용
- 사용자의 답변 날짜 목록으로 연속 일수 계산
- 오늘 기준 역순으로 연속 날짜 카운트
- \`MeService.java\` 내 구현

### 완료 조건
- [ ] 오늘 답변 없어도 어제부터 연속이면 streak 유지
- [ ] 하루 여러 답변 = 1일로 카운트`,
      labels: ["task", "backend"],
    },
    {
      title: "[FEATURE-5-2] Dashboard mock → 실 API 연결",
      body: `## 🔹 Feature: 대시보드 실 API 연결

**Epic:** \`[EPIC-5] 대시보드 및 사용자 통계\`

### 관련 Task
- \`[TASK-5-2-1]\` Dashboard.jsx mockData → \`/api/me/stats\` 연결
- \`[TASK-5-2-2]\` 채용 정보 섹션 관리자 큐레이션 방식 우선 유지 (mock)

### 관련 파일
- \`frontend/src/screens/Dashboard.jsx\`
- \`frontend/src/data/mockData.js\``,
      labels: ["feature", "frontend", "mvp"],
    },

    // ══════════════════════════════════════════
    // EPIC 6: 이력서 피드백
    // ══════════════════════════════════════════
    {
      title: "[EPIC-6] 이력서 피드백 시스템",
      body: `## 📌 Epic: 이력서 피드백 시스템

사용자가 이력서를 업로드하면 AI(Claude)가 피드백을 제공하는 기능입니다.

### 포함 Feature
- \`[FEATURE-6-1]\` 이력서 업로드 및 텍스트 추출 API
- \`[FEATURE-6-2]\` AI 피드백 생성 API
- \`[FEATURE-6-3]\` 프런트엔드 ResumeFeedback.jsx 완성

### 관련 파일
- \`resume/ResumeController.java\`
- \`resume/ResumeFeedbackService.java\`
- \`resume/ResumeTextExtractor.java\`
- \`frontend/src/screens/ResumeFeedback.jsx\`

> ⚠️ Claude API 시크릿은 환경 변수로 관리. 코드에 하드코딩 금지.`,
      labels: ["epic"],
    },
    {
      title: "[FEATURE-6-1] 이력서 업로드 및 텍스트 추출 API",
      body: `## 🔹 Feature: 이력서 업로드

**Epic:** \`[EPIC-6] 이력서 피드백 시스템\`

### 관련 Task
- \`[TASK-6-1-1]\` \`POST /api/resume/upload\` 파일 업로드 API
- \`[TASK-6-1-2]\` \`ResumeTextExtractor.java\` 텍스트 추출 완성 (PDFBox)
- \`[TASK-6-1-3]\` 파일 저장소 처리 (로컬 or S3)

### 완료 조건
- [ ] PDF 업로드 → 텍스트 추출 성공
- [ ] 파일 크기 제한 10MB 처리`,
      labels: ["feature", "backend"],
    },
    {
      title: "[TASK-6-1-2] ResumeTextExtractor PDF 텍스트 추출 완성",
      body: `## ✅ Task: PDF 텍스트 추출

**Feature:** \`[FEATURE-6-1] 이력서 업로드 및 텍스트 추출 API\`

### 작업 내용
- Apache PDFBox로 PDF → 텍스트 추출
- DOCX는 Apache POI 처리
- \`build.gradle\` 의존성 추가

### 완료 조건
- [ ] PDF 업로드 시 텍스트 정상 추출
- [ ] 한국어 텍스트 깨짐 없이 처리`,
      labels: ["task", "backend"],
    },

    // ══════════════════════════════════════════
    // EPIC 7: CI/CD 및 인프라
    // ══════════════════════════════════════════
    {
      title: "[EPIC-7] CI/CD 및 인프라 설정",
      body: `## 📌 Epic: CI/CD 및 인프라 설정

GitHub Actions를 이용한 자동 빌드/테스트/배포 파이프라인 구성입니다.

### 포함 Feature
- \`[FEATURE-7-1]\` GitHub Actions CI 파이프라인
- \`[FEATURE-7-2]\` 환경변수 및 Secrets 관리
- \`[FEATURE-7-3]\` 백엔드 Jar + 프런트 빌드 배포 자동화

### 목표 인프라
- AWS EC2 (백엔드)
- AWS RDS PostgreSQL
- AWS S3 (이력서 파일)
- GitHub Actions (CI/CD)`,
      labels: ["epic"],
    },
    {
      title: "[FEATURE-7-1] GitHub Actions CI 파이프라인 구성",
      body: `## 🔹 Feature: GitHub Actions CI

**Epic:** \`[EPIC-7] CI/CD 및 인프라 설정\`

### 관련 Task
- \`[TASK-7-1-1]\` PR 시 \`./gradlew test\` 자동 실행
- \`[TASK-7-1-2]\` main push 시 \`./gradlew build\` + 배포
- \`[TASK-7-1-3]\` 프런트엔드 \`npm run build\` CI 추가

### 완료 조건
- [ ] \`.github/workflows/ci.yml\` 작성
- [ ] PR 머지 전 테스트 통과 필수
- [ ] 시크릿은 GitHub Secrets 관리 (DB URL, JWT Secret)`,
      labels: ["feature"],
    },
    {
      title: "[TASK-7-1-1] GitHub Actions CI 워크플로 파일 작성",
      body: `## ✅ Task: CI 워크플로

**Feature:** \`[FEATURE-7-1] GitHub Actions CI 파이프라인 구성\`

### 작업 내용
- \`.github/workflows/ci.yml\` 생성
- 트리거: \`pull_request\` → main/develop 브랜치
- Java 21 toolchain 설정
- \`./gradlew test\` 실행 + 테스트 리포트 업로드

### 주의
- \`application.properties\` 절대 커밋 금지
- CI용 환경변수 GitHub Secrets에서 주입`,
      labels: ["task"],
    },
  ],
};

