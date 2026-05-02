# CLAUDE.md — DevPrep (prepnote)

이 저장소에서 코드를 작성·수정·리뷰하기 전에 반드시 이 문서를 먼저 읽으세요.

---

## 1. 프로젝트 한 줄 요약

**직군별 면접·채용 준비 플랫폼.** 1차 타겟은 백엔드/프런트엔드/데이터 직군 개발자 취준생이며, 추후 마케팅·디자인 등 비개발 직군까지 확장할 계획입니다. 차별점은 **AI 답변 피드백 + 직군별 맞춤 문제 + 커뮤니티**.

> 우선순위: **개발 직군 기능을 먼저 완성** → 그다음 비개발 직군 확장. 현재 작업은 모두 개발 직군 기준으로 판단합니다.

---

## 2. 저장소 레이아웃

```
C:\prepnote\
├── prepnote-main-service\    # 백엔드 (Spring Boot)
└── prepnote-frontend\         # 프런트엔드 (React + Vite)
```

두 서비스는 **모노레포 형태로 한 디렉토리에 공존**하지만 별도 빌드 단위입니다. 한쪽을 변경할 때 다른 쪽 API 계약(엔드포인트, 응답 스키마)에 영향이 가는지 항상 확인하세요.

---

## 3. 실제 기술 스택 (기획안과 다름 — 주의)

기획 문서에는 Python/FastAPI로 적혀 있으나 **실제 백엔드는 Spring Boot입니다.** 코드를 작성할 때는 항상 아래 실제 스택을 기준으로 하세요.

### 3.1 백엔드 (`prepnote-main-service`)
| 항목 | 값 |
| --- | --- |
| 언어/런타임 | Java 21 (toolchain 고정) |
| 프레임워크 | Spring Boot **4.0.6** |
| 빌드 | Gradle (Groovy DSL, `build.gradle`) |
| 영속성 | Spring Data JPA + PostgreSQL |
| 메시징 | Spring Kafka |
| 보안 | Spring Security |
| 검증 | Spring Validation |
| 웹 | Spring Web MVC |
| 보일러플레이트 | Lombok (compileOnly + annotationProcessor) |
| 테스트 | JUnit 5 (`useJUnitPlatform()`), `*-test` 스타터 |
| 패키지 루트 | `com.prepnote.main_service` |
| 엔트리 | `MainServiceApplication.java` |

**실행 명령어 (백엔드):**

```bash
# 디렉토리 이동
cd C:\prepnote\prepnote-main-service

# 개발 서버 실행 (기본 포트 8080)
./gradlew bootRun
# Windows CMD/PowerShell:
gradlew.bat bootRun

# 테스트
./gradlew test

# 프로덕션 빌드 (build/libs/*.jar 생성)
./gradlew build

# 빌드된 jar 직접 실행
java -jar build/libs/main-service-0.0.1-SNAPSHOT.jar

# 클린 빌드
./gradlew clean build
```

> Bash 셸(Git Bash, WSL 등)에서는 `./gradlew`, Windows 네이티브 셸에서는 `gradlew.bat`을 사용하세요.
> 첫 실행 시 Gradle/JDK 21 toolchain을 자동 다운로드하므로 시간이 걸릴 수 있습니다.

### 3.2 프런트엔드 (`prepnote-frontend`)
| 항목 | 값 |
| --- | --- |
| 프레임워크 | React 18.3 |
| 라우팅 | react-router-dom 6.30 |
| 빌드 | Vite 6 (`@vitejs/plugin-react`) |
| 모듈 | ESM (`"type": "module"`) |
| 진입 | `src/main.jsx` → `src/App.jsx` |
| 화면 | `src/screens/` (Landing, Auth, Dashboard, QuestionList, Solve, AIFeedback, Community, LevelCheck) |
| 컴포넌트 | `src/components/` |
| 목 데이터 | `src/data/mockData.js` |

**실행 명령어 (프런트엔드):**

```bash
# 디렉토리 이동
cd C:\prephub\frontend

# 의존성 설치 (최초 1회 또는 package.json 변경 시)
npm install

# 개발 서버 실행 (기본 포트 5173, HMR)
npm run dev

# 프로덕션 빌드 (dist/ 디렉토리 생성)
npm run build

# 빌드 결과 미리보기 (기본 포트 4173)
npm run preview
```

> 백엔드와 프런트를 동시에 띄울 때는 별도 터미널 두 개에서 각각 실행하세요.
> Windows에서 PowerShell 실행 정책으로 `npm`이 막히면 한 번 `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`로 풀어줍니다.

> TypeScript 미도입. JS 파일에 타입 어노테이션을 임의로 도입하지 마세요. 도입이 필요하면 먼저 사용자와 합의.

---

## 4. 작업 시 지켜야 할 규칙

