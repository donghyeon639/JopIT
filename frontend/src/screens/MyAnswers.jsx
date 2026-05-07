import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconArrowLeft, IconArrowRight, IconSpark, IconChevronRight,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { questionApi } from "../api/questionApi.js";

const FILTERS = [
  { key: "ALL",     label: "전체" },
  { key: "DONE",    label: "피드백 완료" },
  { key: "PENDING", label: "피드백 진행중" },
  { key: "NONE",    label: "피드백 미요청" },
  { key: "FAILED",  label: "피드백 실패" },
];

const STATUS_META = {
  DONE:    { text: "피드백 완료",    bg: "#ECFDF5", color: "#047857" },
  PENDING: { text: "피드백 진행중",  bg: "#FFFBEB", color: "#B45309" },
  NONE:    { text: "피드백 미요청",  bg: "#F3F4F6", color: "#4B5563" },
  FAILED:  { text: "피드백 실패",    bg: "#FEF2F2", color: "#B91C1C" },
};

const formatDate = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
};

const MyAnswers = () => {
  const navigate = useNavigate();
  const [answers, setAnswers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");

  useEffect(() => {
    setLoading(true);
    setError(false);
    questionApi.myAnswers()
      .then(setAnswers)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const counts = useMemo(() => {
    const c = { ALL: answers.length, DONE: 0, PENDING: 0, NONE: 0, FAILED: 0 };
    answers.forEach(a => { c[a.feedbackStatus] = (c[a.feedbackStatus] || 0) + 1; });
    return c;
  }, [answers]);

  const filtered = useMemo(() => {
    return answers.filter(a => {
      const matchStatus = filter === "ALL" || a.feedbackStatus === filter;
      const q = search.trim().toLowerCase();
      const matchSearch = !q
        || a.questionTitle?.toLowerCase().includes(q)
        || a.questionCategoryName?.toLowerCase().includes(q);
      return matchStatus && matchSearch;
    });
  }, [answers, filter, search]);

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <span onClick={() => navigate("/dashboard")} className="t-sm"
                style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
            <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 대시보드로
          </span>
          <button className="btn btn-outline btn-sm" onClick={() => navigate("/questions")}>
            새 문제 풀기 <IconArrowRight size={14} />
          </button>
        </div>
      </div>

      <div style={{ maxWidth: 1080, margin: "0 auto", padding: "32px 48px 80px" }}>
        <div style={{ marginBottom: 20 }}>
          <h1 className="t-h2" style={{ marginBottom: 6 }}>내 답변</h1>
          <p className="t-body" style={{ fontSize: 14 }}>
            지금까지 작성한 답변과 AI 피드백 결과를 한눈에 확인할 수 있어요.
          </p>
        </div>

        {/* 필터 */}
        <div className="card" style={{ padding: 16, marginBottom: 16 }}>
          <div style={{ display: "flex", gap: 6, flexWrap: "wrap", marginBottom: 12 }}>
            {FILTERS.map(f => (
              <FilterPill
                key={f.key}
                active={filter === f.key}
                onClick={() => setFilter(f.key)}>
                {f.label}
                <span style={{
                  marginLeft: 6,
                  fontSize: 11,
                  padding: "1px 6px",
                  borderRadius: 999,
                  background: filter === f.key ? "rgba(255,255,255,0.25)" : "var(--gray-200)",
                  color: filter === f.key ? "#fff" : "var(--gray-600)",
                }}>
                  {counts[f.key] ?? 0}
                </span>
              </FilterPill>
            ))}
          </div>
          <input
            className="input"
            placeholder="문제 제목, 카테고리로 검색"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        {loading ? (
          <EmptyCard text="불러오는 중..." />
        ) : error ? (
          <EmptyCard text="답변을 불러오지 못했습니다. 잠시 후 다시 시도해주세요." color="#DC2626" />
        ) : answers.length === 0 ? (
          <div className="card" style={{ padding: 60, textAlign: "center" }}>
            <div style={{ fontSize: 36, marginBottom: 10 }}>📝</div>
            <div className="t-h3" style={{ marginBottom: 6 }}>아직 작성한 답변이 없어요</div>
            <p className="t-body" style={{ fontSize: 14, marginBottom: 20 }}>
              첫 문제를 풀고 답변을 작성해보세요.
            </p>
            <button className="btn btn-primary" onClick={() => navigate("/questions")}>
              문제 풀러가기 <IconArrowRight size={14} />
            </button>
          </div>
        ) : filtered.length === 0 ? (
          <EmptyCard text="조건에 맞는 답변이 없습니다." />
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            {filtered.map(a => (
              <AnswerCard key={a.id} answer={a} onClick={() => navigate(`/answer?id=${a.id}`)} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

const AnswerCard = ({ answer, onClick }) => {
  const status = STATUS_META[answer.feedbackStatus] || STATUS_META.NONE;
  const preview = (answer.content || "").slice(0, 120);

  return (
    <div className="card row-hover" onClick={onClick}
         style={{ padding: 22, cursor: "pointer", display: "flex", gap: 16, alignItems: "flex-start" }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 8, flexWrap: "wrap" }}>
          <CategoryBadge name={answer.questionCategoryName} />
          <DifficultyBadge level={answer.questionDifficulty?.toLowerCase()} />
          <span style={{
            fontSize: 11, fontWeight: 600,
            padding: "2px 8px", borderRadius: 999,
            background: status.bg, color: status.color
          }}>
            {answer.feedbackStatus === "DONE" && <IconSpark size={10} style={{ verticalAlign: -1, marginRight: 3 }} />}
            {status.text}
          </span>
          <span style={{ fontSize: 12, color: "var(--gray-400)" }}>· {formatDate(answer.createdAt)}</span>
        </div>
        <div style={{ fontSize: 15, fontWeight: 700, color: "var(--gray-900)", marginBottom: 6,
                      overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
          {answer.questionTitle}
        </div>
        <div style={{ fontSize: 13, color: "var(--gray-600)", lineHeight: 1.55,
                      display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
                      overflow: "hidden" }}>
          {preview}{answer.content?.length > 120 ? "…" : ""}
        </div>
      </div>
      <IconChevronRight size={18} stroke="var(--gray-400)" />
    </div>
  );
};

const FilterPill = ({ active, onClick, children }) => (
  <button onClick={onClick} style={{
    padding: "6px 12px", fontSize: 13, fontWeight: 500, cursor: "pointer",
    borderRadius: 999, border: "none",
    background: active ? "var(--gray-900)" : "var(--gray-100)",
    color: active ? "#fff" : "var(--gray-700)",
    fontFamily: "inherit",
    display: "inline-flex", alignItems: "center"
  }}>{children}</button>
);

const EmptyCard = ({ text, color }) => (
  <div className="card" style={{ padding: 60, textAlign: "center", color: color ?? "var(--gray-400)" }}>
    {text}
  </div>
);

export default MyAnswers;