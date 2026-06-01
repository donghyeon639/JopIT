import React, { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  TopNav, IconSearch, IconBookmark, IconChevronDown,
  IconArrowLeft, IconArrowRight, IconList,
} from "../components/Components.jsx";
import { studyApi, TECH_STACKS, POSITIONS, MODES } from "../api/studyApi.js";

const fmtDate = (iso) => (iso ? iso.slice(0, 10).replace(/-/g, ".") : "");

const dday = (deadline) => {
  if (!deadline) return null;
  const diff = Math.ceil((new Date(deadline + "T23:59:59") - Date.now()) / 86400000);
  return diff;
};

const isNew = (createdAt) =>
  createdAt && (Date.now() - new Date(createdAt).getTime()) < 86400000 * 2;

const TYPE_META = {
  STUDY:   { label: "스터디",  emoji: "✏️", badge: "badge-blue" },
  PROJECT: { label: "프로젝트", emoji: "📁", badge: "badge-gray" },
};

const typeMeta = (t) => TYPE_META[t] ?? TYPE_META.STUDY;

/* ── 마감 D-day pill ── */
const DeadlinePill = ({ deadline, status }) => {
  if (status === "CLOSED") {
    return (
      <span className="badge" style={{ background: "var(--gray-100)", color: "var(--gray-500)" }}>
        모집 마감
      </span>
    );
  }
  const d = dday(deadline);
  return (
    <span className="badge" style={{
      background: "#fff", border: "1px solid #FCA5A5", color: "var(--red-600)",
    }}>
      🔥 마감 {d <= 0 ? "오늘" : `${d}일전`}
    </span>
  );
};

const TypeBadge = ({ type }) => {
  const m = typeMeta(type);
  return <span className={`badge ${m.badge}`}>{m.emoji} {m.label}</span>;
};

