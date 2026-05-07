import React, { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  IconSpark, IconArrowLeft, IconArrowRight,
  TopNav
} from "../components/Components.jsx";
import { PrepBot } from "../components/PrepBot.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { resumeApi } from "../api/resumeApi.js";

const MAX_FILE_MB = 10;
const ACCEPTED_EXT = [".pdf", ".docx", ".doc", ".rtf", ".odt", ".txt", ".md"];
const ACCEPT_ATTR = ACCEPTED_EXT.join(",");

const ResumeFeedback = () => {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const fileInputRef = useRef(null);

  const [mode, setMode] = useState("file"); // "file" | "text"
  const [file, setFile] = useState(null);
  const [text, setText] = useState("");
  const [dragOver, setDragOver] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);

  const jobCategory = auth?.jobCategoryName ?? "";

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

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    pickFile(e.dataTransfer.files?.[0]);
  };

  const submit = async () => {
    setError("");
    setResult(null);

    try {
      if (mode === "file") {
        if (!file) { setError("이력서 파일을 선택해주세요."); return; }
      } else {
        if (text.trim().length < 50) { setError("이력서 내용을 50자 이상 입력해주세요."); return; }
      }

      setLoading(true);
      const data = mode === "file"
        ? await resumeApi.feedbackFromFile(file, jobCategory)
        : await resumeApi.feedbackFromText(text, jobCategory);
      setResult(data);
    } catch (e) {
      setError(e.message || "요청에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setResult(null);
    setError("");
  };

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
          <div style={{ fontSize: 13, color: "var(--gray-500)" }}>
            {jobCategory && <>목표 직군 · <strong style={{ color: "var(--gray-800)" }}>{jobCategory}</strong></>}
          </div>
        </div>
      </div>

      <div style={{ maxWidth: 960, margin: "0 auto", padding: "40px 48px 80px" }}>

        {/* 헤더 */}
        <div className="card" style={{
          padding: 32, marginBottom: 20, position: "relative", overflow: "hidden",
          background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)",
          borderColor: "var(--blue-200)"
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
            <div style={{ flexShrink: 0 }}>
              <PrepBot expression="teach" size={120} />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{
                display: "inline-flex", alignItems: "center", gap: 6,
                padding: "4px 10px", background: "#fff",
                border: "1px solid var(--blue-200)", borderRadius: 999,
                fontSize: 12, fontWeight: 700, color: "var(--blue-700)", marginBottom: 10
              }}>
                <IconSpark size={12} /> AI 이력서 첨삭
              </div>
              <div className="t-h2" style={{ marginBottom: 8 }}>
                이력서를 올리면 부족한 부분을 짚어드려요
              </div>
              <p className="t-body" style={{ fontSize: 14, marginBottom: 0 }}>
                PDF · DOCX · DOC · RTF · ODT · TXT 파일 또는 텍스트 붙여넣기 모두 가능합니다.
                보통 30초~1분 안에 결과가 나옵니다.
              </p>
            </div>
          </div>
        </div>

        {!result && (
          <>
            {/* 모드 탭 */}
            <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
              <TabButton active={mode === "file"} onClick={() => setMode("file")}>📎 파일 업로드</TabButton>
              <TabButton active={mode === "text"} onClick={() => setMode("text")}>✏️ 텍스트로 붙여넣기</TabButton>
            </div>

            {/* 입력 카드 */}
            <div className="card" style={{ padding: 28, marginBottom: 16 }}>
              {mode === "file" ? (
                <div
                  onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                  onDragLeave={() => setDragOver(false)}
                  onDrop={handleDrop}
                  onClick={() => fileInputRef.current?.click()}
                  style={{
                    border: `2px dashed ${dragOver ? "var(--blue-500)" : "var(--gray-300)"}`,
                    background: dragOver ? "#EFF6FF" : "#FAFBFC",
                    borderRadius: 12,
                    padding: "48px 24px",
                    textAlign: "center",
                    cursor: "pointer",
                    transition: "all 0.15s"
                  }}>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept={ACCEPT_ATTR}
                    style={{ display: "none" }}
                    onChange={(e) => pickFile(e.target.files?.[0])}
                  />
                  {file ? (
                    <div>
                      <div style={{ fontSize: 36, marginBottom: 8 }}>📄</div>
                      <div style={{ fontSize: 15, fontWeight: 600, color: "var(--gray-900)" }}>
                        {file.name}
                      </div>
                      <div style={{ fontSize: 13, color: "var(--gray-500)", marginTop: 4 }}>
                        {(file.size / 1024).toFixed(1)} KB · 클릭해서 다른 파일 선택
                      </div>
                    </div>
                  ) : (
                    <div>
                      <div style={{ fontSize: 36, marginBottom: 8 }}>📥</div>
                      <div style={{ fontSize: 15, fontWeight: 600, color: "var(--gray-800)", marginBottom: 4 }}>
                        파일을 드래그하거나 클릭해서 선택
                      </div>
                      <div style={{ fontSize: 13, color: "var(--gray-500)" }}>
                        {ACCEPTED_EXT.join(" · ")} (최대 {MAX_FILE_MB}MB)
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div>
                  <textarea
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder="이력서 본문을 그대로 붙여넣어 주세요. 최소 50자 이상."
                    style={{
                      width: "100%",
                      minHeight: 320,
                      padding: 16,
                      border: "1px solid var(--gray-300)",
                      borderRadius: 10,
                      fontSize: 14,
                      lineHeight: 1.7,
                      fontFamily: "inherit",
                      resize: "vertical",
                      outline: "none",
                      boxSizing: "border-box"
                    }}
                  />
                  <div style={{ marginTop: 8, fontSize: 12, color: "var(--gray-500)", textAlign: "right" }}>
                    {text.length.toLocaleString()}자
                  </div>
                </div>
              )}

              {error && (
                <div style={{
                  marginTop: 14, padding: "10px 14px",
                  background: "#FEF2F2", border: "1px solid #FECACA",
                  borderRadius: 8, color: "#B91C1C", fontSize: 13
                }}>
                  {error}
                </div>
              )}
            </div>

            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <button
                className="btn btn-primary"
                disabled={loading}
                onClick={submit}
                style={{ minWidth: 180 }}>
                {loading ? "AI가 검토 중…" : <>AI 첨삭 받기 <IconArrowRight size={14} /></>}
              </button>
            </div>

            {loading && (
              <div className="card" style={{ padding: 40, marginTop: 20, textAlign: "center" }}>
                <div style={{ marginBottom: 14, display: "flex", justifyContent: "center" }}>
                  <PrepBot expression="thinking" size={80} />
                </div>
                <div className="t-h3" style={{ marginBottom: 4 }}>이력서를 꼼꼼히 읽고 있어요</div>
                <p className="t-sm" style={{ color: "var(--gray-500)" }}>
                  보통 30초~2분 정도 걸립니다. 잠시만 기다려주세요.
                </p>
              </div>
            )}
          </>
        )}

        {result && (
          <>
            <div className="card" style={{ padding: 32, marginBottom: 20 }}>
              <div style={{ display: "flex", alignItems: "flex-start", gap: 16, marginBottom: 20 }}>
                <PrepBot expression="celebrate" size={56} />
                <div style={{ flex: 1 }}>
                  <span className="badge badge-purple" style={{ marginBottom: 6, display: "inline-block" }}>
                    프렙쌤의 이력서 첨삭
                  </span>
                  <div className="t-sm" style={{ marginTop: 4 }}>
                    추출 글자수 {result.extractedCharCount?.toLocaleString()}자
                    {result.detectedFileType ? ` · ${result.detectedFileType}` : ""}
                  </div>
                </div>
              </div>
              <MarkdownRenderer content={result.feedback || ""} />
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 0" }}>
              <button className="btn btn-outline" onClick={reset}>
                다른 이력서 첨삭하기
              </button>
              <button className="btn btn-primary" onClick={() => navigate("/dashboard")}>
                대시보드로 <IconArrowRight size={14} />
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

const TabButton = ({ active, onClick, children }) => (
  <button
    onClick={onClick}
    style={{
      padding: "10px 18px",
      border: "1px solid",
      borderColor: active ? "var(--blue-500)" : "var(--gray-300)",
      background: active ? "var(--blue-500)" : "#fff",
      color: active ? "#fff" : "var(--gray-700)",
      borderRadius: 999,
      fontSize: 14,
      fontWeight: 600,
      cursor: "pointer",
      fontFamily: "inherit"
    }}>
    {children}
  </button>
);

const MarkdownRenderer = ({ content }) => {
  if (!content) return null;
  const lines = content.split("\n");
  const elements = [];

  lines.forEach((line, i) => {
    if (line.startsWith("### ")) {
      elements.push(
        <div key={i} style={{
          fontSize: 15, fontWeight: 700, color: "var(--gray-900)",
          marginTop: elements.length > 0 ? 24 : 0, marginBottom: 8,
          paddingBottom: 6, borderBottom: "2px solid var(--blue-100)"
        }}>
          {line.slice(4)}
        </div>
      );
    } else if (line.startsWith("## ")) {
      elements.push(
        <div key={i} style={{
          fontSize: 16, fontWeight: 700, color: "var(--gray-900)",
          marginTop: elements.length > 0 ? 28 : 0, marginBottom: 10
        }}>
          {line.slice(3)}
        </div>
      );
    } else if (/^[-*] /.test(line)) {
      elements.push(
        <div key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", display: "flex", gap: 8, marginBottom: 4 }}>
          <span style={{ color: "var(--blue-400)", flexShrink: 0 }}>•</span>
          <span>{renderInline(line.slice(2))}</span>
        </div>
      );
    } else if (/^\d+\. /.test(line)) {
      const m = line.match(/^(\d+)\. (.*)/);
      const num = m[1], txt = m[2];
      elements.push(
        <div key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", display: "flex", gap: 8, marginBottom: 4 }}>
          <span style={{ color: "var(--blue-600)", flexShrink: 0, fontWeight: 600, minWidth: 20 }}>{num}.</span>
          <span>{renderInline(txt)}</span>
        </div>
      );
    } else if (line.trim() === "") {
      elements.push(<div key={i} style={{ height: 6 }} />);
    } else {
      elements.push(
        <p key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", margin: "4px 0" }}>
          {renderInline(line)}
        </p>
      );
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

export default ResumeFeedback;