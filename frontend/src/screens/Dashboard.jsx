import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconSpark, IconList, IconSearch, IconUser, IconHeart, IconBookmark,
  IconArrowRight, IconBuilding, IconPlay, IconCode,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { getJobNews, getCompanyThemes } from "../data/mockData.js";
import { meApi } from "../api/questionApi.js";

/* ──────────────────────────────────────────────
 *  Wanted 스타일 메뉴 아이콘 — 부드러운 컬러 + 일러스트 톤
 *  채도를 낮춘 파스텔 컬러 + 단순한 SVG 도형으로 구성
 * ──────────────────────────────────────────────*/

const TileIcon = ({ children }) => (
  <div style={{
    width: 64, height: 64, borderRadius: 16,
    display: "flex", alignItems: "center", justifyContent: "center",
    marginBottom: 8, flexShrink: 0
  }}>
    {children}
  </div>
);

const Layered = () => (
  /* 채용공고 풍 — 쌓인 카드 */
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
    <path d="M24 6L40 14L24 22L8 14L24 6Z" fill="#7BAEF7" />
    <path d="M8 22L24 30L40 22L40 26L24 34L8 26L8 22Z" fill="#4A7BF7" />
    <path d="M8 30L24 38L40 30L40 34L24 42L8 34L8 30Z" fill="#3B66E0" />
  </svg>
);

const Document = ({ accent = "#FFB800" }) => (
  /* 문서/노트 */
  <svg width="44" height="48" viewBox="0 0 44 48" fill="none">
    <path d="M6 4H28L38 14V42C38 43.1 37.1 44 36 44H6C4.9 44 4 43.1 4 42V6C4 4.9 4.9 4 6 4Z" fill="#E8F0FE"/>
    <path d="M28 4V14H38L28 4Z" fill="#C7DCFC"/>
    <circle cx="32" cy="10" r="6" fill={accent}/>
    <path d="M30 10L32 12L34 8" stroke="#fff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const PaperStack = () => (
  /* 문서 관리 */
  <svg width="44" height="48" viewBox="0 0 44 48" fill="none">
    <rect x="10" y="8" width="26" height="34" rx="3" fill="#C7DCFC"/>
    <rect x="6" y="12" width="26" height="32" rx="3" fill="#7BAEF7"/>
    <rect x="11" y="20" width="16" height="2" rx="1" fill="#fff"/>
    <rect x="11" y="26" width="13" height="2" rx="1" fill="#fff"/>
    <rect x="11" y="32" width="10" height="2" rx="1" fill="#fff"/>
  </svg>
);

const Target = () => (
  /* 커리어 조회 — 과녁 */
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
    <circle cx="24" cy="24" r="20" stroke="#FB923C" strokeWidth="3"/>
    <circle cx="24" cy="24" r="13" stroke="#FB923C" strokeWidth="3"/>
    <circle cx="24" cy="24" r="6" fill="#FB923C"/>
  </svg>
);

const Donut = () => (
  /* 진행 상황 — 도넛 차트 */
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
    <circle cx="24" cy="24" r="16" stroke="#FFE3B0" strokeWidth="6"/>
    <path d="M24 8 A16 16 0 0 1 40 24" stroke="#FFB800" strokeWidth="6" strokeLinecap="round" fill="none"/>
    <path d="M40 24 A16 16 0 0 1 30 38" stroke="#F66" strokeWidth="6" strokeLinecap="round" fill="none"/>
  </svg>
);

const ChatBubble = () => (
  /* 면접 제안 — 말풍선 */
  <svg width="48" height="44" viewBox="0 0 48 44" fill="none">
    <path d="M6 6H42C43.1 6 44 6.9 44 8V28C44 29.1 43.1 30 42 30H22L14 38V30H6C4.9 30 4 29.1 4 28V8C4 6.9 4.9 6 6 6Z" fill="#7BAEF7"/>
    <circle cx="16" cy="18" r="2" fill="#fff"/>
    <circle cx="24" cy="18" r="2" fill="#fff"/>
    <circle cx="32" cy="18" r="2" fill="#fff"/>
  </svg>
);

