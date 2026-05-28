import { http } from "./client.js";

/**
 * 스터디 모집 API 클라이언트.
 * 백엔드(EPIC-8 / FEATURE-8-1·8-2)가 아직 없으므로 USE_MOCK=true 동안 mock 데이터로 동작한다.
 * 백엔드 연동 시 USE_MOCK=false 로 바꾸면 동일 시그니처로 실제 API를 호출한다.
 */
const USE_MOCK = true;

function buildStudyQuery({ type, techStack, position, mode, recruitingOnly, bookmarkOnly, q, page = 0, size = 12 } = {}) {
  const params = new URLSearchParams();
  if (type && type !== "all") params.set("type", type);
  if (techStack) params.set("techStack", techStack);
  if (position) params.set("position", position);
  if (mode) params.set("mode", mode);
  if (recruitingOnly) params.set("recruitingOnly", "true");
  if (bookmarkOnly) params.set("bookmarkOnly", "true");
  if (q && q.trim()) params.set("q", q.trim());
  params.set("page", page);
  params.set("size", size);
  return params.toString();
}

const realApi = {
  list:           (opts = {}) => http.get(`/studies?${buildStudyQuery(opts)}`),
  popular:        ()          => http.get(`/studies/popular`),
  detail:         (id)        => http.get(`/studies/${id}`),
  create:         (body)      => http.post(`/studies`, body),
  update:         (id, body)  => http.put(`/studies/${id}`, body),
  close:          (id)        => http.post(`/studies/${id}/close`, {}),
  toggleBookmark: (id)        => http.post(`/studies/${id}/bookmark`, {}),
  apply:          (id, body)  => http.post(`/studies/${id}/apply`, body),
};

const mockApi = {
  list:           (opts = {}) => Promise.resolve(filterMock(opts)),
  popular:        ()          => Promise.resolve([...MOCK_STUDIES].sort((a, b) => b.viewCount - a.viewCount).slice(0, 6)),
  detail:         (id)        => {
    const s = MOCK_STUDIES.find((x) => String(x.id) === String(id));
    if (s) s.viewCount += 1;
    return Promise.resolve(s ? { ...s } : null);
  },
  create:         (body)      => {
    const created = {
      id: Date.now(), viewCount: 0, applied: 0, status: "RECRUITING",
      bookmarked: false, createdAt: new Date().toISOString(), ...body,
    };
    MOCK_STUDIES.unshift(created);
    return Promise.resolve(created);
  },
  update:         (id, body)  => {
    const s = MOCK_STUDIES.find((x) => String(x.id) === String(id));
    if (s) Object.assign(s, body);
    return Promise.resolve(s ? { ...s } : null);
  },
  close:          (id)        => {
    const s = MOCK_STUDIES.find((x) => String(x.id) === String(id));
    if (s) s.status = "CLOSED";
    return Promise.resolve({ status: "CLOSED" });
  },
  toggleBookmark: (id)        => {
    const s = MOCK_STUDIES.find((x) => String(x.id) === String(id));
    if (s) s.bookmarked = !s.bookmarked;
    return Promise.resolve({ bookmarked: s?.bookmarked ?? false });
  },
  apply:          ()          => Promise.resolve({ status: "PENDING" }),
};

function filterMock({ type = "all", techStack, position, mode, recruitingOnly, bookmarkOnly, q } = {}) {
  let list = [...MOCK_STUDIES];
  if (type && type !== "all") list = list.filter((s) => s.type === type);
  if (techStack) list = list.filter((s) => s.techStacks.includes(techStack));
  if (position) list = list.filter((s) => s.positions.includes(position));
  if (mode) list = list.filter((s) => s.mode === mode);
  if (recruitingOnly) list = list.filter((s) => s.status === "RECRUITING");
  if (bookmarkOnly) list = list.filter((s) => s.bookmarked);
  if (q && q.trim()) {
    const kw = q.trim().toLowerCase();
    list = list.filter((s) => s.title.toLowerCase().includes(kw) || s.summary.toLowerCase().includes(kw));
  }
  return list;
}

export const studyApi = USE_MOCK ? mockApi : realApi;

// ===== Mock 데이터 (백엔드 연동 시 제거) =====
export const TECH_STACKS = ["JavaScript", "React", "Spring", "Java", "Python", "TypeScript", "Node.js", "CS"];
export const POSITIONS = ["프론트엔드", "백엔드", "데이터", "기획자", "디자이너"];
export const MODES = [
  { id: "ONLINE", label: "온라인" },
  { id: "OFFLINE", label: "오프라인" },
  { id: "HYBRID", label: "온/오프라인" },
];

