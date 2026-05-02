import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  IconSpark, IconHeart, IconArrowLeft, IconUser,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { PrepCoach } from "../components/PrepBot.jsx";
import { questionApi } from "../api/questionApi.js";
import { useAuth } from "../context/AuthContext.jsx";

const Solve = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const questionId = searchParams.get("id");
  const { isLoggedIn } = useAuth();

  const [question, setQuestion] = useState(null);
  const [otherAnswers, setOtherAnswers] = useState([]);
  const [tab, setTab] = useState("write");
  const [answer, setAnswer] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!questionId) { navigate("/questions"); return; }
    setLoading(true);
    questionApi.detail(questionId)
      .then(setQuestion)
      .catch(() => navigate("/questions"))
      .finally(() => setLoading(false));
  }, [questionId]);

  useEffect(() => {
    if (!questionId) return;
    questionApi.listAnswers(questionId)
      .then(setOtherAnswers)
      .catch(() => {});
  }, [questionId]);

  const handleSubmit = async () => {
    if (!isLoggedIn) { setError("로그인이 필요합니다."); return; }
    if (!answer.trim()) { setError("답변을 작성해주세요."); return; }
    setError("");
    setSubmitting(true);
    try {
      const created = await questionApi.createAnswer(questionId, answer.trim());
      await questionApi.requestFeedback(created.id);
      navigate(`/feedback?answerId=${created.id}`);
    } catch (e) {
      setError(e.message || "오류가 발생했습니다.");
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
        <TopNav />
        <div style={{ padding: 80, textAlign: "center", color: "var(--gray-400)" }}>문제를 불러오는 중...</div>
      </div>
    );
  }

  const diffLevel = question?.difficulty?.toLowerCase();

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div className="t-sm" style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span onClick={() => navigate("/questions")}
                  style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
              <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 문제 목록
            </span>
            {question?.questionCategoryName && (
              <>
                <span style={{ color: "var(--gray-300)" }}>/</span>
                <span>{question.questionCategoryName}</span>
              </>
            )}
          </div>
        </div>
      </div>

      <div style={{ maxWidth: 1280, margin: "0 auto", padding: "32px 48px 80px",
                    display: "grid", gridTemplateColumns: "1fr 1.1fr", gap: 24 }}>

        {/* Left: question */}
        <div>
          <div className="card" style={{ padding: 28, marginBottom: 16, position: "sticky", top: 24 }}>
            <div style={{ display: "flex", gap: 6, marginBottom: 14 }}>
              <DifficultyBadge level={diffLevel} />
              <CategoryBadge name={question?.questionCategoryName} />
            </div>
            <div className="t-h2" style={{ marginBottom: 16 }}>
              {question?.title}
            </div>

            {question?.hint && (
              <div style={{ marginBottom: 16 }}>
                <PrepCoach
                  expression="teach"
                  size={70}
                  title="프렙쌤의 힌트"
                  message={question.hint}
                  layout="card"
                />
              </div>
            )}

            <div style={{ display: "flex", justifyContent: "space-between", padding: "12px 0",
                           borderTop: "1px solid var(--gray-100)", fontSize: 13, color: "var(--gray-600)" }}>
              <span><IconUser size={13} style={{ verticalAlign: -2 }} /> {otherAnswers.length}명이 풀었어요</span>
            </div>
          </div>
        </div>

        {/* Right: tabs */}
        <div>
          <div style={{ display: "flex", gap: 4, marginBottom: 16 }}>
            {[
              { id: "write", label: "내 답변 작성" },
              { id: "others", label: `다른 사람 답변 (${otherAnswers.length})` }
            ].map(t => (
              <div key={t.id} onClick={() => setTab(t.id)}
                   style={{
                     padding: "10px 18px", fontSize: 14, fontWeight: 600,
                     cursor: "pointer", borderRadius: 10,
                     background: tab === t.id ? "#fff" : "transparent",
                     color: tab === t.id ? "var(--gray-900)" : "var(--gray-500)",
                     border: tab === t.id ? "1px solid var(--gray-200)" : "1px solid transparent",
                     boxShadow: tab === t.id ? "var(--shadow-sm)" : "none"
                   }}>{t.label}</div>
            ))}
          </div>

          {tab === "write" && (
            <div className="card" style={{ padding: 0, overflow: "hidden" }}>
              <div style={{ padding: "10px 16px", borderBottom: "1px solid var(--gray-200)",
                             display: "flex", alignItems: "center", gap: 6 }}>
                <div className="t-xs">{answer.length}자</div>
              </div>
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="답변을 작성해주세요..."
                style={{
                  width: "100%", minHeight: 320, padding: 20, border: "none",
                  outline: "none", resize: "vertical", fontFamily: "inherit",
                  fontSize: 14, lineHeight: 1.7, color: "var(--gray-800)",
                  boxSizing: "border-box"
                }}
              />
              <div style={{ padding: "14px 20px", background: "var(--gray-50)",
                             borderTop: "1px solid var(--gray-200)",
                             display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                {error ? (
                  <span style={{ fontSize: 13, color: "#DC2626" }}>{error}</span>
                ) : (
                  <span />
                )}
                <button className="btn btn-primary" onClick={handleSubmit} disabled={submitting}>
                  <IconSpark size={14} /> {submitting ? "제출 중..." : "AI 첨삭 받기"}
                </button>
              </div>
            </div>
          )}

          {tab === "others" && (
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              {otherAnswers.length === 0 ? (
                <div className="card" style={{ padding: 40, textAlign: "center", color: "var(--gray-400)" }}>
                  아직 작성된 답변이 없어요. 첫 번째로 답변해보세요!
                </div>
              ) : (
                otherAnswers.map((a, i) => (
                  <div key={a.id} className="card" style={{ padding: 20 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 12 }}>
                      <div className="avatar" style={{
                        width: 36, height: 36,
                        background: ["#3B82F6", "#8B5CF6", "#10B981", "#F59E0B", "#EC4899"][i % 5]
                      }}>
                        {a.authorNickname?.[0] ?? "?"}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{a.authorNickname}</div>
                        <div className="t-xs">{new Date(a.createdAt).toLocaleDateString("ko-KR")}</div>
                      </div>
                    </div>
                    <div className="t-body" style={{ fontSize: 14, whiteSpace: "pre-wrap" }}>{a.content}</div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Solve;