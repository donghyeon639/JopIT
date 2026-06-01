import { http } from "./client.js";

/**
 * 스터디 모집 API 클라이언트.
 * 백엔드 EPIC-8 (#128~#131) 와 동일 계약을 사용한다.
 */

function buildStudyQuery({ type, techStack, position, mode, recruitingOnly, bookmarkOnly, q, page = 0, size = 12 } = {}) {
  const params = new URLSearchParams();
  if (type && type !== "all") params.set("type", String(type).toUpperCase()); // StudyType enum (STUDY/PROJECT)
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

/** Spring `Page<T>` 응답에서 본문 배열만 추출. 현재 UI는 단순 목록만 다룬다(페이지네이션 미사용). */
const unwrap = (page) => (page && Array.isArray(page.content) ? page.content : page);

export const studyApi = {
  list:           (opts = {}) => http.get(`/studies?${buildStudyQuery(opts)}`).then(unwrap),
  popular:        ()          => http.get(`/studies/popular`),
  detail:         (id)        => http.get(`/studies/${id}`),
  create:         (body)      => http.post(`/studies`, body),
  update:         (id, body)  => http.put(`/studies/${id}`, body),
  close:          (id)        => http.patch(`/studies/${id}/close`),
  toggleBookmark: (id)        => http.post(`/studies/${id}/bookmark`, {}),
  apply:          (id, body)  => http.post(`/studies/${id}/apply`, body ?? {}),
  myBookmarks:    ()          => http.get(`/studies/bookmarks`).then(unwrap),
};

// ===== UI 선택지 상수 (백엔드와 무관, 화이트리스트로 화면에서만 사용) =====
export const TECH_STACKS = ["JavaScript", "React", "Spring", "Java", "Python", "TypeScript", "Node.js", "CS"];
export const POSITIONS = ["프론트엔드", "백엔드", "데이터", "기획자", "디자이너"];
export const MODES = [
  { id: "ONLINE", label: "온라인" },
  { id: "OFFLINE", label: "오프라인" },
  { id: "HYBRID", label: "온/오프라인" },
];
