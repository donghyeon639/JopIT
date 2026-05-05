# Engineering Priorities — JobIT

> 이 문서는 사용자가 이 프로젝트에서 **중요하게 생각하는 엔지니어링 원칙과 의사결정 기준**을 정리한 것입니다.
> Claude는 작업을 실행하기 전에 해당 영역에 손댈 때 이 문서의 원칙을 먼저 확인하고, 코드/설계가 이 기준에 부합하는지 점검해야 합니다.
>
> CLAUDE.md(§1~§7)는 "이 저장소가 무엇이고 어떤 스택인가"를 다루고, 이 문서는 "어떤 관점으로 코드를 만들고 싶은가"를 다룹니다. 둘은 충돌할 수 없으며, 충돌하면 사용자에게 먼저 확인합니다.

---

## 0. 사용 가이드 (Claude 전용)

작업 영역과 매칭되는 섹션을 먼저 확인:

| 작업 영역 | 참고할 섹션 |
| --- | --- |
| JPA 쿼리, Repository, 인덱스 추가/변경, N+1 의심, 페이지네이션, DDL/마이그레이션 | §1 DB 비용 절감 |
| `application.properties`의 `spring.datasource.*`, HikariCP, 캐시(`@Cacheable`, Caffeine) | §2 트래픽 제어 |
| `@Async`, AI 호출, `ClaudeCliService`/`ClaudeApiService`, AI 피드백 파이프라인, 중복 호출/비용 방어 | §3 비동기 AI 처리 |
| `.github/workflows/`, 배포 스크립트, EC2 배포 | §4 CI/CD |
| 새 도메인/엔드포인트, Controller/Service/Repository 분리, REST URI 설계, 예외 체계, 시간/타임존 | §5 코드 설계 |
| `@Transactional` 경계, 영속성 컨텍스트, LAZY 접근, `@Async`·이벤트와 트랜잭션 상호작용 | §6 트랜잭션 경계와 영속성 컨텍스트 |
| 단위/슬라이스/통합 테스트 작성, Testcontainers, `@WebMvcTest`, `@DataJpaTest` | §7 테스트 전략 |
| 로깅 레벨, MDC, 외부 호출 추적, PII 노출 점검 | §8 로깅·관측성 |

작업 후에는 "이 변경이 위 원칙 중 어느 항목을 만족/위반하는가"를 한두 줄로 짚어주면 좋습니다.

---

## 1. DB 비용 절감 — Full Scan 제거와 복합 인덱스 설계

### 핵심 원칙
- **Full Table Scan은 곧 비용**이다. AWS RDS에서 그대로 두면 CPU 사용률이 올라가 인스턴스 사양이 비대해진다.
- **단일 인덱스로 끝내지 않는다.** 실제 조회는 대부분 복합 조건이고, 컬럼 순서가 잘못된 인덱스는 인덱스가 없는 것과 같다.
- **인덱스만이 답은 아니다.** 쿼리 구조 자체를 바꾸는 것이 더 효과적인 경우가 있다.

### 적용 체크리스트
- [ ] 새/변경 쿼리에 대해 `EXPLAIN`(또는 `EXPLAIN ANALYZE`)을 떠봤는가? Seq Scan이 보이면 멈춘다.
- [ ] 복합 인덱스를 추가할 때 **카디널리티가 높은 컬럼 + 동등 비교(`=`) 컬럼이 앞**, 범위/정렬 컬럼이 뒤로 가는가?
- [ ] 같은 테이블에 비슷한 인덱스가 이미 있는지 확인했는가? (인덱스도 쓰기 비용)
- [ ] `IN (subquery)` → `EXISTS` 변환이 더 빠른 케이스인가?
- [ ] `SELECT *` 대신 필요한 컬럼만 명시했는가? (특히 페치 조인 시)
- [ ] JOIN 순서가 작은 결과집합 → 큰 결과집합으로 흐르는가?

### 본 프로젝트에서 우선 점검 대상
- 직군/난이도 필터링이 들어가는 **질문 목록 조회** → `(jobCategory, difficulty)` 같은 복합 인덱스 후보.
- **답변 목록 + 좋아요 수** 같이 조인이 섞인 화면.
- Dashboard의 카테고리/뉴스 조회.