const Headphones = () => (
  /* AI 면접 코칭 */
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
    <path d="M8 28C8 17 15 8 24 8C33 8 40 17 40 28" stroke="#34C5A7" strokeWidth="3.5" fill="none"/>
    <rect x="6" y="26" width="10" height="14" rx="3" fill="#34C5A7"/>
    <rect x="32" y="26" width="10" height="14" rx="3" fill="#34C5A7"/>
    <circle cx="11" cy="33" r="2" fill="#fff"/>
    <circle cx="37" cy="33" r="2" fill="#fff"/>
  </svg>
);

const Folder = () => (
  /* 즐겨찾기/북마크 */
  <svg width="48" height="44" viewBox="0 0 48 44" fill="none">
    <path d="M4 10C4 8.9 4.9 8 6 8H18L22 12H42C43.1 12 44 12.9 44 14V36C44 37.1 43.1 38 42 38H6C4.9 38 4 37.1 4 36V10Z" fill="#FB923C"/>
    <path d="M4 16C4 14.9 4.9 14 6 14H42C43.1 14 44 14.9 44 16V36C44 37.1 43.1 38 42 38H6C4.9 38 4 37.1 4 36V16Z" fill="#FBA572"/>
  </svg>
);

const BarChart = () => (
  /* 직군별 통계 */
  <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
    <rect x="8"  y="26" width="6" height="16" rx="2" fill="#34C5A7"/>
    <rect x="18" y="18" width="6" height="24" rx="2" fill="#34C5A7"/>
    <rect x="28" y="22" width="6" height="20" rx="2" fill="#34C5A7"/>
    <rect x="38" y="10" width="6" height="32" rx="2" fill="#34C5A7"/>
  </svg>
);

const Heart = () => (
  /* 추천/커뮤니티 */
  <svg width="48" height="44" viewBox="0 0 48 44" fill="none">
    <path d="M24 40C24 40 6 28 6 16C6 10 10 6 16 6C20 6 23 8 24 11C25 8 28 6 32 6C38 6 42 10 42 16C42 28 24 40 24 40Z" fill="#FF6B9D"/>
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

const Tile = ({ icon, label, onClick, badge }) => (
  <div onClick={onClick} style={{
    display: "flex", flexDirection: "column", alignItems: "center",
    padding: "12px 8px", borderRadius: 14, cursor: onClick ? "pointer" : "default",
    transition: "background 0.15s"
  }}
  onMouseEnter={(e) => { if (onClick) e.currentTarget.style.background = "#F7F8FA"; }}
  onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; }}>
    <div style={{ position: "relative" }}>
      <TileIcon>{icon}</TileIcon>
      {badge && (
        <span style={{
          position: "absolute", top: -2, right: -2,
          background: "#FF4757", color: "#fff",
          fontSize: 10, fontWeight: 700,
          width: 18, height: 18, borderRadius: 999,
          display: "flex", alignItems: "center", justifyContent: "center",
          letterSpacing: 0
        }}>{badge}</span>
      )}
    </div>
    <div style={{ fontSize: 13, color: "#3D434C", fontWeight: 500, textAlign: "center" }}>
      {label}
    </div>
  </div>
);

