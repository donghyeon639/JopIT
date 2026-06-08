import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { IconSpark, IconArrowLeft, IconArrowRight, TopNav } from "../components/Components.jsx";
import { PrepBot } from "../components/PrepBot.jsx";
import { JOB_CATEGORIES } from "../constants/jobs.js";
import { interviewApi } from "../api/interviewApi.js";
import { useSpeechRecognition } from "../hooks/useSpeechRecognition.js";
import { useCamera } from "../hooks/useCamera.js";

const MAX_FILE_MB = 10;
const ACCEPTED_EXT = [".pdf", ".docx", ".doc", ".rtf", ".odt", ".txt", ".md"];
const ACCEPT_ATTR = ACCEPTED_EXT.join(",");
const ANSWER_SECONDS = 90; // 질문당 답변 제한시간

const INTERVIEW_TYPES = [
  { value: "PERSONALITY", label: "인성 면접", desc: "경험·가치관·협업과 갈등 해결 중심" },
  { value: "IN_DEPTH",    label: "심층 면접", desc: "프로젝트·기술 선택·문제 해결 깊이 중심" },
];

const STATUS_LABEL = {
  QUESTIONS_PENDING: "질문 생성 중",
  QUESTIONS_READY: "진행 가능",
  QUESTIONS_FAILED: "생성 실패",
  COMPLETED: "완료",
};