### 안티패턴 (피할 것)
- 컬럼마다 인덱스를 단일로 우다다 추가하기.
- 인덱스 추가만 하고 EXPLAIN으로 실제 사용 여부를 검증하지 않기.
- 마이그레이션 도구 없이 손으로 인덱스 DDL을 흩뿌리기 (§1.3 참고).

### 1.1 N+1과 페치 전략

JPA에서 가장 흔한 비용 폭발 원인. 인덱스보다 먼저 잡아야 한다.

**핵심 원칙**
- **List를 반환하는 Repository 메서드는 N+1 의심부터.** 결과 행마다 LAZY 연관/카운트 조회가 일어나면 쿼리는 N배로 늘어난다.
- **DTO 변환 코드가 어떤 필드를 건드리는지** 먼저 본다. `dto.getQuestion().getTitle()` 같은 한 줄이 N번의 SELECT를 낳을 수 있다.

**적용 체크리스트**
- [ ] List를 매핑할 때 LAZY 연관(`@ManyToOne`/`@OneToMany`)을 접근하는가? 그렇다면 fetch join 또는 `@EntityGraph` 또는 `@BatchSize` 적용했는가?
- [ ] 자식 카운트(`countByXxxId`)를 행마다 호출하지 않는가? 단일 집계 쿼리(`GROUP BY`) 또는 한 번의 `IN` 쿼리로 모은 뒤 매핑.
- [ ] fetch join + 페이징을 동시에 쓰지 않는가? (Hibernate가 메모리 페이징으로 떨어뜨림 → `@BatchSize`나 2단계 조회로 분리)
- [ ] 운영 환경에서 `spring.jpa.show-sql` 또는 p6spy로 한 번이라도 실제 발사된 쿼리 수를 봤는가?

**본 프로젝트 점검 대상**
- `AnswerService.getCommunityFeed/getByQuestion/getMyAnswers`: 답변마다 `commentRepository.countByAnswerId()` + `getQuestion().getTitle()` + `getUser().getNickname()` → 답변 N개에 쿼리 3N+1. 단일 집계 쿼리 또는 fetch join + comment count IN 조회로 묶어야 한다.

**안티패턴**
- 서비스 메서드 안에서 `for` 돌면서 Repository 호출하기.
- "일단 LAZY, 필요하면 EAGER" — EAGER는 더 큰 비용을 부른다. 항상 LAZY + 명시적 fetch join.

### 1.2 페이지네이션 표준

**핵심 원칙**
- **공개 목록 조회에 `List<T>` 반환은 미래 비용**. 데이터가 늘면 OOM·풀 점유로 이어진다.
- **모든 목록 엔드포인트는 페이지 단위.** 표준을 한 번 정해 도메인 간에 동일하게 따른다.

**적용 체크리스트**
- [ ] Repository 시그니처가 `Page<T>` 또는 cursor 기반인가? (`List<T>` 반환 메서드는 admin/내부 용으로 한정)
- [ ] 클라이언트 파라미터 표준이 정해져 있는가? — 예: `?page=0&size=20&sort=createdAt,desc` (Spring `Pageable`) 또는 `?cursor=...&limit=...`
- [ ] 응답 스키마에 `totalElements`/`hasNext` 등 메타가 일관되게 들어가는가?
- [ ] 정렬 가능한 컬럼을 화이트리스트로 제한했는가? (임의 컬럼 정렬 = 풀스캔 위험)

**본 프로젝트 점검 대상**
- `AnswerRepository.findAllByOrderByCreatedAtDesc()`, `findByQuestionIdOrderByCreatedAtDesc()`, `findByUserIdOrderByCreatedAtDesc()` 모두 `List` 반환 → `Page` 또는 cursor 기반으로 전환 필요.
- 커뮤니티 피드처럼 무한 스크롤 UX는 **cursor 기반(`createdAt + id`)이 OFFSET 기반보다 깊은 페이지에서 압도적으로 빠르다**.

**안티패턴**
- 큰 테이블에 OFFSET 페이지네이션 (깊은 페이지에서 비용 폭발).
- `Pageable` 받아놓고 `findAll()`로 전체 가져온 뒤 메모리에서 자르기.

### 1.3 DB 마이그레이션 도구

