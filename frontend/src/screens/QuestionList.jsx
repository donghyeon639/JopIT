import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  IconChevronRight, IconCheck, IconSearch,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { questionApi, categoryApi } from "../api/questionApi.js";
import { useAuth } from "../context/AuthContext.jsx";

const PAGE_SIZE = 10;
const DIFF_OPTIONS = ["전체", "LOW", "MID", "HIGH"];
const DIFF_LABEL   = { LOW: "하", MID: "중", HIGH: "상" };

const QuestionList = () => {
  const navigate   = useNavigate();
  const { isLoggedIn } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const urlQuery = searchParams.get("q") ?? "";

  const [categories,   setCategories]   = useState([]);
  const [questions,    setQuestions]    = useState([]);
  const [myAnswerQIds, setMyAnswerQIds] = useState(new Set());
  const [activeCatId,  setActiveCatId]  = useState(null);   // null = 전체
  const [activeDiff,   setActiveDiff]   = useState("전체");
  const [searchInput,  setSearchInput]  = useState(urlQuery);  // 입력창 상태(타이핑 중)
  const [loading,      setLoading]      = useState(true);
  const [fetchError,   setFetchError]   = useState(false);

  // 페이지 상태
  const [page, setPage] = useState(0);
  const [pageMeta, setPageMeta] = useState({
    page: 0, totalPages: 0, totalElements: 0, hasNext: false, hasPrev: false,
  });

  // 카테고리 목록 (한 번만)
  useEffect(() => {
    categoryApi.list().then(setCategories).catch(console.error);
  }, []);

  // URL의 q가 외부에서 바뀌면 (TopNav 검색 등) 입력칸에도 반영
  useEffect(() => {
    setSearchInput(urlQuery);
    setPage(0);
  }, [urlQuery]);

  // 문제 목록 (필터/검색어/페이지 변경 시 서버 검색 재조회)
  useEffect(() => {
    setLoading(true);
    setFetchError(false);
    questionApi.list({
      categoryId: activeCatId || undefined,
      difficulty: activeDiff === "전체" ? undefined : activeDiff,
      q: urlQuery || undefined,
      page,
      size: PAGE_SIZE,
    })
      .then(res => {
        setQuestions(res.content || []);
        setPageMeta({
          page: res.page,
          totalPages: res.totalPages,
          totalElements: res.totalElements,
          hasNext: res.hasNext,
          hasPrev: res.hasPrev,
        });
      })
      .catch(() => setFetchError(true))
      .finally(() => setLoading(false));
  }, [activeCatId, activeDiff, urlQuery, page]);

  // 내가 푼 문제 ID 세트 (로그인한 경우만, 첫 로드 시)
  useEffect(() => {
    if (!isLoggedIn) return;
    questionApi.myAnswers()
      .then(answers => setMyAnswerQIds(new Set(answers.map(a => a.questionId))))
      .catch(() => {});
  }, [isLoggedIn]);

  // 필터 변경 시 page=0으로 리셋
  const handleCategoryChange = (id) => {
    setActiveCatId(id);
    setPage(0);
  };
  const handleDifficultyChange = (d) => {
    setActiveDiff(d);
    setPage(0);
  };

  // 검색 — Enter 또는 검색 아이콘 클릭 시 URL의 q 업데이트 → 서버 재조회
  const applySearch = () => {
    const next = searchInput.trim();
    const params = new URLSearchParams(searchParams);
    if (next) params.set("q", next);
    else params.delete("q");
    setSearchParams(params);
  };

  const visibleQuestions = questions;

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        {/* 헤더 */}
        <div style={{ marginBottom: 24 }}>
          <h1 className="t-h2" style={{ marginBottom: 6 }}>문제 풀어보기</h1>
          <p className="t-body" style={{ fontSize: 14 }}>
            직군별 기술 면접 문제를 풀어보세요.
          </p>
        </div>

        {/* 직군 탭 */}
        <div style={{ display: "flex", gap: 4, marginBottom: 20, borderBottom: "1px solid var(--gray-200)" }}>
          <div
            onClick={() => handleCategoryChange(null)}
            style={tabStyle(activeCatId === null)}
          >
            전체
          </div>
          {categories.map(cat => (
            <div
              key={cat.id}
              onClick={() => handleCategoryChange(cat.id)}
              style={tabStyle(activeCatId === cat.id)}
            >
              {cat.name}
            </div>
          ))}
        </div>

        {/* 필터 바 */}
        <div className="card" style={{ padding: 20, marginBottom: 16 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 14 }}>
            <div style={{ flex: 1, position: "relative" }}>
              <div
                onClick={applySearch}
                style={{
                  position: "absolute", left: 14, top: "50%", transform: "translateY(-50%)",
                  color: "var(--gray-400)", cursor: "pointer",
                }}
                aria-label="검색 실행"
              >
                <IconSearch size={16} />
              </div>
              <input
                className="input"
                style={{ paddingLeft: 40 }}
                placeholder="문제 제목으로 검색 (Enter)"
                value={searchInput}
                onChange={e => setSearchInput(e.target.value)}
                onKeyDown={e => { if (e.key === "Enter") applySearch(); }}
              />
            </div>
          </div>

          <div style={{ display: "flex", gap: 6, flexWrap: "wrap", alignItems: "center" }}>
            <span className="t-xs" style={{ alignSelf: "center", marginRight: 6, fontWeight: 600, color: "var(--gray-700)" }}>
              난이도
            </span>
            {DIFF_OPTIONS.map(d => (
              <Pill key={d} active={activeDiff === d} onClick={() => handleDifficultyChange(d)}>
                {d === "전체" ? "전체" : DIFF_LABEL[d]}
              </Pill>
            ))}
            <div style={{ flex: 1 }} />
            <span className="t-xs">
              총 <b style={{ color: "var(--gray-900)" }}>{pageMeta.totalElements}</b>개 문제
            </span>
          </div>
        </div>

        {/* 문제 목록 */}
        <div className="card" style={{ overflow: "hidden" }}>
          <div style={{
            display: "grid",
            gridTemplateColumns: "60px 1fr 120px 100px 60px",
            padding: "14px 20px",
            background: "var(--gray-50)",
            borderBottom: "1px solid var(--gray-200)",
            fontSize: 12, fontWeight: 600, color: "var(--gray-500)"
          }}>
            <div>상태</div>
            <div>문제</div>
            <div>카테고리</div>
            <div>난이도</div>
            <div></div>
          </div>

          {loading ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
          ) : fetchError ? (
            <div style={{ padding: 60, textAlign: "center", color: "#DC2626" }}>
              문제를 불러오지 못했습니다. 서버 연결을 확인해주세요.
            </div>
          ) : visibleQuestions.length === 0 ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
              {urlQuery
                ? `"${urlQuery}" 검색 결과가 없습니다.`
                : "조건에 맞는 문제가 없습니다."}
            </div>
          ) : (
            visibleQuestions.map((q, i) => {
              const solved = myAnswerQIds.has(q.id);
              return (
                <div
                  key={q.id}
                  className="row-hover"
                  onClick={() => navigate(`/solve?id=${q.id}`)}
                  style={{
                    display: "grid",
                    gridTemplateColumns: "60px 1fr 120px 100px 60px",
                    padding: "16px 20px", alignItems: "center",
                    borderBottom: i === visibleQuestions.length - 1 ? "none" : "1px solid var(--gray-100)",
                    cursor: "pointer"
                  }}
                >
                  <div>
                    {solved ? (
                      <div style={{
                        width: 22, height: 22, borderRadius: 999,
                        background: "var(--green-50)", color: "var(--green-700)",
                        display: "flex", alignItems: "center", justifyContent: "center"
                      }}>
                        <IconCheck size={12} />
                      </div>
                    ) : (
                      <div style={{ width: 22, height: 22, borderRadius: 999, border: "1.5px solid var(--gray-300)" }} />
                    )}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4, color: solved ? "var(--gray-500)" : "var(--gray-900)" }}>
                      {q.title}
                    </div>
                  </div>
                  <div><CategoryBadge name={q.questionCategoryName} /></div>
                  <div><DifficultyBadge level={q.difficulty?.toLowerCase()} /></div>
                  <div style={{ textAlign: "right" }}>
                    <IconChevronRight size={16} stroke="var(--gray-400)" />
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* 페이지네이션 */}
        <Pagination
          page={pageMeta.page}
          totalPages={pageMeta.totalPages}
          hasPrev={pageMeta.hasPrev}
          hasNext={pageMeta.hasNext}
          onChange={(next) => {
            setPage(next);
            window.scrollTo({ top: 0, behavior: "smooth" });
          }}
        />
      </div>
    </div>
  );
};

