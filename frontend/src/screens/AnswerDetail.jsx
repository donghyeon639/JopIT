import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  IconArrowLeft, IconUser,
  TopNav, DifficultyBadge
} from "../components/Components.jsx";
import { questionApi, commentApi } from "../api/questionApi.js";
import { useAuth } from "../context/AuthContext.jsx";

const AVATAR_COLORS = ["#3B82F6", "#8B5CF6", "#10B981", "#F59E0B", "#EC4899"];

const AnswerDetail = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const answerId = searchParams.get("id");
  const { auth, isLoggedIn } = useAuth();

  const [answer, setAnswer] = useState(null);
  const [question, setQuestion] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentDraft, setCommentDraft] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!answerId) { navigate("/community"); return; }
    let cancelled = false;

    (async () => {
      setLoading(true);
      try {
        const a = await questionApi.getAnswer(answerId);
        if (cancelled) return;
        setAnswer(a);
        const [q, cs] = await Promise.all([
          questionApi.detail(a.questionId).catch(() => null),
          commentApi.list(answerId).catch(() => []),
        ]);
        if (cancelled) return;
        setQuestion(q);
        setComments(cs);
      } catch {
        if (!cancelled) navigate("/community");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [answerId]);

  const handleSubmit = async () => {
    if (!isLoggedIn) { setError("로그인이 필요합니다."); return; }
    if (!commentDraft.trim()) { setError("댓글 내용을 입력해주세요."); return; }
    setError("");
    setSubmitting(true);
    try {
      const created = await commentApi.create(answerId, commentDraft.trim());
      setComments((prev) => [...prev, created]);
      setCommentDraft("");
    } catch (e) {
      setError(e.message || "댓글 작성에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId) => {
    if (!confirm("이 댓글을 삭제하시겠어요?")) return;
    try {
      await commentApi.delete(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (e) {
      alert(e.message || "삭제에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
        <TopNav />
        <div style={{ padding: 80, textAlign: "center", color: "var(--gray-400)" }}>답변을 불러오는 중...</div>
      </div>
    );
  }

  const diffLevel = question?.difficulty?.toLowerCase();
  const myUsername = auth?.username;

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 960, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div className="t-sm" style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span onClick={() => navigate(-1)}
                  style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
              <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 뒤로가기
            </span>
            <span style={{ color: "var(--gray-300)" }}>/</span>
            <span onClick={() => navigate("/community")}
                  style={{ cursor: "pointer", color: "var(--gray-600)" }}>
              커뮤니티
            </span>
          </div>
          {answer?.questionId && (
            <button className="btn btn-secondary"
                    onClick={() => navigate(`/solve?id=${answer.questionId}`)}>
              이 문제 직접 풀어보기
            </button>
          )}
        </div>
      </div>

      <div style={{ maxWidth: 960, margin: "0 auto", padding: "32px 48px 80px" }}>

        {/* Question summary */}
        <div className="card" style={{ padding: 24, marginBottom: 16 }}>
          <div style={{ display: "flex", gap: 6, marginBottom: 10 }}>
            {diffLevel && <DifficultyBadge level={diffLevel} />}
            {question?.questionCategoryName && (
              <span style={{ fontSize: 12, padding: "2px 8px", borderRadius: 6,
                              background: "var(--gray-100)", color: "var(--gray-700)" }}>
                {question.questionCategoryName}
              </span>
            )}
          </div>
          <div className="t-h3" style={{ marginBottom: 0 }}>
            Q. {answer?.questionTitle}
          </div>
        </div>

        {/* Answer */}
        <div className="card" style={{ padding: 28, marginBottom: 24 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
            <div className="avatar" style={{
              width: 40, height: 40,
              background: AVATAR_COLORS[(answer?.authorNickname?.charCodeAt(0) ?? 0) % AVATAR_COLORS.length]
            }}>
              {answer?.authorNickname?.[0] ?? "?"}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{answer?.authorNickname}</div>
              <div className="t-xs">{answer?.createdAt && new Date(answer.createdAt).toLocaleString("ko-KR")}</div>
            </div>
          </div>
          <div className="t-body" style={{ fontSize: 15, lineHeight: 1.8, whiteSpace: "pre-wrap" }}>
            {answer?.content}
          </div>
        </div>

        {/* Comments */}
        <div className="card" style={{ padding: 24 }}>
          <div className="t-h4" style={{ marginBottom: 16 }}>
            💬 댓글 {comments.length}
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 14, marginBottom: 20 }}>
            {comments.length === 0 ? (
              <div style={{ padding: 24, textAlign: "center", color: "var(--gray-400)", fontSize: 14 }}>
                첫 번째 댓글을 남겨보세요.
              </div>
            ) : comments.map((c) => (
              <div key={c.id} style={{ display: "flex", gap: 10, paddingBottom: 14,
                                        borderBottom: "1px solid var(--gray-100)" }}>
                <div className="avatar" style={{
                  width: 32, height: 32, fontSize: 13,
                  background: AVATAR_COLORS[(c.authorNickname?.charCodeAt(0) ?? 0) % AVATAR_COLORS.length]
                }}>
                  {c.authorNickname?.[0] ?? "?"}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <span style={{ fontWeight: 600, fontSize: 13 }}>{c.authorNickname}</span>
                    <span className="t-xs">{new Date(c.createdAt).toLocaleString("ko-KR")}</span>
                    {c.authorUsername === myUsername && (
                      <span onClick={() => handleDelete(c.id)}
                            style={{ marginLeft: "auto", fontSize: 12, color: "var(--gray-500)",
                                     cursor: "pointer" }}>
                        삭제
                      </span>
                    )}
                  </div>
                  <div className="t-body" style={{ fontSize: 14, whiteSpace: "pre-wrap" }}>{c.content}</div>
                </div>
              </div>
            ))}
          </div>

          {isLoggedIn ? (
            <div style={{ border: "1px solid var(--gray-200)", borderRadius: 10, overflow: "hidden" }}>
              <textarea
                value={commentDraft}
                onChange={(e) => setCommentDraft(e.target.value)}
                placeholder="댓글을 작성해주세요..."
                maxLength={500}
                style={{
                  width: "100%", minHeight: 80, padding: 14, border: "none",
                  outline: "none", resize: "vertical", fontFamily: "inherit",
                  fontSize: 14, lineHeight: 1.6, color: "var(--gray-800)",
                  boxSizing: "border-box"
                }}
              />
              <div style={{ padding: "10px 14px", background: "var(--gray-50)",
                             borderTop: "1px solid var(--gray-200)",
                             display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                {error ? (
                  <span style={{ fontSize: 13, color: "#DC2626" }}>{error}</span>
                ) : (
                  <span className="t-xs">{commentDraft.length}/500</span>
                )}
                <button className="btn btn-primary" onClick={handleSubmit} disabled={submitting}>
                  {submitting ? "작성 중..." : "댓글 작성"}
                </button>
              </div>
            </div>
          ) : (
            <div style={{ padding: 16, textAlign: "center", background: "var(--gray-50)",
                           borderRadius: 10, color: "var(--gray-600)", fontSize: 14 }}>
              댓글을 작성하려면 <span onClick={() => navigate("/login")}
                                       style={{ color: "var(--blue-600)", fontWeight: 600, cursor: "pointer" }}>
                로그인
              </span> 해주세요.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AnswerDetail;