**핵심 원칙**
- §1, §1.1, §1.2의 모든 인덱스/스키마 변경은 **마이그레이션 파일**로 들어가야 재현·롤백 가능하다.
- `ddl-auto=update` 운영 의존은 **장애의 시한폭탄**. 의도하지 않은 ALTER가 일어나는 순간 끝.

**적용 체크리스트**
- [ ] Flyway 또는 Liquibase 도입 결정이 합의되었는가? (CLAUDE.md §4.3 — 미합의 상태)
- [ ] 모든 DDL이 `db/migration/V{version}__{name}.sql` 같은 버전 관리 파일에 들어가는가?
- [ ] 운영 `application.properties`에 `spring.jpa.hibernate.ddl-auto=validate` (또는 `none`)가 명시되어 있는가?
- [ ] PR에 스키마 변경이 포함되면 마이그레이션 파일이 함께 들어오는가?

**안티패턴**
- 콘솔에서 직접 ALTER 친 뒤 마이그레이션 파일을 안 만들기.
- 이미 머지된 마이그레이션 파일을 사후 수정 (체크섬 깨짐).

---

## 2. 트래픽 제어 — 커넥션 풀과 캐싱 전략

### 핵심 원칙
- **DB는 보호 대상**이다. 동시 요청이 무제한으로 커넥션을 잡으면 RDS 자체가 죽는다.
- **커넥션 풀 상한 + 타임아웃**을 서비스 부하 패턴에 맞춰 설정한다. 디폴트 그대로 두지 않는다.
- **캐시는 만능이 아니다.** "변경 빈도 ↓ + 조회 빈도 ↑ + 정합성 요구 ↓"가 동시에 성립할 때만 쓴다.

### HikariCP 적용 체크리스트
- [ ] `spring.datasource.hikari.maximum-pool-size`가 RDS의 `max_connections`를 인스턴스 수만큼 곱했을 때 안전한가?
- [ ] `connection-timeout`, `idle-timeout`, `max-lifetime`이 명시적으로 설정되어 있는가? (RDS 권장: `max-lifetime` < RDS의 `wait_timeout`)
- [ ] 새 외부 호출이 트랜잭션 안에서 일어나서 커넥션을 길게 잡고 있지 않은가?

### 캐시 적용 기준
| 데이터 | 캐시 적용? | 이유 |
| --- | --- | --- |
| CS 면접 문제, 카테고리 | ✅ | 변경 거의 없음, 모두에게 동일 |
| 채용 공고 외부 API 응답 (네이버/사람인) | ✅ | 외부 호출 비용 + 분당 변경 없음 (CLAUDE.md §5에 jobCategory별 키 명시) |
| 사용자별 답변/좋아요 상태 | ❌ | 실시간 정합성 필요 |
| 사용자 프로필 | ⚠️ | TTL 짧게, 변경 시 evict |

### 안티패턴 (피할 것)
- 캐시 키에 사용자 ID가 들어갈 만한 데이터를 글로벌 캐시에 넣기.
- TTL/eviction 정책 없이 `@Cacheable`만 붙여놓기.
- Redis가 없는 상태에서 분산 환경을 가정한 캐시 설계 (현재는 Caffeine 로컬 캐시 — CLAUDE.md §5).

---

## 3. 비동기 AI 처리 — 응답 지연 제거와 모듈 분리

### 핵심 원칙
- **외부 LLM 호출은 동기 응답 경로에 두지 않는다.** 수 초~수십 초 지연은 사용자 경험과 서버 스레드를 동시에 망가뜨린다.
- **저장 트랜잭션과 AI 호출을 분리**한다. 사용자에게는 즉시 저장 응답을 주고, AI 결과는 백그라운드에서 갱신한다.
- **MSA를 미리 하지 않는다. 그러나 떼어낼 수 있게 둔다.** AI 모듈은 다른 도메인과 결합도를 최소화 — 향후 분리 비용을 낮추는 것 자체가 가치.

