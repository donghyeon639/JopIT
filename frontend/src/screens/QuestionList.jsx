import React, { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconHeart, IconChevronRight, IconChevronDown, IconCheck,
  IconSearch, IconUser,
  TopNav, DifficultyBadge, CategoryBadge
} from "../components/Components.jsx";
import { questionApi, categoryApi } from "../api/questionApi.js";
import { useAuth } from "../context/AuthContext.jsx";

const DIFF_OPTIONS = ["전체", "LOW", "MID", "HIGH"];
const DIFF_LABEL   = { LOW: "하", MID: "중", HIGH: "상" };

const QuestionList = () => {
  const navigate   = useNavigate();
  const { isLoggedIn } = useAuth();

  const [categories,   setCategories]   = useState([]);
  const [questions,    setQuestions]    = useState([]);
  const [myAnswerQIds, setMyAnswerQIds] = useState(new Set());
  const [activeCatId,  setActiveCatId]  = useState(null);   // null = 전체
  const [activeDiff,   setActiveDiff]   = useState("전체");
  const [search,       setSearch]       = useState("");
  const [loading,      setLoading]      = useState(true);
  const [fetchError,   setFetchError]   = useState(false);

  // 카테고리 목록
  useEffect(() => {
    categoryApi.list().then(setCategories).catch(console.error);
  }, []);

  // 문제 목록 (카테고리 변경 시 재조회)
  useEffect(() => {
    setLoading(true);
    setFetchError(false);
    questionApi.list(activeCatId)
      .then(setQuestions)
      .catch(() => setFetchError(true))
      .finally(() => setLoading(false));
  }, [activeCatId]);

  // 내가 푼 문제 ID 세트 (로그인한 경우만)
  useEffect(() => {
    if (!isLoggedIn) return;
    questionApi.myAnswers()
      .then(answers => setMyAnswerQIds(new Set(answers.map(a => a.questionId))))
      .catch(() => {});
  }, [isLoggedIn]);

  // 클라이언트 필터 (난이도 + 검색)
  const filtered = useMemo(() => {
    return questions.filter(q => {
      const matchDiff   = activeDiff === "전체" || q.difficulty === activeDiff;
      const matchSearch = !search.trim() ||
        q.title.toLowerCase().includes(search.toLowerCase()) ||
        q.questionCategoryName?.toLowerCase().includes(search.toLowerCase());
      return matchDiff && matchSearch;
    });
  }, [questions, activeDiff, search]);

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "32px 48px 80px", maxWidth: 1280, margin: "0 auto" }}>
        {/* 헤더 */}
        <div style={{ marginBottom: 24 }}>
          <h1 className="t-h2" style={{ marginBottom: 6 }}>문제 풀어보기</h1>
          <p className="t-body" style={{ fontSize: 14 }}>
            직군별 기술 면접 문제를 풀어보세요.
          </p>
        </div>

        {/* 직군 탭 */}
        <div style={{ display: "flex", gap: 4, marginBottom: 20, borderBottom: "1px solid var(--gray-200)" }}>
          <div
            onClick={() => setActiveCatId(null)}
            style={{
              padding: "12px 18px", fontSize: 14, fontWeight: 600, cursor: "pointer", marginBottom: -1,
              borderBottom: "2px solid",
              borderColor: activeCatId === null ? "var(--blue-600)" : "transparent",
              color: activeCatId === null ? "var(--blue-700)" : "var(--gray-500)"
            }}
          >
            전체
          </div>
          {categories.map(cat => (
            <div
              key={cat.id}
              onClick={() => setActiveCatId(cat.id)}
              style={{
                padding: "12px 18px", fontSize: 14, fontWeight: 600, cursor: "pointer", marginBottom: -1,
                borderBottom: "2px solid",
                borderColor: activeCatId === cat.id ? "var(--blue-600)" : "transparent",
                color: activeCatId === cat.id ? "var(--blue-700)" : "var(--gray-500)"
              }}
            >
              {cat.name}
            </div>
          ))}
        </div>

        {/* 필터 바 */}
        <div className="card" style={{ padding: 20, marginBottom: 16 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 14 }}>
            <div style={{ flex: 1, position: "relative" }}>
              <div style={{ position: "absolute", left: 14, top: "50%", transform: "translateY(-50%)", color: "var(--gray-400)" }}>
                <IconSearch size={16} />
              </div>
              <input
                className="input"
                style={{ paddingLeft: 40 }}
                placeholder="문제 제목, 키워드로 검색해보세요"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: "flex", gap: 6, flexWrap: "wrap", alignItems: "center" }}>
            <span className="t-xs" style={{ alignSelf: "center", marginRight: 6, fontWeight: 600, color: "var(--gray-700)" }}>
              난이도
            </span>
            {DIFF_OPTIONS.map(d => (
              <Pill key={d} active={activeDiff === d} onClick={() => setActiveDiff(d)}>
                {d === "전체" ? "전체" : DIFF_LABEL[d]}
              </Pill>
            ))}
            <div style={{ flex: 1 }} />
            <span className="t-xs">
              총 <b style={{ color: "var(--gray-900)" }}>{filtered.length}</b>개 문제
            </span>
          </div>
        </div>

        {/* 문제 목록 */}
        <div className="card" style={{ overflow: "hidden" }}>
          <div style={{
            display: "grid",
            gridTemplateColumns: "60px 1fr 120px 100px 60px",
            padding: "14px 20px",
            background: "var(--gray-50)",
            borderBottom: "1px solid var(--gray-200)",
            fontSize: 12, fontWeight: 600, color: "var(--gray-500)"
          }}>
            <div>상태</div>
            <div>문제</div>
            <div>카테고리</div>
            <div>난이도</div>
            <div></div>
          </div>

          {loading ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
          ) : fetchError ? (
            <div style={{ padding: 60, textAlign: "center", color: "#DC2626" }}>
              문제를 불러오지 못했습니다. 서버 연결을 확인해주세요.
            </div>
          ) : filtered.length === 0 ? (
            <div style={{ padding: 60, textAlign: "center", color: "var(--gray-400)" }}>
              {search || activeDiff !== "전체"
                ? "검색 결과가 없습니다."
                : "등록된 문제가 없습니다. 관리자 페이지에서 문제를 추가해보세요."}
            </div>
          ) : (
            filtered.map((q, i) => {
              const solved = myAnswerQIds.has(q.id);
              return (
                <div
                  key={q.id}
                  className="row-hover"
                  onClick={() => navigate(`/solve?id=${q.id}`)}
                  style={{
                    display: "grid",
                    gridTemplateColumns: "60px 1fr 120px 100px 60px",
                    padding: "16px 20px", alignItems: "center",
                    borderBottom: i === filtered.length - 1 ? "none" : "1px solid var(--gray-100)",
                    cursor: "pointer"
                  }}
                >
                  <div>
                    {solved ? (
                      <div style={{
                        width: 22, height: 22, borderRadius: 999,
                        background: "var(--green-50)", color: "var(--green-700)",
                        display: "flex", alignItems: "center", justifyContent: "center"
                      }}>
                        <IconCheck size={12} />
                      </div>
                    ) : (
                      <div style={{ width: 22, height: 22, borderRadius: 999, border: "1.5px solid var(--gray-300)" }} />
                    )}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4, color: solved ? "var(--gray-500)" : "var(--gray-900)" }}>
                      {q.title}
                    </div>
                  </div>
                  <div><CategoryBadge name={q.questionCategoryName} /></div>
                  <div><DifficultyBadge level={q.difficulty?.toLowerCase()} /></div>
                  <div style={{ textAlign: "right" }}>
                    <IconChevronRight size={16} stroke="var(--gray-400)" />
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};

const Pill = ({ children, active, onClick }) => (
  <div onClick={onClick} style={{
    padding: "6px 12px", fontSize: 13, fontWeight: 500, cursor: "pointer",
    borderRadius: 999,
    background: active ? "var(--gray-900)" : "var(--gray-100)",
    color: active ? "#fff" : "var(--gray-700)",
    transition: "all 0.15s"
  }}>{children}</div>
);

export default QuestionList;
export { Pill };