import React, { useEffect, useState } from "react";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminUsers } from "../../api/adminApi.js";
import { useAuth } from "../../context/AuthContext.jsx";

export default function AdminUsers() {
  const { auth } = useAuth();
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch]   = useState("");
  const [changing, setChanging] = useState(null);

  useEffect(() => {
    adminUsers.list()
      .then(setUsers)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const toggleRole = async (user) => {
    if (user.username === auth?.username) {
      alert("자기 자신의 권한은 변경할 수 없습니다.");
      return;
    }
    const newRole = user.role === "ADMIN" ? "USER" : "ADMIN";
    const label   = newRole === "ADMIN" ? "관리자" : "일반 사용자";
    if (!window.confirm(`${user.nickname}님을 ${label}로 변경하시겠습니까?`)) return;

    setChanging(user.id);
    try {
      const updated = await adminUsers.changeRole(user.id, newRole);
      setUsers(prev => prev.map(u => u.id === updated.id ? updated : u));
    } catch (e) {
      alert("권한 변경 실패: " + e.message);
    } finally {
      setChanging(null);
    }
  };

  const filtered = users.filter(u =>
    u.nickname.toLowerCase().includes(search.toLowerCase()) ||
    u.username.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <AdminLayout>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 6 }}>회원 관리</h1>
      <p style={{ color: "var(--gray-500)", marginBottom: 24 }}>전체 {users.length}명</p>

      <input
        type="text"
        placeholder="닉네임 또는 아이디 검색..."
        value={search}
        onChange={e => setSearch(e.target.value)}
        style={{
          width: "100%", padding: "9px 14px", borderRadius: 8,
          border: "1px solid var(--gray-200)", fontSize: 14, outline: "none",
          marginBottom: 20, boxSizing: "border-box",
        }}
      />

      <div style={{ background: "#fff", borderRadius: 12, border: "1px solid var(--gray-200)", overflow: "hidden" }}>
        {loading ? (
          <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
        ) : filtered.length === 0 ? (
          <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>검색 결과가 없습니다.</div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ background: "var(--gray-50)", borderBottom: "1px solid var(--gray-200)" }}>
                {["닉네임", "아이디", "직군", "가입일", "권한", ""].map((h, i) => (
                  <th key={i} style={{
                    padding: "12px 16px", textAlign: "left",
                    fontSize: 12, fontWeight: 600, color: "var(--gray-500)",
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((user, idx) => (
                <tr
                  key={user.id}
                  style={{ borderBottom: idx < filtered.length - 1 ? "1px solid var(--gray-100)" : "none" }}
                >
                  <td style={{ padding: "13px 16px", fontSize: 14, fontWeight: 600 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <div style={{
                        width: 28, height: 28, borderRadius: "50%",
                        background: "linear-gradient(135deg, #3B82F6, #1D4ED8)",
                        display: "flex", alignItems: "center", justifyContent: "center",
                        fontSize: 11, fontWeight: 700, color: "#fff", flexShrink: 0,
                      }}>
                        {user.nickname[0]}
                      </div>
                      {user.nickname}
                      {user.username === auth?.username && (
                        <span style={{ fontSize: 11, color: "#3B82F6", fontWeight: 500 }}>(나)</span>
                      )}
                    </div>
                  </td>
                  <td style={{ padding: "13px 16px", fontSize: 13, color: "var(--gray-600)" }}>
                    {user.username}
                  </td>
                  <td style={{ padding: "13px 16px", fontSize: 13, color: "var(--gray-600)" }}>
                    {user.jobCategoryName}
                  </td>
                  <td style={{ padding: "13px 16px", fontSize: 13, color: "var(--gray-600)" }}>
                    {new Date(user.createdAt).toLocaleDateString("ko-KR")}
                  </td>
                  <td style={{ padding: "13px 16px" }}>
                    <span style={{
                      fontSize: 12, fontWeight: 600, padding: "3px 10px", borderRadius: 20,
                      background: user.role === "ADMIN" ? "#EEF2FF" : "var(--gray-100)",
                      color: user.role === "ADMIN" ? "#4F46E5" : "var(--gray-600)",
                    }}>
                      {user.role === "ADMIN" ? "관리자" : "일반"}
                    </span>
                  </td>
                  <td style={{ padding: "13px 16px", textAlign: "right" }}>
                    <button
                      className="btn btn-ghost btn-sm"
                      onClick={() => toggleRole(user)}
                      disabled={changing === user.id || user.username === auth?.username}
                      style={{ opacity: user.username === auth?.username ? 0.3 : 1 }}
                    >
                      {changing === user.id
                        ? "변경 중..."
                        : user.role === "ADMIN" ? "권한 해제" : "관리자 지정"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </AdminLayout>
  );
}