### 적용 체크리스트
- [ ] 답변 제출 API는 AI 호출 결과를 **기다리지 않고 반환**하는가?
- [ ] AI 결과를 클라이언트가 받는 경로(폴링/WebSocket/SSE)가 정의되어 있는가?
- [ ] AI 호출 실패/타임아웃 시 사용자 답변은 보존되고, 재시도/실패 표시가 가능한가?
- [ ] AI 도메인이 다른 도메인의 엔티티/Service를 직접 import하지 않고, 인터페이스/이벤트로만 통신하는가?
- [ ] AI 처리 실패가 답변 저장 트랜잭션을 롤백시키지 않는가?

### 본 프로젝트의 현재 위치 (CLAUDE.md §7과 함께 읽을 것)
- 현재 `ClaudeCliService`는 매 호출마다 `npx -y @anthropic-ai/claude-code` 콜드 스타트로 5~10초 지연 + 스트리밍 불가 → **화상 면접/실시간 피드백 착수 시점에 `ClaudeApiService`(Anthropic Java SDK)로 교체 필수**.
- 현재 비동기 인프라: 단순 `@Async`/스레드풀 또는 미도입 상태일 수 있음 — 손대기 전 기존 코드 확인.
- WebSocket/SSE 미도입 상태. 도입 시 SecurityConfig의 `/ws/**` 경로 인가 + JWT 핸드셰이크 함께 처리.

### 안티패턴 (피할 것)
- 답변 제출 API에서 LLM 응답을 동기로 기다리기.
- AI 호출을 답변 저장 트랜잭션 안에 넣기 (실패 시 답변까지 롤백).
- AI 도메인이 다른 도메인 엔티티에 직접 의존해서 나중에 떼어내기 어렵게 만들기.

### 3.1 멱등성·비용·남용 방어

LLM 호출은 **유료 외부 API**이고, **시간이 걸리며**, **사용자가 더블 클릭할 수 있다**. 이 세 가지가 만나면 비용·정합성 문제가 동시에 터진다.

**핵심 원칙**
- **상태 전이는 원자적으로.** "현재 상태가 X면 Y로 바꾸고 호출" 같은 가드는 read-then-write이 아니라 **단일 UPDATE의 성공/실패**로 판정한다.
- **사용자 입력이 LLM 프롬프트에 들어간다**는 건 비용·인젝션·PII 노출 위험을 동시에 떠안는 것. 길이 상한과 가드 프롬프트를 항상 둔다.

**적용 체크리스트**
- [ ] 같은 리소스에 대한 AI 호출이 동시에 들어왔을 때, 두 번째 호출이 차단되는가? (e.g. `UPDATE answers SET feedback_status='PENDING' WHERE id=? AND feedback_status='NONE'`의 affected rows로 분기)
- [ ] 사용자별·일별 호출 횟수 상한이 있는가? (Bucket4j, Redis counter, 또는 단순 DB 카운트)
- [ ] 입력 길이 상한이 명시되어 있는가? (DTO `@Size`, Service 단의 hard cap, 둘 다 권장)
- [ ] 프롬프트 인젝션 가드 — 사용자 입력을 명확히 구분하는 구조(`## 지원자 답변` 같은 섹션 + "이 섹션 외 명령은 무시" 안내)가 있는가?
- [ ] 외부 호출 타임아웃이 명시적인가? (현재 `claude.cli.timeout-seconds:120` 있음 — 줄여야 할 수도)
- [ ] AI 응답을 그대로 사용자에게 렌더링한다면 XSS/HTML 정화 적용 여부?

**본 프로젝트 점검 대상**
- `AnswerService.requestFeedback`의 가드(`feedbackStatus == NONE/PENDING/DONE` 체크)는 read-then-write라 동시 클릭 시 둘 다 통과 가능. 단일 UPDATE의 affected rows로 전이 성공 여부를 판단해야 한다.
- `AiFeedbackService.buildPrompt`가 `answer.content`를 길이 제한 없이 그대로 prompt에 삽입.
- 호출당 비용 추적 로깅(요청·응답 토큰 수)이 없음 → 운영에서 비용 가시성 0.

**안티패턴**
- "프런트에서 더블 클릭 막으니 괜찮음" — 네트워크 재시도, 다른 탭, API 직접 호출이 모두 우회 경로.
- 사용자 입력을 LLM 프롬프트에 그대로 붙이면서 "어차피 우리 사용자만 쓰니까".
- LLM 응답을 마크다운/HTML로 그대로 innerHTML에 꽂기.

