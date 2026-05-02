import React, { useEffect, useState } from "react";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminQuestions, adminQuestionCategories } from "../../api/adminApi.js";

const DIFF_LABEL = { LOW: "하", MID: "중", HIGH: "상" };
const DIFF_COLOR = { LOW: "#10B981", MID: "#F59E0B", HIGH: "#EF4444" };

export default function AdminPreview() {
  const [questions, setQuestions]   = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCat, setSelectedCat] = useState("");
  const [filterDiff, setFilterDiff] = useState("");
  const [selected, setSelected]     = useState(null);
  const [loading, setLoading]       = useState(true);

  useEffect(() => {
    Promise.all([
      adminQuestions.list().catch(() => []),
      adminQuestionCategories.list().catch(() => []),
    ]).then(([qs, cats]) => {
      setQuestions(qs);
      setCategories(cats);
    }).finally(() => setLoading(false));
  }, []);

  const filtered = questions.filter(q => {
    const matchCat  = !selectedCat  || q.questionCategoryId === selectedCat;
    const matchDiff = !filterDiff   || q.difficulty === filterDiff;
    return matchCat && matchDiff;
  });

  return (
    <AdminLayout>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 6 }}>미리보기</h1>
      <p style={{ color: "var(--gray-500)", marginBottom: 24 }}>사용자 화면에서 문제가 어떻게 보이는지 확인합니다.</p>

      {/* 필터 */}
      <div style={{ display: "flex", gap: 12, marginBottom: 24 }}>
        <select
          value={selectedCat}
          onChange={e => setSelectedCat(e.target.value)}
          style={{ padding: "9px 14px", borderRadius: 8, border: "1px solid var(--gray-200)", fontSize: 14, background: "#fff" }}
        >
          <option value="">전체 카테고리</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select
          value={filterDiff}
          onChange={e => setFilterDiff(e.target.value)}
          style={{ padding: "9px 14px", borderRadius: 8, border: "1px solid var(--gray-200)", fontSize: 14, background: "#fff" }}
        >
          <option value="">전체 난이도</option>
          <option value="LOW">하</option>
          <option value="MID">중</option>
          <option value="HIGH">상</option>
        </select>
        <div style={{ marginLeft: "auto", padding: "9px 0", fontSize: 13, color: "var(--gray-500)" }}>
          {filtered.length}개 표시 중
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: selected ? "1fr 400px" : "1fr", gap: 20, alignItems: "start" }}>
        {/* 문제 카드 그리드 */}
        <div>
          {loading ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
          ) : filtered.length === 0 ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>해당하는 문제가 없습니다.</div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {filtered.map(q => (
                <div
                  key={q.id}
                  onClick={() => setSelected(selected?.id === q.id ? null : q)}
                  style={{
                    background: "#fff", borderRadius: 12, padding: "16px 20px",
                    border: `1px solid ${selected?.id === q.id ? "#3B82F6" : "var(--gray-200)"}`,
                    cursor: "pointer", transition: "all 0.15s",
                    boxShadow: selected?.id === q.id ? "0 0 0 3px #DBEAFE" : "none",
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                    <span style={{
                      fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 20,
                      background: DIFF_COLOR[q.difficulty] + "20",
                      color: DIFF_COLOR[q.difficulty],
                    }}>
                      {DIFF_LABEL[q.difficulty]}
                    </span>
                    <span style={{ fontSize: 12, color: "var(--gray-500)" }}>{q.questionCategoryName}</span>
                  </div>
                  <div style={{ fontSize: 15, fontWeight: 500, marginTop: 8 }}>{q.title}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 상세 미리보기 */}
        {selected && (
          <div style={{
            background: "#fff", borderRadius: 12, border: "1px solid var(--gray-200)",
            padding: "20px 22px", position: "sticky", top: 20,
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: "var(--gray-700)" }}>상세 미리보기</div>
              <button
                onClick={() => setSelected(null)}
                style={{ background: "none", border: "none", cursor: "pointer", fontSize: 18, color: "var(--gray-400)" }}
              >
                ×
              </button>
            </div>

            <div style={{ display: "flex", gap: 6, marginBottom: 12 }}>
              <span style={{
                fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 20,
                background: DIFF_COLOR[selected.difficulty] + "20",
                color: DIFF_COLOR[selected.difficulty],
              }}>
                {DIFF_LABEL[selected.difficulty]}
              </span>
              <span style={{ fontSize: 12, padding: "2px 8px", borderRadius: 20, background: "var(--gray-100)", color: "var(--gray-600)" }}>
                {selected.questionCategoryName}
              </span>
            </div>

            <h2 style={{ fontSize: 16, fontWeight: 700, lineHeight: 1.5, marginBottom: 16 }}>
              {selected.title}
            </h2>

            <DetailBlock label="모범 답안" content={selected.modelAnswer} />
          </div>
        )}
      </div>
    </AdminLayout>
  );
}

function DetailBlock({ label, content }) {
  if (!content) return null;
  return (
    <div>
      <div style={{ fontSize: 12, fontWeight: 600, color: "var(--gray-500)", marginBottom: 6, letterSpacing: "0.05em" }}>
        {label.toUpperCase()}
      </div>
      <div style={{
        background: "var(--gray-50)", borderRadius: 8, padding: "12px 14px",
        fontSize: 13, lineHeight: 1.7, color: "var(--gray-700)", whiteSpace: "pre-wrap",
      }}>
        {content}
      </div>
    </div>
  );
}