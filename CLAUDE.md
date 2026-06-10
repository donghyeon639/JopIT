# CLAUDE.md — JobIT

이 저장소에서 코드를 작성·수정·리뷰하기 전에 반드시 이 문서를 먼저 읽으세요.

> **엔지니어링 원칙은 [`docs/ENGINEERING_PRIORITIES.md`](docs/ENGINEERING_PRIORITIES.md)에 정리되어 있습니다.**
> DB·캐시·비동기 AI·CI/CD·계층 분리·트랜잭션 경계 등 사용자가 중요하게 보는 관점입니다. 해당 영역에 손대기 전에 반드시 같이 읽어야 합니다.

---

## 1. 프로젝트 한 줄 요약

**JobIT — 직군별 면접·채용 준비 플랫폼.** 1차 타깃은 백엔드/프런트엔드/데이터 직군 개발자 취준생이며, 추후 마케팅·디자인 등 비개발 직군까지 확장할 계획입니다. 차별점은 **AI 답변 피드백 + 직군별 맞춤 문제 + 커뮤니티**.

- 서비스 주소: **https://job-it.site**
- 원격 저장소: `github.com/donghyeon639/devprep`
- 우선순위: **개발 직군 기능을 먼저 완성** → 그다음 비개발 직군 확장. 현재 작업은 모두 개발 직군 기준으로 판단합니다.

---

## 2. 저장소 레이아웃

```
C:\prephub\prephub\
├── src\                  # 백엔드 (Spring Boot, Java 21) — 패키지 루트 com.main.jobit
├── frontend\             # 프런트엔드 (React 18 + Vite 6)
├── build.gradle
├── settings.gradle       # rootProject.name = 'JobIT'
├── README.md
├── CLAUDE.md
└── docs\
    └── ENGINEERING_PRIORITIES.md
```

백엔드와 프런트엔드는 **하나의 디렉토리에 공존**하지만 별도 빌드 단위입니다. 한쪽을 변경할 때 다른 쪽 API 계약(엔드포인트, 응답 스키마)에 영향이 가는지 항상 확인하세요.

### 백엔드 도메인 구조 (`com.main.jobit`)
```
ai/                  # AI 추상화: port(LlmPort) + bedrock(BedrockLlmService) + feedback(AiFeedbackService)
domain/
  user/              # 회원, 인증(JWT), 소셜 로그인(OAuth2), Role(USER/ADMIN)
  question/          # 면접 질문 + 관리자 CRUD
  category/          # 질문 카테고리(QuestionCategory)
  job/               # 직군(JobCategory)
  answer/            # 답변 작성/저장 + AI 피드백 상태(FeedbackStatus)
  comment/           # 답변 댓글
  study/             # 스터디 모집/신청/북마크
  techtrend/         # 기술 블로그 RSS 수집
  jobposting/        # 채용 공고(공공 API 동기화)
  resume/            # 이력서 업로드 + AI 피드백(Tika 추출)
  mypage/            # 학습 통계
global/
  security/          # JWT, SecurityConfig, @Admin AOP(AdminAspect)
  config/            # AsyncConfig, CacheConfig(Caffeine)
  exception/         # GlobalExceptionHandler
infra/
  publicjob/         # 외부 채용 소스 어댑터(JobPostingFetcher 포트 + alio 구현 + NormalizedJob)
```

---

## 3. 실제 기술 스택 (기획안과 다름 — 주의)

기획 문서에는 Python/FastAPI로 적혀 있으나 **실제 백엔드는 Spring Boot입니다.** 코드를 작성할 때는 항상 아래 실제 스택과 `build.gradle`을 진실의 원천으로 삼으세요.

### 3.1 백엔드
| 항목 | 값 |
| --- | --- |
| 언어/런타임 | Java 21 (toolchain 고정) |
| 프레임워크 | Spring Boot **4.0.6** |
| 빌드 | Gradle (Groovy DSL, `build.gradle`) |
| 영속성 | Spring Data JPA + PostgreSQL |
| 보안 | Spring Security + JWT(`io.jsonwebtoken:jjwt` 0.12.6) + OAuth2 Client |
| 권한 | AspectJ AOP 기반 `@Admin` 커스텀 어노테이션 (`AdminAspect`) |
| 캐시 | Spring Cache + Caffeine (로컬 캐시) |
| 파일 추출 | Apache Tika 3.0.0 (이력서 텍스트 파싱) |
| RSS | ROME 2.1.0 (기술 트렌드 수집) |
| LLM 호출 | AWS Bedrock (AWS SDK v2 `bedrockruntime`, Converse API). 인증은 EC2 IAM 역할 — API 키 불필요 |
| 검증 | Spring Validation |
| 웹 | Spring Web MVC |
| 보일러플레이트 | Lombok |
| 테스트 | JUnit 5(`useJUnitPlatform()`), `*-test` 스타터, **ArchUnit**(아키텍처 규칙) |
| 패키지 루트 | `com.main.jobit` |
| 엔트리 | `JobitApplication.java` |