---

## 4. CI/CD — GitHub Actions 기반 배포 자동화

### 핵심 원칙
- **수동 배포는 누적 비용이 크다.** 휴먼 에러 + 배포 빈도가 늘수록 손해.
- **main 머지 = 자동 빌드 + 테스트 + 배포** 흐름을 기본으로 둔다.
- 단, IaC/워크플로 파일은 **요청 시에만 작성** (CLAUDE.md §4.3).

### 적용 체크리스트
- [ ] PR 단계에서 `./gradlew test`가 자동 실행되는가?
- [ ] 빌드 캐시(Gradle/Yarn)가 워크플로에 적용되어 시간을 절약하는가?
- [ ] 시크릿(JWT secret, DB 패스워드, AI API 키)이 GitHub Actions Secrets로만 주입되고, 로그에 echo되지 않는가?
- [ ] 배포 실패 시 롤백 또는 헬스체크 실패 알림이 있는가?
- [ ] 프런트(`JobIT/frontend`)와 백엔드는 별도 빌드 단위 — 워크플로도 분리되어야 하는가? 변경 경로(`paths:`)로 트리거를 좁혔는가?

### 안티패턴 (피할 것)
- 시크릿을 `application.properties`에 박은 채로 배포하기.
- 테스트를 건너뛰는 워크플로(`-x test`).
- main에 직접 푸시 + main에서 hotfix를 거치지 않고 force push.

---

## 5. 코드 설계 — 객체지향 본질을 살린 REST API 구조

### 핵심 원칙
> "객체지향의 핵심은 유지보수성과 코드 재사용성이다. 이를 살리지 못한 자바 코드는 절차적 코드를 자바 문법으로 옮긴 것에 지나지 않는다."

- **계층 책임을 엄격히 분리**한다.
  - **Controller**: 요청/응답 변환만. 비즈니스 로직 금지.
  - **Service**: 비즈니스 로직 집중. 트랜잭션 경계.
  - **Repository**: DB 접근 한정. 비즈니스 로직 금지.
- **자원(Resource) 중심 URI + HTTP 메서드의 의미** 를 지킨다.
- **일관된 응답 포맷 + 명확한 상태 코드**로 새 기능이 기존 패턴을 그대로 따라가도록 한다.

### Controller 체크리스트
- [ ] DTO ↔ 엔티티 변환만 하는가? `if`/조건 분기가 비즈니스 의미를 갖는다면 Service로 옮긴다.
- [ ] `@Valid` + `@RequestBody`로 입력 검증을 위임했는가?
- [ ] 인증된 사용자 정보를 `@AuthenticationPrincipal` 등으로 받는가? (직접 SecurityContext 들춰보지 않기)

### Service 체크리스트
- [ ] 트랜잭션 경계(`@Transactional`)가 명확한가? 외부 API 호출이 트랜잭션 내에 갇혀 있지 않은가? (§3과 연결)
- [ ] 한 메서드가 하나의 일을 하는가? 답변 저장 + AI 호출 + 알림 전송이 하나의 메서드에 섞여 있지 않은가?
- [ ] 다른 도메인 Service에 직접 의존하기 전에, 이벤트/포트 인터페이스로 끊을 수 있는지 검토했는가? (§3 모듈 분리와 연결)

### REST URI 체크리스트
- [ ] 동사가 아닌 자원 명사인가? (`/api/questions/{id}/answers` ✅, `/api/getAnswers?qid=...` ❌)
- [ ] HTTP 메서드 의미를 지키는가? (`GET` 안 부수효과, `POST` 생성, `PUT` 전체 교체, `PATCH` 부분 수정, `DELETE` 삭제)
- [ ] 상태 코드를 적절히 쓰는가? (생성 201, 검증 실패 400, 인증 실패 401, 권한 없음 403, 없음 404, 충돌 409)
- [ ] 응답 포맷이 다른 엔드포인트와 일관된가? 에러 응답 스키마가 통일되어 있는가?

