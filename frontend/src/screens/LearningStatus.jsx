import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconArrowLeft, IconArrowRight, IconSpark,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { meApi } from "../api/questionApi.js";
import { useAuth } from "../context/AuthContext.jsx";

const DIFF_LABEL = { LOW: "하", MID: "중", HIGH: "상" };
const DIFF_COLOR = { LOW: "#10B981", MID: "#F59E0B", HIGH: "#EF4444" };

const formatDate = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
};

const dayLabel = (isoDate) => {
  const d = new Date(isoDate + "T00:00:00");
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return days[d.getDay()];
};

const LearningStatus = () => {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setLoading(true);
    meApi.stats()
      .then(setStats)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const nickname = auth?.nickname ?? "회원";

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <span onClick={() => navigate("/dashboard")} className="t-sm"
                style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
            <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 대시보드로
          </span>
          <button className="btn btn-outline btn-sm" onClick={() => navigate("/my/answers")}>
            내 답변 전체보기 <IconArrowRight size={14} />
          </button>
        </div>
      </div>

      <div style={{ maxWidth: 1080, margin: "0 auto", padding: "32px 48px 80px" }}>
        <div style={{ marginBottom: 24 }}>
          <h1 className="t-h2" style={{ marginBottom: 6 }}>{nickname}님의 학습 현황</h1>
          <p className="t-body" style={{ fontSize: 14 }}>
            그동안 풀어온 문제와 AI 피드백 결과를 한눈에 확인해보세요.
          </p>
        </div>

        {loading ? (
          <Empty text="불러오는 중..." />
        ) : error ? (
          <Empty text="학습 현황을 불러오지 못했습니다." color="#DC2626" />
        ) : stats?.totalAnswers === 0 ? (
          <div className="card" style={{ padding: 60, textAlign: "center" }}>
            <div style={{ fontSize: 36, marginBottom: 10 }}>📊</div>
            <div className="t-h3" style={{ marginBottom: 6 }}>아직 학습 기록이 없어요</div>
            <p className="t-body" style={{ fontSize: 14, marginBottom: 20 }}>
              첫 문제를 풀면 통계가 여기에 표시돼요.
            </p>
            <button className="btn btn-primary" onClick={() => navigate("/questions")}>
              문제 풀러가기 <IconArrowRight size={14} />
            </button>
          </div>
        ) : (
          <>
            {/* KPI 4개 */}
            <div style={{
              display: "grid",
              gridTemplateColumns: "repeat(4, 1fr)",
              gap: 12,
              marginBottom: 20
            }}>
              <Kpi label="총 답변" value={stats.totalAnswers} unit="개" accent="#2563EB" />
              <Kpi label="푼 문제" value={stats.uniqueQuestions} unit="개" accent="#7C3AED" />
              <Kpi
                label="AI 피드백 완료"
                value={stats.feedbackDone}
                unit="개"
                sub={stats.feedbackPending > 0 ? `진행중 ${stats.feedbackPending}` : null}
                accent="#059669"
              />
              <Kpi
                label="연속 학습"
                value={stats.currentStreakDays}
                unit="일"
                accent="#F59E0B"
              />
            </div>

            {/* 최근 7일 활동 */}
            <div className="card" style={{ padding: 24, marginBottom: 16 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: 18 }}>
                <div className="t-h3">최근 7일 활동</div>
                <div className="t-xs" style={{ color: "var(--gray-500)" }}>
                  하루 작성한 답변 수
                </div>
              </div>
              <Last7DaysChart data={stats.last7Days} />
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginBottom: 16 }}>
              {/* 카테고리별 */}
              <div className="card" style={{ padding: 24 }}>
                <div className="t-h3" style={{ marginBottom: 16 }}>카테고리별 답변</div>
                <Distribution data={stats.byCategory} colorOf={() => "#3B82F6"} />
              </div>

              {/* 난이도별 */}
              <div className="card" style={{ padding: 24 }}>
                <div className="t-h3" style={{ marginBottom: 16 }}>난이도별 답변</div>
                <Distribution
                  data={stats.byDifficulty}
                  labelOf={(k) => `난이도 ${DIFF_LABEL[k] || k}`}
                  colorOf={(k) => DIFF_COLOR[k] || "#3B82F6"}
                  order={["LOW", "MID", "HIGH"]}
                />
              </div>
            </div>

            {/* 가장 최근 답변 */}
            {stats.latestAnswer && (
              <div className="card" style={{ padding: 24 }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: 14 }}>
                  <div className="t-h3">가장 최근 답변</div>
                  <span onClick={() => navigate(`/answer?id=${stats.latestAnswer.answerId}`)}
                        style={{ fontSize: 12, color: "var(--blue-600)", fontWeight: 600, cursor: "pointer" }}>
                    자세히 보기
                  </span>
                </div>
                <div onClick={() => navigate(`/answer?id=${stats.latestAnswer.answerId}`)}
                     className="row-hover"
                     style={{ padding: 14, borderRadius: 10, cursor: "pointer", border: "1px solid var(--gray-100)" }}>
                  <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 8, flexWrap: "wrap" }}>
                    <CategoryBadge name={stats.latestAnswer.questionCategoryName} />
                    <DifficultyBadge level={stats.latestAnswer.questionDifficulty?.toLowerCase()} />
                    <FeedbackBadge status={stats.latestAnswer.feedbackStatus} />
                    <span style={{ fontSize: 12, color: "var(--gray-400)" }}>
                      · {formatDate(stats.latestAnswer.createdAt)}
                    </span>
                  </div>
                  <div style={{ fontSize: 15, fontWeight: 700, color: "var(--gray-900)" }}>
                    {stats.latestAnswer.questionTitle}
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