### 4.1 스택 관련
- 백엔드 코드를 작성할 때 **Python/FastAPI 코드를 절대 만들지 마세요.** 기획 문서의 잔재일 뿐, 현재 진실의 원천은 `build.gradle`입니다.
- Spring Boot **4.x** 기준으로 API를 사용하세요 (3.x에서 deprecated된 패턴 주의: e.g. `WebSecurityConfigurerAdapter` 미사용).
- Java 21 기능(record, sealed, pattern matching, virtual threads 등)을 자유롭게 사용해도 됩니다.

### 4.2 아키텍처
- 현재는 **모놀리식 단일 서비스**(`prepnote-main-service`)로 시작합니다. 기획서의 MSA 분리도(유저/문제/답변/알림/AI)는 **트래픽이 늘었을 때의 미래 계획**이며, 지금은 패키지 분리 수준으로 충분합니다.
- 도메인을 패키지로 나눌 때는 `com.prepnote.main_service.<domain>` (예: `user`, `question`, `answer`, `notification`, `aifeedback`) 형태를 권장합니다. 새 패턴을 도입하기 전에 기존 코드를 확인하세요.
- AI 피드백 기능은 **2차 고도화 범위**입니다. MVP 작업 중이라면 이 부분에 손대기 전에 확인.

### 4.3 데이터/인프라
- DB는 PostgreSQL입니다. 마이그레이션 도구는 아직 미도입 — 추가 제안 시 사용자와 먼저 합의(Flyway vs Liquibase).
- Kafka 의존성이 빌드에 들어 있지만 **현재 사용처가 없을 수 있습니다.** 새 코드에서 Kafka를 끌어 쓰기 전에 실제 토픽/컨슈머 구성이 있는지 먼저 확인하세요.
- 인프라 가정: AWS EC2 / RDS / S3 / CloudWatch / GitHub Actions. 단, 아직 IaC·워크플로 파일은 미정 — 임의로 만들지 말고 요청 시에만 작성.

### 4.4 보안
- Spring Security가 켜져 있는 상태입니다. 새 엔드포인트를 만들 때 인증/인가 정책을 항상 함께 고려하세요.
- 인증은 **JWT** 기반(기획 명시). 토큰 발급/검증 코드를 추가할 때는 기존 보안 설정 클래스에 일관되게 통합합니다.
- 비밀값(DB 패스워드, JWT secret, AI API 키)은 절대 코드/리포지토리에 하드코딩하지 마세요. `application.properties` 대신 환경 변수 또는 외부 설정으로.

### 4.5 프런트엔드
- 스타일링 방식은 `src/styles/`에 있는 기존 파일을 먼저 보고 그에 맞춥니다. 새 스타일 라이브러리(Tailwind, MUI 등)를 임의 도입 금지.
- API 호출 레이어가 아직 표준화되어 있지 않을 수 있습니다. fetch 래퍼/axios 도입을 결정하기 전 사용자와 합의.
- 화면 구성은 `screens/`에 한 파일로 두고, 재사용 단위만 `components/`로 분리하는 현재 컨벤션을 유지하세요.

### 4.6 일반
- 한국어 응답을 기본으로. 코드 내 식별자는 영어, 주석/문서는 필요 시 한국어 OK.
- 작업 전 항상 해당 영역의 기존 파일을 읽어 컨벤션을 파악한 뒤 수정. 새 추상화·라이브러리 도입은 반드시 합의 후에.
- 기획서에 적힌 기능이라도 MVP 범위(§5)를 벗어나면 먼저 확인.

---

## 5. MVP 범위 (1차)

다음 기능 안에서 작업하고 있다면 그린라이트:

1. 회원가입 / 로그인 (JWT)
2. 직군별 기술면접 질문 목록 (CS, DB, 네트워크, OS, 백엔드, 프런트)
3. 답변 작성 및 저장
4. 다른 사람 답변 열람 및 좋아요
5. 난이도별 필터링 (하/중/상)

아래는 **2차 고도화** — 별도 합의 없이 착수하지 말 것:
- AI 답변 피드백 (Claude/GPT)
- 기업별 면접 후기
- 랭킹 시스템
- 모의면접 타이머
- 오답 노트

---

## 6. 작업 시작 전 체크리스트
- [ ] application.properties, .env와 같은 설정 파일을 git에 commit에 하지말것
- [ ] 변경 대상이 백엔드인지 프런트인지 확인 (두 개의 별도 빌드 단위)
- [ ] 기존 패키지/폴더 구조와 컨벤션을 먼저 읽었는가
- [ ] MVP 범위인가, 고도화 범위인가
- [ ] 새 의존성/라이브러리/추상화를 도입하려 한다면 사용자에게 먼저 확인했는가
- [ ] 비밀값을 코드에 박지 않았는가
- [ ] 백엔드 변경이 프런트 API 계약에 영향을 주는가 (또는 그 반대)