### 안티패턴 (피할 것)
- Controller 안에서 Repository를 직접 호출.
- Service가 HTTP 상태 코드/`HttpServletRequest`를 알고 있음.
- "유틸 클래스"에 도메인 로직이 쌓이는 것 (`UserUtils.calculateScore(user)` 같은 것은 `User`나 도메인 Service로).
- 동작은 같지만 시그니처가 다른 메서드를 도메인마다 복붙(공통 추상화 검토 — 단, CLAUDE.md §4.6 "새 추상화 도입은 합의 후" 원칙은 지킨다).

### 5.1 예외 체계와 에러 응답 스키마

**핵심 원칙**
- **상태 코드가 곧 클라이언트 분기 로직.** "없음/금지/충돌/검증 실패"가 모두 400으로 떨어지면 프런트는 메시지 문자열로 분기해야 하고, 그건 깨지기 쉽다.
- **도메인 의미를 가진 예외 클래스**를 던지고, 전역 핸들러가 HTTP 상태로 매핑한다. Service는 `HttpStatus`를 모른다.

**적용 체크리스트**
- [ ] 도메인 예외 베이스(`BusinessException`)와 의미별 서브타입(`NotFoundException`, `ForbiddenException`, `ConflictException`, `ValidationException`)이 정의되어 있는가?
- [ ] `GlobalExceptionHandler`가 도메인 예외를 정확한 HTTP 상태로 매핑하는가? (404/403/409/400/401)
- [ ] 에러 응답 스키마가 통일되어 있는가? — 예: `{ code: "ANSWER_NOT_FOUND", message: "...", fields?: {...} }`
- [ ] 스택 트레이스/내부 메시지가 응답 body로 새지 않는가? (5xx는 일반 메시지로 포장)

**본 프로젝트 점검 대상**
- `AnswerService.requestFeedback`이 모두 `IllegalArgumentException` → "존재하지 않음"은 404, "본인 답변 아님"은 403, "이미 진행 중"은 409로 분리.
- `GlobalExceptionHandler`가 `IllegalArgumentException → 400`만 처리. 도메인 예외 추가 후 매핑 보강 필요.
- `MethodArgumentNotValidException` 응답이 평면 `Map<field, message>` — 통일 스키마(`{code, message, fields}`)로 전환 검토.

**안티패턴**
- 모든 비즈니스 오류를 `IllegalArgumentException`/`RuntimeException`으로 던지기.
- 예외 메시지에 사용자 입력값을 그대로 붙여 응답에 노출.
- `try { ... } catch (Exception e) { return ResponseEntity.status(500).body(e.getMessage()); }` — Service에서 직접 처리하지 말고 던져서 핸들러가 받게.

### 5.2 시간과 타임존

**핵심 원칙**
- **저장은 UTC 절대시각.** 표시는 클라이언트에서 변환. 서버 타임존 의존은 멀티 인스턴스/멀티 리전 즉시 깨짐.
- 새 시간 컬럼에 `LocalDateTime`을 쓰지 않는다. **`Instant` 또는 `OffsetDateTime`**.

**적용 체크리스트**
- [ ] 새 엔티티의 시간 필드가 `Instant`/`OffsetDateTime`인가? (`LocalDateTime`이면 멈춘다)
- [ ] DB 컬럼 타입이 `TIMESTAMP WITH TIME ZONE` (PostgreSQL `timestamptz`)인가?
- [ ] 자동 채움은 `@CreationTimestamp`/`@UpdateTimestamp` 또는 `Auditing`(`@CreatedDate`)를 통해 일관되게 들어가는가?
- [ ] JSON 직렬화 포맷이 ISO-8601 + `Z`/오프셋 포함인가?

**본 프로젝트 점검 대상**
- `Answer.java`, `Question.java`의 `createdAt`이 `LocalDateTime` + `LocalDateTime.now()` → 마이그레이션 시점에 `Instant` 전환 검토. (호환성 비용 있으니 합의 필요)
- 신규 테이블/엔티티는 처음부터 `Instant`로.

**안티패턴**
- `LocalDateTime.now()` 직접 호출 (서버 타임존 의존 + 테스트 시간 고정 불가). `Clock` 주입 검토.
- `String`으로 시간 저장(`"2026-05-05 12:00"`).

---

## 6. 트랜잭션 경계와 영속성 컨텍스트

§3·§5와 교차하는 영역. 별도 섹션으로 두는 이유는 **여기서 일어나는 사고는 디버깅이 가장 어렵기 때문**.