const Kpi = ({ label, value, unit, sub, accent }) => (
  <div className="card" style={{ padding: 18 }}>
    <div style={{ fontSize: 12, fontWeight: 600, color: "var(--gray-500)", marginBottom: 8 }}>
      {label}
    </div>
    <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
      <span style={{ fontSize: 28, fontWeight: 800, color: accent || "var(--gray-900)", letterSpacing: "-0.02em" }}>
        {value}
      </span>
      <span style={{ fontSize: 13, color: "var(--gray-500)", fontWeight: 600 }}>{unit}</span>
    </div>
    {sub && (
      <div style={{ fontSize: 11, color: "var(--gray-500)", marginTop: 4 }}>{sub}</div>
    )}
  </div>
);

const Last7DaysChart = ({ data }) => {
  if (!data || data.length === 0) return null;
  const max = Math.max(1, ...data.map(d => d.count));

  return (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", gap: 8, height: 140 }}>
      {data.map((d, i) => {
        const ratio = d.count / max;
        const heightPx = Math.max(4, Math.round(ratio * 110));
        const isToday = i === data.length - 1;
        return (
          <div key={d.date} style={{
            flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 6
          }}>
            <div style={{
              fontSize: 11, fontWeight: 600,
              color: d.count > 0 ? "var(--gray-700)" : "var(--gray-300)",
              minHeight: 14
            }}>
              {d.count > 0 ? d.count : ""}
            </div>
            <div style={{
              width: "100%", maxWidth: 36, height: heightPx,
              borderRadius: 6,
              background: d.count === 0
                ? "var(--gray-100)"
                : isToday
                  ? "linear-gradient(180deg, #60A5FA 0%, #2563EB 100%)"
                  : "linear-gradient(180deg, #93C5FD 0%, #3B82F6 100%)",
              transition: "height 0.25s"
            }} />
            <div style={{
              fontSize: 11, color: isToday ? "var(--blue-700)" : "var(--gray-500)",
              fontWeight: isToday ? 700 : 500
            }}>
              {dayLabel(d.date)}
            </div>
          </div>
        );
      })}
    </div>
  );
};

const Distribution = ({ data, labelOf, colorOf, order }) => {
  const entries = Object.entries(data || {});
  if (entries.length === 0) {
    return <div style={{ fontSize: 13, color: "var(--gray-400)" }}>데이터 없음</div>;
  }
  if (order) {
    entries.sort((a, b) => order.indexOf(a[0]) - order.indexOf(b[0]));
  } else {
    entries.sort((a, b) => b[1] - a[1]);
  }
  const max = Math.max(...entries.map(([, v]) => v));

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      {entries.map(([k, v]) => {
        const ratio = (v / max) * 100;
        return (
          <div key={k}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
              <span style={{ fontSize: 13, color: "var(--gray-700)", fontWeight: 600 }}>
                {labelOf ? labelOf(k) : k}
              </span>
              <span style={{ fontSize: 12, color: "var(--gray-500)", fontWeight: 600 }}>
                {v}개
              </span>
            </div>
            <div style={{ height: 8, background: "var(--gray-100)", borderRadius: 999, overflow: "hidden" }}>
              <div style={{
                width: `${ratio}%`, height: "100%",
                background: colorOf ? colorOf(k) : "#3B82F6",
                borderRadius: 999, transition: "width 0.25s"
              }} />
            </div>
          </div>
        );
      })}
    </div>
  );
};

const FeedbackBadge = ({ status }) => {
  const meta = {
    DONE:    { text: "피드백 완료",   bg: "#ECFDF5", color: "#047857", icon: true },
    PENDING: { text: "피드백 진행중", bg: "#FFFBEB", color: "#B45309" },
    NONE:    { text: "피드백 미요청", bg: "#F3F4F6", color: "#4B5563" },
    FAILED:  { text: "피드백 실패",   bg: "#FEF2F2", color: "#B91C1C" },
  }[status] || { text: status, bg: "#F3F4F6", color: "#4B5563" };

  return (
    <span style={{
      fontSize: 11, fontWeight: 600,
      padding: "2px 8px", borderRadius: 999,
      background: meta.bg, color: meta.color
    }}>
      {meta.icon && <IconSpark size={10} style={{ verticalAlign: -1, marginRight: 3 }} />}
      {meta.text}
    </span>
  );
};

const Empty = ({ text, color }) => (
  <div className="card" style={{ padding: 60, textAlign: "center", color: color ?? "var(--gray-400)" }}>
    {text}
  </div>
);

export default LearningStatus;