export const sampleQuestions = [
  { id: 1, title: "TCP와 UDP의 차이를 설명해주세요", cat: "네트워크", diff: "mid", solved: 1240 },
  { id: 2, title: "Process와 Thread의 차이는?", cat: "운영체제", diff: "low", solved: 980 },
  { id: 3, title: "Index가 항상 빠를까요?", cat: "DB", diff: "high", solved: 720 },
  { id: 4, title: "React의 Virtual DOM은 왜 빠른가요?", cat: "프론트엔드", diff: "mid", solved: 1430 },
  { id: 5, title: "MSA와 Monolith의 트레이드오프", cat: "백엔드", diff: "high", solved: 540 },
  { id: 6, title: "HTTP/1.1과 HTTP/2의 차이", cat: "네트워크", diff: "mid", solved: 880 },
];

/* ─────────────────────────────────────────────
 *  채용/취업 mock — 직군별
 *  실제 외부 API 미연동. 추후 사람인 OpenAPI / 네이버 검색 API 등으로 교체 예정.
 *  (CLAUDE.md §5 "2차 고도화" 항목 후보)
 * ─────────────────────────────────────────────*/

export const jobNewsByJob = {
  "백엔드": [
    { id: "be-1", title: "합격은 확률이다",        subtitle: "백엔드 개발자 연봉 데이터 한눈에 보기",
      gradient: "linear-gradient(135deg, #2C3D5F 0%, #1A1F2E 100%)", emoji: "📊" },
    { id: "be-2", title: "일할맛 나는 백엔드 팀",   subtitle: "최고의 동료와 함께 쌓는 시스템 경험",
      gradient: "linear-gradient(135deg, #34C5A7 0%, #2BA890 100%)", emoji: "🚀" },
    { id: "be-3", title: "올해 가장 핫한 기술 스택", subtitle: "2026년 백엔드 채용 트렌드 리포트",
      gradient: "linear-gradient(135deg, #4A7BF7 0%, #3B66E0 100%)", emoji: "💡" },
  ],
  "프론트엔드": [
    { id: "fe-1", title: "디자인이 코드가 되는 순간", subtitle: "프론트엔드 개발자 합격 노하우",
      gradient: "linear-gradient(135deg, #FF6B9D 0%, #E84F87 100%)", emoji: "🎨" },
    { id: "fe-2", title: "일할맛 나는 프론트 팀",     subtitle: "사용자 경험을 함께 만드는 동료들",
      gradient: "linear-gradient(135deg, #34C5A7 0%, #2BA890 100%)", emoji: "✨" },
    { id: "fe-3", title: "React, Next.js 그 이후",    subtitle: "2026년 프론트엔드 채용 트렌드",
      gradient: "linear-gradient(135deg, #2C3D5F 0%, #1A1F2E 100%)", emoji: "⚡" },
  ],
  "데이터": [
    { id: "da-1", title: "데이터로 의사결정하는 회사", subtitle: "데이터 직군 합격 가이드",
      gradient: "linear-gradient(135deg, #2C3D5F 0%, #1A1F2E 100%)", emoji: "📈" },
    { id: "da-2", title: "AI 시대, 데이터의 가치",     subtitle: "데이터 엔지니어/사이언티스트 트렌드",
      gradient: "linear-gradient(135deg, #FB923C 0%, #E37828 100%)", emoji: "🤖" },
    { id: "da-3", title: "분석가가 만드는 임팩트",      subtitle: "최고의 동료와 함께 풀어가는 문제들",
      gradient: "linear-gradient(135deg, #34C5A7 0%, #2BA890 100%)", emoji: "🔍" },
  ],
};

export const companyThemesByJob = {
  "백엔드": [
    { id: "bt-1", title: "AI 백엔드 포지션",       subtitle: "LLM 시대 새로운 백엔드 역할",  bg: "#E4ECFB", icon: "🤖" },
    { id: "bt-2", title: "한 번쯤 도전해보고 싶은", subtitle: "글로벌 TOP 기업",            bg: "#EAF2FE", icon: "🌏" },
    { id: "bt-3", title: "대규모 채용 기업",        subtitle: "성장하는 IT 기업의 백엔드",   bg: "#E5F7F2", icon: "🏢" },
    { id: "bt-4", title: "재택 가능한 회사",        subtitle: "유연하게 일할 수 있는 곳",    bg: "#FFEAF1", icon: "🏠" },
  ],
  "프론트엔드": [
    { id: "ft-1", title: "AI UX 포지션",            subtitle: "AI 제품을 만드는 프론트",     bg: "#E4ECFB", icon: "✨" },
    { id: "ft-2", title: "한 번쯤 도전해보고 싶은", subtitle: "글로벌 TOP 기업",            bg: "#EAF2FE", icon: "🌏" },
    { id: "ft-3", title: "디자인 협업 강한 팀",     subtitle: "디자이너와 가까운 환경",       bg: "#FFEAF1", icon: "🎨" },
    { id: "ft-4", title: "재택 가능한 회사",        subtitle: "유연하게 일할 수 있는 곳",    bg: "#E5F7F2", icon: "🏠" },
  ],
  "데이터": [
    { id: "dt-1", title: "AI/ML 포지션 모아보기",   subtitle: "AI 시대 핵심 데이터 직군",     bg: "#E4ECFB", icon: "🤖" },
    { id: "dt-2", title: "데이터 인프라 팀",        subtitle: "대규모 데이터 처리 경험",      bg: "#FEEFE2", icon: "🛠️" },
    { id: "dt-3", title: "한 번쯤 도전해보고 싶은", subtitle: "글로벌 TOP 기업",            bg: "#EAF2FE", icon: "🌏" },
    { id: "dt-4", title: "재택 가능한 회사",        subtitle: "유연하게 일할 수 있는 곳",    bg: "#E5F7F2", icon: "🏠" },
  ],
};