### 핵심 원칙
- **DTO 변환은 트랜잭션 안에서.** LAZY 연관에 닿는 매핑 코드는 영속성 컨텍스트가 살아 있어야 한다.
- **`@Async` + `@Transactional`은 함정.** Spring AOP 프록시가 자기호출(self-invocation)을 잡지 못하고, 트랜잭션 시작 타이밍이 의도와 다를 수 있다.
- **트랜잭션 커밋 전에 비동기 작업이 다른 스레드에서 새 데이터를 못 본다.** 항상 커밋 후에 트리거.

### 적용 체크리스트
- [ ] DTO 변환 메서드(`Response.from(entity)`)가 LAZY 필드를 건드리는데 호출 지점이 `@Transactional` 밖은 아닌가?
- [ ] `@Async` 메서드를 호출하는 쪽이 같은 클래스 내부 호출(self-invocation)이 아닌가? (프록시를 거치지 않으면 비동기로 동작 안 함)
- [ ] AI/이메일/이벤트 같은 사이드 이펙트는 `ApplicationEventPublisher` + `@TransactionalEventListener(phase = AFTER_COMMIT)`로 트리거하는가? 아니면 **트랜잭션 커밋 후 별도 호출**인가?
- [ ] `@Transactional(readOnly = true)`가 단순 조회에 적용되어 있는가? (성능/의도 명시)
- [ ] 외부 호출(LLM, HTTP)이 트랜잭션 안에서 일어나지 않는가? (커넥션 점유 — §2와 연결)

### 본 프로젝트 점검 대상
- `AnswerResponse.from()`이 `a.getQuestion().getTitle()`/`a.getUser().getNickname()` 등 LAZY 접근. 현재는 Service `@Transactional` 안에서 호출되어 OK지만, **Controller에서 직접 호출하는 패턴이 추가되는 순간 `LazyInitializationException`**.
- `AiFeedbackService.requestFeedback`은 `@Async + @Transactional` 동시 적용 + `AnswerService.requestFeedback`(자체 `@Transactional`) 안에서 호출됨. 외부 트랜잭션 커밋 전에 비동기 작업이 시작되면 새/변경된 행을 못 볼 수 있다 → `AFTER_COMMIT` 이벤트 또는 호출 분리 권장.
- `AiFeedbackService` 안에서 첫 줄이 `answer.markFeedbackPending()` + save인데, 이건 §3.1의 "원자적 상태 전이"와 결합해 단일 UPDATE로 끌어올려야 정합성 보장.

### 안티패턴
- 같은 클래스 내에서 `this.someAsyncMethod()` 호출 (프록시 우회 → 비동기/트랜잭션 둘 다 무력화).
- Controller에서 lazy 연관 접근 후 OSIV(Open Session In View)에 의존.
- 한 트랜잭션 안에서 외부 HTTP/LLM 호출 → 커넥션 점유 + 응답 시간 증폭.

---

## 7. 테스트 전략

### 핵심 원칙
- **테스트는 의도의 기록.** 코드만 보고는 알 수 없는 "이게 왜 이래야 하는지"를 박아둔다.
- **테스트 피라미드:** 단위 多 → 슬라이스 中 → 통합/E2E 少. 모든 걸 통합 테스트로 덮으면 빌드가 느려져 결국 안 돌리게 된다.
- **DB는 진짜 PostgreSQL을 써라** (Testcontainers). H2/모킹은 §1·§6의 함정을 잡지 못한다.

### 계층별 테스트
| 계층 | 도구 | 경계 |
| --- | --- | --- |
| 도메인/Service 단위 | JUnit + Mockito | Repository는 모킹, 외부 API 모킹 |
| Repository | `@DataJpaTest` + Testcontainers PostgreSQL | 실 DB로 N+1·인덱스 사용 검증 |
| Controller 슬라이스 | `@WebMvcTest` + `MockMvc` | Service 모킹, Security 통합 포함 |
| 통합/인수 | `@SpringBootTest` + Testcontainers | 골든 패스 1~2개만 |