const tabStyle = (active) => ({
  padding: "12px 18px", fontSize: 14, fontWeight: 600, cursor: "pointer", marginBottom: -1,
  borderBottom: "2px solid",
  borderColor: active ? "var(--blue-600)" : "transparent",
  color: active ? "var(--blue-700)" : "var(--gray-500)",
});

const Pill = ({ children, active, onClick }) => (
  <div onClick={onClick} style={{
    padding: "6px 12px", fontSize: 13, fontWeight: 500, cursor: "pointer",
    borderRadius: 999,
    background: active ? "var(--gray-900)" : "var(--gray-100)",
    color: active ? "#fff" : "var(--gray-700)",
    transition: "all 0.15s"
  }}>{children}</div>
);

// 5개 윈도우. 현재 페이지가 중앙에 오도록, 양 끝 페이지에선 한쪽으로 치우침.
const PAGE_WINDOW_SIZE = 5;
function getPageWindow(page, totalPages) {
  if (totalPages <= 0) return [];
  if (totalPages <= PAGE_WINDOW_SIZE) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }
  const half = Math.floor(PAGE_WINDOW_SIZE / 2);
  let start = page - half;
  let end = page + half;
  if (start < 0) { end -= start; start = 0; }
  if (end > totalPages - 1) { start -= (end - (totalPages - 1)); end = totalPages - 1; }
  start = Math.max(0, start);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
}

