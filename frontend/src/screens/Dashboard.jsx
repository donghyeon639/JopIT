import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconSpark, IconList, IconSearch, IconUser, IconHeart, IconBookmark,
  IconArrowRight, IconBuilding, IconPlay, IconCode,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { getTechTrends } from "../data/mockData.js";
import { meApi, techTrendApi, jobPostingApi } from "../api/questionApi.js";

/* ──────────────────────────────────────────────
 *  앱 아이콘 스타일 — 솔리드 컬러 배경 + 흰색 아이콘
 *  iOS 앱 아이콘 / Notion·Linear 계열 느낌
 * ──────────────────────────────────────────────*/

const TileIcon = ({ children, bg = "#3B6AE8" }) => (
  <div style={{
    width: 56, height: 56, borderRadius: 16,
    background: bg,
    display: "flex", alignItems: "center", justifyContent: "center",
    marginBottom: 8, flexShrink: 0,
    transition: "transform 0.18s ease",
    boxShadow: `0 4px 14px ${bg}55`
  }}>
    {children}
  </div>
);

/* 문제 풀기 — 번개 (도전·에너지) */
const QuizIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M15 2L5 15H12L11 24L21 11H14L15 2Z" fill="#fff" fillRule="evenodd"/>
  </svg>
);

/* 이력서 첨삭 — 펜 + 별 (AI 첨삭) */
const ResumeIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M4 19.5L14.5 9L17 11.5L6.5 22H4V19.5Z" fill="#fff"/>
    <path d="M17 4L18.2 7.2L21.5 8.5L18.2 9.8L17 13L15.8 9.8L12.5 8.5L15.8 7.2L17 4Z" fill="#fff" opacity="0.9"/>
  </svg>
);

/* 내 답변 — 클립보드 체크 */
const AnswerIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <rect x="5" y="6" width="16" height="18" rx="3" fill="rgba(255,255,255,0.2)" stroke="#fff" strokeWidth="1.5"/>
    <path d="M10 4H16C16 5.1 15.1 6 14 6H12C10.9 6 10 5.1 10 4Z" fill="#fff"/>
    <path d="M9 15L12 18L17 12" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

/* 레벨 체크 — 트로피 */
const LevelIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M9 3H17V14C17 16.8 15.2 18 13 18C10.8 18 9 16.8 9 14V3Z" fill="#fff"/>
    <path d="M5 5H9V10C6.8 10 5 8.2 5 6V5Z" fill="rgba(255,255,255,0.65)"/>
    <path d="M17 5H21V6C21 8.2 19.2 10 17 10V5Z" fill="rgba(255,255,255,0.65)"/>
    <rect x="10.5" y="18" width="5" height="1.5" rx="0.75" fill="#fff"/>
    <rect x="8" y="19.5" width="10" height="2" rx="1" fill="#fff"/>
  </svg>
);

/* 학습 현황 — 로켓 */
const RocketIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M13 2C13 2 20 5.5 20 13C20 17 17.2 19.5 13 21C8.8 19.5 6 17 6 13C6 5.5 13 2 13 2Z" fill="#fff"/>
    <circle cx="13" cy="12.5" r="3" fill="rgba(255,210,0,0.85)"/>
    <path d="M9.5 19L7 24L11 22L9.5 19Z" fill="rgba(255,255,255,0.75)"/>
    <path d="M16.5 19L19 24L15 22L16.5 19Z" fill="rgba(255,255,255,0.75)"/>
  </svg>
);

/* 커뮤니티 — 두 사람 */
const CommunityIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <circle cx="9.5" cy="8" r="4" fill="#fff"/>
    <path d="M1.5 22C1.5 17.9 5.1 14.6 9.5 14.6" stroke="#fff" strokeWidth="2.5" strokeLinecap="round"/>
    <circle cx="18.5" cy="8" r="3" fill="rgba(255,255,255,0.65)"/>
    <path d="M16.5 14.6C20.3 14.6 24.5 17.3 24.5 22" stroke="rgba(255,255,255,0.65)" strokeWidth="2" strokeLinecap="round"/>
  </svg>
);

