import React from "react";
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

const TopNav = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { isLoggedIn, isAdmin, auth, logout } = useAuth();

  const isActive = (path) => {
    if (path === "/questions") return ["/questions", "/solve", "/feedback"].includes(pathname);
    return pathname === path;
  };

  const avatarLetter = auth?.nickname ? auth.nickname[0] : "?";

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
            <div style={{ width: 36, height: 36, borderRadius: 999,
              background: "var(--gray-100)", display: "flex",
              alignItems: "center", justifyContent: "center", cursor: "pointer" }}>
              <IconBell size={18} />
            </div>
            <div className="avatar"
                 style={{ background: "linear-gradient(135deg, #3B82F6, #1D4ED8)", cursor: "pointer" }}
                 title={auth?.nickname}
                 onClick={logout}>
              {avatarLetter}
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