### 적용 체크리스트
- [ ] 새 Service 메서드에 단위 테스트가 있는가? 분기/예외 경로 포함?
- [ ] Repository에 커스텀 쿼리/`@EntityGraph`/네이티브 쿼리가 들어가면 `@DataJpaTest` + Testcontainers로 **실제 발사된 SQL을 검증**했는가?
- [ ] Controller에 새 엔드포인트가 추가되면 `@WebMvcTest`로 인증/검증/상태코드를 커버했는가?
- [ ] 도메인 예외 → HTTP 상태 매핑(§5.1)을 인수 테스트로 한 번은 확인했는가?
- [ ] 시간 의존 로직은 `Clock` 주입으로 결정적으로 테스트되는가?

### 안티패턴
- Repository를 모킹한 Service 테스트만으로 만족 — JPA의 영속성 컨텍스트 동작이 검증되지 않는다.
- H2 인메모리로 PostgreSQL 코드 테스트 (방언 차이로 prod에서 깨짐).
- 모든 외부 호출을 통과시키는 통합 테스트만 잔뜩 (CI 시간 폭발 + 플레이키).

---

## 8. 로깅·관측성

운영에서 디버깅이 가능하려면 로그가 **요청 단위로 추적 가능**해야 하고, **외부 호출의 비용/지연이 보여야** 한다.

### 핵심 원칙
- **요청 추적은 MDC.** 요청마다 `requestId`/`userId`를 MDC에 심으면 모든 로그 라인이 그 컨텍스트로 묶인다.
- **외부 호출은 INFO로 시작·끝·소요시간.** LLM, 채용 API 등 돈/시간이 드는 호출은 가시화 필수.
- **PII/시크릿/사용자 본문은 로그 금지.** "디버깅용으로 잠깐"이 그대로 prod에 박힌다.

### 적용 체크리스트
- [ ] 모든 요청에 `requestId`(예: UUID) MDC가 심어지는가? (서블릿 필터 또는 인터셉터)
- [ ] 인증된 요청은 `userId`도 MDC에 들어가는가? (단, `username`/`email` 같은 PII는 피한다)
- [ ] 외부 호출(LLM, 채용 API)이 시작·종료·예외에 INFO/ERROR로 찍히고, 소요시간(ms)이 함께 들어가는가?
- [ ] 4xx 비즈니스 예외는 WARN, 5xx 시스템 예외는 ERROR인가? (4xx를 ERROR로 찍으면 알람 노이즈)
- [ ] 답변 본문, 비밀번호, 토큰, JWT, AI 프롬프트 본문은 **절대** 로그에 안 들어가는가?
- [ ] 비용 추적 — LLM 호출 시 요청·응답 토큰 수가 로그에 남는가? (sdk 전환 후 가능)

### 본 프로젝트 점검 대상
- `ClaudeCliService`가 "실행 시작/완료"는 찍지만 **소요시간·답변 ID·사용자 ID 컨텍스트 없음** → MDC 도입 후 자연스럽게 묶이도록.
- 요청 단위 추적 인프라(필터/MDC) 미도입 상태.

### 안티패턴
- `e.printStackTrace()` 또는 `System.out.println` 잔존.
- 같은 예외를 catch에서 한 번 + 상위에서 또 한 번 로그 (이중 로그 + 노이즈).
- 정상 흐름에 ERROR 레벨, 예외 흐름에 INFO 레벨 (알람 시스템이 무력화됨).

---

## 9. 우선순위 충돌 시 판단 기준

여러 원칙이 충돌할 때(예: "모듈을 분리하고 싶지만 지금은 모놀리식이 맞다"), 다음 순서로 판단:

1. **MVP 범위인가?** (CLAUDE.md §5) — 범위 밖이면 멈추고 합의.
2. **비밀값/보안 위반이 있는가?** — 있으면 즉시 거부.
3. **단순함이 이긴다.** 추상화 도입 < 결합도 낮춘 단순 구현. (CLAUDE.md §4.6)
4. **미래의 확장 비용**을 고려하되, 현재 트래픽으로 정당화되지 않는 분산/MSA/큐 도입은 하지 않는다 (§3 "MSA를 미리 하지 않는다").
5. 그래도 모호하면 **사용자에게 한 줄로 묻는다.** "A는 ~이유로 단순하지만 ~한 비용이 있고, B는 그 반대입니다. 어느 쪽?"