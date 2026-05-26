import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import Logo from "../common/Logo.jsx";
import { IconSearch, IconBell } from "../icons/index.jsx";
import { questionApi } from "../../api/questionApi.js";

const NAV_LINKS = [
  { label: "홈",      path: "/dashboard" },
  { label: "문제",    path: "/questions" },
  { label: "커뮤니티", path: "/community" },
  { label: "면접 후기", path: "/reviews"   },
];

/**
 * 전역 검색이 매칭할 수 있는 페이지 카탈로그.
 * keywords는 사용자가 입력할 만한 동의어/유사어.
 */
const PAGE_CATALOG = [
  { label: "문제 풀기",     path: "/questions",  description: "직군별 기술면접 문제",
    keywords: ["문제", "기술면접", "cs", "풀기", "questions"] },
  { label: "이력서 첨삭",   path: "/resume",     description: "AI가 이력서를 첨삭",
    keywords: ["이력서", "첨삭", "resume", "cv"] },
  { label: "내 답변",       path: "/my/answers", description: "내가 작성한 답변 모음",
    keywords: ["내답변", "답변", "내가 푼", "my answers"] },
  { label: "학습 현황",     path: "/my/status",  description: "내 풀이 통계와 연속 학습",
    keywords: ["학습", "현황", "통계", "stats"] },
  { label: "커뮤니티",      path: "/community",  description: "다른 사람의 답변 보기",
    keywords: ["커뮤니티", "피드", "community", "다른사람"] },
  { label: "면접 후기",     path: "/reviews",    description: "기업별 면접 후기",
    keywords: ["면접 후기", "후기", "review"] },
  { label: "레벨 체크",     path: "/levelcheck", description: "내 실력 레벨 측정",
    keywords: ["레벨", "체크", "level"] },
];

function matchPages(query) {
  const q = query.trim().toLowerCase();
  if (!q) return [];
  return PAGE_CATALOG.filter(p =>
    p.label.toLowerCase().includes(q) ||
    p.description.toLowerCase().includes(q) ||
    p.keywords.some(k => k.toLowerCase().includes(q))
  ).slice(0, 4);
}

const MenuItem = ({ label, onClick, danger }) => (
  <div
    onClick={onClick}
    style={{
      padding: "9px 16px",
      fontSize: 13,
      fontWeight: 500,
      color: danger ? "#DC2626" : "#3D434C",
      cursor: "pointer",
      transition: "background 0.12s",
    }}
    onMouseEnter={e => e.currentTarget.style.background = danger ? "#FEF2F2" : "#F7F9FF"}
    onMouseLeave={e => e.currentTarget.style.background = "transparent"}
  >
    {label}
  </div>
);

