import React from "react";
import { useNavigate } from "react-router-dom";
import {
  IconSpark, IconList, IconBuilding, IconHeart, IconClock, IconBookmark,
  IconArrowRight, IconPlay, IconUser,
  Logo, TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { PrepBot } from "../components/PrepBot.jsx";
import { sampleQuestions } from "../data/mockData.js";

const Landing = () => {
  const navigate = useNavigate();

  return (
    <div className="dp-screen" style={{ width: "100%", background: "#fff" }}>
      <TopNav />

      {/* Hero — bold purple */}
      <section style={{
        background: "linear-gradient(135deg, #4338CA 0%, #4F46E5 50%, #5B47D9 100%)",
        padding: "80px 48px 100px", position: "relative", overflow: "hidden"
      }}>
        <div style={{ position: "absolute", top: 60, right: 120, width: 8, height: 8, borderRadius: 999, background: "rgba(255,255,255,0.3)" }} />
        <div style={{ position: "absolute", top: 200, right: 80, width: 6, height: 6, borderRadius: 999, background: "rgba(255,255,255,0.2)" }} />
        <div style={{ position: "absolute", bottom: 120, left: 80, width: 10, height: 10, borderRadius: 999, background: "rgba(255,255,255,0.18)" }} />
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "grid",
          gridTemplateColumns: "1.3fr 1fr", gap: 48, alignItems: "center",
          minHeight: 460 }}>
          <div>
            <h1 style={{ fontSize: 80, fontWeight: 800, lineHeight: 1.1, letterSpacing: "-0.035em", color: "#fff", marginBottom: 28 }}>
              취업준비,<br />
              아직도 혼자 하세요?
            </h1>
            <p style={{ fontSize: 18, color: "rgba(255,255,255,0.85)", maxWidth: 520, marginBottom: 36, lineHeight: 1.6 }}>
              내 수준을 알면 준비가 빨라져요. 5분이면 충분해요.
            </p>
            <div style={{ display: "flex", gap: 12 }}>
              <button onClick={() => navigate("/levelcheck")} style={{
                padding: "16px 28px", background: "#fff", color: "#4338CA",
                border: "none", borderRadius: 12, fontSize: 16, fontWeight: 700,
                cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 8,
                boxShadow: "0 8px 24px -4px rgba(0,0,0,0.25)", fontFamily: "inherit"
              }}>
                내 수준 확인하기 <IconArrowRight size={16} />
              </button>
              <button onClick={() => navigate("/signup")} style={{
                padding: "16px 24px", background: "transparent", color: "#fff",
                border: "1.5px solid rgba(255,255,255,0.4)", borderRadius: 12,
                fontSize: 15, fontWeight: 600, cursor: "pointer", fontFamily: "inherit"
              }}>
                무료로 시작하기
              </button>
            </div>
            <div style={{ display: "flex", gap: 32, marginTop: 56 }}>
              <Stat n="1,200+" label="면접 질문" white />
            </div>
          </div>
          <div style={{ position: "relative", height: 460, display: "flex", alignItems: "flex-end", justifyContent: "center" }}>
            <div style={{
              position: "absolute", top: 110, right: 20,
              background: "linear-gradient(135deg, #C7D2FE, #A5B4FC)",
              color: "#3730A3", padding: "12px 20px", borderRadius: 999,
              fontSize: 15, fontWeight: 700, boxShadow: "0 4px 16px -2px rgba(0,0,0,0.2)"
            }}>
              내 수준 확인하기
              <div style={{ position: "absolute", bottom: -7, left: "50%", transform: "translateX(-50%) rotate(45deg)", width: 14, height: 14, background: "#A5B4FC" }} />
            </div>
            <div onClick={() => navigate("/levelcheck")} style={{ cursor: "pointer" }}>
              <PrepBot expression="wave" size={220} accent="#4F46E5" />
            </div>
          </div>
        </div>
      </section>

      {/* How it works — 3단계 */}
      <section style={{ padding: "64px 48px", background: "var(--gray-50)",
        borderTop: "1px solid var(--gray-200)",
        borderBottom: "1px solid var(--gray-200)" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto" }}>
          <div style={{ textAlign: "center", marginBottom: 48 }}>
            <span className="badge badge-blue" style={{ marginBottom: 12 }}>How it works</span>
            <h2 className="t-h1" style={{ marginTop: 8 }}>3단계로 끝내는 면접 준비</h2>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 20 }}>
            <Step n="01" title="직군과 난이도를 골라요" desc="백엔드, 프론트, 데이터 등 직군별 맞춤 문제를 추천해드려요." />
            <Step n="02" title="내 답변을 작성해요" desc="시간을 재고 풀거나, 마음껏 정리하며 풀 수 있어요." />
            <Step n="03" title="AI가 즉시 첨삭해줘요" desc="놓친 키워드, 보강할 포인트를 구체적으로 알려드려요." />
          </div>
        </div>
      </section>

      {/* Features grid */}
      <section style={{ padding: "80px 48px", maxWidth: 1280, margin: "0 auto" }}>
        <div style={{ marginBottom: 48 }}>
          <h2 className="t-h1" style={{ marginBottom: 12 }}>면접 준비, 이게 다르니까요</h2>
          <p className="t-body" style={{ fontSize: 16, maxWidth: 560 }}>
            단순히 문제만 모아놓은 사이트가 아니에요. 실제로 합격으로 이어지는 학습 경험을 설계했어요.
          </p>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 16 }}>
          <Feat icon={<IconSpark />} color="#3B82F6" bg="var(--blue-50)"
            title="AI 답변 피드백" desc="놓친 개념, 더 깊이 있는 표현을 즉시 알려드려요." />
          <Feat icon={<IconList />} color="#7C3AED" bg="var(--purple-50)"
            title="직군별 큐레이션" desc="백엔드/프론트/데이터, 직군에 맞는 문제만 모았어요." />
          <Feat icon={<IconBuilding />} color="#059669" bg="var(--green-50)"
            title="기업별 면접 후기" desc="실제 합격자들이 받은 질문을 회사별로 확인해요." />
          <Feat icon={<IconClock />} color="#D97706" bg="var(--amber-50)"
            title="모의면접 타이머" desc="실전처럼 시간 안에 답변하는 훈련을 해보세요." />
          <Feat icon={<IconBookmark />} color="#DC2626" bg="var(--red-50)"
            title="오답 노트" desc="틀렸거나 어려웠던 문제만 모아 다시 풀어요." />
          <Feat icon={<IconHeart />} color="#DB2777" bg="#FDF2F8"
            title="커뮤니티" desc="다른 개발자의 답변에 좋아요와 댓글을 남겨요." />
        </div>
      </section>

      {/* Sample questions preview */}
      <section style={{ padding: "0 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between",
          marginBottom: 24 }}>
          <div>
            <h2 className="t-h2" style={{ marginBottom: 8 }}>지금 인기 있는 질문</h2>
            <p className="t-sm">이번 주 가장 많이 풀어본 문제들이에요.</p>
          </div>
          <button className="btn btn-ghost" onClick={() => navigate("/questions")}>전체 보기 <IconArrowRight size={14} /></button>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 12 }}>
          {sampleQuestions.slice(0, 4).map((q, i) => <PopularQRow key={i} q={q} />)}
        </div>
      </section>

      {/* CTA */}
      <section style={{ padding: "0 48px 80px" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto",
          background: "linear-gradient(135deg, #1E3A8A 0%, #2563EB 100%)",
          borderRadius: 24, padding: "64px 48px", color: "#fff",
          textAlign: "center" }}>
          <h2 className="t-h1" style={{ color: "#fff", marginBottom: 12 }}>
            지금 바로 시작해보세요
          </h2>
          <p style={{ fontSize: 16, opacity: 0.85, marginBottom: 28 }}>
            가입 30초. 첫 문제는 바로 풀 수 있어요.
          </p>
          <button className="btn btn-lg" style={{ background: "#fff", color: "var(--blue-700)" }}
            onClick={() => navigate("/signup")}>
            무료로 시작하기 <IconArrowRight size={16} />
          </button>
        </div>
      </section>

      <Footer />
    </div>
  );
};