const Pagination = ({ page, totalPages, hasPrev, hasNext, onChange }) => {
  if (totalPages <= 1) return null;
  const window = getPageWindow(page, totalPages);
  const isFirst = page === 0;
  const isLast = page === totalPages - 1;
  return (
    <div style={{
      display: "flex", justifyContent: "center", alignItems: "center",
      gap: 6, marginTop: 24,
    }}>
      <NavButton disabled={isFirst} onClick={() => onChange(0)} aria-label="첫 페이지">
        <IconDoubleChevron direction="left" />
      </NavButton>
      <NavButton disabled={!hasPrev} onClick={() => onChange(page - 1)} aria-label="이전 페이지">
        <IconChevron direction="left" />
      </NavButton>

      <div style={{
        display: "flex", alignItems: "center", gap: 2,
        padding: "4px 8px", borderRadius: 999, background: "var(--gray-100)",
      }}>
        {window.map(p => (
          <PageNumberButton key={p} active={p === page} onClick={() => onChange(p)}>
            {p + 1}
          </PageNumberButton>
        ))}
      </div>

      <NavButton disabled={!hasNext} onClick={() => onChange(page + 1)} aria-label="다음 페이지">
        <IconChevron direction="right" />
      </NavButton>
      <NavButton disabled={isLast} onClick={() => onChange(totalPages - 1)} aria-label="마지막 페이지">
        <IconDoubleChevron direction="right" />
      </NavButton>
    </div>
  );
};

const NavButton = ({ children, disabled, onClick, ...rest }) => (
  <button
    {...rest}
    disabled={disabled}
    onClick={onClick}
    style={{
      width: 36, height: 36, padding: 0,
      borderRadius: 999, border: "none",
      background: "var(--gray-100)",
      color: disabled ? "var(--gray-300)" : "var(--gray-700)",
      display: "flex", alignItems: "center", justifyContent: "center",
      cursor: disabled ? "not-allowed" : "pointer",
      transition: "all 0.15s",
    }}
  >
    {children}
  </button>
);

const PageNumberButton = ({ children, active, onClick }) => (
  <button
    onClick={onClick}
    style={{
      minWidth: 32, height: 32, padding: "0 10px",
      borderRadius: 999, border: "none",
      background: active ? "var(--gray-900)" : "transparent",
      color: active ? "#fff" : "var(--gray-700)",
      fontSize: 13, fontWeight: 600,
      cursor: "pointer",
      transition: "all 0.15s",
    }}
  >
    {children}
  </button>
);

const IconChevron = ({ direction = "right", size = 16 }) => (
  <svg
    width={size} height={size} viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ transform: direction === "left" ? "rotate(180deg)" : "none" }}
  >
    <polyline points="9 18 15 12 9 6" />
  </svg>
);

const IconDoubleChevron = ({ direction = "right", size = 16 }) => (
  <svg
    width={size} height={size} viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ transform: direction === "left" ? "rotate(180deg)" : "none" }}
  >
    <polyline points="6 18 12 12 6 6" />
    <polyline points="13 18 19 12 13 6" />
  </svg>
);

export default QuestionList;
export { Pill };