export const getJobNews = (jobCategoryName) =>
  jobNewsByJob[jobCategoryName] ?? jobNewsByJob["백엔드"];

export const getCompanyThemes = (jobCategoryName) =>
  companyThemesByJob[jobCategoryName] ?? companyThemesByJob["백엔드"];

/* ─────────────────────────────────────────────
 *  IT 기술 트렌드 mock — 직군별 테크 블로그 큐레이션
 *  실제 외부 API 미연동. 추후 기술 블로그 RSS / 크롤링으로 교체 예정.
 * ─────────────────────────────────────────────*/

export const techTrendsByJob = {
  "백엔드": [
    {
      id: "tt-be-1",
      title: "Java 21 Virtual Threads 실전 적용기",
      source: "카카오 기술 블로그",
      description: "Spring Boot 3.2에서 Virtual Threads를 도입해 처리량을 3배 개선한 사례. 마이그레이션 포인트와 주의사항을 정리했습니다.",
      tag: "Java",
      readTime: "5분",
      date: "2026.05.05",
    },
    {
      id: "tt-be-2",
      title: "Redis 캐싱 전략 완벽 가이드",
      source: "우아한형제들 기술 블로그",
      description: "캐시 스탬피드·캐시 미스 대응 전략부터 분산 캐시 설계까지, 실무에서 얻은 노하우를 정리했습니다.",
      tag: "Redis",
      readTime: "8분",
      date: "2026.05.02",
    },
    {
      id: "tt-be-3",
      title: "Kafka 이벤트 드리븐 아키텍처 전환기",
      source: "LINE 기술 블로그",
      description: "동기식 REST 호출 구조에서 Kafka 기반 이벤트 드리븐으로 전환하면서 얻은 교훈들을 공유합니다.",
      tag: "Kafka",
      readTime: "7분",
      date: "2026.04.28",
    },
    {
      id: "tt-be-4",
      title: "PostgreSQL 쿼리 최적화 실전 사례",
      source: "토스 기술 블로그",
      description: "인덱스 설계부터 EXPLAIN ANALYZE 분석까지, 레거시 쿼리를 90% 빠르게 만든 과정을 담았습니다.",
      tag: "Database",
      readTime: "6분",
      date: "2026.04.24",
    },
  ],
  "프론트엔드": [
    {
      id: "tt-fe-1",
      title: "React Server Components 도입 후기",
      source: "카카오 기술 블로그",
      description: "Next.js 15의 RSC를 프로덕션에 도입하며 마주친 트레이드오프와 해결 방법을 솔직하게 공유합니다.",
      tag: "React",
      readTime: "7분",
      date: "2026.05.06",
    },
    {
      id: "tt-fe-2",
      title: "TypeScript 5.5의 새로운 기능들",
      source: "토스 기술 블로그",
      description: "infer 키워드 개선, const 타입 매개변수 등 실무에서 바로 쓸 수 있는 새 기능들을 정리했습니다.",
      tag: "TypeScript",
      readTime: "5분",
      date: "2026.05.03",
    },
    {
      id: "tt-fe-3",
      title: "Vite 6 마이그레이션 가이드",
      source: "우아한형제들 기술 블로그",
      description: "Vite 5에서 6로 업그레이드하면서 겪은 Breaking Changes와 빌드 속도 개선 효과를 정리했습니다.",
      tag: "Vite",
      readTime: "4분",
      date: "2026.04.30",
    },
    {
      id: "tt-fe-4",
      title: "Core Web Vitals 실전 최적화",
      source: "LINE 기술 블로그",
      description: "코드 스플리팅, 이미지 최적화, CLS 방지 기법으로 LCP를 1.2초 단축한 경험을 담았습니다.",
      tag: "Performance",
      readTime: "9분",
      date: "2026.04.25",
    },
  ],
  "데이터": [
    {
      id: "tt-da-1",
      title: "dbt로 데이터 파이프라인 혁신하기",
      source: "카카오 기술 블로그",
      description: "SQL 기반 dbt를 도입해 데이터 변환 로직을 버전 관리하고 테스트 자동화를 구현한 사례를 소개합니다.",
      tag: "dbt",
      readTime: "6분",
      date: "2026.05.04",
    },
    {
      id: "tt-da-2",
      title: "Vector DB 도입 전 꼭 알아야 할 것들",
      source: "토스 기술 블로그",
      description: "Pinecone·Weaviate·pgvector 비교부터 실제 RAG 파이프라인 구축 경험까지 핵심만 정리했습니다.",
      tag: "AI/ML",
      readTime: "8분",
      date: "2026.05.01",
    },
    {
      id: "tt-da-3",
      title: "Apache Spark vs Flink 선택 기준",
      source: "LINE 기술 블로그",
      description: "배치 중심 vs 스트리밍 중심 아키텍처 결정 트리와 실무 선택 가이드를 제공합니다.",
      tag: "Spark",
      readTime: "7분",
      date: "2026.04.27",
    },
    {
      id: "tt-da-4",
      title: "MLOps 파이프라인 구축 실전기",
      source: "우아한형제들 기술 블로그",
      description: "모델 학습부터 서빙·모니터링까지 MLOps 전체 파이프라인을 구축하면서 얻은 인사이트를 담았습니다.",
      tag: "MLOps",
      readTime: "10분",
      date: "2026.04.22",
    },
  ],
};

export const getTechTrends = (jobCategoryName) =>
  techTrendsByJob[jobCategoryName] ?? techTrendsByJob["백엔드"];