/* AI 면접 코칭 — 마이크 + 스파클 */
const AICoachIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <rect x="9" y="2" width="8" height="12" rx="4" fill="#fff"/>
    <path d="M5 12C5 17.5 8.6 20.5 13 20.5C17.4 20.5 21 17.5 21 12"
      stroke="#fff" strokeWidth="2" strokeLinecap="round" fill="none"/>
    <line x1="13" y1="20.5" x2="13" y2="23" stroke="#fff" strokeWidth="2" strokeLinecap="round"/>
    <line x1="10" y1="23" x2="16" y2="23" stroke="#fff" strokeWidth="2" strokeLinecap="round"/>
    <circle cx="21" cy="5" r="1.5" fill="rgba(255,255,255,0.7)"/>
    <circle cx="23.5" cy="2.5" r="1" fill="rgba(255,255,255,0.5)"/>
  </svg>
);

/* 즐겨찾기 — 북마크 리본 */
const BookmarkIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M7 3H19C19.6 3 20 3.4 20 4V23L13 18.5L6 23V4C6 3.4 6.4 3 7 3Z" fill="#fff"/>
    <line x1="10" y1="9" x2="16" y2="9" stroke="rgba(234,88,12,0.45)" strokeWidth="1.5" strokeLinecap="round"/>
  </svg>
);

/* 직군별 통계 — 포디엄 */
const StatIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <rect x="9.5" y="10" width="7" height="13" rx="1.5" fill="#fff"/>
    <rect x="2.5" y="15" width="7" height="8" rx="1.5" fill="rgba(255,255,255,0.65)"/>
    <rect x="16.5" y="12" width="7" height="11" rx="1.5" fill="rgba(255,255,255,0.85)"/>
    <path d="M12 7.5L13 5L14 7.5" stroke="#fff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

/* 추천 문제 — 전구 */
const RecommendIcon = () => (
  <svg width="26" height="26" viewBox="0 0 26 26" fill="none">
    <path d="M13 3C9.7 3 7 5.7 7 9C7 11.7 8.8 13.6 10 15H16C17.2 13.6 19 11.7 19 9C19 5.7 16.3 3 13 3Z" fill="#fff"/>
    <rect x="10" y="15" width="6" height="2" rx="0.5" fill="rgba(255,255,255,0.85)"/>
    <rect x="10.5" y="17" width="5" height="2" rx="0.5" fill="rgba(255,255,255,0.65)"/>
    <path d="M11.5 19L13 22L14.5 19Z" fill="rgba(255,255,255,0.55)"/>
  </svg>
);

const SectionHeader = ({ title, hint }) => (
  <div style={{
    display: "flex", alignItems: "flex-end", justifyContent: "space-between",
    marginBottom: 16
  }}>
    <div style={{ display: "flex", alignItems: "baseline", gap: 10 }}>
      <h2 style={{
        fontSize: 20, fontWeight: 700, color: "#1A1F2E",
        letterSpacing: "-0.014em", margin: 0
      }}>
        {title}
      </h2>
      {hint && (
        <span style={{
          fontSize: 12, fontWeight: 600,
          padding: "3px 10px", borderRadius: 999,
          background: "#E4ECFB", color: "#4A7BF7"
        }}>
          {hint}
        </span>
      )}
    </div>
  </div>
);

const tagColorMap = {
  "Java":        { bg: "#FEF3C7", color: "#D97706" },
  "Spring":      { bg: "#DCFCE7", color: "#16A34A" },
  "Redis":       { bg: "#FEE2E2", color: "#DC2626" },
  "Kafka":       { bg: "#F5F3FF", color: "#7C3AED" },
  "Database":    { bg: "#FFF7ED", color: "#EA580C" },
  "React":       { bg: "#DBEAFE", color: "#2563EB" },
  "TypeScript":  { bg: "#EEF2FF", color: "#4F46E5" },
  "Vite":        { bg: "#F0FDF4", color: "#16A34A" },
  "Performance": { bg: "#FFF1F2", color: "#E11D48" },
  "AI/ML":       { bg: "#F5F3FF", color: "#7C3AED" },
  "dbt":         { bg: "#ECFDF5", color: "#059669" },
  "Spark":       { bg: "#FFF7ED", color: "#EA580C" },
  "MLOps":       { bg: "#EEF2FF", color: "#4F46E5" },
};