const TopNav = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { isLoggedIn, isAdmin, auth, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [searchInput, setSearchInput] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [questionResults, setQuestionResults] = useState([]);
  const [questionLoading, setQuestionLoading] = useState(false);
  const menuRef = useRef(null);
  const searchBoxRef = useRef(null);

  const trimmed = searchInput.trim();
  const pageMatches = matchPages(trimmed);

  const submitSearch = () => {
    if (trimmed) {
      navigate(`/questions?q=${encodeURIComponent(trimmed)}`);
    } else {
      navigate("/questions");
    }
    setSearchOpen(false);
  };

  // 검색어 변경 시 debounce로 서버에서 문제 검색
  useEffect(() => {
    if (!trimmed) {
      setQuestionResults([]);
      setQuestionLoading(false);
      return;
    }
    setQuestionLoading(true);
    const handle = setTimeout(() => {
      questionApi.list({ q: trimmed, size: 5 })
        .then(res => setQuestionResults(res.content || []))
        .catch(() => setQuestionResults([]))
        .finally(() => setQuestionLoading(false));
    }, 200);
    return () => clearTimeout(handle);
  }, [trimmed]);

  // 외부 클릭 / ESC로 드롭다운 닫기
  useEffect(() => {
    if (!searchOpen) return;
    const onMouseDown = (e) => {
      if (searchBoxRef.current && !searchBoxRef.current.contains(e.target)) {
        setSearchOpen(false);
      }
    };
    const onKey = (e) => { if (e.key === "Escape") setSearchOpen(false); };
    document.addEventListener("mousedown", onMouseDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onMouseDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [searchOpen]);

  // 라우트가 바뀌면 드롭다운 닫기
  useEffect(() => { setSearchOpen(false); }, [pathname]);

  const goSearchTarget = (path) => {
    setSearchOpen(false);
    navigate(path);
  };

  const isActive = (path) => {
    if (path === "/questions") return ["/questions", "/solve", "/feedback"].includes(pathname);
    return pathname === path;
  };

  const avatarLetter = auth?.nickname ? auth.nickname[0].toUpperCase() : "?";

  // 드롭다운 바깥 클릭 시 닫기
  useEffect(() => {
    if (!menuOpen) return;
    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [menuOpen]);

  // 라우트 이동 시 모바일 드로어 자동 닫기
  useEffect(() => { setDrawerOpen(false); }, [pathname]);

  // 드로어 열렸을 땐 body 스크롤 잠금
  useEffect(() => {
    if (!drawerOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.body.style.overflow = prev; };
  }, [drawerOpen]);

  const goAndClose = (path) => { setDrawerOpen(false); navigate(path); };

  return (
    <>
      <div className="dp-nav">
        <div className="dp-nav-left">
          <Logo />
        </div>

        <div className="dp-nav-links">
          {NAV_LINKS.map(l => (
            <div key={l.path}
                 className={"dp-nav-link " + (isActive(l.path) ? "active" : "")}
                 onClick={() => navigate(l.path)}>
              {l.label}
            </div>
          ))}
          {isAdmin && (
            <div
              className={"dp-nav-link " + (pathname.startsWith("/admin") ? "active" : "")}
              onClick={() => navigate("/admin")}
              style={{ color: pathname.startsWith("/admin") ? undefined : "#6366F1" }}
            >
              관리자
            </div>
          )}
        </div>

        <div className="dp-nav-right">
          {isLoggedIn ? (
            <>
              {/* 검색바 — pill 스타일 + 드롭다운 */}
              <div ref={searchBoxRef} className="dp-nav-search-wrap" role="search">
                <div className="dp-nav-search-pill">
                  <span
                    onClick={submitSearch}
                    style={{ display: "inline-flex", cursor: "pointer", color: "#9BA3B2" }}
                    aria-label="검색"
                  >
                    <IconSearch size={18} />
                  </span>
                  <input
                    type="search"
                    className="dp-nav-search-input"
                    placeholder="문제·이력서 첨삭·커뮤니티 검색"
                    value={searchInput}
                    onChange={(e) => { setSearchInput(e.target.value); setSearchOpen(true); }}
                    onFocus={() => setSearchOpen(true)}
                    onKeyDown={(e) => { if (e.key === "Enter") submitSearch(); }}
                  />
                </div>

                {searchOpen && trimmed && (
                  <div className="dp-nav-search-dropdown">
                    {/* 페이지 매칭 섹션 */}
                    {pageMatches.length > 0 && (
                      <div className="dp-search-section">
                        <div className="dp-search-section-title">페이지</div>
                        {pageMatches.map(p => (
                          <div
                            key={p.path}
                            className="dp-search-row"
                            onClick={() => goSearchTarget(p.path)}
                          >
                            <div className="dp-search-row-title">{p.label}</div>
                            <div className="dp-search-row-desc">{p.description}</div>
                          </div>
                        ))}
                      </div>
                    )}

                    {/* 문제 매칭 섹션 */}
                    <div className="dp-search-section">
                      <div className="dp-search-section-title">문제</div>
                      {questionLoading ? (
                        <div className="dp-search-empty">검색 중...</div>
                      ) : questionResults.length === 0 ? (
                        <div className="dp-search-empty">
                          "{trimmed}" 관련 문제가 없습니다.
                        </div>
                      ) : (
                        <>
                          {questionResults.map(q => (
                            <div
                              key={q.id}
                              className="dp-search-row"
                              onClick={() => goSearchTarget(`/solve?id=${q.id}`)}
                            >
                              <div className="dp-search-row-title">{q.title}</div>
                              <div className="dp-search-row-desc">
                                {q.questionCategoryName ?? "문제"} · {q.difficulty ?? ""}
                              </div>
                            </div>
                          ))}
                          <div
                            className="dp-search-row dp-search-row-more"
                            onClick={submitSearch}
                          >
                            "{trimmed}" 검색 결과 전체 보기 →
                          </div>
                        </>
                      )}
                    </div>

                    {pageMatches.length === 0 && questionResults.length === 0 && !questionLoading && (
                      <div className="dp-search-empty-global">
                        매칭되는 페이지나 문제가 없습니다. Enter로 문제 검색 페이지 이동.
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* 알림 */}
              <div style={{ width: 36, height: 36, borderRadius: 999,
                background: "var(--gray-100)", display: "flex",
                alignItems: "center", justifyContent: "center", cursor: "pointer" }}>
                <IconBell size={18} />
              </div>

              {/* 아바타 + 드롭다운 */}
              <div ref={menuRef} style={{ position: "relative" }}>
                <div
                  className="avatar"
                  style={{
                    background: "linear-gradient(135deg, #3B82F6, #1D4ED8)",
                    cursor: "pointer",
                    outline: menuOpen ? "2px solid #93C5FD" : "none",
                    outlineOffset: 2,
                  }}
                  onClick={() => setMenuOpen(prev => !prev)}
                >
                  {avatarLetter}
                </div>

                {menuOpen && (
                  <div style={{
                    position: "absolute", top: "calc(100% + 8px)", right: 0,
                    width: 200,
                    background: "#fff",
                    border: "1px solid #ECEEF2",
                    borderRadius: 12,
                    boxShadow: "0 8px 24px rgba(0,0,0,0.10)",
                    overflow: "hidden",
                    zIndex: 1000,
                  }}>
                    {/* 프로필 헤더 */}
                    <div style={{ padding: "14px 16px", borderBottom: "1px solid #F3F4F6" }}>
                      <div style={{ fontSize: 14, fontWeight: 700, color: "#1A1F2E" }}>
                        {auth?.nickname}
                      </div>
                      <div style={{ fontSize: 12, color: "#9BA3B2", marginTop: 2 }}>
                        {auth?.jobCategoryName ? `${auth.jobCategoryName} 개발자` : "직군 미설정"}
                      </div>
                    </div>

                    {/* 메뉴 항목 */}
                    <div style={{ padding: "6px 0" }}>
                      <MenuItem label="내 답변" onClick={() => { navigate("/my/answers"); setMenuOpen(false); }} />
                      <MenuItem label="학습 현황" onClick={() => { navigate("/my/status"); setMenuOpen(false); }} />
                    </div>

                    <div style={{ borderTop: "1px solid #F3F4F6", padding: "6px 0" }}>
                      <MenuItem
                        label="로그아웃"
                        danger
                        onClick={() => { logout(); setMenuOpen(false); navigate("/"); }}
                      />
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="dp-nav-auth-buttons" style={{ gap: 8 }}>
              <button className="btn btn-ghost btn-sm" onClick={() => navigate("/login")}>
                로그인
              </button>
              <button className="btn btn-primary btn-sm" onClick={() => navigate("/signup")}>
                무료 시작
              </button>
            </div>
          )}

          {/* 햄버거 — 모바일에서만 노출 (CSS로 제어) */}
          <button
            type="button"
            aria-label="메뉴"
            aria-expanded={drawerOpen}
            className={"dp-nav-burger " + (drawerOpen ? "is-open" : "")}
            onClick={() => setDrawerOpen(o => !o)}
          >
            <span />
          </button>
        </div>
      </div>

      {/* 모바일 드로어 (CSS로 768px 이하에서만 노출) */}
      {drawerOpen && (
        <>
          <div
            onClick={() => setDrawerOpen(false)}
            style={{
              position: "fixed", inset: 0, background: "rgba(16,24,40,0.4)",
              zIndex: 900,
            }}
          />
          <nav
            className="dp-nav-mobile-drawer is-open"
            style={{
              position: "fixed",
              top: 56, left: 0, right: 0,
              background: "#fff",
              borderBottom: "1px solid var(--gray-200)",
              boxShadow: "0 8px 24px rgba(16,24,40,0.08)",
              zIndex: 950,
              padding: "8px 0",
              maxHeight: "calc(100vh - 56px)",
              overflowY: "auto",
            }}
          >
            {NAV_LINKS.map(l => (
              <div
                key={l.path}
                onClick={() => goAndClose(l.path)}
                style={{
                  padding: "14px 20px",
                  fontSize: 15,
                  fontWeight: 600,
                  color: isActive(l.path) ? "var(--blue-600)" : "var(--gray-800)",
                  background: isActive(l.path) ? "var(--blue-50)" : "transparent",
                  cursor: "pointer",
                  borderLeft: isActive(l.path) ? "3px solid var(--blue-600)" : "3px solid transparent",
                }}
              >
                {l.label}
              </div>
            ))}
            {isAdmin && (
              <div
                onClick={() => goAndClose("/admin")}
                style={{
                  padding: "14px 20px", fontSize: 15, fontWeight: 600,
                  color: pathname.startsWith("/admin") ? "var(--blue-600)" : "#6366F1",
                  background: pathname.startsWith("/admin") ? "var(--blue-50)" : "transparent",
                  cursor: "pointer",
                  borderLeft: pathname.startsWith("/admin")
                    ? "3px solid var(--blue-600)"
                    : "3px solid transparent",
                }}
              >
                관리자
              </div>
            )}

            <div style={{ height: 1, background: "var(--gray-100)", margin: "8px 0" }} />

            {isLoggedIn ? (
              <>
                <div
                  onClick={() => goAndClose("/my/answers")}
                  style={{ padding: "12px 20px", fontSize: 14, color: "var(--gray-700)", cursor: "pointer" }}
                >
                  내 답변
                </div>
                <div
                  onClick={() => goAndClose("/my/status")}
                  style={{ padding: "12px 20px", fontSize: 14, color: "var(--gray-700)", cursor: "pointer" }}
                >
                  학습 현황
                </div>
                <div
                  onClick={() => { logout(); setDrawerOpen(false); navigate("/"); }}
                  style={{ padding: "12px 20px", fontSize: 14, color: "#DC2626", cursor: "pointer" }}
                >
                  로그아웃
                </div>
              </>
            ) : (
              <div style={{ display: "flex", gap: 8, padding: "12px 20px" }}>
                <button className="btn btn-outline btn-sm" style={{ flex: 1 }} onClick={() => goAndClose("/login")}>
                  로그인
                </button>
                <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => goAndClose("/signup")}>
                  무료 시작
                </button>
              </div>
            )}
          </nav>
        </>
      )}
    </>
  );
};

export default TopNav;