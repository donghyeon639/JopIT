# JobIT

> 직군별 면접·채용 준비 플랫폼 — AI 답변 피드백 + 직군별 맞춤 문제 + 커뮤니티

**서비스 주소: https://job-it.site**

JobIT은 백엔드·프런트엔드·데이터 직군 개발자 취업 준비생을 1차 타깃으로 하는 면접/채용 준비 플랫폼입니다. 직군별 기술면접 질문을 풀고, 작성한 답변에 AI 피드백을 받고, 이력서 피드백·스터디·커뮤니티·기술 트렌드·채용 공고까지 한 곳에서 준비할 수 있습니다.

---

## 주요 기능

| 영역 | 기능 |
| --- | --- |
| 인증 | 이메일 회원가입·로그인(JWT), 소셜 로그인(OAuth2), 권한 분리(USER/ADMIN) |
| 문제 풀이 | 직군·카테고리(CS/DB/네트워크/OS 등)·난이도(하/중/상)별 기술면접 질문, 답변 작성·저장 |
| AI 피드백 | 작성한 답변에 대한 AI 면접 코치 피드백(비동기 처리) |
| 이력서 | 이력서 파일 업로드 → 텍스트 추출(Apache Tika) → AI 피드백 |
| 커뮤니티 | 다른 사용자의 답변 열람, 댓글 |
| 스터디 | 스터디 모집글 작성, 참여 신청, 북마크 |
| 기술 트렌드 | 기술 블로그 RSS 자동 수집·태깅 |
| 채용 공고 | 공공 채용 데이터 API 주기 동기화 |
| 마이페이지 | 학습 통계, 내 답변 모아보기, 레벨 체크 |
| 관리자 | 문제·카테고리·채용·회원 관리 콘솔(`@Admin` 권한) |

---

## 기술 스택

### 백엔드
| 항목 | 값 |
| --- | --- |
| 언어/런타임 | Java 21 (toolchain 고정) |
| 프레임워크 | Spring Boot 4.0.6 |
| 빌드 | Gradle (Groovy DSL) |
| 영속성 | Spring Data JPA + PostgreSQL |
| 보안 | Spring Security + JWT(jjwt) + OAuth2 Client |
| 권한 | AspectJ AOP 기반 `@Admin` 커스텀 어노테이션 |
| 캐시 | Spring Cache + Caffeine (로컬 캐시) |
| 파일 추출 | Apache Tika (이력서 텍스트 파싱) |
| RSS | ROME (기술 트렌드 수집) |
| 검증 | Spring Validation |
| 테스트 | JUnit 5, ArchUnit(아키텍처 규칙 검증) |
| 보일러플레이트 | Lombok |

### 프런트엔드
| 항목 | 값 |
| --- | --- |
| 프레임워크 | React 18.3 |
| 라우팅 | react-router-dom 6.30 |
| 빌드 | Vite 6 |
| 모듈 | ESM |

### 인프라 (가정)
AWS EC2 / RDS(PostgreSQL) / S3 / CloudWatch, GitHub Actions, 도메인 `job-it.site`.

---

## 프로젝트 구조

백엔드와 프런트엔드는 **하나의 저장소에 공존하지만 별도 빌드 단위**입니다.