const TechTrendCard = ({ item, onClick }) => {
  const tagStyle = tagColorMap[item.tag] ?? { bg: "#F3F4F6", color: "#6B7280" };
  const [imgError, setImgError] = useState(false);

  return (
    <div
      onClick={onClick}
      style={{
        border: "1px solid #ECEEF2",
        borderRadius: 14,
        background: "#fff",
        cursor: "pointer",
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
        transition: "all 0.18s ease",
      }}
      onMouseEnter={e => {
        e.currentTarget.style.borderColor = "#C7D7FA";
        e.currentTarget.style.boxShadow = "0 4px 16px rgba(74,123,247,0.1)";
        e.currentTarget.style.transform = "translateY(-2px)";
      }}
      onMouseLeave={e => {
        e.currentTarget.style.borderColor = "#ECEEF2";
        e.currentTarget.style.boxShadow = "none";
        e.currentTarget.style.transform = "translateY(0)";
      }}
    >
      {/* 썸네일 영역 */}
      <div style={{
        aspectRatio: "16 / 9",
        background: item.imageUrl && !imgError
          ? "#F3F4F6"
          : `linear-gradient(135deg, ${tagStyle.bg} 0%, #fff 100%)`,
        display: "flex", alignItems: "center", justifyContent: "center",
        position: "relative", overflow: "hidden",
      }}>
        {item.imageUrl && !imgError ? (
          <img
            src={item.imageUrl}
            alt={item.title}
            onError={() => setImgError(true)}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
          />
        ) : (
          <span style={{
            fontSize: 14, fontWeight: 700,
            color: tagStyle.color, opacity: 0.7,
            letterSpacing: "0.04em",
          }}>
            {item.tag}
          </span>
        )}
      </div>

      <div style={{ padding: "16px 18px 14px", display: "flex", flexDirection: "column", gap: 8, flexGrow: 1 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span style={{
            fontSize: 11, fontWeight: 700,
            padding: "3px 10px", borderRadius: 999,
            background: tagStyle.bg, color: tagStyle.color,
          }}>{item.tag}</span>
          {item.readTime && (
            <span style={{ fontSize: 11, color: "#9BA3B2" }}>{item.readTime} 읽기</span>
          )}
        </div>
        <div style={{
          fontSize: 15, fontWeight: 700, color: "#1A1F2E",
          lineHeight: 1.45,
          overflow: "hidden", textOverflow: "ellipsis",
          display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
        }}>{item.title}</div>
        <div style={{
          fontSize: 13, color: "#7B8290", lineHeight: 1.55,
          overflow: "hidden", textOverflow: "ellipsis",
          display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
          flexGrow: 1,
        }}>{item.description}</div>
        <div style={{
          display: "flex", justifyContent: "space-between", alignItems: "center",
          paddingTop: 10, borderTop: "1px solid #F3F4F6", marginTop: 4,
        }}>
          <span style={{ fontSize: 12, fontWeight: 600, color: "#4A7BF7" }}>{item.source}</span>
          <span style={{ fontSize: 11, color: "#9BA3B2" }}>{item.date}</span>
        </div>
      </div>
    </div>
  );
};

const Tile = ({ icon, label, onClick, badge, bg }) => (
  <div onClick={onClick} style={{
    display: "flex", flexDirection: "column", alignItems: "center",
    padding: "16px 10px 12px", borderRadius: 16,
    cursor: onClick ? "pointer" : "default",
    border: "1px solid transparent",
    transition: "all 0.18s ease"
  }}
  onMouseEnter={(e) => {
    if (!onClick) return;
    e.currentTarget.style.background = "#F7F9FF";
    e.currentTarget.style.borderColor = "#E0E7FF";
    const icon = e.currentTarget.querySelector(".tile-icon-inner");
    if (icon) icon.style.transform = "translateY(-3px)";
  }}
  onMouseLeave={(e) => {
    e.currentTarget.style.background = "transparent";
    e.currentTarget.style.borderColor = "transparent";
    const icon = e.currentTarget.querySelector(".tile-icon-inner");
    if (icon) icon.style.transform = "translateY(0)";
  }}>
    <div style={{ position: "relative" }}>
      <div className="tile-icon-inner" style={{ transition: "transform 0.18s ease" }}>
        <TileIcon bg={bg}>{icon}</TileIcon>
      </div>
      {badge && (
        <span style={{
          position: "absolute", top: 0, right: 0,
          background: "#FF4757", color: "#fff",
          fontSize: 9, fontWeight: 800,
          width: 16, height: 16, borderRadius: 999,
          display: "flex", alignItems: "center", justifyContent: "center",
          border: "2px solid #fff"
        }}>{badge}</span>
      )}
    </div>
    <div style={{ fontSize: 12.5, color: "#3D434C", fontWeight: 600, textAlign: "center", letterSpacing: "-0.01em" }}>
      {label}
    </div>
  </div>
);

const Dashboard = () => {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const nickname = auth?.nickname ?? "회원";
  const jobCategoryName = auth?.jobCategoryName ?? "백엔드";

  const [stats, setStats] = useState(null);
  const [techTrends, setTechTrends] = useState([]);
  const [trendsLoading, setTrendsLoading] = useState(true);
  const [publicJobs, setPublicJobs] = useState([]);
  const [publicJobsLoading, setPublicJobsLoading] = useState(true);

  useEffect(() => {
    meApi.stats().then(setStats).catch(() => {});
  }, []);

  useEffect(() => {
    setTrendsLoading(true);
    techTrendApi.list()
      .then(setTechTrends)
      .catch(() => setTechTrends(getTechTrends(jobCategoryName)))
      .finally(() => setTrendsLoading(false));
  }, []);

  useEffect(() => {
    setPublicJobsLoading(true);
    jobPostingApi.public()
      .then((res) => setPublicJobs(res?.content ?? []))
      .catch(() => setPublicJobs([]))
      .finally(() => setPublicJobsLoading(false));
  }, []);

  const tiles = [
    { icon: <QuizIcon />,      label: "문제 풀기",    onClick: () => navigate("/questions"), bg: "#3B6AE8" },
    { icon: <ResumeIcon />,    label: "이력서 첨삭",  onClick: () => navigate("/resume"),    bg: "#D97706" },
    { icon: <AnswerIcon />,    label: "내 답변",      onClick: () => navigate("/my/answers"),bg: "#4F46E5" },
    { icon: <LevelIcon />,     label: "레벨 체크",    onClick: () => navigate("/levelcheck"),bg: "#DC2626" },
    { icon: <RocketIcon />,    label: "학습 현황",    onClick: () => navigate("/my/status"), bg: "#DB2777" },
    { icon: <CommunityIcon />, label: "커뮤니티",     onClick: () => navigate("/community"), bg: "#0284C7" },
    { icon: <AICoachIcon />,   label: "AI 면접 코칭", badge: "N",                            bg: "#059669" },
    { icon: <BookmarkIcon />,  label: "즐겨찾기",                                             bg: "#EA580C" },
    { icon: <StatIcon />,      label: "직군별 통계",                                         bg: "#7C3AED" },
    { icon: <RecommendIcon />, label: "추천 문제",    onClick: () => navigate("/questions"), bg: "#E11D48" },
  ];

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "#fff" }}>
      <TopNav />

      <div style={{ padding: "48px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>

        {/* 히어로 배너 */}
        <div className="dp-hero-banner" style={{
          marginBottom: 40,
          background: "linear-gradient(135deg, #1A1F2E 0%, #283268 55%, #4A7BF7 100%)",
          borderRadius: 22,
          padding: "32px 40px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          position: "relative",
          overflow: "hidden"
        }}>
          {/* 배경 장식 */}
          <div style={{
            position: "absolute", right: 220, top: -50,
            width: 140, height: 140, borderRadius: "50%",
            background: "rgba(255,255,255,0.04)", pointerEvents: "none"
          }} />
          <div style={{
            position: "absolute", right: 60, bottom: -60,
            width: 200, height: 200, borderRadius: "50%",
            background: "rgba(255,255,255,0.04)", pointerEvents: "none"
          }} />
          <div style={{
            position: "absolute", right: 160, top: 10,
            width: 60, height: 60, borderRadius: "50%",
            background: "rgba(74,123,247,0.25)", pointerEvents: "none"
          }} />

          <div style={{ zIndex: 1 }}>
            <div style={{
              display: "inline-flex", alignItems: "center", gap: 6,
              background: "rgba(255,255,255,0.12)",
              borderRadius: 999, padding: "4px 12px",
              fontSize: 12, color: "rgba(255,255,255,0.85)",
              fontWeight: 600, marginBottom: 12, letterSpacing: "0.02em"
            }}>
              <span style={{ fontSize: 10 }}>●</span> {jobCategoryName} 직군 면접 준비
            </div>
            <div style={{
              fontSize: 22, fontWeight: 700, color: "#fff",
              marginBottom: 8, letterSpacing: "-0.02em", lineHeight: 1.3
            }}>
              {nickname}님, 오늘도 성장하는 하루 되세요! 🎯
            </div>
            <div style={{ fontSize: 13.5, color: "rgba(255,255,255,0.65)", fontWeight: 400 }}>
              {stats?.currentStreakDays > 0
                ? `🔥 ${stats.currentStreakDays}일 연속 학습 중 · 총 ${stats.totalAnswers ?? 0}개 답변 완료`
                : "첫 번째 답변을 작성하고 학습을 시작해보세요"}
            </div>
          </div>

          <div className="dp-hero-cta-group" style={{ display: "flex", gap: 10, zIndex: 1, flexShrink: 0 }}>
            <button
              onClick={() => navigate("/levelcheck")}
              style={{
                background: "rgba(255,255,255,0.12)",
                color: "#fff",
                border: "1px solid rgba(255,255,255,0.25)",
                borderRadius: 12,
                padding: "12px 22px",
                fontWeight: 600,
                fontSize: 14,
                cursor: "pointer",
                fontFamily: "inherit"
              }}>
              레벨 체크
            </button>
            <button
              onClick={() => navigate("/questions")}
              style={{
                background: "#fff",
                color: "#4A7BF7",
                border: "none",
                borderRadius: 12,
                padding: "12px 24px",
                fontWeight: 700,
                fontSize: 14,
                cursor: "pointer",
                fontFamily: "inherit",
                display: "flex",
                alignItems: "center",
                gap: 6
              }}>
              문제 풀러가기 <IconArrowRight size={14} />
            </button>
          </div>
        </div>

        {/* 빠른 메뉴 */}
        <div style={{ marginBottom: 12 }}>
          <span style={{
            fontSize: 12, fontWeight: 700, color: "#9BA3B2",
            letterSpacing: "0.06em", textTransform: "uppercase"
          }}>
            빠른 메뉴
          </span>
        </div>
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(5, 1fr)",
          gap: 4,
          marginBottom: 52,
          padding: "0 4px"
        }}>
          {tiles.map((t, i) => (
            <Tile key={i} {...t} />
          ))}
        </div>

        {/* ─── 공공 채용 (청년일자리지원 OpenAPI) ─── */}
        <SectionHeader
          title="공공 채용"
          hint="청년일자리지원 OpenAPI"
        />
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, 1fr)",
          gap: 16,
          marginBottom: 48
        }}>
          {publicJobsLoading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} style={{
                border: "1px solid #ECEEF2", borderRadius: 14,
                height: 168,
                background: "linear-gradient(90deg, #F3F4F6 25%, #E9EAEC 50%, #F3F4F6 75%)",
                backgroundSize: "200% 100%",
                animation: "shimmer 1.4s infinite",
              }} />
            ))
          ) : publicJobs.length === 0 ? (
            <div style={{
              gridColumn: "1 / -1",
              padding: "32px 24px",
              border: "1px dashed #ECEEF2",
              borderRadius: 14,
              textAlign: "center",
              color: "#7B8290",
              fontSize: 14,
            }}>
              아직 공공 채용 공고가 동기화되지 않았습니다. 잠시 후 다시 확인해주세요.
            </div>
          ) : (
            publicJobs.map((job) => (
              <div
                key={job.id}
                onClick={() => job.applyUrl && window.open(job.applyUrl, "_blank", "noopener,noreferrer")}
                style={{
                  border: "1px solid #ECEEF2",
                  borderRadius: 14,
                  background: "#fff",
                  padding: "20px 22px",
                  cursor: job.applyUrl ? "pointer" : "default",
                  display: "flex",
                  flexDirection: "column",
                  gap: 10,
                  transition: "all 0.18s ease",
                }}
                onMouseEnter={(e) => {
                  if (!job.applyUrl) return;
                  e.currentTarget.style.borderColor = "#C7D7FA";
                  e.currentTarget.style.boxShadow = "0 4px 16px rgba(74,123,247,0.1)";
                  e.currentTarget.style.transform = "translateY(-2px)";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = "#ECEEF2";
                  e.currentTarget.style.boxShadow = "none";
                  e.currentTarget.style.transform = "translateY(0)";
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{
                    fontSize: 11, fontWeight: 700,
                    padding: "3px 10px", borderRadius: 999,
                    background: "#E5F7F2", color: "#059669",
                  }}>공공기관</span>
                  {job.expiresAt && (
                    <span style={{ fontSize: 11, color: "#9BA3B2" }}>
                      ~ {String(job.expiresAt).slice(0, 10)}
                    </span>
                  )}
                </div>
                <div style={{
                  fontSize: 15, fontWeight: 700, color: "#1A1F2E",
                  lineHeight: 1.45,
                  overflow: "hidden", textOverflow: "ellipsis",
                  display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
                }}>{job.title}</div>
                <div style={{ fontSize: 13, color: "#4A7BF7", fontWeight: 600 }}>
                  {job.company}
                </div>
                <div style={{
                  display: "flex", flexWrap: "wrap", gap: 6,
                  fontSize: 12, color: "#7B8290",
                }}>
                  {job.location && <span>📍 {job.location}</span>}
                  {job.employmentType && <span>· {job.employmentType}</span>}
                  {job.careerLevel && <span>· {job.careerLevel}</span>}
                </div>
              </div>
            ))
          )}
        </div>

        {/* ─── 요즘 IT 기술 트렌드 ─── */}
        <SectionHeader
          title="요즘 IT 기술 트렌드"
          hint="기술 블로그 큐레이션"
        />
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 16,
          marginBottom: 48
        }}>
          {trendsLoading
            ? Array.from({ length: 4 }).map((_, i) => (
                <div key={i} style={{
                  border: "1px solid #ECEEF2", borderRadius: 14,
                  height: 280,
                  background: "linear-gradient(90deg, #F3F4F6 25%, #E9EAEC 50%, #F3F4F6 75%)",
                  backgroundSize: "200% 100%",
                  animation: "shimmer 1.4s infinite",
                }} />
              ))
            : techTrends.map((t) => (
                <TechTrendCard
                  key={t.id ?? t.url}
                  item={t}
                  onClick={() => {
                    if (t.id) {
                      navigate(`/tech-trends/${t.id}`);
                    } else if (t.url) {
                      window.open(t.url, "_blank", "noopener,noreferrer");
                    }
                  }}
                />
              ))
          }
        </div>
        <style>{`
          @keyframes shimmer {
            0%   { background-position: 200% 0; }
            100% { background-position: -200% 0; }
          }
        `}</style>


        {/* 하단 — 가벼운 요약 (이어가기 + 추천 문제) */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>

          {/* 최근 학습 카드 */}
          <div style={{
            padding: 28, background: "#fff",
            border: "1px solid #ECEEF2", borderRadius: 16
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
              <div style={{ fontSize: 13, color: "#7B8290", fontWeight: 600 }}>
                📚 최근 학습
              </div>
              {stats && stats.totalAnswers > 0 && (
                <span onClick={() => navigate("/my/status")}
                  style={{ fontSize: 12, color: "#4A7BF7", fontWeight: 600, cursor: "pointer" }}>
                  학습 현황
                </span>
              )}
            </div>
            {stats && stats.latestAnswer ? (
              <>
                <div style={{ fontSize: 13, color: "#7B8290", marginBottom: 6 }}>
                  마지막으로 푼 문제
                </div>
                <div style={{
                  fontSize: 17, fontWeight: 600, color: "#1A1F2E", marginBottom: 6,
                  overflow: "hidden", textOverflow: "ellipsis",
                  display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                }}>
                  {stats.latestAnswer.questionTitle}
                </div>
                <div style={{ fontSize: 13, color: "#7B8290", marginBottom: 18 }}>
                  지금까지 답변 {stats.totalAnswers}개
                  {stats.currentStreakDays > 0 && ` · 연속 ${stats.currentStreakDays}일`}
                </div>
                <button onClick={() => navigate(`/answer?id=${stats.latestAnswer.answerId}`)}
                  style={{
                    padding: "10px 18px", fontSize: 14, fontWeight: 600,
                    background: "#1A1F2E", color: "#fff",
                    border: "none", borderRadius: 8, cursor: "pointer",
                    fontFamily: "inherit",
                    display: "inline-flex", alignItems: "center", gap: 6
                  }}>
                  이어서 보기 <IconArrowRight size={14} />
                </button>
              </>
            ) : (
              <>
                <div style={{ fontSize: 18, fontWeight: 600, color: "#1A1F2E", marginBottom: 6 }}>
                  아직 풀던 문제가 없어요
                </div>
                <div style={{ fontSize: 14, color: "#7B8290", marginBottom: 18 }}>
                  첫 문제를 풀어보면 여기에 이어가기가 표시돼요.
                </div>
                <button onClick={() => navigate("/questions")}
                  style={{
                    padding: "10px 18px", fontSize: 14, fontWeight: 600,
                    background: "#1A1F2E", color: "#fff",
                    border: "none", borderRadius: 8, cursor: "pointer",
                    fontFamily: "inherit",
                    display: "inline-flex", alignItems: "center", gap: 6
                  }}>
                  문제 보러가기 <IconArrowRight size={14} />
                </button>
              </>
            )}
          </div>

          {/* 커뮤니티 미리보기 */}
          <div style={{
            padding: 28, background: "#fff",
            border: "1px solid #ECEEF2", borderRadius: 16
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
              <div style={{ fontSize: 13, color: "#7B8290", fontWeight: 600 }}>
                💬 커뮤니티
              </div>
              <span onClick={() => navigate("/community")}
                style={{ fontSize: 12, color: "#4A7BF7", fontWeight: 600, cursor: "pointer" }}>
                전체 보기
              </span>
            </div>
            <div style={{ fontSize: 18, fontWeight: 600, color: "#1A1F2E", marginBottom: 6 }}>
              다른 사람의 답변에서 인사이트 얻기
            </div>
            <div style={{ fontSize: 14, color: "#7B8290" }}>
              같은 질문도 사람마다 푸는 방식이 다 달라요. 다른 답변을 보고
              댓글로 의견을 나눠보세요.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;