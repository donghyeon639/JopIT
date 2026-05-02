import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import Logo from "../common/Logo.jsx";

const NAV_ITEMS = [
  { label: "대시보드",   path: "/admin",            icon: "⊞" },
  { label: "문제 관리",  path: "/admin/questions",  icon: "❓" },
  { label: "카테고리",   path: "/admin/categories", icon: "🗂" },
  { label: "회원 관리",  path: "/admin/users",       icon: "👥" },
  { label: "미리보기",   path: "/admin/preview",     icon: "👁" },
];

export default function AdminLayout({ children }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { auth, logout } = useAuth();

  const isActive = (path) =>
    path === "/admin" ? pathname === "/admin" : pathname.startsWith(path);

  return (
    <div style={{ display: "flex", minHeight: "100vh", background: "var(--gray-50)" }}>
      {/* 사이드바 */}
      <aside style={{
        width: 240,
        background: "#0F172A",
        color: "#fff",
        display: "flex",
        flexDirection: "column",
        flexShrink: 0,
        position: "fixed",
        top: 0,
        left: 0,
        height: "100vh",
        zIndex: 100,
      }}>
        {/* 로고 영역 */}
        <div style={{ padding: "20px 20px 16px", borderBottom: "1px solid #1E293B" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
            <Logo />
          </div>
          <span style={{
            fontSize: 11, color: "#64748B", fontWeight: 600,
            letterSpacing: "0.08em", textTransform: "uppercase",
          }}>
            Admin Console
          </span>
        </div>

        {/* 네비게이션 */}
        <nav style={{ flex: 1, padding: "12px 0", overflowY: "auto" }}>
          {NAV_ITEMS.map((item) => (
            <div
              key={item.path}
              onClick={() => navigate(item.path)}
              style={{
                display: "flex", alignItems: "center", gap: 10,
                padding: "10px 20px", cursor: "pointer",
                fontSize: 14, fontWeight: isActive(item.path) ? 600 : 400,
                color: isActive(item.path) ? "#fff" : "#94A3B8",
                background: isActive(item.path) ? "#1E293B" : "transparent",
                borderLeft: isActive(item.path) ? "3px solid #3B82F6" : "3px solid transparent",
                transition: "all 0.15s",
              }}
            >
              <span style={{ fontSize: 16 }}>{item.icon}</span>
              {item.label}
            </div>
          ))}
        </nav>

        {/* 하단 프로필 */}
        <div style={{
          padding: "16px 20px",
          borderTop: "1px solid #1E293B",
          display: "flex", alignItems: "center", gap: 10,
        }}>
          <div style={{
            width: 32, height: 32, borderRadius: "50%",
            background: "linear-gradient(135deg, #3B82F6, #1D4ED8)",
            display: "flex", alignItems: "center", justifyContent: "center",
            fontSize: 13, fontWeight: 700, color: "#fff", flexShrink: 0,
          }}>
            {auth?.nickname?.[0] ?? "A"}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: "#fff", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {auth?.nickname}
            </div>
            <div style={{ fontSize: 11, color: "#64748B" }}>관리자</div>
          </div>
          <button
            onClick={logout}
            style={{
              background: "none", border: "none", color: "#64748B",
              cursor: "pointer", fontSize: 12, padding: "4px 0",
            }}
          >
            로그아웃
          </button>
        </div>
      </aside>

      {/* 본문 */}
      <main style={{ flex: 1, marginLeft: 240, padding: 32, minHeight: "100vh" }}>
        {children}
      </main>
    </div>
  );
}