const Interview = () => {
  const navigate = useNavigate();

  const [step, setStep] = useState("setup"); // setup | check | generating | interview | result
  const [file, setFile] = useState(null);
  const [jobCategory, setJobCategory] = useState(JOB_CATEGORIES[0]?.value ?? "");
  const [interviewType, setInterviewType] = useState("PERSONALITY");
  const [session, setSession] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [completing, setCompleting] = useState(false);
  const [pastSessions, setPastSessions] = useState([]);
  const [answering, setAnswering] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(ANSWER_SECONDS);
  const fileInputRef = useRef(null);

  const stt = useSpeechRecognition({ lang: "ko-KR" });
  const camera = useCamera();

  const questions = session?.questions ?? [];
  const currentQuestion = questions[currentIndex];
  const isLast = currentIndex >= questions.length - 1;

  const needsPolling =
    step === "generating" ||
    (step === "result" &&
      (questions.some((q) => q.status === "EVAL_PENDING") || (completing && !session?.overallFeedback)));

  useEffect(() => {
    interviewApi.listSessions().then(setPastSessions).catch(() => {});
  }, []);

  useEffect(() => {
    if (session?.overallFeedback) setCompleting(false);
  }, [session?.overallFeedback]);

  useEffect(() => {
    if (!needsPolling || !session?.id) return;
    const interval = setInterval(async () => {
      try {
        const data = await interviewApi.getSession(session.id);
        setSession(data);
        if (step === "generating") {
          if (data.status === "QUESTIONS_READY") {
            setCurrentIndex(0);
            setStep("interview");
          } else if (data.status === "QUESTIONS_FAILED") {
            setError("질문 생성에 실패했어요. 다시 시도해주세요.");
            setStep("setup");
          }
        }
      } catch { /* 다음 폴링에서 재시도 */ }
    }, 2500);
    return () => clearInterval(interval);
  }, [needsPolling, session?.id, step]);

  /* 환경 체크부터 면접까지 카메라 on */
  useEffect(() => {
    if (["check", "generating", "interview"].includes(step)) camera.start();
    else camera.stop();
  }, [step]); // eslint-disable-line react-hooks/exhaustive-deps

  /* 질문이 바뀌면 타이머·녹음 초기화 */
  useEffect(() => {
    setAnswering(false);
    setSecondsLeft(ANSWER_SECONDS);
    stt.reset();
    stt.stop();
  }, [currentQuestion?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  /* 카운트다운 — 0이 되면 자동 제출 */
  useEffect(() => {
    if (!answering) return;
    if (secondsLeft <= 0) {
      finishAnswer(true);
      return;
    }
    const id = setTimeout(() => setSecondsLeft((s) => s - 1), 1000);
    return () => clearTimeout(id);
  }, [answering, secondsLeft]); // eslint-disable-line react-hooks/exhaustive-deps

  const pickFile = (f) => {
    setError("");
    if (!f) { setFile(null); return; }
    const ext = "." + (f.name.split(".").pop() || "").toLowerCase();
    if (!ACCEPTED_EXT.includes(ext)) {
      setError(`지원하지 않는 형식입니다. (${ACCEPTED_EXT.join(", ")})`);
      return;
    }
    if (f.size > MAX_FILE_MB * 1024 * 1024) {
      setError(`파일이 너무 큽니다. 최대 ${MAX_FILE_MB}MB까지 가능합니다.`);
      return;
    }
    setFile(f);
  };

  // 설정 검증 후 환경 체크 화면으로
  const goToCheck = () => {
    setError("");
    if (!file) { setError("이력서 파일을 선택해주세요."); return; }
    if (!jobCategory) { setError("직무를 선택해주세요."); return; }
    setStep("check");
  };

  // 환경 체크 "다음" → 세션 생성 + 질문 생성 시작
  const startInterview = async () => {
    setError("");
    try {
      const data = await interviewApi.createSession(file, jobCategory, interviewType);
      setSession(data);
      setStep("generating");
    } catch (e) {
      setError(e.message || "면접 생성에 실패했습니다.");
      setStep("setup");
    }
  };

  const startAnswering = () => {
    setError("");
    stt.reset();
    setSecondsLeft(ANSWER_SECONDS);
    setAnswering(true);
    stt.start();
  };

  const finishAnswer = async (auto) => {
    if (submitting) return;
    const raw = stt.transcript.trim();
    if (!auto && raw.length === 0) {
      setError("아직 인식된 답변이 없어요. 말한 뒤 완료해주세요.");
      return;
    }
    setSubmitting(true);
    setAnswering(false);
    setError("");
    stt.stop();
    const text = raw.length > 0 ? raw : "(시간 내 답변 없음)";
    try {
      const data = await interviewApi.submitAnswer(session.id, currentQuestion.id, text);
      setSession(data);
      stt.reset();
      if (currentIndex < data.questions.length - 1) {
        setCurrentIndex((i) => i + 1);
      } else {
        setStep("result");
      }
    } catch (e) {
      setError(e.message || "답변 제출에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const requestOverall = async () => {
    setCompleting(true);
    setError("");
    try {
      const data = await interviewApi.complete(session.id);
      setSession(data);
    } catch (e) {
      setError(e.message || "종합 평가 요청에 실패했습니다.");
      setCompleting(false);
    }
  };

  const openPast = async (id) => {
    setError("");
    try {
      const data = await interviewApi.getSession(id);
      setSession(data);
      setCurrentIndex(0);
      setStep("result");
    } catch (e) {
      setError(e.message || "면접 기록을 불러오지 못했습니다.");
    }
  };

  const restart = () => {
    stt.stop();
    stt.reset();
    setSession(null);
    setFile(null);
    setCurrentIndex(0);
    setError("");
    setCompleting(false);
    setAnswering(false);
    setStep("setup");
    interviewApi.listSessions().then(setPastSessions).catch(() => {});
  };

  const wide = step === "interview";

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div className="t-sm">
            <span onClick={() => navigate("/dashboard")}
                  style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
              <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 대시보드로
            </span>
          </div>
          {session && (
            <div style={{ fontSize: 13, color: "var(--gray-500)" }}>
              {session.jobCategory} · <strong style={{ color: "var(--gray-800)" }}>{session.interviewTypeLabel}</strong>
            </div>
          )}
        </div>
      </div>

      <div style={{ maxWidth: wide ? 1120 : 880, margin: "0 auto", padding: "40px 48px 80px" }}>
        {step === "setup" && (
          <SetupView
            file={file} pickFile={pickFile} fileInputRef={fileInputRef}
            jobCategory={jobCategory} setJobCategory={setJobCategory}
            interviewType={interviewType} setInterviewType={setInterviewType}
            error={error} onStart={goToCheck}
            pastSessions={pastSessions} onOpenPast={openPast}
          />
        )}

        {step === "check" && (
          <EnvCheckView setVideoEl={camera.setVideoEl} cameraError={camera.error} onNext={startInterview} />
        )}

        {step === "generating" && <GeneratingView />}

        {step === "interview" && currentQuestion && (
          <div style={{ display: "flex", gap: 20, alignItems: "stretch" }}>
            {/* 좌: 큰 카메라 */}
            <div style={{ flex: "1.5", minWidth: 0 }}>
              <div style={{ position: "relative", borderRadius: 16, overflow: "hidden", background: "#0B1020", aspectRatio: "16 / 10" }}>
                <video ref={camera.setVideoEl} autoPlay muted playsInline
                       style={{ width: "100%", height: "100%", objectFit: "cover", transform: "scaleX(-1)" }} />
                {camera.error && (
                  <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 13, padding: 16, textAlign: "center" }}>
                    {camera.error}
                  </div>
                )}
                {answering && (
                  <div style={{ position: "absolute", top: 12, left: 12, display: "flex", alignItems: "center", gap: 6, background: "rgba(0,0,0,0.55)", color: "#fff", fontSize: 12, padding: "5px 10px", borderRadius: 999 }}>
                    <span style={{ width: 8, height: 8, borderRadius: 999, background: "#EF4444", display: "inline-block" }} /> 녹음 중
                  </div>
                )}
                {answering && (
                  <div style={{
                    position: "absolute", top: 12, right: 12, fontSize: 15, fontWeight: 700, color: "#fff",
                    padding: "6px 12px", borderRadius: 999,
                    background: secondsLeft <= 10 ? "rgba(220,38,38,0.9)" : "rgba(0,0,0,0.55)"
                  }}>
                    ⏱ {secondsLeft}초
                  </div>
                )}
                <span style={{ position: "absolute", bottom: 10, left: 12, background: "rgba(0,0,0,0.5)", color: "#fff", fontSize: 11, padding: "2px 8px", borderRadius: 999 }}>
                  내 화면
                </span>
              </div>
            </div>

            {/* 우: 질문 + 답변 + 컨트롤 */}
            <div style={{ flex: 1, minWidth: 320, display: "flex", flexDirection: "column" }}>
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 12 }}>
                <span className="badge badge-blue">질문 {currentIndex + 1} / {questions.length}</span>
                <div className="progress-track" style={{ width: 120 }}>
                  <div className="progress-fill" style={{ width: `${(currentIndex / questions.length) * 100}%` }} />
                </div>
              </div>

              <div className="card" style={{ padding: 22, marginBottom: 14 }}>
                <div className="t-xs" style={{ color: "var(--gray-500)", marginBottom: 8 }}>면접관 질문</div>
                <div className="t-h3" style={{ lineHeight: 1.5 }}>{currentQuestion.content}</div>
              </div>

              <div className="card" style={{ padding: 18, marginBottom: 14, flex: 1, minHeight: 140 }}>
                <div className="t-xs" style={{ color: "var(--gray-500)", marginBottom: 8 }}>인식된 답변</div>
                <div style={{ fontSize: 14, lineHeight: 1.7, whiteSpace: "pre-wrap", color: stt.transcript ? "var(--gray-800)" : "var(--gray-400)" }}>
                  {stt.transcript || "‘답변 시작’을 누르고 말하면 인식된 내용이 여기에 표시돼요."}
                </div>
                {stt.error && <div className="t-xs" style={{ color: "var(--red-600)", marginTop: 6 }}>{stt.error}</div>}
              </div>

              {error && <ErrorBox>{error}</ErrorBox>}

              {!stt.supported ? (
                <ErrorBox>이 브라우저는 음성 인식을 지원하지 않아요. Chrome 또는 Edge에서 이용해주세요.</ErrorBox>
              ) : !answering ? (
                <button className="btn btn-primary btn-lg" onClick={startAnswering} style={{ width: "100%" }}>
                  🎤 답변 시작 ({ANSWER_SECONDS}초)
                </button>
              ) : (
                <button className="btn btn-primary btn-lg" disabled={submitting} onClick={() => finishAnswer(false)} style={{ width: "100%" }}>
                  {submitting ? "제출 중…" : (isLast ? "답변 완료하고 마치기" : "답변 완료하고 다음")}
                </button>
              )}
            </div>
          </div>
        )}

        {step === "result" && (
          <ResultView
            questions={questions} overallFeedback={session?.overallFeedback}
            completing={completing} onComplete={requestOverall}
            onRestart={restart} onDashboard={() => navigate("/dashboard")}
          />
        )}
      </div>
    </div>
  );
};

