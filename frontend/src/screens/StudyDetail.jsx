import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  TopNav, IconBookmark, IconArrowLeft, IconClock, IconUser,
} from "../components/Components.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { studyApi, MODES } from "../api/studyApi.js";

const fmtDate = (iso) => (iso ? iso.slice(0, 10).replace(/-/g, ".") : "");

const dday = (deadline) => {
  if (!deadline) return null;
  return Math.ceil((new Date(deadline + "T23:59:59") - Date.now()) / 86400000);
};

const TYPE_META = {
  STUDY:   { label: "스터디",  emoji: "✏️", badge: "badge-blue" },
  PROJECT: { label: "프로젝트", emoji: "📁", badge: "badge-gray" },
};

const InfoRow = ({ label, children }) => (
  <div style={{ display: "grid", gridTemplateColumns: "100px 1fr", gap: 16, alignItems: "start", padding: "12px 0" }}>
    <div className="t-sm" style={{ fontWeight: 500, color: "var(--gray-600)" }}>{label}</div>
    <div style={{ fontSize: 14, color: "var(--gray-900)" }}>{children}</div>
  </div>
);

const StudyDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { auth } = useAuth();

  const [study, setStudy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [applyStatus, setApplyStatus] = useState(null); // null | "PENDING"
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    studyApi.detail(id)
      .then((s) => { if (!cancelled) setStudy(s); })
      .catch((e) => { if (!cancelled) setError(e.message || "스터디 정보를 불러오지 못했어요."); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [id]);

  if (loading) {
    return (
      <div className="dp-screen" style={{ minHeight: "100vh", background: "var(--gray-50)" }}>
        <TopNav />
        <div className="dp-page" style={{ maxWidth: 1280 }}>
          <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
        </div>
      </div>
    );
  }

  if (error || !study) {
    return (
      <div className="dp-screen" style={{ minHeight: "100vh", background: "var(--gray-50)" }}>
        <TopNav />
        <div className="dp-page" style={{ maxWidth: 1280 }}>
          <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--red-600)" }}>
            {error || "스터디를 찾을 수 없어요."}
          </div>
        </div>
      </div>
    );
  }

  const meta = TYPE_META[study.type] ?? TYPE_META.STUDY;
  // 백엔드 StudyDetailResponse.owner 플래그를 우선 사용, 없으면 닉네임 fallback
  const isOwner = typeof study.owner === "boolean" ? study.owner : (auth?.nickname === study.author);
  const isClosed = study.status === "CLOSED";
  const isFull = study.applied >= study.capacity;
  const d = dday(study.deadline);
  const modeLabel = MODES.find((m) => m.id === study.mode)?.label ?? study.mode;

  const handleBookmark = async () => {
    setBusy(true);
    try {
      const res = await studyApi.toggleBookmark(study.id);
      const next = res?.bookmarked ?? !study.bookmarked;
      setStudy((prev) => ({ ...prev, bookmarked: next }));
    } finally { setBusy(false); }
  };

  const handleApply = async () => {
    if (isClosed || isFull || applyStatus) return;
    setBusy(true);
    try {
      const res = await studyApi.apply(study.id, {});
      setApplyStatus(res?.status ?? "PENDING");
    } catch (e) {
      alert(e.message || "신청에 실패했어요.");
    } finally { setBusy(false); }
  };

  const handleClose = async () => {
    if (!confirm("정말 모집을 마감하시겠어요? 이후 신청을 받을 수 없습니다.")) return;
    setBusy(true);
    try {
      await studyApi.close(study.id);
      setStudy((prev) => ({ ...prev, status: "CLOSED" }));
    } finally { setBusy(false); }
  };

  return (
    <div className="dp-screen" style={{ minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "24px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        {/* 뒤로 가기 */}
        <div onClick={() => navigate("/study")}
          style={{ display: "inline-flex", alignItems: "center", gap: 6, cursor: "pointer",
                   color: "var(--gray-600)", fontSize: 14, marginBottom: 16 }}>
          <IconArrowLeft size={14} />
          <span>스터디 목록으로</span>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: 24, alignItems: "start" }}>
          {/* ── 메인 ── */}
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div className="card" style={{ padding: 32 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 14 }}>
                <span className={`badge ${meta.badge}`}>{meta.emoji} {meta.label}</span>
                {isClosed ? (
                  <span className="badge" style={{ background: "var(--gray-100)", color: "var(--gray-500)" }}>모집 마감</span>
                ) : (
                  <span className="badge" style={{ background: "#fff", border: "1px solid #FCA5A5", color: "var(--red-600)" }}>
                    🔥 마감 {d <= 0 ? "오늘" : `${d}일전`}
                  </span>
                )}
              </div>

              <h1 className="t-h2" style={{ marginBottom: 10 }}>{study.title}</h1>

              <div className="t-sm" style={{ display: "flex", gap: 14, alignItems: "center", paddingBottom: 18, borderBottom: "1px solid var(--gray-100)" }}>
                <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
                  <IconUser size={13} /> {study.author}
                </span>
                <span>·</span>
                <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
                  <IconClock size={13} /> {fmtDate(study.createdAt)}
                </span>
                <span style={{ marginLeft: "auto" }}>조회 {study.viewCount}</span>
              </div>

              <div style={{ paddingTop: 4 }}>
                <InfoRow label="진행 방식">{modeLabel}</InfoRow>
                <InfoRow label="정원">
                  {study.applied}/{study.capacity}명 {isFull && <span style={{ color: "var(--red-600)", marginLeft: 6 }}>(마감 임박)</span>}
                </InfoRow>
                <InfoRow label="마감일">{fmtDate(study.deadline)}</InfoRow>
                <InfoRow label="기술 스택">
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
                    {study.techStacks.map((t) => (
                      <span key={t} className="badge badge-blue" style={{ fontWeight: 500 }}>{t}</span>
                    ))}
                  </div>
                </InfoRow>
                <InfoRow label="포지션">
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
                    {study.positions.map((p) => (
                      <span key={p} className="badge badge-gray" style={{ fontWeight: 500 }}>{p}</span>
                    ))}
                  </div>
                </InfoRow>
              </div>
            </div>

            <div className="card" style={{ padding: 32 }}>
              <div className="t-h4" style={{ marginBottom: 12 }}>소개</div>
              <div className="t-body" style={{ whiteSpace: "pre-wrap" }}>{study.summary}</div>
            </div>
          </div>

          {/* ── 사이드바 ── */}
          <div style={{ display: "flex", flexDirection: "column", gap: 16, position: "sticky", top: 88 }}>
            <div className="card" style={{ padding: 24 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 16 }}>
                <div className="avatar" style={{ background: "var(--blue-600)" }}>{study.author?.[0]}</div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{study.author}</div>
                  <div className="t-xs">모집글 작성자</div>
                </div>
              </div>

              {isOwner ? (
                <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                  <button className="btn btn-outline" onClick={() => navigate(`/study/${study.id}/edit`)}>
                    수정하기
                  </button>
                  {!isClosed && (
                    <button className="btn btn-secondary" disabled={busy} onClick={handleClose}>
                      모집 마감하기
                    </button>
                  )}
                  <button className="btn btn-ghost" disabled
                    style={{ color: "var(--gray-400)" }}
                    title="다음 task에서 추가 예정">
                    신청자 관리 (준비 중)
                  </button>
                </div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                  <button className="btn btn-primary btn-lg" disabled={isClosed || isFull || !!applyStatus || busy} onClick={handleApply}>
                    {applyStatus === "PENDING" ? "신청 완료 (대기 중)" :
                      isClosed ? "모집 마감됨" :
                      isFull ? "정원이 모두 찼어요" : "참여 신청하기"}
                  </button>
                  <button className="btn btn-outline" onClick={handleBookmark} disabled={busy}>
                    <IconBookmark size={16} fill={study.bookmarked ? "var(--blue-600)" : "none"} stroke={study.bookmarked ? "var(--blue-600)" : "currentColor"} />
                    {study.bookmarked ? "북마크 해제" : "북마크"}
                  </button>
                </div>
              )}
            </div>

            <div className="card" style={{ padding: 20, fontSize: 13, color: "var(--gray-600)", lineHeight: 1.6 }}>
              💡 처음 만나는 분들과 함께 공부할 땐, 목표·진행 시간·연락 채널을 먼저 합의해두면 좋아요.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudyDetail;
