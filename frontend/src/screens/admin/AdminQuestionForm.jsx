import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminQuestions, adminQuestionCategories } from "../../api/adminApi.js";

const DIFFICULTIES = [
  { value: "LOW",  label: "하 (기본)" },
  { value: "MID",  label: "중 (응용)" },
  { value: "HIGH", label: "상 (심화)" },
];

export default function AdminQuestionForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [form, setForm] = useState({
    title: "",
    hint: "",
    modelAnswer: "",
    difficulty: "MID",
    questionCategoryId: "",
  });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // 질문 카테고리 목록 로드
  useEffect(() => {
    adminQuestionCategories.list().then(setCategories).catch(console.error);
  }, []);

  // 수정 모드: 기존 데이터 로드
  useEffect(() => {
    if (!isEdit) return;
    setLoading(true);
    adminQuestions.detail(id)
      .then((q) => {
        setForm({
          title: q.title ?? "",
          hint: q.hint ?? "",
          modelAnswer: q.modelAnswer ?? "",
          difficulty: q.difficulty ?? "MID",
          questionCategoryId: q.questionCategoryId ?? "",
        });
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const set = (key, val) => setForm(prev => ({ ...prev, [key]: val }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.title.trim()) { setError("문제 제목을 입력해주세요."); return; }
    if (!form.questionCategoryId) { setError("카테고리를 선택해주세요."); return; }
    if (!form.modelAnswer.trim()) { setError("모범 답안을 입력해주세요."); return; }

    setSaving(true);
    try {
      const payload = {
        title: form.title.trim(),
        hint: form.hint.trim() || null,
        modelAnswer: form.modelAnswer.trim(),
        difficulty: form.difficulty,
        questionCategoryId: form.questionCategoryId,
      };
      if (isEdit) {
        await adminQuestions.update(id, payload);
      } else {
        await adminQuestions.create(payload);
      }
      navigate("/admin/questions");
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      {/* 헤더 */}
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 32 }}>
        <button
          onClick={() => navigate("/admin/questions")}
          style={{ background: "none", border: "none", cursor: "pointer", fontSize: 20, color: "var(--gray-500)", padding: 0 }}
        >
          ←
        </button>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>{isEdit ? "문제 수정" : "새 문제 만들기"}</h1>
          <p style={{ color: "var(--gray-500)", marginTop: 2 }}>
            {isEdit ? "기존 문제를 편집합니다." : "CS 면접 문제를 등록합니다."}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: 24, alignItems: "start" }}>
          {/* 왼쪽: 메인 입력 */}
          <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            {/* 문제 제목 */}
            <div style={cardStyle}>
              <Label>문제 제목 *</Label>
              <input
                type="text"
                value={form.title}
                onChange={e => set("title", e.target.value)}
                placeholder="예: HTTP와 HTTPS의 차이점을 설명하세요."
                style={inputStyle}
                maxLength={500}
              />
              <div style={{ fontSize: 12, color: "var(--gray-400)", textAlign: "right", marginTop: 4 }}>
                {form.title.length} / 500
              </div>
            </div>

            {/* 힌트 */}
            <div style={cardStyle}>
              <Label>힌트 <span style={{ color: "var(--gray-400)", fontWeight: 400 }}>(선택)</span></Label>
              <textarea
                value={form.hint}
                onChange={e => set("hint", e.target.value)}
                placeholder="풀이에 도움이 되는 힌트를 입력하세요."
                rows={3}
                style={{ ...inputStyle, resize: "vertical" }}
              />
            </div>

            {/* 모범 답안 */}
            <div style={cardStyle}>
              <Label>모범 답안 *</Label>
              <textarea
                value={form.modelAnswer}
                onChange={e => set("modelAnswer", e.target.value)}
                placeholder="이 문제의 핵심 답변 내용을 작성하세요. AI 피드백의 기준이 됩니다."
                rows={10}
                style={{ ...inputStyle, resize: "vertical", fontFamily: "var(--font-mono)", fontSize: 13 }}
              />
            </div>
          </div>

          {/* 오른쪽: 설정 패널 */}
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            {/* 난이도 */}
            <div style={cardStyle}>
              <Label>난이도 *</Label>
              <div style={{ display: "flex", gap: 8 }}>
                {DIFFICULTIES.map(d => (
                  <button
                    key={d.value}
                    type="button"
                    onClick={() => set("difficulty", d.value)}
                    style={{
                      flex: 1, padding: "8px 0", borderRadius: 8, border: "1px solid",
                      borderColor: form.difficulty === d.value ? "#3B82F6" : "var(--gray-200)",
                      background: form.difficulty === d.value ? "#EFF6FF" : "#fff",
                      color: form.difficulty === d.value ? "#3B82F6" : "var(--gray-600)",
                      fontWeight: form.difficulty === d.value ? 600 : 400,
                      fontSize: 13, cursor: "pointer", transition: "all 0.15s",
                    }}
                  >
                    {d.label.split(" ")[0]}
                  </button>
                ))}
              </div>
            </div>

            {/* 카테고리 선택 */}
            <div style={cardStyle}>
              <Label>카테고리 *</Label>
              <select
                value={form.questionCategoryId}
                onChange={e => set("questionCategoryId", e.target.value)}
                style={{ ...inputStyle, background: "#fff" }}
              >
                <option value="">카테고리 선택...</option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
              {categories.length === 0 && (
                <p style={{ fontSize: 12, color: "var(--gray-400)", marginTop: 6 }}>
                  먼저 카테고리 관리에서 카테고리를 등록하세요.
                </p>
              )}
            </div>

            {/* 에러 */}
            {error && (
              <div style={{
                padding: "12px 14px", background: "#FEF2F2", border: "1px solid #FECACA",
                borderRadius: 8, fontSize: 13, color: "#DC2626",
              }}>
                {error}
              </div>
            )}

            {/* 버튼 */}
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving}
                style={{ width: "100%", justifyContent: "center" }}
              >
                {saving ? "저장 중..." : isEdit ? "수정 저장" : "문제 등록"}
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => navigate("/admin/questions")}
                style={{ width: "100%", justifyContent: "center" }}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      </form>
    </AdminLayout>
  );
}

const cardStyle = {
  background: "#fff",
  borderRadius: 12,
  border: "1px solid var(--gray-200)",
  padding: "16px 18px",
};

const inputStyle = {
  width: "100%",
  padding: "9px 12px",
  borderRadius: 8,
  border: "1px solid var(--gray-200)",
  fontSize: 14,
  outline: "none",
  boxSizing: "border-box",
};

function Label({ children }) {
  return (
    <div style={{ fontSize: 13, fontWeight: 600, color: "var(--gray-700)", marginBottom: 8 }}>
      {children}
    </div>
  );
}