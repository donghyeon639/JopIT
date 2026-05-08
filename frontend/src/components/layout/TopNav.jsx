import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import Logo from "../common/Logo.jsx";
import { IconSearch, IconBell } from "../icons/index.jsx";

const NAV_LINKS = [
  { label: "홈",      path: "/dashboard" },
  { label: "문제",    path: "/questions" },
  { label: "커뮤니티", path: "/community" },
  { label: "면접 후기", path: "/reviews"   },
];

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
  const menuRef = useRef(null);

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

  return (
    <div className="dp-nav">
      <div className="dp-nav-left">
        <Logo />
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
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        {isLoggedIn ? (
          <>
            {/* 검색바 */}
            <div style={{
              display: "flex", alignItems: "center", gap: 8,
              padding: "8px 12px", background: "var(--gray-50)",
              border: "1px solid var(--gray-200)", borderRadius: 8,
              width: 280, color: "var(--gray-500)", fontSize: 13, cursor: "pointer"
            }}>
              <IconSearch size={16} />
              <span>문제, 키워드, 회사를 검색해보세요</span>
              <span style={{ marginLeft: "auto", fontSize: 11, padding: "1px 6px",
                background: "#fff", border: "1px solid var(--gray-200)",
                borderRadius: 4, fontFamily: "var(--font-mono)" }}>⌘K</span>
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
          <div style={{ display: "flex", gap: 8 }}>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate("/login")}>
              로그인
            </button>
            <button className="btn btn-primary btn-sm" onClick={() => navigate("/signup")}>
              무료 시작
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default TopNav;