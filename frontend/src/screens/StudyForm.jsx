import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { TopNav, IconArrowLeft } from "../components/Components.jsx";
import { studyApi, TECH_STACKS, POSITIONS, MODES } from "../api/studyApi.js";

const TYPES = [
  { id: "STUDY",   label: "스터디",  emoji: "✏️" },
  { id: "PROJECT", label: "프로젝트", emoji: "📁" },
];

/* ── 다중선택 칩 ── */
const ChipMultiSelect = ({ options, value, onChange }) => (
  <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
    {options.map((opt) => {
      const id = typeof opt === "string" ? opt : opt.id;
      const label = typeof opt === "string" ? opt : opt.label;
      const active = value.includes(id);
      return (
        <button key={id} type="button" onClick={() =>
            onChange(active ? value.filter((v) => v !== id) : [...value, id])}
          className="btn btn-sm"
          style={{
            borderRadius: 999, fontWeight: 500,
            border: `1px solid ${active ? "var(--blue-500)" : "var(--gray-300)"}`,
            background: active ? "var(--blue-50)" : "#fff",
            color: active ? "var(--blue-700)" : "var(--gray-700)",
          }}>
          {label}
        </button>
      );
    })}
  </div>
);

const today = () => new Date().toISOString().slice(0, 10);

const StudyForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [form, setForm] = useState({
    type: "STUDY", title: "", summary: "",
    techStacks: [], positions: [],
    mode: "ONLINE", capacity: 4, deadline: today(),
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // 수정 모드: 기존 데이터 로드
  useEffect(() => {
    if (!isEdit) return;
    setLoading(true);
    studyApi.detail(id)
      .then((s) => {
        if (!s) { setError("스터디를 찾을 수 없어요."); return; }
        setForm({
          type: s.type ?? "STUDY",
          title: s.title ?? "",
          summary: s.summary ?? "",
          techStacks: s.techStacks ?? [],
          positions: s.positions ?? [],
          mode: s.mode ?? "ONLINE",
          capacity: s.capacity ?? 4,
          deadline: (s.deadline ?? today()).slice(0, 10),
        });
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const set = (key, val) => setForm((prev) => ({ ...prev, [key]: val }));

  const validate = () => {
    if (!form.title.trim()) return "제목을 입력해주세요.";
    if (form.title.length > 80) return "제목은 80자 이내로 입력해주세요.";
    if (!form.summary.trim()) return "소개 내용을 입력해주세요.";
    if (form.summary.length > 2000) return "소개는 2,000자 이내로 입력해주세요.";
    if (form.techStacks.length === 0) return "기술 스택을 1개 이상 선택해주세요.";
    if (form.positions.length === 0) return "포지션을 1개 이상 선택해주세요.";
    if (form.capacity < 2 || form.capacity > 20) return "정원은 2~20명 사이로 설정해주세요.";
    if (!form.deadline) return "마감일을 선택해주세요.";
    if (new Date(form.deadline) < new Date(today())) return "마감일은 오늘 이후로 설정해주세요.";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const msg = validate();
    if (msg) { setError(msg); return; }
    setError(""); setSaving(true);
    try {
      const payload = {
        type: form.type,
        title: form.title.trim(),
        summary: form.summary.trim(),
        techStacks: form.techStacks,
        positions: form.positions,
        mode: form.mode,
        capacity: Number(form.capacity),
        deadline: form.deadline,
      };
      const saved = isEdit
        ? await studyApi.update(id, payload)
        : await studyApi.create(payload);
      navigate(`/study/${saved?.id ?? id}`);
    } catch (e) {
      setError(e.message || "저장에 실패했어요.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="dp-screen" style={{ minHeight: "100vh", background: "var(--gray-50)" }}>
        <TopNav />
        <div className="dp-page" style={{ maxWidth: 880 }}>
          <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="dp-screen" style={{ minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "24px 48px 80px", maxWidth: 880, margin: "0 auto" }}>
        <div onClick={() => navigate(-1)}
          style={{ display: "inline-flex", alignItems: "center", gap: 6, cursor: "pointer",
                   color: "var(--gray-600)", fontSize: 14, marginBottom: 16 }}>
          <IconArrowLeft size={14} />
          <span>돌아가기</span>
        </div>

        <h1 className="t-h2" style={{ marginBottom: 6 }}>
          {isEdit ? "모집글 수정" : "스터디·프로젝트 모집"}
        </h1>
        <p className="t-body" style={{ fontSize: 14, marginBottom: 24 }}>
          어떤 사람들과 함께 공부·작업하고 싶은지 구체적으로 적어주세요.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="card" style={{ padding: 28, display: "flex", flexDirection: "column", gap: 20 }}>
            {/* 유형 */}
            <Field label="유형">
              <div style={{ display: "flex", gap: 8 }}>
                {TYPES.map((t) => (
                  <button key={t.id} type="button" onClick={() => set("type", t.id)}
                    className="btn"
                    style={{
                      flex: 1, padding: "12px 14px", justifyContent: "center",
                      border: `1px solid ${form.type === t.id ? "var(--blue-500)" : "var(--gray-300)"}`,
                      background: form.type === t.id ? "var(--blue-50)" : "#fff",
                      color: form.type === t.id ? "var(--blue-700)" : "var(--gray-700)",
                      fontWeight: form.type === t.id ? 700 : 500,
                    }}>
                    {t.emoji} {t.label}
                  </button>
                ))}
              </div>
            </Field>

            {/* 제목 */}
            <Field label="제목" hint={`${form.title.length}/80`}>
              <input className="input" value={form.title} maxLength={80}
                placeholder="예: 백엔드 기술면접 스터디 — CS/네트워크/DB 매주 모의면접"
                onChange={(e) => set("title", e.target.value)} />
            </Field>

            {/* 소개 */}
            <Field label="소개" hint={`${form.summary.length}/2000`}>
              <textarea className="input" value={form.summary} maxLength={2000}
                placeholder="진행 방식, 목표, 기대하는 멤버 등을 적어주세요."
                onChange={(e) => set("summary", e.target.value)}
                style={{ minHeight: 140, resize: "vertical", fontFamily: "inherit", lineHeight: 1.6 }} />
            </Field>

            {/* 기술 스택 */}
            <Field label="기술 스택" hint="여러 개 선택 가능">
              <ChipMultiSelect options={TECH_STACKS} value={form.techStacks}
                onChange={(v) => set("techStacks", v)} />
            </Field>

            {/* 포지션 */}
            <Field label="포지션" hint="여러 개 선택 가능">
              <ChipMultiSelect options={POSITIONS} value={form.positions}
                onChange={(v) => set("positions", v)} />
            </Field>

            {/* 진행 방식 / 정원 / 마감일 */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 14 }}>
              <Field label="진행 방식">
                <select className="input" value={form.mode}
                  onChange={(e) => set("mode", e.target.value)}>
                  {MODES.map((m) => <option key={m.id} value={m.id}>{m.label}</option>)}
                </select>
              </Field>
              <Field label="정원">
                <input className="input" type="number" min={2} max={20} value={form.capacity}
                  onChange={(e) => set("capacity", e.target.value)} />
              </Field>
              <Field label="마감일">
                <input className="input" type="date" value={form.deadline} min={today()}
                  onChange={(e) => set("deadline", e.target.value)} />
              </Field>
            </div>

            {error && (
              <div style={{ padding: "10px 14px", background: "var(--red-50)", color: "var(--red-600)",
                            border: "1px solid #FCA5A5", borderRadius: "var(--r-md)", fontSize: 13 }}>
                {error}
              </div>
            )}

            <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 4 }}>
              <button type="button" className="btn btn-outline" onClick={() => navigate(-1)} disabled={saving}>
                취소
              </button>
              <button type="submit" className="btn btn-primary btn-lg" disabled={saving}>
                {saving ? "저장 중..." : isEdit ? "수정 완료" : "모집글 올리기"}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

const Field = ({ label, hint, children }) => (
  <div>
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: 8 }}>
      <label style={{ fontSize: 14, fontWeight: 600, color: "var(--gray-800)" }}>{label}</label>
      {hint && <span className="t-xs">{hint}</span>}
    </div>
    {children}
  </div>
);

export default StudyForm;