/* ─── 환경 체크 ─── */
const EnvCheckView = ({ setVideoEl, cameraError, onNext }) => {
  const [micActive, setMicActive] = useState(false);
  const [micLevel, setMicLevel] = useState(0);
  const [micOk, setMicOk] = useState(false);
  const [spkOk, setSpkOk] = useState(false);
  const audioRef = useRef({ stream: null, ctx: null, raf: null });

  const stopMic = () => {
    const a = audioRef.current;
    if (a.raf) cancelAnimationFrame(a.raf);
    a.stream?.getTracks().forEach((t) => t.stop());
    if (a.ctx && a.ctx.state !== "closed") a.ctx.close();
    audioRef.current = { stream: null, ctx: null, raf: null };
    setMicActive(false);
    setMicLevel(0);
  };

  const toggleMic = async () => {
    if (micActive) { stopMic(); return; }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const Ctx = window.AudioContext || window.webkitAudioContext;
      const ctx = new Ctx();
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 256;
      ctx.createMediaStreamSource(stream).connect(analyser);
      const data = new Uint8Array(analyser.frequencyBinCount);
      audioRef.current = { stream, ctx, raf: null };
      setMicActive(true);
      const loop = () => {
        analyser.getByteTimeDomainData(data);
        let sum = 0;
        for (let i = 0; i < data.length; i++) { const v = (data[i] - 128) / 128; sum += v * v; }
        const level = Math.min(100, Math.round(Math.sqrt(sum / data.length) * 300));
        setMicLevel(level);
        if (level > 15) setMicOk(true);
        audioRef.current.raf = requestAnimationFrame(loop);
      };
      loop();
    } catch {
      setMicOk(false);
    }
  };

  const checkSpeaker = () => {
    try {
      if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
        const u = new SpeechSynthesisUtterance("스피커가 잘 들리시나요? 이 소리가 들리면 면접을 시작하세요.");
        u.lang = "ko-KR";
        window.speechSynthesis.speak(u);
        setSpkOk(true);
      }
    } catch { /* noop */ }
  };

  useEffect(() => () => stopMic(), []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div style={{ maxWidth: 720, margin: "0 auto" }}>
      <div className="card" style={{ padding: 32, background: "var(--gray-50)" }}>
        <div style={{ textAlign: "center", fontWeight: 700, fontSize: 18, color: "var(--gray-900)", lineHeight: 1.5, marginBottom: 20 }}>
          정확한 분석을 위해<br />면접 환경을 체크해볼게요!
        </div>

        <div style={{ position: "relative", borderRadius: 14, overflow: "hidden", background: "#0B1020", aspectRatio: "16 / 9", marginBottom: 18 }}>
          <video ref={setVideoEl} autoPlay muted playsInline
                 style={{ width: "100%", height: "100%", objectFit: "cover", transform: "scaleX(-1)" }} />
          {cameraError && (
            <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontSize: 13, padding: 16, textAlign: "center" }}>
              {cameraError}
            </div>
          )}
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
          <CheckButton onClick={toggleMic} ok={micOk} active={micActive}>
            🎤 {micActive ? "마이크 듣는 중…" : "마이크 체크"} {micOk && "✓"}
          </CheckButton>
          <CheckButton onClick={checkSpeaker} ok={spkOk}>
            🔊 스피커 체크 {spkOk && "✓"}
          </CheckButton>
        </div>

        {micActive && (
          <div style={{ marginTop: 14 }}>
            <div className="t-xs" style={{ color: "var(--gray-500)", marginBottom: 5 }}>마이크 입력 레벨 — 말해보세요</div>
            <div className="progress-track">
              <div className="progress-fill" style={{ width: `${micLevel}%`, background: "var(--green-500)", transition: "width 0.08s linear" }} />
            </div>
          </div>
        )}
      </div>

      <button className="btn btn-primary btn-lg" onClick={onNext} style={{ width: "100%", marginTop: 16 }}>
        다음
      </button>
    </div>
  );
};

