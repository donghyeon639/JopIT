import React from "react";
import { useNavigate } from "react-router-dom";
import { PrepBot } from "../components/PrepBot.jsx";

const LevelCheck = () => {
  const navigate = useNavigate();
  const [step, setStep] = React.useState(0); // 0: category, 1: role, 2: quiz, 3: result
  const [category, setCategory] = React.useState(null);
  const [role, setRole] = React.useState(null);
  const [qIdx, setQIdx] = React.useState(0);
  const [answers, setAnswers] = React.useState([]);

  const CATEGORIES = [
    { id: "dev", name: "개발", desc: "프론트/백엔드/모바일 등", emoji: "💻", color: "#4F46E5", count: "6개 직군" },
    { id: "design", name: "디자인", desc: "UI/UX, 브랜딩, 프로덕트", emoji: "🎨", color: "#EC4899", count: "4개 직군" },
    { id: "plan", name: "기획", desc: "PM, PO, 서비스 기획", emoji: "📋", color: "#0EA5E9", count: "3개 직군" },
    { id: "marketing", name: "마케팅", desc: "퍼포말스, 콘텐츠, 그로스", emoji: "📢", color: "#F59E0B", count: "4개 직군" },
    { id: "data", name: "데이터", desc: "분석가, 사이언티스트, 엔지니어", emoji: "📊", color: "#10B981", count: "3개 직군" },
    { id: "biz", name: "비즈니스", desc: "영업, 전략, 운영, HR", emoji: "💼", color: "#8B5CF6", count: "5개 직군" },
  ];

  const ROLES_BY_CAT = {
    dev: [
      { id: "fe", name: "프론트엔드", desc: "React, TypeScript", emoji: "🖥️", color: "#3B82F6" },
      { id: "be", name: "백엔드", desc: "Spring, Node.js", emoji: "⚙️", color: "#10B981" },
      { id: "android", name: "안드로이드", desc: "Kotlin, Compose", emoji: "📱", color: "#22C55E" },
      { id: "ios", name: "iOS", desc: "Swift, SwiftUI", emoji: "🍎", color: "#A855F7" },
      { id: "data", name: "데이터 엔지니어", desc: "SQL, Python", emoji: "🗄️", color: "#F59E0B" },
      { id: "devops", name: "DevOps", desc: "K8s, AWS", emoji: "☁️", color: "#06B6D4" },
    ],
    design: [
      { id: "ux", name: "UX 디자이너", desc: "리서치, 설계", emoji: "🔍", color: "#EC4899" },
      { id: "ui", name: "UI 디자이너", desc: "프로덕트 디자인", emoji: "📐", color: "#F472B6" },
      { id: "brand", name: "브랜딩 디자이너", desc: "비주얼, 아이덴티티", emoji: "🎨", color: "#A855F7" },
      { id: "product", name: "프로덕트 디자이너", desc: "프로덕트 전반", emoji: "💡", color: "#FB7185" },
    ],
    plan: [
      { id: "pm", name: "프로덕트 매니저", desc: "PM", emoji: "📋", color: "#0EA5E9" },
      { id: "po", name: "프로덕트 오너", desc: "PO", emoji: "🎯", color: "#3B82F6" },
      { id: "service", name: "서비스 기획자", desc: "서비스/운영", emoji: "🔧", color: "#06B6D4" },
    ],
    marketing: [
      { id: "perf", name: "퍼포말스 마케터", desc: "광고/퍼널", emoji: "📈", color: "#F59E0B" },
      { id: "content", name: "콘텐츠 마케터", desc: "글, 영상", emoji: "✍️", color: "#FB923C" },
      { id: "growth", name: "그로스 마케터", desc: "지표, 실험", emoji: "🚀", color: "#EF4444" },
      { id: "brandm", name: "브랜드 마케터", desc: "캠페인", emoji: "📣", color: "#F97316" },
    ],
    data: [
      { id: "da", name: "데이터 분석가", desc: "SQL, BI", emoji: "📊", color: "#10B981" },
      { id: "ds", name: "데이터 사이언티스트", desc: "ML, 통계", emoji: "🧪", color: "#22C55E" },
      { id: "de", name: "데이터 엔지니어", desc: "파이프라인", emoji: "🔗", color: "#14B8A6" },
    ],
    biz: [
      { id: "sales", name: "영업", desc: "B2B, B2C", emoji: "🤝", color: "#8B5CF6" },
      { id: "strategy", name: "전략/기획", desc: "경영전략", emoji: "🎲", color: "#7C3AED" },
      { id: "ops", name: "운영", desc: "CS, 스토어", emoji: "⚙️", color: "#A78BFA" },
      { id: "hr", name: "HR", desc: "채용, 조직", emoji: "👥", color: "#C084FC" },
      { id: "finance", name: "재무", desc: "회계, 재무", emoji: "💰", color: "#9333EA" },
    ],
  };

  const QUIZZES = {
    fe: [
      { type: "code", title: "다음 코드의 출력은?", code: "const arr = [1,2,3];\narr.map(x => x*2)\n  .filter(x => x>2);", choices: ["[2,4,6]", "[4,6]", "[2,4]", "[6]"], answer: 1 },
      { type: "concept", title: "Virtual DOM의 역할은?", choices: ["DOM 직접 조작 차단", "변경 사항을 모아 효율적으로 반영", "CSS 렌더링 최적화", "메모리 누수 방지"], answer: 1 },
      { type: "concept", title: "useEffect의 의존성 배열에 빈 배열을 넣으면?", choices: ["매 렌더마다 실행", "마운트 시 1번만", "언마운트 시", "에러 발생"], answer: 1 },
    ],
    be: [
      { type: "code", title: "Idempotent한 HTTP 메서드는?", choices: ["POST", "PATCH", "PUT", "CONNECT"], answer: 2 },
      { type: "concept", title: "DB 인덱스의 단점은?", choices: ["조회 속도 저하", "쓰기 작업 시 오버헤드", "데이터 정합성 손실", "용량 절약"], answer: 1 },
      { type: "concept", title: "트랜잭션 ACID 중 'I'는?", choices: ["Integrity", "Independence", "Isolation", "Identity"], answer: 2 },
    ],
    android: [
      { type: "concept", title: "Activity Lifecycle 순서는?", choices: ["onCreate→onStart→onResume", "onStart→onCreate→onResume", "onResume→onCreate→onStart", "onCreate→onResume→onStart"], answer: 0 },
      { type: "concept", title: "Jetpack Compose의 핵심 개념은?", choices: ["XML 기반 UI", "선언형 UI", "MVC 아키텍처", "Fragment 관리"], answer: 1 },
      { type: "concept", title: "Coroutine의 장점은?", choices: ["멀티스레드 제거", "경량 동시성", "GC 비활성화", "UI 자동 갱신"], answer: 1 },
    ],
    ios: [
      { type: "concept", title: "Swift의 옵셔널이 해결하는 문제는?", choices: ["성능 최적화", "nil 안전성", "메모리 관리", "동시성 제어"], answer: 1 },
      { type: "concept", title: "ARC가 관리하는 것은?", choices: ["스레드", "참조 카운트", "네트워크", "캐시"], answer: 1 },
      { type: "concept", title: "SwiftUI의 @State 역할은?", choices: ["전역 상태", "뷰 로컬 상태", "네트워크 상태", "DB 상태"], answer: 1 },
    ],
    data: [
      { type: "code", title: "SELECT COUNT(DISTINCT id)의 의미는?", choices: ["전체 행 수", "고유한 id 개수", "id 합계", "최대값"], answer: 1 },
      { type: "concept", title: "JOIN 중 '교집합'을 반환하는 것은?", choices: ["LEFT", "RIGHT", "INNER", "FULL"], answer: 2 },
      { type: "concept", title: "정규화의 목적은?", choices: ["조회 속도 향상", "중복 최소화", "용량 증가", "보안 강화"], answer: 1 },
    ],
    devops: [
      { type: "concept", title: "Kubernetes Pod이란?", choices: ["VM 단위", "컨테이너 그룹 최소 단위", "노드", "네트워크"], answer: 1 },
      { type: "concept", title: "CI/CD에서 CD는?", choices: ["Continuous Design", "Code Delivery", "Continuous Deployment", "Cloud Deploy"], answer: 2 },
      { type: "concept", title: "IaC 대표 도구는?", choices: ["Jenkins", "Terraform", "Grafana", "Slack"], answer: 1 },
    ],
  };

  const GENERIC_QUIZ = [
    { type: "concept", title: "\"자기소개\" 1분 안에 꼭 담아야 할 것은?", choices: ["학력/수상 이력 전부", "내 강점 + 지원 직무 적합성", "은행 이력서도 골골", "특이한 취미단어"], answer: 1 },
    { type: "concept", title: "\"갈등 경험\" 질문에서 핵심 구조는?", choices: ["상황 → 상대탓", "상황 → 행동 → 결과 → 배움", "내 입장만 상세히", "높은 직책 동료님을 이기는 사례"], answer: 1 },
    { type: "concept", title: "\"우리 회사에 와야 하는 이유는?\" 질문의 의도는?", choices: ["연봉 협상", "지원자의 동기와 회사 이해도 확인", "경쟁사 정보 수집", "가족관계 파악"], answer: 1 },
  ];

  const roles = category ? ROLES_BY_CAT[category.id] : [];
  const quiz = role ? (QUIZZES[role.id] || GENERIC_QUIZ) : [];

  const select = (i) => {
    const next = [...answers, i];
    setAnswers(next);
    if (qIdx + 1 < quiz.length) setQIdx(qIdx + 1);
    else setStep(3);
  };

  const score = answers.reduce((s, a, i) => s + (a === quiz[i]?.answer ? 1 : 0), 0);
  const percent = quiz.length ? Math.round((score / quiz.length) * 100) : 0;

  return (
    <div style={{ width: "100%", minHeight: "100vh", background: "linear-gradient(180deg, #EEF2FF 0%, #fff 60%)", position: "relative", fontFamily: "Pretendard, sans-serif" }}>
      {/* close */}
      <button onClick={() => navigate("/")} style={{ position: "absolute", top: 24, right: 24, width: 40, height: 40, borderRadius: 999, border: "1px solid #E5E7EB", background: "#fff", cursor: "pointer", fontSize: 18 }}>✕</button>

      {/* progress */}
      <div style={{ position: "absolute", top: 24, left: 24, right: 80, height: 6, background: "#E5E7EB", borderRadius: 999, overflow: "hidden" }}>
        <div style={{ width: `${((step + (step === 2 ? qIdx / quiz.length : 0)) / 3) * 100}%`, height: "100%", background: "#4F46E5", transition: "width 0.3s" }} />
      </div>

      <div style={{ maxWidth: 720, margin: "0 auto", padding: "100px 32px 40px" }}>

        {/* STEP 0: category select */}
        {step === 0 && (
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 32 }}>
              <PrepBot expression="wave" size={80} accent="#4F46E5" />
              <div>
                <div style={{ fontSize: 14, color: "#6366F1", fontWeight: 700, marginBottom: 6 }}>STEP 1 / 3</div>
                <h2 style={{ fontSize: 32, fontWeight: 800, letterSpacing: "-0.02em", margin: 0 }}>어떤 분야에서 일하세요?</h2>
                <p style={{ color: "#6B7280", marginTop: 6, fontSize: 15 }}>개발뿐만 아니라 다양한 직군을 준비할 수 있어요.</p>
              </div>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
              {CATEGORIES.map((c) => (
                <button key={c.id} onClick={() => { setCategory(c); setStep(1); }} style={{
                  padding: 20, borderRadius: 16, border: "1.5px solid #E5E7EB", background: "#fff",
                  cursor: "pointer", textAlign: "left", fontFamily: "inherit", transition: "all 0.15s",
                }}
                  onMouseEnter={(e) => { e.currentTarget.style.borderColor = c.color; e.currentTarget.style.transform = "translateY(-2px)"; }}
                  onMouseLeave={(e) => { e.currentTarget.style.borderColor = "#E5E7EB"; e.currentTarget.style.transform = "none"; }}>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>{c.emoji}</div>
                  <div style={{ fontSize: 16, fontWeight: 700 }}>{c.name}</div>
                  <div style={{ fontSize: 12, color: "#9CA3AF", marginTop: 2 }}>{c.desc}</div>
                  <div style={{ fontSize: 11, color: c.color, fontWeight: 700, marginTop: 8, paddingTop: 8, borderTop: "1px dashed #F3F4F6" }}>{c.count}</div>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 1: role select */}
        {step === 1 && category && (
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
              <PrepBot expression="happy" size={80} accent={category.color} />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, color: category.color, fontWeight: 700, marginBottom: 6 }}>STEP 2 / 3 · {category.name}</div>
                <h2 style={{ fontSize: 32, fontWeight: 800, letterSpacing: "-0.02em", margin: 0 }}>세부 직군을 알려주세요</h2>
                <p style={{ color: "#6B7280", marginTop: 6, fontSize: 15 }}>맞춤 문제로 3분만에 수준을 봐드릴게요.</p>
              </div>
              <button onClick={() => { setStep(0); setCategory(null); }} style={{ padding: "8px 14px", border: "1px solid #E5E7EB", background: "#fff", borderRadius: 999, fontSize: 13, color: "#6B7280", cursor: "pointer", fontFamily: "inherit" }}>← 카테고리 다시</button>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
              {roles.map((r) => (
                <button key={r.id} onClick={() => { setRole(r); setStep(2); }} style={{
                  padding: 20, borderRadius: 16, border: "1.5px solid #E5E7EB", background: "#fff",
                  cursor: "pointer", textAlign: "left", fontFamily: "inherit", transition: "all 0.15s",
                }}
                  onMouseEnter={(e) => { e.currentTarget.style.borderColor = r.color; e.currentTarget.style.transform = "translateY(-2px)"; }}
                  onMouseLeave={(e) => { e.currentTarget.style.borderColor = "#E5E7EB"; e.currentTarget.style.transform = "none"; }}>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>{r.emoji}</div>
                  <div style={{ fontSize: 16, fontWeight: 700 }}>{r.name}</div>
                  <div style={{ fontSize: 12, color: "#9CA3AF", marginTop: 2 }}>{r.desc}</div>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 2: quiz */}
        {step === 2 && quiz[qIdx] && (
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 24 }}>
              <span style={{ padding: "4px 10px", background: role.color + "22", color: role.color, fontSize: 12, fontWeight: 700, borderRadius: 999 }}>{role.name}</span>
              <span style={{ fontSize: 13, color: "#6B7280", fontWeight: 600 }}>문제 {qIdx + 1} / {quiz.length}</span>
            </div>
            <h2 style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-0.02em", margin: "0 0 20px", lineHeight: 1.4 }}>
              {quiz[qIdx].title}
            </h2>
            {quiz[qIdx].code && (
              <pre style={{ background: "#1F2937", color: "#E5E7EB", padding: 20, borderRadius: 12, fontSize: 14, lineHeight: 1.6, marginBottom: 24, fontFamily: "ui-monospace, monospace" }}>
                {quiz[qIdx].code}
              </pre>
            )}
            <div style={{ display: "grid", gap: 10 }}>
              {quiz[qIdx].choices.map((c, i) => (
                <button key={i} onClick={() => select(i)} style={{
                  padding: "16px 20px", borderRadius: 12, border: "1.5px solid #E5E7EB", background: "#fff",
                  cursor: "pointer", textAlign: "left", fontFamily: "inherit", fontSize: 15,
                  display: "flex", alignItems: "center", gap: 12,
                }}
                  onMouseEnter={(e) => { e.currentTarget.style.borderColor = "#4F46E5"; e.currentTarget.style.background = "#EEF2FF"; }}
                  onMouseLeave={(e) => { e.currentTarget.style.borderColor = "#E5E7EB"; e.currentTarget.style.background = "#fff"; }}>
                  <span style={{ width: 28, height: 28, borderRadius: 999, background: "#F3F4F6", display: "inline-flex", alignItems: "center", justifyContent: "center", fontSize: 13, fontWeight: 700, color: "#6B7280" }}>
                    {String.fromCharCode(65 + i)}
                  </span>
                  {c}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 3: result */}
        {step === 3 && (
          <div>
            <div style={{ textAlign: "center", marginBottom: 32 }}>
              <PrepBot expression={percent >= 70 ? "celebrate" : "thinking"} size={120} accent="#4F46E5" />
              <div style={{ fontSize: 14, color: "#6366F1", fontWeight: 700, marginTop: 12, marginBottom: 8 }}>{role.name} · 수준 진단 완료</div>
              <h2 style={{ fontSize: 36, fontWeight: 800, letterSpacing: "-0.025em", margin: 0 }}>
                {percent >= 80 ? "탄탄해요! 💪" : percent >= 50 ? "감을 잡으셨네요" : "지금부터 시작해요"}
              </h2>
            </div>

            <div style={{ background: "#fff", border: "1px solid #E5E7EB", borderRadius: 20, padding: 32, boxShadow: "0 8px 24px -8px rgba(0,0,0,0.08)", marginBottom: 16 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 24 }}>
                <div style={{
                  width: 120, height: 120, borderRadius: 999, position: "relative",
                  background: `conic-gradient(#4F46E5 ${percent * 3.6}deg, #E5E7EB 0deg)`,
                  display: "flex", alignItems: "center", justifyContent: "center"
                }}>
                  <div style={{ width: 92, height: 92, borderRadius: 999, background: "#fff", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
                    <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: "-0.02em" }}>{percent}</div>
                    <div style={{ fontSize: 11, color: "#9CA3AF", fontWeight: 600 }}>점</div>
                  </div>
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 14 }}>
                    <span style={{ fontSize: 13, color: "#6B7280", fontWeight: 600 }}>맞힌 문제</span>
                    <span style={{ fontSize: 14, fontWeight: 700 }}>{score} / {quiz.length}</span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 14 }}>
                    <span style={{ fontSize: 13, color: "#6B7280", fontWeight: 600 }}>예상 수준</span>
                    <span style={{ fontSize: 14, fontWeight: 700, color: "#4F46E5" }}>
                      {percent >= 80 ? "Senior 🥇" : percent >= 50 ? "Mid-level 🥈" : "Junior 🥉"}
                    </span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between" }}>
                    <span style={{ fontSize: 13, color: "#6B7280", fontWeight: 600 }}>추천 학습량</span>
                    <span style={{ fontSize: 14, fontWeight: 700 }}>주 {percent >= 80 ? "3" : percent >= 50 ? "5" : "7"}문제</span>
                  </div>
                </div>
              </div>
            </div>

            <div style={{ background: "linear-gradient(135deg, #EEF2FF, #F5F3FF)", border: "1px solid #C7D2FE", borderRadius: 16, padding: 20, display: "flex", gap: 14, marginBottom: 32 }}>
              <PrepBot expression="teach" size={56} accent="#4F46E5" />
              <div>
                <div style={{ fontSize: 12, fontWeight: 700, color: "#4338CA", marginBottom: 6 }}>프렙쌤 한마디</div>
                <div style={{ fontSize: 14, color: "#3730A3", lineHeight: 1.6 }}>
                  {percent >= 80
                    ? `${role.name} 기본기는 충분해요. 이제 시스템 설계나 실전 면접 후기로 깊이를 더해봐요.`
                    : percent >= 50
                    ? `${role.name} 핵심 개념은 익숙하시네요. 자주 틀리는 영역만 콕 집어 채워드릴게요.`
                    : `걱정 마세요. ${role.name} 기초부터 차근차근 짚어드릴게요. 매일 1문제면 충분해요.`}
                </div>
              </div>
            </div>

            <div style={{ display: "flex", gap: 12 }}>
              <button onClick={() => navigate("/dashboard")} style={{
                flex: 1, padding: "18px", background: "#4F46E5", color: "#fff",
                border: "none", borderRadius: 14, fontSize: 16, fontWeight: 700,
                cursor: "pointer", fontFamily: "inherit", display: "inline-flex",
                alignItems: "center", justifyContent: "center", gap: 8,
                boxShadow: "0 4px 14px -2px rgba(79,70,229,0.4)"
              }}>
                맞춤 학습 시작하기 →
              </button>
              <button onClick={() => { setStep(0); setQIdx(0); setAnswers([]); setRole(null); setCategory(null); }} style={{
                padding: "18px 24px", background: "#fff", color: "#6B7280",
                border: "1.5px solid #E5E7EB", borderRadius: 14, fontSize: 15, fontWeight: 600,
                cursor: "pointer", fontFamily: "inherit"
              }}>
                다시 풀기
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default LevelCheck;
