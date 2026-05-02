import React from "react";
import { useNavigate } from "react-router-dom";
import {
  IconHeart, IconHeartFill, IconArrowRight,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { sampleQuestions } from "../data/mockData.js";

const Community = () => {
  const navigate = useNavigate();
  const [sort, setSort] = React.useState("hot");

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        <div style={{ marginBottom: 24, display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
          <div>
            <h1 className="t-h2" style={{ marginBottom: 6 }}>커뮤니티</h1>
            <p className="t-body" style={{ fontSize: 14 }}>
              다른 개발자들의 답변을 보고, 좋은 답변에 좋아요를 눌러주세요.
            </p>
          </div>
          <div style={{ display: "flex", gap: 4, padding: 4, background: "#fff",
                         border: "1px solid var(--gray-200)", borderRadius: 10 }}>
            {[{ id: "hot", label: "🔥 인기" }, { id: "new", label: "🆕 최신" }, { id: "top", label: "⭐ 추천" }].map(t => (
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
            {[
              { user: "박서준", role: "백엔드 4년차", color: "#3B82F6",
                qcat: "네트워크", qdiff: "mid", q: "TCP와 UDP의 차이",
                excerpt: "TCP는 신뢰성, UDP는 속도라는 핵심을 잡고 답변하면 좋습니다. 특히 면접에서는 '3-way handshake' 같은 구체적 메커니즘을 함께 언급하면 깊이가 살아나요...",
                likes: 142, comments: 23, time: "2시간 전", liked: true },
              { user: "이수민", role: "프론트 2년차", color: "#8B5CF6",
                qcat: "프론트엔드", qdiff: "mid", q: "React의 Virtual DOM은 왜 빠른가요?",
                excerpt: "사실 Virtual DOM이 무조건 빠른 건 아닙니다. 진짜 핵심은 \"실제 DOM 조작 횟수를 최소화\"하는 것이고, 그래서 diffing 알고리즘이 중요한데...",
                likes: 98, comments: 14, time: "5시간 전", liked: false },
              { user: "김지훈", role: "백엔드 7년차", color: "#10B981",
                qcat: "백엔드", qdiff: "high", q: "MSA를 도입하면 어떤 트레이드오프?",
                excerpt: "MSA는 만능이 아닙니다. 팀 규모가 작을 때는 오히려 모놀리식이 효율적이에요. 우리 팀이 MSA로 전환했을 때 겪은 운영 복잡도 이슈를 공유해보면...",
                likes: 76, comments: 31, time: "어제", liked: false },
              { user: "정유진", role: "데이터 3년차", color: "#F59E0B",
                qcat: "데이터베이스", qdiff: "high", q: "DB 인덱스를 걸면 항상 빠를까요?",
                excerpt: "인덱스는 양날의 검입니다. 읽기는 빨라지지만 쓰기는 느려져요. 특히 카디널리티가 낮은 컬럼에 인덱스를 거는 건 거의 의미가 없습니다...",
                likes: 54, comments: 8, time: "어제", liked: false },
            ].map((p, i) => (
              <div key={i} className="card row-hover" style={{ padding: 24 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 12 }}>
                  <div className="avatar" style={{ width: 36, height: 36, background: p.color }}>
                    {p.user[0]}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{p.user}</div>
                    <div className="t-xs">{p.role} · {p.time}</div>
                  </div>
                </div>

                <div style={{ display: "flex", gap: 6, marginBottom: 10 }}>
                  <DifficultyBadge level={p.qdiff} />
                  <CategoryBadge name={p.qcat} />
                </div>
                <div className="t-h4" style={{ marginBottom: 8, color: "var(--gray-800)" }}>
                  Q. {p.q}
                </div>
                <div className="t-body" style={{ fontSize: 14, marginBottom: 16 }}>
                  {p.excerpt}
                </div>

                <div style={{ display: "flex", alignItems: "center", gap: 16,
                               paddingTop: 14, borderTop: "1px solid var(--gray-100)",
                               fontSize: 13, color: "var(--gray-600)" }}>
                  <span style={{ display: "flex", alignItems: "center", gap: 6,
                                  cursor: "pointer", fontWeight: 500,
                                  color: p.liked ? "var(--red-500)" : "inherit" }}>
                    {p.liked ? <IconHeartFill size={15} /> : <IconHeart size={15} />}
                    {p.likes}
                  </span>
                  <span style={{ display: "flex", alignItems: "center", gap: 6, cursor: "pointer", fontWeight: 500 }}>
                    💬 {p.comments}
                  </span>
                  <span style={{ marginLeft: "auto", color: "var(--blue-600)", fontWeight: 500, cursor: "pointer" }}
                        onClick={() => navigate("/solve")}>
                    전체 답변 보기 <IconArrowRight size={12} style={{ verticalAlign: -1 }} />
                  </span>
                </div>
              </div>
            ))}
          </div>

          {/* Sidebar */}
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div className="card" style={{ padding: 24 }}>
              <div className="t-h4" style={{ marginBottom: 14 }}>🏆 이번 주 답변왕</div>
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                {[
                  { name: "박서준", count: 23, color: "#3B82F6" },
                  { name: "이수민", count: 18, color: "#8B5CF6" },
                  { name: "정유진", count: 14, color: "#F59E0B" },
                  { name: "김지훈", count: 11, color: "#10B981" },
                ].map((u, i) => (
                  <div key={i} style={{ display: "flex", alignItems: "center", gap: 10 }}>
                    <span style={{ width: 18, fontSize: 13, fontWeight: 700,
                                    color: i < 3 ? "var(--amber-600)" : "var(--gray-400)" }}>
                      {i + 1}
                    </span>
                    <div className="avatar" style={{ width: 28, height: 28, fontSize: 11, background: u.color }}>
                      {u.name[0]}
                    </div>
                    <span style={{ flex: 1, fontSize: 13, fontWeight: 500 }}>{u.name}</span>
                    <span className="t-xs">{u.count}답변</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="card" style={{ padding: 24 }}>
              <div className="t-h4" style={{ marginBottom: 14 }}>🔥 핫한 질문</div>
              <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                {sampleQuestions.slice(0, 4).map((q, i) => (
                  <div key={i} className="row-hover" style={{ paddingLeft: 0, cursor: "pointer" }}
                       onClick={() => navigate("/solve")}>
                    <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4,
                                   color: "var(--gray-800)" }}>{q.title}</div>
                    <div className="t-xs" style={{ display: "flex", gap: 8 }}>
                      <DifficultyBadge level={q.diff} />
                      <span>답변 {q.solved % 300}+</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Community;