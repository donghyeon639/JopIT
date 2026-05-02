import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  IconSpark, IconArrowRight, IconArrowLeft,
  TopNav
} from "../components/Components.jsx";
import { PrepBot } from "../components/PrepBot.jsx";
import { questionApi } from "../api/questionApi.js";

const AIFeedback = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const answerId = searchParams.get("answerId");

  const [answerData, setAnswerData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!answerId) { navigate("/questions"); return; }

    let stopped = false;

    const poll = async () => {
      if (stopped) return;
      try {
        const data = await questionApi.getAnswer(answerId);
        if (!stopped) {
          setAnswerData(data);
          setLoading(false);
          if (data.feedbackStatus === "DONE" || data.feedbackStatus === "FAILED") {
            stopped = true;
            clearInterval(interval);
          }
        }
      } catch {
        if (!stopped) {
          setLoading(false);
          stopped = true;
          clearInterval(interval);
        }
      }
    };

    poll();
    const interval = setInterval(poll, 3000);

    return () => {
      stopped = true;
      clearInterval(interval);
    };
  }, [answerId]);

  const isPending = loading || answerData?.feedbackStatus === "NONE" || answerData?.feedbackStatus === "PENDING";
  const isFailed = !isPending && answerData?.feedbackStatus === "FAILED";
  const questionId = answerData?.questionId;
  const backPath = questionId ? `/solve?id=${questionId}` : "/questions";

  return (
    <div className="dp-screen" style={{ width: "100%", minHeight: "100vh", background: "var(--gray-50)" }}>
      <TopNav />

      <div style={{ padding: "20px 48px", borderBottom: "1px solid var(--gray-200)", background: "#fff" }}>
        <div style={{ maxWidth: 1280, margin: "0 auto", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div className="t-sm">
            <span onClick={() => navigate(backPath)}
                  style={{ cursor: "pointer", color: "var(--blue-600)", fontWeight: 500 }}>
              <IconArrowLeft size={14} style={{ verticalAlign: -2 }} /> 답변 화면으로
            </span>
          </div>
          <button className="btn btn-outline btn-sm" onClick={() => navigate("/questions")}>
            다음 문제 풀기 <IconArrowRight size={14} />
          </button>
        </div>
      </div>

      <div style={{ maxWidth: 1024, margin: "0 auto", padding: "40px 48px 80px" }}>
        {isPending ? (
          <PendingCard />
        ) : isFailed ? (
          <FailedCard onBack={() => navigate(backPath)} />
        ) : (
          <>
            {/* Header */}
            <div className="card" style={{
              padding: 32, marginBottom: 20, position: "relative", overflow: "hidden",
              background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)",
              borderColor: "var(--blue-200)"
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
                <div style={{ flexShrink: 0 }}>
                  <PrepBot expression="celebrate" size={120} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{
                    display: "inline-flex", alignItems: "center", gap: 6,
                    padding: "4px 10px", background: "#fff",
                    border: "1px solid var(--blue-200)", borderRadius: 999,
                    fontSize: 12, fontWeight: 700, color: "var(--blue-700)", marginBottom: 10
                  }}>
                    <IconSpark size={12} /> AI 답변 분석 완료
                  </div>
                  <div className="t-h2" style={{ marginBottom: 8 }}>
                    {answerData?.questionTitle}
                  </div>
                  <p className="t-body" style={{ fontSize: 15, marginBottom: 0 }}>
                    AI 피드백을 확인하고 답변을 보완해보세요.
                  </p>
                </div>
              </div>
            </div>

            {/* AI Feedback */}
            <div className="card" style={{ padding: 32, marginBottom: 20 }}>
              <div style={{ display: "flex", alignItems: "flex-start", gap: 16, marginBottom: 20 }}>
                <PrepBot expression="teach" size={56} />
                <div style={{ flex: 1 }}>
                  <span className="badge badge-purple" style={{ marginBottom: 6, display: "inline-block" }}>프렙쌤의 AI 피드백</span>
                  <div className="t-sm" style={{ marginTop: 4 }}>AI가 모범 답안을 기준으로 분석한 피드백입니다.</div>
                </div>
              </div>
              <MarkdownRenderer content={answerData?.aiFeedback || ""} />
            </div>

            {/* My answer */}
            <div className="card" style={{ padding: 28, marginBottom: 20 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: "var(--gray-500)", marginBottom: 10 }}>
                내 답변
              </div>
              <div style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-800)", whiteSpace: "pre-wrap" }}>
                {answerData?.content}
              </div>
            </div>

            {/* Footer */}
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "20px 0" }}>
              <div />
              <div style={{ display: "flex", gap: 8 }}>
                <button className="btn btn-outline" onClick={() => navigate(backPath)}>
                  다시 작성하기
                </button>
                <button className="btn btn-primary" onClick={() => navigate("/questions")}>
                  다음 문제 <IconArrowRight size={14} />
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

const PendingCard = () => (
  <div className="card" style={{ padding: 60, textAlign: "center" }}>
    <div style={{ marginBottom: 20, display: "flex", justifyContent: "center" }}>
      <PrepBot expression="thinking" size={100} />
    </div>
    <div className="t-h2" style={{ marginBottom: 8 }}>AI가 답변을 분석하고 있어요</div>
    <p className="t-body" style={{ color: "var(--gray-500)" }}>
      잠시만 기다려주세요. 보통 30초~1분 정도 소요됩니다.
    </p>
  </div>
);

const FailedCard = ({ onBack }) => (
  <div className="card" style={{ padding: 60, textAlign: "center" }}>
    <div className="t-h2" style={{ marginBottom: 8 }}>피드백 생성에 실패했어요</div>
    <p className="t-body" style={{ color: "var(--gray-500)", marginBottom: 20 }}>
      일시적인 오류가 발생했습니다. 다시 시도해보세요.
    </p>
    <button className="btn btn-primary" onClick={onBack}>돌아가기</button>
  </div>
);

const MarkdownRenderer = ({ content }) => {
  if (!content) return null;

  const lines = content.split("\n");
  const elements = [];

  lines.forEach((line, i) => {
    if (line.startsWith("## ")) {
      elements.push(
        <div key={i} style={{
          fontSize: 15, fontWeight: 700, color: "var(--gray-900)",
          marginTop: elements.length > 0 ? 28 : 0, marginBottom: 10,
          paddingBottom: 8, borderBottom: "2px solid var(--blue-100)"
        }}>
          {line.slice(3)}
        </div>
      );
    } else if (line.startsWith("### ")) {
      elements.push(
        <div key={i} style={{ fontSize: 14, fontWeight: 600, color: "var(--gray-800)", marginTop: 14, marginBottom: 6 }}>
          {line.slice(4)}
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
      const [, num, text] = line.match(/^(\d+)\. (.*)/);
      elements.push(
        <div key={i} style={{ fontSize: 14, lineHeight: 1.75, color: "var(--gray-700)", display: "flex", gap: 8, marginBottom: 4 }}>
          <span style={{ color: "var(--blue-600)", flexShrink: 0, fontWeight: 600, minWidth: 20 }}>{num}.</span>
          <span>{renderInline(text)}</span>
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

export default AIFeedback;