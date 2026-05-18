import React, { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminQuestions } from "../../api/adminApi.js";

const PAGE_SIZE = 20;
const DIFF_LABEL = { LOW: "하", MID: "중", HIGH: "상" };
const DIFF_COLOR = { LOW: "#10B981", MID: "#F59E0B", HIGH: "#EF4444" };

export default function AdminQuestions() {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterDiff, setFilterDiff] = useState("");
  const [deleting, setDeleting] = useState(null);

  // 페이지 상태
  const [page, setPage] = useState(0);
  const [pageMeta, setPageMeta] = useState({
    page: 0, totalPages: 0, totalElements: 0, hasNext: false, hasPrev: false,
  });

  const load = () => {
    setLoading(true);
    adminQuestions.list({
      difficulty: filterDiff || undefined,
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
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  // 필터/페이지 변경 시 재조회
  useEffect(() => { load(); }, [filterDiff, page]);

  const handleFilterDiff = (val) => {
    setFilterDiff(val);
    setPage(0);
  };

  const handleDelete = async (id, title) => {
    if (!window.confirm(`"${title}" 문제를 삭제하시겠습니까?`)) return;
    setDeleting(id);
    try {
      await adminQuestions.remove(id);
      // 마지막 페이지의 마지막 항목을 지웠으면 이전 페이지로
      if (questions.length === 1 && pageMeta.hasPrev) {
        setPage(p => p - 1);
      } else {
        load();
      }
    } catch (e) {
      alert("삭제에 실패했습니다: " + e.message);
    } finally {
      setDeleting(null);
    }
  };

  // 클라이언트 검색 — 현재 페이지 내에서만
  const visible = useMemo(() => {
    if (!search.trim()) return questions;
    const q = search.toLowerCase();
    return questions.filter(item =>
      item.title.toLowerCase().includes(q) ||
      item.questionCategoryName?.toLowerCase().includes(q)
    );
  }, [questions, search]);

  return (
    <AdminLayout>
      {/* 헤더 */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>문제 관리</h1>
          <p style={{ color: "var(--gray-500)", marginTop: 4 }}>
            전체 {pageMeta.totalElements}개
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => navigate("/admin/questions/new")}
        >
          + 새 문제 만들기
        </button>
      </div>

      {/* 필터 */}
      <div style={{ display: "flex", gap: 12, marginBottom: 20 }}>
        <input
          type="text"
          placeholder="현재 페이지에서 검색..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{
            flex: 1, padding: "9px 14px", borderRadius: 8,
            border: "1px solid var(--gray-200)", fontSize: 14, outline: "none",
          }}
        />
        <select
          value={filterDiff}
          onChange={e => handleFilterDiff(e.target.value)}
          style={{
            padding: "9px 14px", borderRadius: 8,
            border: "1px solid var(--gray-200)", fontSize: 14, background: "#fff",
          }}
        >
          <option value="">전체 난이도</option>
          <option value="LOW">하</option>
          <option value="MID">중</option>
          <option value="HIGH">상</option>
        </select>
      </div>

      {/* 테이블 */}
      <div style={{ background: "#fff", borderRadius: 12, border: "1px solid var(--gray-200)", overflow: "hidden" }}>
        {loading ? (
          <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
        ) : visible.length === 0 ? (
          <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
            {search || filterDiff ? "조건에 맞는 문제가 없습니다." : "등록된 문제가 없습니다."}
          </div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ background: "var(--gray-50)", borderBottom: "1px solid var(--gray-200)" }}>
                {["제목", "카테고리", "난이도", ""].map((h, i) => (
                  <th key={i} style={{
                    padding: "12px 16px", textAlign: "left",
                    fontSize: 12, fontWeight: 600, color: "var(--gray-500)",
                    letterSpacing: "0.05em",
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visible.map((q, idx) => (
                <tr
                  key={q.id}
                  style={{
                    borderBottom: idx < visible.length - 1 ? "1px solid var(--gray-100)" : "none",
                    transition: "background 0.1s",
                  }}
                  onMouseEnter={e => e.currentTarget.style.background = "var(--gray-50)"}
                  onMouseLeave={e => e.currentTarget.style.background = "transparent"}
                >
                  <td style={{ padding: "14px 16px", fontSize: 14, fontWeight: 500, maxWidth: 360 }}>
                    <div style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {q.title}
                    </div>
                  </td>
                  <td style={{ padding: "14px 16px", fontSize: 13, color: "var(--gray-600)" }}>
                    {q.questionCategoryName}
                  </td>
                  <td style={{ padding: "14px 16px" }}>
                    <span style={{
                      fontSize: 12, fontWeight: 600, padding: "3px 10px", borderRadius: 20,
                      background: DIFF_COLOR[q.difficulty] + "20",
                      color: DIFF_COLOR[q.difficulty],
                    }}>
                      {DIFF_LABEL[q.difficulty]}
                    </span>
                  </td>
                  <td style={{ padding: "14px 16px", textAlign: "right" }}>
                    <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
                      <button
                        className="btn btn-ghost btn-sm"
                        onClick={() => navigate(`/admin/questions/${q.id}`)}
                      >
                        수정
                      </button>
                      <button
                        className="btn btn-sm"
                        style={{ color: "#EF4444", background: "#FEF2F2", border: "1px solid #FECACA" }}
                        onClick={() => handleDelete(q.id, q.title)}
                        disabled={deleting === q.id}
                      >
                        {deleting === q.id ? "삭제 중..." : "삭제"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* 페이지네이션 */}
      {pageMeta.totalPages > 1 && (
        <div style={{
          display: "flex", justifyContent: "center", alignItems: "center",
          gap: 4, marginTop: 20,
        }}>
          <button
            className="btn btn-sm"
            disabled={!pageMeta.hasPrev}
            onClick={() => setPage(p => Math.max(0, p - 1))}
          >
            이전
          </button>
          <span style={{ padding: "0 14px", fontSize: 13, color: "var(--gray-600)" }}>
            {pageMeta.page + 1} / {pageMeta.totalPages}
          </span>
          <button
            className="btn btn-sm"
            disabled={!pageMeta.hasNext}
            onClick={() => setPage(p => p + 1)}
          >
            다음
          </button>
        </div>
      )}
    </AdminLayout>
  );
}