export const MOCK_STUDIES = [
  {
    id: 1, type: "study", title: "백엔드 기술면접 스터디 — CS/네트워크/DB 매주 모의면접",
    summary: "운영체제·네트워크·데이터베이스 핵심 질문으로 매주 실전 모의면접을 진행합니다.",
    techStacks: ["Java", "Spring", "CS"], positions: ["백엔드"], mode: "ONLINE",
    capacity: 6, applied: 3, deadline: "2026-06-09", status: "RECRUITING",
    viewCount: 352, author: "면접왕", createdAt: "2026-05-27T09:00:00Z", bookmarked: false,
  },
  {
    id: 2, type: "study", title: "프론트엔드 CS + React 심화 스터디원 모집",
    summary: "브라우저 동작 원리, 렌더링, React 내부 동작을 함께 깊게 파봅니다.",
    techStacks: ["React", "JavaScript", "TypeScript"], positions: ["프론트엔드"], mode: "HYBRID",
    capacity: 5, applied: 2, deadline: "2026-06-07", status: "RECRUITING",
    viewCount: 142, author: "리액트러", createdAt: "2026-05-26T12:00:00Z", bookmarked: true,
  },
  {
    id: 3, type: "study", title: "알고리즘 코딩테스트 데일리 스터디 (Python)",
    summary: "매일 2문제, 주 1회 풀이 리뷰. 카카오/네이버 코테 대비반.",
    techStacks: ["Python", "CS"], positions: ["백엔드", "프론트엔드"], mode: "ONLINE",
    capacity: 8, applied: 7, deadline: "2026-06-05", status: "RECRUITING",
    viewCount: 473, author: "코테마스터", createdAt: "2026-05-28T01:00:00Z", bookmarked: false,
  },
  {
    id: 4, type: "study", title: "데이터 직군 면접 대비 — SQL/통계/ML 개념 정리",
    summary: "데이터 분석가/엔지니어 면접 빈출 개념을 함께 정리하고 발표합니다.",
    techStacks: ["Python", "CS"], positions: ["데이터"], mode: "OFFLINE",
    capacity: 4, applied: 1, deadline: "2026-06-20", status: "RECRUITING",
    viewCount: 98, author: "데이터덕후", createdAt: "2026-05-25T08:00:00Z", bookmarked: false,
  },
  {
    id: 5, type: "project", title: "[사이드프로젝트] 취준생 면접 준비 플랫폼 함께 만들 팀원",
    summary: "Spring + React 기반 서비스. 기획/디자인/개발 전 직군 환영합니다.",
    techStacks: ["Spring", "React", "Java"], positions: ["백엔드", "프론트엔드", "디자이너"], mode: "HYBRID",
    capacity: 6, applied: 4, deadline: "2026-06-30", status: "RECRUITING",
    viewCount: 510, author: "팀빌더", createdAt: "2026-05-24T10:00:00Z", bookmarked: false,
  },
  {
    id: 6, type: "study", title: "Node.js 백엔드 면접 스터디 — 비동기/이벤트루프 집중",
    summary: "Node 런타임 내부와 비동기 처리, 실무 트러블슈팅 경험을 공유합니다.",
    techStacks: ["Node.js", "JavaScript", "CS"], positions: ["백엔드"], mode: "ONLINE",
    capacity: 5, applied: 5, deadline: "2026-06-03", status: "CLOSED",
    viewCount: 221, author: "노드러버", createdAt: "2026-05-20T11:00:00Z", bookmarked: true,
  },
  {
    id: 7, type: "study", title: "신입 개발자 인성/직무 면접 스터디 (전 직군)",
    summary: "자기소개·지원동기·갈등경험 등 인성 면접을 모의로 연습합니다.",
    techStacks: ["CS"], positions: ["프론트엔드", "백엔드", "기획자"], mode: "ONLINE",
    capacity: 10, applied: 6, deadline: "2026-06-15", status: "RECRUITING",
    viewCount: 287, author: "인성장인", createdAt: "2026-05-28T03:00:00Z", bookmarked: false,
  },
  {
    id: 8, type: "project", title: "[공모전] 관광데이터 기반 무장애 여행 코스 플래너 팀원 모집",
    summary: "프론트/백엔드/디자이너를 찾습니다. 데이터 활용 공모전 참가.",
    techStacks: ["React", "Spring"], positions: ["프론트엔드", "백엔드", "디자이너"], mode: "HYBRID",
    capacity: 5, applied: 2, deadline: "2026-06-10", status: "RECRUITING",
    viewCount: 463, author: "여행가즈아", createdAt: "2026-05-23T09:00:00Z", bookmarked: false,
  },
];