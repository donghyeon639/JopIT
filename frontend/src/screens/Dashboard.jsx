import React from "react";
import { useNavigate } from "react-router-dom";
import {
  IconSpark, IconBookmark, IconHeartFill, IconCheck,
  IconClock, IconArrowRight, IconUser,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { PrepBot } from "../components/PrepBot.jsx";
import { sampleQuestions } from "../data/mockData.js";

const Dashboard = () => {
  const navigate = useNavigate();

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        {/* Greeting with Prep mascot */}
        <div style={{
          display: "flex", alignItems: "center", gap: 20, marginBottom: 28,
          background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)",
          border: "1px solid #BFDBFE", borderRadius: 18, padding: "20px 24px"
        }}>
          <div style={{ flexShrink: 0 }}>
            <PrepBot expression="wave" size={96} />
          </div>
          <div style={{ flex: 1 }}>
            <h1 className="t-h2" style={{ marginBottom: 4 }}>안녕하세요, 김개발님 👋</h1>
            <p className="t-body" style={{ fontSize: 14, marginBottom: 0 }}>
              저는 프렉쌌이에요! 오늘은 <b style={{ color: "var(--gray-900)" }}>네트워크 3문제</b>를 준비해드렸어요.
            </p>
          </div>
          <button className="btn btn-primary" onClick={() => navigate("/questions")}>
            바로 시작하기 <IconArrowRight size={14} />
          </button>
        </div>

        {/* Streak / Resume */}
        <div style={{ display: "grid", gridTemplateColumns: "1.4fr 1fr", gap: 16, marginBottom: 24 }}>
          <div className="card" style={{
            padding: 24, background: "linear-gradient(135deg, #1E3A8A 0%, #2563EB 100%)",
            color: "#fff", border: "none"
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12,
                           opacity: 0.9, fontSize: 13, fontWeight: 600 }}>
              <IconClock size={14} /> 어제 풀던 문제 이어가기
            </div>
            <div className="t-h3" style={{ color: "#fff", marginBottom: 6 }}>
              Q. Index가 항상 빠를까요?
            </div>
            <div style={{ fontSize: 13, opacity: 0.85, marginBottom: 18 }}>
              DB · 어려움 · 작성 중 (지난 답변 432자)
            </div>
            <button className="btn btn-sm" style={{ background: "#fff", color: "var(--blue-700)" }}
                     onClick={() => navigate("/solve")}>
              이어서 작성하기 <IconArrowRight size={14} />
            </button>
          </div>

          <div className="card" style={{ padding: 24 }}>
            <div className="t-xs" style={{ marginBottom: 12 }}>이번 주 학습</div>
            <div style={{ display: "flex", alignItems: "baseline", gap: 8, marginBottom: 16 }}>
              <span style={{ fontSize: 36, fontWeight: 700, letterSpacing: "-0.02em" }}>12</span>
              <span className="t-sm">/ 20 문제</span>
              <span className="badge badge-low" style={{ marginLeft: "auto" }}>+3 어제보다</span>
            </div>
            <div className="progress-track" style={{ marginBottom: 14 }}>
              <div className="progress-fill" style={{ width: "60%" }} />
            </div>
            <div style={{ display: "flex", gap: 4 }}>
              {[1, 1, 1, 0, 1, 1, 0].map((d, i) => (
                <div key={i} style={{
                  flex: 1, height: 32, borderRadius: 6,
                  background: d ? "var(--blue-600)" : "var(--gray-100)"
                }} />
              ))}
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginTop: 8 }}>
              {["월", "화", "수", "목", "금", "토", "일"].map((d, i) => (
                <span key={i} className="t-xs" style={{ flex: 1, textAlign: "center" }}>{d}</span>
              ))}
            </div>
          </div>
        </div>

        {/* Category progress */}
        <div className="card" style={{ padding: 28, marginBottom: 24 }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end",
                         marginBottom: 20 }}>
            <div>
              <div className="t-h3" style={{ marginBottom: 4 }}>백엔드 학습 진도</div>
              <div className="t-sm">카테고리별로 진행 상황을 확인해보세요.</div>
            </div>
            <div className="t-sm">전체 <b style={{ color: "var(--gray-900)" }}>34%</b> 완료</div>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 12 }}>
            {[
              { name: "CS", done: 18, total: 40, color: "#3B82F6" },
              { name: "데이터베이스", done: 12, total: 30, color: "#7C3AED" },
              { name: "네트워크", done: 8, total: 25, color: "#059669" },
              { name: "운영체제", done: 6, total: 20, color: "#D97706" },
              { name: "백엔드", done: 11, total: 35, color: "#DC2626" },
              { name: "시스템 설계", done: 3, total: 15, color: "#0891B2" },
            ].map((c, i) => {
              const pct = Math.round((c.done / c.total) * 100);
              return (
                <div key={i} style={{ padding: 16, background: "var(--gray-50)",
                                       borderRadius: 10, cursor: "pointer" }}>
                  <div style={{ display: "flex", justifyContent: "space-between",
                                alignItems: "center", marginBottom: 12 }}>
                    <span style={{ fontWeight: 600, fontSize: 14 }}>{c.name}</span>
                    <span className="t-xs">{c.done}/{c.total}</span>
                  </div>
                  <div className="progress-track" style={{ height: 5 }}>
                    <div style={{ height: "100%", borderRadius: 999, width: pct + "%",
                                   background: c.color }} />
                  </div>
                  <div style={{ marginTop: 8, fontSize: 11, color: "var(--gray-500)" }}>{pct}%</div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Recommended + Recent activity */}
        <div style={{ display: "grid", gridTemplateColumns: "1.4fr 1fr", gap: 16 }}>
          <div className="card" style={{ padding: 28 }}>
            <div style={{ display: "flex", justifyContent: "space-between",
                           alignItems: "center", marginBottom: 16 }}>
              <div className="t-h3">오늘의 추천 문제</div>
              <button className="btn btn-ghost btn-sm" onClick={() => navigate("/questions")}>
                전체 보기 <IconArrowRight size={12} />
              </button>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              {sampleQuestions.slice(0, 4).map((q, i) => (
                <div key={i} className="row-hover"
                      style={{ padding: 14, borderRadius: 10, border: "1px solid var(--gray-200)",
                               display: "flex", alignItems: "center", gap: 14 }}
                      onClick={() => navigate("/solve")}>
                  <span style={{ fontSize: 12, color: "var(--gray-400)",
                                  fontFamily: "var(--font-mono)", width: 24 }}>
                    {String(i + 1).padStart(2, "0")}
                  </span>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4 }}>{q.title}</div>
                    <div style={{ display: "flex", gap: 6 }}>
                      <DifficultyBadge level={q.diff} />
                      <CategoryBadge name={q.cat} />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="card" style={{ padding: 28 }}>
            <div className="t-h3" style={{ marginBottom: 16 }}>최근 활동</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
              {[
                { icon: <IconCheck size={14} />, color: "#059669", bg: "#ECFDF5",
                  text: <><b>TCP와 UDP의 차이</b> 답변 완료</>, time: "2시간 전" },
                { icon: <IconHeartFill size={14} />, color: "#DC2626", bg: "#FEF2F2",
                  text: <>박서준님이 내 답변에 좋아요</>, time: "5시간 전" },
                { icon: <IconSpark size={14} />, color: "#7C3AED", bg: "#F5F3FF",
                  text: <>AI 피드백 도착 (DB 인덱스)</>, time: "어제" },
                { icon: <IconBookmark size={14} />, color: "#D97706", bg: "#FFFBEB",
                  text: <>오답 노트에 3개 추가됨</>, time: "어제" },
              ].map((a, i) => (
                <div key={i} style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
                  <div style={{ width: 28, height: 28, borderRadius: 8, background: a.bg,
                                 color: a.color, display: "flex", alignItems: "center",
                                 justifyContent: "center", flexShrink: 0 }}>
                    {a.icon}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 13, color: "var(--gray-800)" }}>{a.text}</div>
                    <div className="t-xs" style={{ marginTop: 2 }}>{a.time}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;