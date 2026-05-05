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