const CheckButton = ({ onClick, active, ok, children }) => (
  <button onClick={onClick} style={{
    padding: "14px 16px", borderRadius: 999, fontSize: 14, fontWeight: 600, cursor: "pointer", fontFamily: "inherit",
    border: `1px solid ${ok ? "var(--green-500)" : active ? "var(--blue-500)" : "var(--gray-300)"}`,
    background: "#fff", color: ok ? "var(--green-700)" : "var(--gray-700)",
    display: "flex", alignItems: "center", justifyContent: "center", gap: 8
  }}>{children}</button>
);

/* ─── Setup ─── */
const SetupView = ({ file, pickFile, fileInputRef, jobCategory, setJobCategory, interviewType, setInterviewType, error, onStart, pastSessions, onOpenPast }) => {
  const [dragOver, setDragOver] = useState(false);
  return (
    <>
      <div className="card" style={{
        padding: 32, marginBottom: 20, overflow: "hidden",
        background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)", borderColor: "var(--blue-200)"
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ flexShrink: 0 }}><PrepBot expression="wave" size={120} /></div>
          <div style={{ flex: 1 }}>
            <div style={{
              display: "inline-flex", alignItems: "center", gap: 6, padding: "4px 10px",
              background: "#fff", border: "1px solid var(--blue-200)", borderRadius: 999,
              fontSize: 12, fontWeight: 700, color: "var(--blue-700)", marginBottom: 10
            }}>
              <IconSpark size={12} /> AI 화상 모의 면접
            </div>
            <div className="t-h2" style={{ marginBottom: 8 }}>이력서로 맞춤 화상 면접을 시작해요</div>
            <p className="t-body" style={{ fontSize: 14, marginBottom: 0 }}>
              이력서를 올리면 AI가 질문을 만들어요. 카메라를 켜고 제한시간 안에 음성으로 답하면 AI가 평가해드려요.
            </p>
          </div>
        </div>
      </div>

      <div className="card" style={{ padding: 28, marginBottom: 16 }}>
        <div className="t-h4" style={{ marginBottom: 12 }}>1. 이력서 업로드</div>
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={(e) => { e.preventDefault(); setDragOver(false); pickFile(e.dataTransfer.files?.[0]); }}
          onClick={() => fileInputRef.current?.click()}
          style={{
            border: `2px dashed ${dragOver ? "var(--blue-500)" : "var(--gray-300)"}`,
            background: dragOver ? "#EFF6FF" : "#FAFBFC", borderRadius: 12,
            padding: "40px 24px", textAlign: "center", cursor: "pointer", transition: "all 0.15s"
          }}>
          <input ref={fileInputRef} type="file" accept={ACCEPT_ATTR} style={{ display: "none" }}
                 onChange={(e) => pickFile(e.target.files?.[0])} />
          {file ? (
            <div>
              <div style={{ fontSize: 32, marginBottom: 8 }}>📄</div>
              <div style={{ fontSize: 15, fontWeight: 600, color: "var(--gray-900)" }}>{file.name}</div>
              <div style={{ fontSize: 13, color: "var(--gray-500)", marginTop: 4 }}>
                {(file.size / 1024).toFixed(1)} KB · 클릭해서 다른 파일 선택
              </div>
            </div>
          ) : (
            <div>
              <div style={{ fontSize: 32, marginBottom: 8 }}>📥</div>
              <div style={{ fontSize: 15, fontWeight: 600, color: "var(--gray-800)", marginBottom: 4 }}>
                파일을 드래그하거나 클릭해서 선택
              </div>
              <div style={{ fontSize: 13, color: "var(--gray-500)" }}>{ACCEPTED_EXT.join(" · ")} (최대 {MAX_FILE_MB}MB)</div>
            </div>
          )}
        </div>
      </div>

      <div className="card" style={{ padding: 28, marginBottom: 16 }}>
        <div className="t-h4" style={{ marginBottom: 12 }}>2. 직무 선택</div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
          {JOB_CATEGORIES.map((j) => (
            <Chip key={j.value} active={jobCategory === j.value} onClick={() => setJobCategory(j.value)}>
              {j.label}
            </Chip>
          ))}
        </div>
      </div>

      <div className="card" style={{ padding: 28, marginBottom: 16 }}>
        <div className="t-h4" style={{ marginBottom: 12 }}>3. 면접 종류 선택</div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
          {INTERVIEW_TYPES.map((t) => {
            const active = interviewType === t.value;
            return (
              <div key={t.value} onClick={() => setInterviewType(t.value)} style={{
                padding: 18, borderRadius: 12, cursor: "pointer",
                border: `1.5px solid ${active ? "var(--blue-500)" : "var(--gray-300)"}`,
                background: active ? "#EFF6FF" : "#fff", transition: "all 0.15s"
              }}>
                <div style={{ fontSize: 15, fontWeight: 700, color: active ? "var(--blue-700)" : "var(--gray-900)", marginBottom: 4 }}>
                  {t.label}
                </div>
                <div style={{ fontSize: 13, color: "var(--gray-600)", lineHeight: 1.5 }}>{t.desc}</div>
              </div>
            );
          })}
        </div>
      </div>

      {error && <ErrorBox>{error}</ErrorBox>}

      <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 4 }}>
        <button className="btn btn-primary btn-lg" onClick={onStart} style={{ minWidth: 200 }}>
          환경 체크하기 <IconArrowRight size={14} />
        </button>
      </div>

      {pastSessions?.length > 0 && (
        <div className="card" style={{ padding: 24, marginTop: 28 }}>
          <div className="t-h4" style={{ marginBottom: 12 }}>지난 면접 기록</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {pastSessions.map((s) => (
              <div key={s.id} className="row-hover" onClick={() => onOpenPast(s.id)} style={{
                display: "flex", alignItems: "center", justifyContent: "space-between",
                padding: "12px 14px", border: "1px solid var(--gray-200)", borderRadius: 10
              }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span className="badge badge-gray">{s.jobCategory}</span>
                  <span style={{ fontSize: 14, fontWeight: 600, color: "var(--gray-800)" }}>{s.interviewTypeLabel}</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <span className="t-xs" style={{ color: "var(--gray-500)" }}>{STATUS_LABEL[s.status] ?? s.status}</span>
                  <span className="t-xs" style={{ color: "var(--gray-400)" }}>{(s.createdAt || "").slice(0, 10)}</span>
                  <IconArrowRight size={14} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );
};

/* ─── Generating ─── */
const GeneratingView = () => (
  <div className="card" style={{ padding: 56, textAlign: "center" }}>
    <div style={{ marginBottom: 16, display: "flex", justifyContent: "center" }}>
      <PrepBot expression="thinking" size={100} />
    </div>
    <div className="t-h3" style={{ marginBottom: 6 }}>이력서를 읽고 질문을 만들고 있어요</div>
    <p className="t-sm" style={{ color: "var(--gray-500)" }}>보통 30초~1분 정도 걸려요. 잠시만 기다려주세요.</p>
  </div>
);

/* ─── Result ─── */
const ResultView = ({ questions, overallFeedback, completing, onComplete, onRestart, onDashboard }) => {
  const allEvaluated = questions.length > 0 && questions.every((q) => q.status === "EVAL_DONE" || q.status === "EVAL_FAILED");
  return (
    <>
      <div className="card" style={{ padding: 28, marginBottom: 20, textAlign: "center" }}>
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 10 }}>
          <PrepBot expression="celebrate" size={72} />
        </div>
        <div className="t-h2" style={{ marginBottom: 6 }}>면접이 끝났어요</div>
        <p className="t-sm" style={{ color: "var(--gray-500)" }}>질문별 답변과 AI 평가를 확인하세요.</p>
      </div>

      <div className="card" style={{ padding: 24, marginBottom: 20, background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)", borderColor: "var(--blue-200)" }}>
        <span className="badge badge-purple" style={{ marginBottom: 10, display: "inline-block" }}>종합 평가</span>
        {overallFeedback ? (
          <MarkdownRenderer content={overallFeedback} />
        ) : completing ? (
          <div className="t-sm" style={{ color: "var(--gray-600)" }}>전체 답변을 종합해 평가를 작성하고 있어요…</div>
        ) : (
          <div>
            <p className="t-sm" style={{ color: "var(--gray-600)", marginBottom: 12 }}>모든 답변을 종합한 총평을 받아보세요.</p>
            <button className="btn btn-primary" disabled={!allEvaluated} onClick={onComplete}>
              {allEvaluated ? "AI 종합 평가 받기" : "평가 완료 후 가능해요…"}
            </button>
          </div>
        )}
      </div>

      {questions.map((q) => (
        <div key={q.id} className="card" style={{ padding: 24, marginBottom: 16 }}>
          <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12 }}>
            <span className="badge badge-blue">Q{q.orderNo}</span>
            <div style={{ fontSize: 15, fontWeight: 700, color: "var(--gray-900)" }}>{q.content}</div>
          </div>

          <div style={{ background: "var(--gray-50)", borderRadius: 10, padding: 14, marginBottom: 14 }}>
            <div className="t-xs" style={{ color: "var(--gray-500)", marginBottom: 4 }}>내 답변</div>
            <div style={{ fontSize: 14, lineHeight: 1.7, color: "var(--gray-800)", whiteSpace: "pre-wrap" }}>
              {q.transcript || "(답변 없음)"}
            </div>
          </div>

          <div>
            <span className="badge badge-purple" style={{ marginBottom: 8, display: "inline-block" }}>프렙쌤의 평가</span>
            {q.status === "EVAL_DONE" ? (
              <MarkdownRenderer content={q.evaluation || ""} />
            ) : q.status === "EVAL_FAILED" ? (
              <div className="t-sm" style={{ color: "var(--red-600)" }}>평가에 실패했어요.</div>
            ) : (
              <div className="t-sm" style={{ color: "var(--gray-500)" }}>평가 중이에요…</div>
            )}
          </div>
        </div>
      ))}

      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 0" }}>
        <button className="btn btn-outline" onClick={onRestart}>새 면접 보기</button>
        <button className="btn btn-primary" onClick={onDashboard}>대시보드로 <IconArrowRight size={14} /></button>
      </div>
    </>
  );
};

/* ─── 공통 작은 컴포넌트 ─── */
const Chip = ({ active, onClick, children }) => (
  <button onClick={onClick} style={{
    padding: "9px 16px", borderRadius: 999, fontSize: 14, fontWeight: 600, cursor: "pointer",
    fontFamily: "inherit", border: "1px solid",
    borderColor: active ? "var(--blue-500)" : "var(--gray-300)",
    background: active ? "var(--blue-500)" : "#fff",
    color: active ? "#fff" : "var(--gray-700)"
  }}>{children}</button>
);

const ErrorBox = ({ children }) => (
  <div style={{
    marginBottom: 14, padding: "10px 14px", background: "#FEF2F2",
    border: "1px solid #FECACA", borderRadius: 8, color: "#B91C1C", fontSize: 13
  }}>{children}</div>
);

const MarkdownRenderer = ({ content }) => {
  if (!content) return null;
  const elements = [];
  content.split("\n").forEach((line, i) => {
    if (line.startsWith("### ")) {
      elements.push(
        <div key={i} style={{
          fontSize: 15, fontWeight: 700, color: "var(--gray-900)",
          marginTop: elements.length > 0 ? 18 : 0, marginBottom: 6,
          paddingBottom: 5, borderBottom: "2px solid var(--blue-100)"
        }}>{line.slice(4)}</div>
      );
    } else if (line.startsWith("## ")) {
      elements.push(<div key={i} style={{ fontSize: 16, fontWeight: 700, color: "var(--gray-900)", marginTop: elements.length > 0 ? 20 : 0, marginBottom: 8 }}>{line.slice(3)}</div>);
    } else if (/^[-*] /.test(line)) {
      elements.push(
        <div key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", display: "flex", gap: 8, marginBottom: 4 }}>
          <span style={{ color: "var(--blue-400)", flexShrink: 0 }}>•</span>
          <span>{renderInline(line.slice(2))}</span>
        </div>
      );
    } else if (line.trim() === "") {
      elements.push(<div key={i} style={{ height: 6 }} />);
    } else {
      elements.push(<p key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", margin: "4px 0" }}>{renderInline(line)}</p>);
    }
  });
  return <div>{elements}</div>;
};

function renderInline(text) {
  return text.split(/(\*\*[^*]+\*\*)/g).map((part, i) =>
    part.startsWith("**") && part.endsWith("**")
      ? <strong key={i} style={{ color: "var(--gray-900)" }}>{part.slice(2, -2)}</strong>
      : part
  );
}

export default Interview;