/* ── 필터 드롭다운 ── */
const FilterSelect = ({ label, value, options, onChange }) => {
  const [open, setOpen] = useState(false);
  const current = options.find((o) => o.id === value);
  return (
    <div style={{ position: "relative" }}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="btn btn-outline btn-sm"
        style={{ borderRadius: 999, fontWeight: 500, color: value ? "var(--gray-900)" : "var(--gray-600)" }}
      >
        {current ? current.label : label}
        <IconChevronDown size={14} style={{ marginLeft: 2, opacity: 0.6 }} />
      </button>
      {open && (
        <>
          <div onClick={() => setOpen(false)}
               style={{ position: "fixed", inset: 0, zIndex: 40 }} />
          <div className="card" style={{
            position: "absolute", top: "calc(100% + 6px)", left: 0, zIndex: 50,
            minWidth: 160, padding: 6, boxShadow: "var(--shadow-lg)",
          }}>
            <div className="row-hover" style={rowStyle(!value)}
                 onClick={() => { onChange(null); setOpen(false); }}>전체</div>
            {options.map((o) => (
              <div key={o.id} className="row-hover" style={rowStyle(o.id === value)}
                   onClick={() => { onChange(o.id); setOpen(false); }}>{o.label}</div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

const rowStyle = (active) => ({
  padding: "8px 12px", fontSize: 13, borderRadius: 7, cursor: "pointer",
  fontWeight: active ? 700 : 500,
  color: active ? "var(--blue-600)" : "var(--gray-700)",
});

/* ── 토글 버튼 ── */
const ToggleChip = ({ active, onClick, children }) => (
  <button type="button" onClick={onClick} className="btn btn-sm"
    style={{
      borderRadius: 999, fontWeight: 600,
      border: `1px solid ${active ? "var(--blue-500)" : "var(--gray-300)"}`,
      background: active ? "var(--blue-50)" : "#fff",
      color: active ? "var(--blue-700)" : "var(--gray-700)",
    }}>
    {children}
  </button>
);

/* ── 인기글 카드 ── */
const PopularCard = ({ s, onClick }) => (
  <div className="card row-hover" onClick={onClick}
    style={{ flex: "0 0 282px", padding: 20, cursor: "pointer", display: "flex", flexDirection: "column", gap: 12 }}>
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <TypeBadge type={s.type} />
      <DeadlinePill deadline={s.deadline} status={s.status} />
    </div>
    <div className="t-xs">마감일 | {fmtDate(s.deadline)}</div>
    <div className="t-h4" style={{ ...clamp2, minHeight: 46, color: "var(--gray-900)" }}>{s.title}</div>
    <div className="t-xs" style={{ marginTop: "auto", textAlign: "right" }}>👀 조회수 {s.viewCount}회</div>
  </div>
);

/* ── 목록 카드 ── */
const StudyCard = ({ s, onClick, onBookmark }) => (
  <div className="card row-hover" onClick={onClick}
    style={{ padding: 20, cursor: "pointer", display: "flex", flexDirection: "column", gap: 10,
             opacity: s.status === "CLOSED" ? 0.65 : 1 }}>
    <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
      <TypeBadge type={s.type} />
      {isNew(s.createdAt) && (
        <span className="badge" style={{ background: "var(--amber-50)", color: "var(--amber-600)" }}>
          📦 따끈따끈 새글
        </span>
      )}
      <button type="button" onClick={(e) => { e.stopPropagation(); onBookmark(s); }}
        style={{ marginLeft: "auto", background: "none", border: "none", cursor: "pointer", padding: 2,
                 color: s.bookmarked ? "var(--blue-600)" : "var(--gray-300)" }}
        aria-label="북마크">
        <IconBookmark size={20} fill={s.bookmarked ? "var(--blue-600)" : "none"} />
      </button>
    </div>
    <div className="t-xs">마감일 | {fmtDate(s.deadline)}</div>
    <div className="t-h4" style={{ ...clamp2, minHeight: 46, color: "var(--gray-900)" }}>{s.title}</div>
    <div style={{ display: "flex", flexWrap: "wrap", gap: 5 }}>
      {s.positions.slice(0, 3).map((p) => (
        <span key={p} className="badge badge-gray" style={{ fontWeight: 500 }}>{p}</span>
      ))}
    </div>
    <div className="t-xs" style={{ display: "flex", gap: 12, paddingTop: 8, borderTop: "1px solid var(--gray-100)" }}>
      <span>👥 {s.applied}/{s.capacity}명</span>
      <span style={{ marginLeft: "auto" }}>👀 {s.viewCount}</span>
    </div>
  </div>
);

const clamp2 = {
  display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical",
  overflow: "hidden", lineHeight: 1.4,
};

const TABS = [
  { id: "all",     label: "전체" },
  { id: "PROJECT", label: "프로젝트" },
  { id: "STUDY",   label: "스터디" },
];

const StudyBoard = () => {
  const navigate = useNavigate();
  const carouselRef = useRef(null);

  const [tab, setTab] = useState("all");
  const [techStack, setTechStack] = useState(null);
  const [position, setPosition] = useState(null);
  const [mode, setMode] = useState(null);
  const [bookmarkOnly, setBookmarkOnly] = useState(false);
  const [recruitingOnly, setRecruitingOnly] = useState(false);
  const [search, setSearch] = useState("");
  const [view, setView] = useState("card");

  const [popular, setPopular] = useState([]);
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    studyApi.popular().then(setPopular).catch(() => setPopular([]));
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    studyApi.list({ type: tab, techStack, position, mode, recruitingOnly, bookmarkOnly, q: search })
      .then((data) => { if (!cancelled) setList(data); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [tab, techStack, position, mode, recruitingOnly, bookmarkOnly, search]);

  const toggleBookmark = (s) => {
    studyApi.toggleBookmark(s.id).then((res) => {
      const next = res?.bookmarked ?? !s.bookmarked;
      setList((prev) => prev.map((x) => (x.id === s.id ? { ...x, bookmarked: next } : x)));
    });
  };

  const scrollCarousel = (dir) => {
    carouselRef.current?.scrollBy({ left: dir * 300, behavior: "smooth" });
  };

  const techOptions = useMemo(() => TECH_STACKS.map((t) => ({ id: t, label: t })), []);
  const posOptions = useMemo(() => POSITIONS.map((p) => ({ id: p, label: p })), []);

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        {/* ── 헤더 ── */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: 8 }}>
          <div>
            <h1 className="t-h2" style={{ marginBottom: 6 }}>스터디</h1>
            <p className="t-body" style={{ fontSize: 14 }}>
              기술면접을 함께 준비할 스터디원과 사이드 프로젝트 팀원을 찾아보세요.
            </p>
          </div>
          <button className="btn btn-primary" onClick={() => navigate("/study/new")}>
            + 모집글 작성
          </button>
        </div>

        {/* ── 이번주 인기글 ── */}
        <div style={{ marginTop: 28, marginBottom: 12, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div className="t-h3">🔥 이번주 인기 모집글</div>
          <div style={{ display: "flex", gap: 8 }}>
            {[[-1, IconArrowLeft], [1, IconArrowRight]].map(([dir, Icon]) => (
              <button key={dir} onClick={() => scrollCarousel(dir)}
                className="btn btn-outline" style={{ width: 36, height: 36, padding: 0, borderRadius: 999 }}>
                <Icon size={16} />
              </button>
            ))}
          </div>
        </div>
        <div ref={carouselRef}
          style={{ display: "flex", gap: 16, overflowX: "auto", paddingBottom: 6, scrollbarWidth: "none" }}>
          {popular.map((s) => (
            <PopularCard key={s.id} s={s} onClick={() => navigate(`/study/${s.id}`)} />
          ))}
        </div>

        {/* ── 탭 ── */}
        <div style={{ display: "flex", gap: 24, marginTop: 36, marginBottom: 18, borderBottom: "1px solid var(--gray-200)" }}>
          {TABS.map((t) => (
            <div key={t.id} onClick={() => setTab(t.id)}
              style={{
                paddingBottom: 12, cursor: "pointer", fontSize: 17,
                fontWeight: tab === t.id ? 700 : 500,
                color: tab === t.id ? "var(--gray-900)" : "var(--gray-400)",
                borderBottom: `2px solid ${tab === t.id ? "var(--gray-900)" : "transparent"}`,
                marginBottom: -1,
              }}>{t.label}</div>
          ))}
        </div>

        {/* ── 필터바 ── */}
        <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap", marginBottom: 14 }}>
          <FilterSelect label="기술 스택" value={techStack} options={techOptions} onChange={setTechStack} />
          <FilterSelect label="포지션" value={position} options={posOptions} onChange={setPosition} />
          <FilterSelect label="진행 방식" value={mode} options={MODES} onChange={setMode} />
          <ToggleChip active={bookmarkOnly} onClick={() => setBookmarkOnly((v) => !v)}>👋 내 북마크 보기</ToggleChip>
          <ToggleChip active={recruitingOnly} onClick={() => setRecruitingOnly((v) => !v)}>👀 모집 중만 보기</ToggleChip>

          <div style={{ marginLeft: "auto", position: "relative", width: 280 }}>
            <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)",
                           color: "var(--gray-400)", display: "flex", pointerEvents: "none" }}>
              <IconSearch size={16} />
            </span>
            <input className="input" placeholder="제목, 글 내용을 검색해보세요."
              value={search} onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: 36, borderRadius: 999 }} />
          </div>
        </div>

        {/* ── 뷰 전환 ── */}
        <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
          <button className="btn btn-ghost btn-sm" onClick={() => setView((v) => (v === "card" ? "list" : "card"))}
            style={{ color: "var(--gray-600)", fontWeight: 500 }}>
            <IconList size={15} style={{ marginRight: 4 }} />
            {view === "card" ? "리스트뷰 보기" : "카드뷰 보기"}
          </button>
        </div>

        {/* ── 목록 ── */}
        {loading ? (
          <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
        ) : list.length === 0 ? (
          <div className="card" style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
            조건에 맞는 모집글이 없어요. 가장 먼저 스터디를 모집해보세요!
          </div>
        ) : view === "card" ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 20 }}>
            {list.map((s) => (
              <StudyCard key={s.id} s={s} onClick={() => navigate(`/study/${s.id}`)} onBookmark={toggleBookmark} />
            ))}
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {list.map((s) => (
              <div key={s.id} className="card row-hover" onClick={() => navigate(`/study/${s.id}`)}
                style={{ padding: "16px 20px", display: "flex", alignItems: "center", gap: 16, cursor: "pointer",
                         opacity: s.status === "CLOSED" ? 0.65 : 1 }}>
                <div style={{ display: "flex", flexDirection: "column", gap: 6, flex: 1, minWidth: 0 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                    <TypeBadge type={s.type} />
                    {isNew(s.createdAt) && (
                      <span className="badge" style={{ background: "var(--amber-50)", color: "var(--amber-600)" }}>📦 새글</span>
                    )}
                    <DeadlinePill deadline={s.deadline} status={s.status} />
                  </div>
                  <div className="t-h4" style={{ ...clamp2, WebkitLineClamp: 1, color: "var(--gray-900)" }}>{s.title}</div>
                  <div className="t-xs">{s.positions.join(" · ")} · {fmtDate(s.deadline)} 마감</div>
                </div>
                <div className="t-xs" style={{ textAlign: "right", whiteSpace: "nowrap" }}>
                  👥 {s.applied}/{s.capacity}<br />👀 {s.viewCount}
                </div>
                <button type="button" onClick={(e) => { e.stopPropagation(); toggleBookmark(s); }}
                  style={{ background: "none", border: "none", cursor: "pointer",
                           color: s.bookmarked ? "var(--blue-600)" : "var(--gray-300)" }} aria-label="북마크">
                  <IconBookmark size={20} fill={s.bookmarked ? "var(--blue-600)" : "none"} />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default StudyBoard;