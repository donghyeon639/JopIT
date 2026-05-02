import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminQuestions, adminUsers, adminCategories } from "../../api/adminApi.js";

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({ questions: 0, users: 0, categories: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      adminQuestions.list().catch(() => []),
      adminUsers.list().catch(() => []),
      adminCategories.list().catch(() => []),
    ]).then(([questions, users, categories]) => {
      setStats({
        questions: questions.length,
        users: users.length,
        categories: categories.length,
      });
    }).finally(() => setLoading(false));
  }, []);

  const CARDS = [
    { label: "전체 문제",    value: stats.questions, unit: "개", path: "/admin/questions", color: "#3B82F6" },
    { label: "전체 회원",    value: stats.users,     unit: "명", path: "/admin/users",     color: "#10B981" },
    { label: "카테고리",     value: stats.categories,unit: "개", path: "/admin/categories",color: "#F59E0B" },
  ];

  const SHORTCUTS = [
    { label: "새 문제 만들기",  path: "/admin/questions/new", desc: "CS 면접 문제를 추가합니다" },
    { label: "카테고리 관리",   path: "/admin/categories",    desc: "직군·세부 분류를 편집합니다" },
    { label: "회원 목록",       path: "/admin/users",          desc: "회원 권한을 관리합니다" },
    { label: "미리보기",        path: "/admin/preview",        desc: "사용자 화면을 미리 봅니다" },
  ];

  return (
    <AdminLayout>
      <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 8 }}>관리자 대시보드</h1>
      <p style={{ color: "var(--gray-500)", marginBottom: 32 }}>PrepHub 콘텐츠와 회원을 관리합니다.</p>

      {/* 통계 카드 */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 20, marginBottom: 40 }}>
        {CARDS.map((card) => (
          <div
            key={card.label}
            onClick={() => navigate(card.path)}
            style={{
              background: "#fff", borderRadius: 12, padding: "24px 28px",
              border: "1px solid var(--gray-200)", cursor: "pointer",
              boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
              transition: "box-shadow 0.2s",
            }}
            onMouseEnter={e => e.currentTarget.style.boxShadow = "0 4px 12px rgba(0,0,0,0.1)"}
            onMouseLeave={e => e.currentTarget.style.boxShadow = "0 1px 3px rgba(0,0,0,0.06)"}
          >
            <div style={{ fontSize: 13, color: "var(--gray-500)", marginBottom: 8 }}>{card.label}</div>
            {loading ? (
              <div style={{ height: 36, background: "var(--gray-100)", borderRadius: 6 }} />
            ) : (
              <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
                <span style={{ fontSize: 36, fontWeight: 700, color: card.color }}>{card.value}</span>
                <span style={{ fontSize: 14, color: "var(--gray-400)" }}>{card.unit}</span>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 바로가기 */}
      <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>바로가기</h2>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 16 }}>
        {SHORTCUTS.map((s) => (
          <div
            key={s.path}
            onClick={() => navigate(s.path)}
            style={{
              background: "#fff", borderRadius: 12, padding: "20px 24px",
              border: "1px solid var(--gray-200)", cursor: "pointer",
              display: "flex", flexDirection: "column", gap: 6,
              transition: "border-color 0.15s",
            }}
            onMouseEnter={e => e.currentTarget.style.borderColor = "#3B82F6"}
            onMouseLeave={e => e.currentTarget.style.borderColor = "var(--gray-200)"}
          >
            <div style={{ fontSize: 15, fontWeight: 600 }}>{s.label}</div>
            <div style={{ fontSize: 13, color: "var(--gray-500)" }}>{s.desc}</div>
          </div>
        ))}
      </div>
    </AdminLayout>
  );
}