```
prephub/
├── build.gradle                # 백엔드 빌드 (rootProject.name = 'JobIT')
├── settings.gradle
├── CLAUDE.md                   # 코드 작성 규칙 (작업 전 필독)
├── docs/
│   └── ENGINEERING_PRIORITIES.md  # 엔지니어링 원칙 (DB/캐시/비동기 AI/CI·CD/설계)
│
├── src/main/java/com/main/jobit/
│   ├── JobitApplication.java
│   ├── ai/                     # AI 추상화
│   │   ├── port/LlmPort.java        # LLM 포트(도메인이 의존하는 인터페이스)
│   │   ├── claude/ClaudeCliService.java  # 현재 어댑터(Claude CLI)
│   │   └── feedback/AiFeedbackService.java
│   ├── domain/
│   │   ├── user/               # 회원, 인증(JWT), 소셜 로그인(OAuth2)
│   │   ├── question/           # 면접 질문 + 관리자 CRUD
│   │   ├── category/           # 질문 카테고리
│   │   ├── job/                # 직군(JobCategory)
│   │   ├── answer/             # 답변 작성/저장 + AI 피드백 상태
│   │   ├── comment/            # 답변 댓글
│   │   ├── study/              # 스터디 모집/신청/북마크
│   │   ├── techtrend/          # 기술 블로그 RSS 수집
│   │   ├── jobposting/         # 채용 공고(공공 API 동기화)
│   │   ├── resume/             # 이력서 업로드 + AI 피드백(Tika)
│   │   └── mypage/             # 학습 통계
│   ├── global/
│   │   ├── security/           # JWT, SecurityConfig, @Admin AOP
│   │   ├── config/             # Async, Cache(Caffeine)
│   │   └── exception/          # GlobalExceptionHandler
│   └── infra/
│       └── publicjob/          # 외부 채용 소스 어댑터(포트 + Alio 구현)
│
└── frontend/
    └── src/
        ├── main.jsx, App.jsx   # 진입 + 라우팅
        ├── screens/            # 화면 단위(Landing, Dashboard, Solve, ...)
        │   └── admin/          # 관리자 화면
        ├── components/         # 재사용 컴포넌트(PrepBot, layout, common, icons)
        ├── api/                # API 호출 레이어(client + 도메인별)
        ├── context/            # AuthContext(인증 상태)
        ├── constants/          # jobs.js
        └── data/               # mockData.js
```

### 아키텍처 요약
- **모놀리식 단일 서비스**로 시작. 기획서의 MSA 분리(유저/문제/답변/알림/AI)는 트래픽 증가 시의 미래 계획.
- **포트-어댑터 경계** 두 곳을 유지: `LlmPort`(AI 교체 지점), `JobPostingFetcher`(외부 채용 소스 확장 지점). 향후 AI/수집부를 분리해도 도메인 코드는 변하지 않도록 설계.
- **AI 호출은 비동기**(`@Async`)로 응답 경로와 분리.
- **외부 데이터 수집은 스케줄러**(`@Scheduled`) + Caffeine 캐시: 채용 공고(6시간), 기술 트렌드 RSS(3일).

---

## 시작하기

### 사전 요구사항
- JDK 21
- Node.js 18+ (프런트엔드)
- PostgreSQL (로컬 또는 RDS)

### 백엔드 실행

```bash
# 저장소 루트에서
# 개발 서버 (기본 포트 8080)
./gradlew bootRun        # Windows: gradlew.bat bootRun

# 테스트
./gradlew test

# 프로덕션 빌드 → build/libs/JobIT-0.0.1-SNAPSHOT.jar
./gradlew build
java -jar build/libs/JobIT-0.0.1-SNAPSHOT.jar
```

### 프런트엔드 실행

```bash
cd frontend
npm install              # 최초 1회 또는 package.json 변경 시
npm run dev              # 개발 서버 (기본 포트 5173, HMR)
npm run build            # 프로덕션 빌드 → dist/
```

> 백엔드·프런트를 동시에 띄울 때는 터미널 두 개에서 각각 실행하세요.

### 환경 변수 / 설정

비밀값은 **절대 코드·저장소에 커밋하지 않습니다.** 환경 변수 또는 외부 설정으로 주입하세요. (`application.properties`, `.env`는 git 추적 제외)

| 키(예시) | 용도 |
| --- | --- |
| `spring.datasource.*` | PostgreSQL 접속 정보 |
| JWT secret | 토큰 서명 키 |
| OAuth2 client id/secret | 소셜 로그인 |
| `publicjob.alio.api-key` | 공공 채용 데이터 API 키 |

---

## 문서
- [`CLAUDE.md`](CLAUDE.md) — 코드 작성·수정·리뷰 전에 읽어야 할 프로젝트 규칙
- [`docs/ENGINEERING_PRIORITIES.md`](docs/ENGINEERING_PRIORITIES.md) — DB 비용·트래픽 제어·비동기 AI·CI/CD·계층 설계 등 엔지니어링 원칙