const Dashboard = () => {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const nickname = auth?.nickname ?? "회원";
  const jobCategoryName = auth?.jobCategoryName ?? "백엔드";
  const jobNews = getJobNews(jobCategoryName);
  const companyThemes = getCompanyThemes(jobCategoryName);

  const [stats, setStats] = useState(null);
  useEffect(() => {
    meApi.stats().then(setStats).catch(() => {});
  }, []);

  const tiles = [
    { icon: <Layered />,    label: "문제 풀기",     onClick: () => navigate("/questions") },
    { icon: <Document accent="#FFB800" />, label: "이력서 첨삭", onClick: () => navigate("/resume") },
    { icon: <PaperStack />, label: "내 답변",       onClick: () => navigate("/my/answers") },
    { icon: <Target />,     label: "레벨 체크",     onClick: () => navigate("/levelcheck") },
    { icon: <Donut />,      label: "학습 현황",     onClick: () => navigate("/my/status") },
    { icon: <ChatBubble />, label: "커뮤니티",      onClick: () => navigate("/community") },
    { icon: <Headphones />, label: "AI 면접 코칭",  badge: "N" },
    { icon: <Folder />,     label: "즐겨찾기" },
    { icon: <BarChart />,   label: "직군별 통계" },
    { icon: <Heart />,      label: "추천 문제",     onClick: () => navigate("/questions") },
  ];

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "#fff" }}>
      <TopNav />

      <div style={{ padding: "48px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>

        {/* 검색/배너 박스 — wanted 식 슬림 */}
        <div style={{
          maxWidth: 720, margin: "0 auto 36px",
          display: "flex", alignItems: "center", gap: 12,
          padding: "14px 22px",
          background: "#F7F8FA",
          border: "1px solid #ECEEF2",
          borderRadius: 999,
          cursor: "pointer"
        }}
          onClick={() => navigate("/levelcheck")}>
          <div style={{
            width: 36, height: 36, borderRadius: 999,
            background: "#E4ECFB", display: "flex",
            alignItems: "center", justifyContent: "center",
            fontSize: 20
          }}>
            🧑‍💻
          </div>
          <span style={{
            flex: 1, fontSize: 14, color: "#7B8290", fontWeight: 500
          }}>
            {nickname}님, 면접 준비를 시작해볼까요?
          </span>
          <IconArrowRight size={16} style={{ color: "#7B8290" }} />
        </div>

        {/* 메뉴 아이콘 그리드 */}
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(10, 1fr)",
          gap: 4,
          marginBottom: 56,
          padding: "0 8px"
        }}>
          {tiles.map((t, i) => (
            <Tile key={i} {...t} />
          ))}
        </div>

        {/* ─── 지금 주목할 소식 ─── */}
        <SectionHeader
          title="지금 주목할 소식"
          hint={`${jobCategoryName} 직군 추천`}
        />
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, 1fr)",
          gap: 16,
          marginBottom: 48
        }}>
          {jobNews.map((n) => (
            <div key={n.id} style={{
              position: "relative",
              borderRadius: 16,
              overflow: "hidden",
              aspectRatio: "16 / 11",
              background: n.gradient,
              padding: 24,
              cursor: "pointer",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between"
            }}>
              <div style={{ fontSize: 36, lineHeight: 1 }}>{n.emoji}</div>
              <div>
                <div style={{
                  fontSize: 20, fontWeight: 700, color: "#fff",
                  marginBottom: 6, lineHeight: 1.3
                }}>{n.title}</div>
                <div style={{
                  fontSize: 13, color: "rgba(255,255,255,0.85)",
                  lineHeight: 1.5
                }}>{n.subtitle}</div>
              </div>
            </div>
          ))}
        </div>

        {/* ─── 테마로 살펴보는 회사/포지션 ─── */}
        <SectionHeader
          title="테마로 살펴보는 회사/포지션"
          hint={`${jobCategoryName} 직군 추천`}
        />
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 16,
          marginBottom: 56
        }}>
          {companyThemes.map((t) => (
            <div key={t.id} style={{
              border: "1px solid #ECEEF2",
              borderRadius: 14,
              overflow: "hidden",
              cursor: "pointer",
              background: "#fff"
            }}>
              <div style={{
                aspectRatio: "16 / 9",
                background: t.bg,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 56
              }}>
                {t.icon}
              </div>
              <div style={{ padding: "16px 18px" }}>
                <div style={{
                  fontSize: 15, fontWeight: 700, color: "#1A1F2E",
                  marginBottom: 4
                }}>{t.title}</div>
                <div style={{
                  fontSize: 13, color: "#7B8290",
                  lineHeight: 1.4
                }}>{t.subtitle}</div>
              </div>
            </div>
          ))}
        </div>

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