const Stat = ({ n, label, white }) =>
  <div>
    <div style={{ fontSize: 24, fontWeight: 700, letterSpacing: "-0.02em", color: white ? "#fff" : undefined }}>{n}</div>
    <div className="t-sm" style={{ marginTop: 2, color: white ? "rgba(255,255,255,0.7)" : undefined }}>{label}</div>
  </div>;

const Step = ({ n, title, desc }) =>
  <div className="card" style={{ padding: 28, background: "#fff" }}>
    <div style={{ fontSize: 13, fontWeight: 700, color: "var(--blue-600)",
      fontFamily: "var(--font-mono)", marginBottom: 12 }}>{n}</div>
    <div className="t-h3" style={{ marginBottom: 8 }}>{title}</div>
    <div className="t-body" style={{ fontSize: 14 }}>{desc}</div>
  </div>;

const Feat = ({ icon, color, bg, title, desc }) =>
  <div className="card" style={{ padding: 24 }}>
    <div style={{ width: 40, height: 40, borderRadius: 10, background: bg,
      display: "flex", alignItems: "center", justifyContent: "center",
      color: color, marginBottom: 16 }}>
      {icon}
    </div>
    <div className="t-h4" style={{ marginBottom: 6 }}>{title}</div>
    <div className="t-sm">{desc}</div>
  </div>;

const PopularQRow = ({ q }) =>
  <div className="card row-hover" style={{ padding: 20 }}>
    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
      <DifficultyBadge level={q.diff} />
      <CategoryBadge name={q.cat} />
    </div>
    <div className="t-h4" style={{ marginBottom: 10 }}>{q.title}</div>
    <div className="t-xs" style={{ display: "flex", alignItems: "center", gap: 12 }}>
      <span><IconUser size={12} style={{ verticalAlign: -2 }} /> {q.solved.toLocaleString()}명이 풀었어요</span>
    </div>
  </div>;

const Footer = () =>
  <footer style={{ borderTop: "1px solid var(--gray-200)", padding: "40px 48px",
    background: "var(--gray-50)" }}>
    <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex",
      justifyContent: "space-between", alignItems: "center" }}>
      <Logo />
      <div className="t-sm">© 2026 PrepNote · 개발자를 위한 면접 준비 플랫폼</div>
    </div>
  </footer>;

export default Landing;
