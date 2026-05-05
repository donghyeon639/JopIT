import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconArrowRight,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { communityApi } from "../api/questionApi.js";

const AVATAR_COLORS = ["#3B82F6", "#8B5CF6", "#10B981", "#F59E0B", "#EC4899"];

const excerpt = (text, n = 160) => {
  if (!text) return "";
  return text.length > n ? text.slice(0, n) + "..." : text;
};

const timeAgo = (iso) => {
  if (!iso) return "";
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (diff < 60) return "방금 전";
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
  if (diff < 86400 * 7) return `${Math.floor(diff / 86400)}일 전`;
  return new Date(iso).toLocaleDateString("ko-KR");
};

const Community = () => {
  const navigate = useNavigate();
  const [sort, setSort] = useState("new");
  const [feed, setFeed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    communityApi.feed()
      .then((data) => { if (!cancelled) setFeed(data); })
      .catch((e) => { if (!cancelled) setError(e.message || "불러오기에 실패했습니다."); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const sortedFeed = useMemo(() => {
    const list = [...feed];
    if (sort === "comments") {
      list.sort((a, b) => (b.commentCount ?? 0) - (a.commentCount ?? 0));
    } else {
      list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
    return list;
  }, [feed, sort]);

  const topAuthors = useMemo(() => {
    const counts = new Map();
    for (const a of feed) {
      counts.set(a.authorNickname, (counts.get(a.authorNickname) ?? 0) + 1);
    }
    return [...counts.entries()]
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 4);
  }, [feed]);

  const hotQuestions = useMemo(() => {
    const map = new Map();
    for (const a of feed) {
      const key = a.questionId;
      if (!map.has(key)) {
        map.set(key, { questionId: key, title: a.questionTitle, count: 0 });
      }
      map.get(key).count += 1;
    }
    return [...map.values()].sort((a, b) => b.count - a.count).slice(0, 4);
  }, [feed]);

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        <div style={{ marginBottom: 24, display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
          <div>
            <h1 className="t-h2" style={{ marginBottom: 6 }}>커뮤니티</h1>
            <p className="t-body" style={{ fontSize: 14 }}>
              다른 개발자들의 답변을 보고, 좋은 답변에 댓글로 의견을 남겨보세요.
            </p>
          </div>
          <div style={{ display: "flex", gap: 4, padding: 4, background: "#fff",
                         border: "1px solid var(--gray-200)", borderRadius: 10 }}>
            {[{ id: "new", label: "🆕 최신" }, { id: "comments", label: "💬 댓글 많은 순" }].map(t => (
              <div key={t.id} onClick={() => setSort(t.id)}
                    style={{
                      padding: "8px 14px", fontSize: 13, fontWeight: 600,
                      cursor: "pointer", borderRadius: 7,
                      background: sort === t.id ? "var(--gray-900)" : "transparent",
                      color: sort === t.id ? "#fff" : "var(--gray-600)"
                    }}>{t.label}</div>
            ))}
          </div>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: 24 }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            {loading ? (
              <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
                답변을 불러오는 중...
              </div>
            ) : error ? (
              <div className="card" style={{ padding: 40, textAlign: "center", color: "#DC2626" }}>
                {error}
              </div>
            ) : sortedFeed.length === 0 ? (
              <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
                아직 작성된 답변이 없어요. 가장 먼저 답변을 작성해보세요!
              </div>
            ) : sortedFeed.map((a, i) => {
              const color = AVATAR_COLORS[(a.authorNickname?.charCodeAt(0) ?? i) % AVATAR_COLORS.length];
              return (
                <div key={a.id} className="card row-hover" style={{ padding: 24, cursor: "pointer" }}
                     onClick={() => navigate(`/answer?id=${a.id}`)}>
                  <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 12 }}>
                    <div className="avatar" style={{ width: 36, height: 36, background: color }}>
                      {a.authorNickname?.[0] ?? "?"}
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: 14 }}>{a.authorNickname}</div>
                      <div className="t-xs">{timeAgo(a.createdAt)}</div>
                    </div>
                  </div>

                  <div className="t-h4" style={{ marginBottom: 8, color: "var(--gray-800)" }}>
                    Q. {a.questionTitle}
                  </div>
                  <div className="t-body" style={{ fontSize: 14, marginBottom: 16, whiteSpace: "pre-wrap" }}>
                    {excerpt(a.content)}
                  </div>

                  <div style={{ display: "flex", alignItems: "center", gap: 16,
                                 paddingTop: 14, borderTop: "1px solid var(--gray-100)",
                                 fontSize: 13, color: "var(--gray-600)" }}>
                    <span style={{ display: "flex", alignItems: "center", gap: 6, fontWeight: 500 }}>
                      💬 {a.commentCount ?? 0}
                    </span>
                    <span style={{ marginLeft: "auto", color: "var(--blue-600)", fontWeight: 500 }}>
                      답변 자세히 보기 <IconArrowRight size={12} style={{ verticalAlign: -1 }} />
                    </span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Sidebar */}
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div className="card" style={{ padding: 24 }}>
              <div className="t-h4" style={{ marginBottom: 14 }}>🏆 답변 많이 쓴 사람</div>
              {topAuthors.length === 0 ? (
                <div className="t-xs" style={{ color: "var(--gray-400)" }}>아직 데이터가 없어요.</div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                  {topAuthors.map((u, i) => (
                    <div key={u.name} style={{ display: "flex", alignItems: "center", gap: 10 }}>
                      <span style={{ width: 18, fontSize: 13, fontWeight: 700,
                                      color: i < 3 ? "var(--amber-600)" : "var(--gray-400)" }}>
                        {i + 1}
                      </span>
                      <div className="avatar" style={{
                        width: 28, height: 28, fontSize: 11,
                        background: AVATAR_COLORS[i % AVATAR_COLORS.length]
                      }}>
                        {u.name?.[0] ?? "?"}
                      </div>
                      <span style={{ flex: 1, fontSize: 13, fontWeight: 500 }}>{u.name}</span>
                      <span className="t-xs">{u.count}답변</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="card" style={{ padding: 24 }}>
              <div className="t-h4" style={{ marginBottom: 14 }}>🔥 답변이 많은 질문</div>
              {hotQuestions.length === 0 ? (
                <div className="t-xs" style={{ color: "var(--gray-400)" }}>아직 데이터가 없어요.</div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                  {hotQuestions.map((q) => (
                    <div key={q.questionId} className="row-hover"
                         style={{ paddingLeft: 0, cursor: "pointer" }}
                         onClick={() => navigate(`/solve?id=${q.questionId}`)}>
                      <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4,
                                     color: "var(--gray-800)" }}>{q.title}</div>
                      <div className="t-xs">답변 {q.count}개</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Community;