> 과거 문서에 있던 **Kafka는 현재 빌드 의존성에 없습니다.** 메시징이 필요해지면 도입을 먼저 합의하세요.

**실행 명령어 (백엔드, 저장소 루트에서):**
```bash
./gradlew bootRun        # 개발 서버(기본 8080). Windows: gradlew.bat bootRun
./gradlew test           # 테스트
./gradlew build          # 프로덕션 빌드 → build/libs/JobIT-0.0.1-SNAPSHOT.jar
java -jar build/libs/JobIT-0.0.1-SNAPSHOT.jar
./gradlew clean build    # 클린 빌드
```

> Bash 셸에서는 `./gradlew`, Windows 네이티브 셸에서는 `gradlew.bat`. 첫 실행 시 Gradle/JDK 21 toolchain 자동 다운로드로 시간이 걸릴 수 있습니다.

### 3.2 프런트엔드 (`frontend\`)
| 항목 | 값 |
| --- | --- |
| 프레임워크 | React 18.3 |
| 라우팅 | react-router-dom 6.30 |
| 빌드 | Vite 6 (`@vitejs/plugin-react`) |
| 모듈 | ESM (`"type": "module"`) |
| 진입 | `src/main.jsx` → `src/App.jsx` |
| 화면 | `src/screens/` (Landing, Auth, OAuthCallback, SocialSetup, Dashboard, QuestionList, Solve, AIFeedback, ResumeFeedback, MyAnswers, LearningStatus, Community, StudyBoard/Detail/Form, AnswerDetail, TechTrendDetail, LevelCheck, admin/*) |
| 컴포넌트 | `src/components/` (PrepBot, layout/TopNav, common/*, admin/*, icons) |
| API 레이어 | `src/api/` (client + authApi/questionApi/adminApi/resumeApi/studyApi) |
| 인증 상태 | `src/context/AuthContext.jsx` |
| 목 데이터 | `src/data/mockData.js` |

**실행 명령어 (프런트엔드):**
```bash
cd frontend
npm install              # 최초 1회 또는 package.json 변경 시
npm run dev              # 개발 서버(기본 5173, HMR)
npm run build            # 프로덕션 빌드 → dist/
npm run preview          # 빌드 결과 미리보기(기본 4173)
```

> Windows에서 PowerShell 실행 정책으로 `npm`이 막히면 `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`로 풀어줍니다.
> TypeScript 미도입. JS 파일에 타입 어노테이션을 임의로 도입하지 마세요. 필요하면 먼저 합의.

---

## 4. 작업 시 지켜야 할 규칙

### 4.1 스택 관련
- 백엔드 코드를 작성할 때 **Python/FastAPI 코드를 절대 만들지 마세요.** 기획 문서의 잔재일 뿐, 진실의 원천은 `build.gradle`입니다.
- Spring Boot **4.x** 기준으로 API를 사용하세요 (3.x deprecated 패턴 주의).
- Java 21 기능(record, sealed, pattern matching, virtual threads 등)을 자유롭게 사용해도 됩니다.

### 4.2 아키텍처
- 현재는 **모놀리식 단일 서비스**(`JobIT`)입니다. 기획서의 MSA 분리도(유저/문제/답변/알림/AI)는 **트래픽이 늘었을 때의 미래 계획**이며, 지금은 패키지 분리 수준으로 충분합니다.
- 도메인은 `com.main.jobit.domain.<도메인>` 형태. 새 패턴 도입 전 기존 코드를 확인하세요.
- **포트-어댑터 경계 두 곳을 존중**하세요. 이 경계가 미래 분리(MSA/Airflow 등)의 절단면입니다.
  - `ai/port/LlmPort` — 도메인은 이 인터페이스만 의존. AI 교체(모델 변경·백엔드 변경)는 구현체만 갈아끼움. 현재 구현은 `BedrockLlmService`(AWS Bedrock).
  - `infra/publicjob/JobPostingFetcher` — 새 채용 소스(사람인/네이버 등)는 어댑터 하나 추가로 `JobPostingSyncService`에 자동 포함.
- 외부 데이터 수집은 in-process `@Scheduled` + Caffeine 캐시로 동작 중(채용 6시간, RSS 3일). Airflow 등 별도 오케스트레이터는 소스가 늘고 재시도·백필 요구가 생길 때 합의 후 도입.

### 4.3 데이터/인프라
- DB는 PostgreSQL. 마이그레이션 도구는 미도입 — 추가 제안 시 먼저 합의(Flyway vs Liquibase). 운영에서 `ddl-auto=update` 의존 금지(§ENGINEERING_PRIORITIES 1.3).
- 인프라 가정: AWS EC2 / RDS / S3 / CloudWatch / GitHub Actions, 도메인 `job-it.site`. IaC·워크플로 파일은 임의로 만들지 말고 요청 시에만 작성.

### 4.4 보안
- Spring Security가 켜져 있습니다. 새 엔드포인트마다 인증/인가 정책을 함께 고려하세요.
- 인증은 **JWT** 기반 + **OAuth2 소셜 로그인**. 토큰 발급/검증은 기존 보안 클래스(`JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`)에 일관되게 통합.
- 관리자 전용 동작은 **`@Admin` 커스텀 어노테이션(AOP)** 으로 보호합니다. 새 관리자 기능에 동일 패턴 적용.
- 비밀값(DB 패스워드, JWT secret, OAuth2 시크릿, 외부 API 키)은 절대 코드/리포지토리에 하드코딩하지 마세요. `application.properties` 대신 환경 변수/외부 설정으로. `application.properties`·`.env`는 git에 커밋 금지.

### 4.5 프런트엔드
- 스타일링은 `src/styles/`의 기존 파일을 먼저 보고 맞춥니다. 새 스타일 라이브러리(Tailwind, MUI 등) 임의 도입 금지.
- API 호출은 `src/api/`의 기존 레이어(`client.js` + 도메인별 모듈) 패턴을 따릅니다.
- 화면은 `screens/`에 한 파일, 재사용 단위만 `components/`로 분리하는 컨벤션 유지.
- 마스코트/캐릭터 식별자(`PrepBot`, "프렙쌤")는 브랜드명(JobIT)과 별개로 **현재 그대로 유지**. 변경 시 합의.

### 4.6 일반
- 한국어 응답 기본. 코드 식별자는 영어, 주석/문서는 필요 시 한국어 OK.
- 작업 전 항상 해당 영역의 기존 파일을 읽어 컨벤션을 파악한 뒤 수정. 새 추상화·라이브러리 도입은 반드시 합의 후.
- 기획서에 적힌 기능이라도 현재 범위(§5)를 벗어나면 먼저 확인.

---

## 5. 기능 범위

### 5.1 현재 구현된 기능
1. 회원가입 / 로그인 (JWT) + 소셜 로그인 (OAuth2)
2. 직군·카테고리(CS/DB/네트워크/OS 등)·난이도(하/중/상)별 기술면접 질문 목록
3. 답변 작성 및 저장, 다른 사용자 답변 열람, 댓글
4. **AI 답변 피드백** (`AiFeedbackService`, 비동기) — 현재 어댑터는 `BedrockLlmService`(AWS Bedrock, Converse API)
5. **AI 이력서 피드백** (파일 업로드 → Tika 텍스트 추출 → LLM)
6. 스터디 모집/신청/북마크
7. 기술 트렌드 RSS 자동 수집·태깅
8. 채용 공고 공공 API 주기 동기화
9. 마이페이지 학습 통계, 레벨 체크
10. 관리자 콘솔(문제·카테고리·채용·회원 관리)

### 5.2 향후 계획 (착수 전 합의 필수)
- **AI 화상 면접** (§7 참고)
- 기업별 면접 후기, 랭킹 시스템, 모의면접 타이머, 오답 노트
- 채용 소스 확장: 네이버 검색 API → 사람인 OpenAPI (jobCategory별 캐시 키, 시크릿은 환경 변수)
- 챗봇(직군별 문제 추천/채용 안내/학습 현황) — 구조화 데이터는 Tool Calling/Intent 라우팅, 비정형 텍스트는 RAG(pgvector) 검토
- 외부 데이터 수집 오케스트레이션(Airflow 등)으로의 분리

> 현재 어댑터는 `BedrockLlmService`(AWS Bedrock, Converse API, EC2 IAM 역할 인증). 단발 `generate()`만 구현돼 있어, 스트리밍·tool use가 필요한 챗봇/화상 면접 단계에서 `generateStream`/`converse`를 `LlmPort`에 추가 확장. 어느 쪽이든 `LlmPort` 구현체만 바꾸면 되도록 설계되어 있음.

---

## 6. 작업 시작 전 체크리스트
- [ ] `application.properties`, `.env` 같은 설정 파일을 git에 커밋하지 않았는가
- [ ] 변경 대상이 백엔드인지 프런트인지 확인 (두 개의 별도 빌드 단위)
- [ ] 기존 패키지/폴더 구조와 컨벤션을 먼저 읽었는가
- [ ] 현재 범위인가, 향후 계획 범위인가 (§5)
- [ ] 새 의존성/라이브러리/추상화를 도입하려 한다면 먼저 확인했는가
- [ ] 비밀값을 코드에 박지 않았는가
- [ ] 백엔드 변경이 프런트 API 계약에 영향을 주는가 (또는 그 반대)
- [ ] 포트 경계(`LlmPort`, `JobPostingFetcher`)를 침범하지 않고 어댑터로 확장했는가

---

## 7. AI 화상 면접 기능 기획 (향후 계획)

> 기획 단계. 착수 전 사용자 합의 필수. 1단계만으로도 포트폴리오 차원에서 충분히 임팩트 있음.

### 7.1 컨셉
사용자가 카메라/마이크를 켜고 AI 면접관과 실시간 음성 대화로 모의 기술면접을 진행. 종료 시 전체 회차에 대한 종합 피드백 제공.

### 7.2 컴포넌트 4단계 분해
| 단계 | 역할 | 채택 후보 |
| --- | --- | --- |
| ① 입력 (사용자 → 텍스트) | 마이크 캡처 + STT | 브라우저 `getUserMedia` + Web Speech API(무료) → 추후 Whisper |
| ② 추론 (텍스트 → 다음 질문/피드백) | LLM | `LlmPort` 구현체 — 스트리밍 가능한 어댑터로 교체 필요 |
| ③ 출력 (텍스트 → 음성) | TTS | 브라우저 `SpeechSynthesis`(무료) → 추후 ElevenLabs / OpenAI TTS |
| ④ 영상 (선택) | AI 면접관 얼굴 | HeyGen / D-ID / Tavus Streaming Avatar(분당 과금) |

### 7.3 단계별 로드맵
**1단계 — "오디오 면접" MVP (현재 스택만으로 가능)**
- 프런트: `getUserMedia` + Web Speech API로 마이크→텍스트
- 백엔드: `POST /api/interview/turn` — 사용자 발화 + 세션ID를 받아 다음 질문 텍스트 반환
- 응답 음성: 브라우저 `SpeechSynthesisUtterance`로 재생
- 화면: 본인 카메라 self-view + AI 면접관 자리에 캐릭터/카드 UI
- 추가 비용·인프라 ≈ 0

**2단계 — 품질 업그레이드**
- STT를 Whisper API로 교체, TTS를 ElevenLabs/OpenAI TTS로 교체
- `LlmPort`를 스트리밍 가능한 어댑터(SDK 또는 로컬 LLM)로 전환
- WebSocket 도입 — 첫 음성이 1초 내 시작되도록 토큰 스트리밍
- `interview_session` / `interview_turn` 테이블로 멀티턴 컨텍스트 누적

**3단계 — 진짜 화상 면접 (아바타)**
- HeyGen / D-ID / Tavus 등 Streaming Avatar API
- AI 답변 텍스트 → 입모양 동기화 영상 스트림 → WebRTC 송출
- 분당 과금 — 비용 정책 사전 합의 필요

### 7.4 현재 환경에서 미리 알아둘 제약
1. **현재 `BedrockLlmService`는 단발 `generate()`만 지원** — 스트리밍 미구현. 화상/음성 면접 착수 시 `LlmPort`에 `generateStream`(Bedrock Converse 스트리밍) 추가 필요.
2. **WebSocket / SSE 미도입** — 도입 시 `SecurityConfig`에 `/ws/**` 인가 정책, JWT 핸드셰이크 처리 추가.
3. **세션 상태 저장소** — 회차당 다회 turn. 1차는 PostgreSQL 단순 저장 권장(Redis 미도입).
4. **음성 파일 보관**(선택) — "내 면접 다시 듣기"가 필요하면 S3 연동.

### 7.5 새 도메인 패키지 (착수 시)
```
com.main.jobit.domain.interview
├── InterviewSession.java
├── InterviewTurn.java
├── InterviewSessionRepository.java
├── InterviewTurnRepository.java
├── InterviewService.java
├── InterviewController.java
└── dto/
```

### 7.6 작업 순서 (착수 시)
1. `LlmPort`를 스트리밍 가능한 구현체로 교체
2. `interview` 도메인 추가(엔티티 + 단순 REST)
3. 프런트 `Interview.jsx`(카메라 self-view + STT + TTS)
4. 멀티턴 + 종료 시 종합 피드백(기존 `AiFeedbackService` 로직 재사용)
