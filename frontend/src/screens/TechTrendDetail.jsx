import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { TopNav, IconArrowRight } from "../components/Components.jsx";
import { techTrendApi } from "../api/questionApi.js";

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
  "Kubernetes":  { bg: "#FEE2E2", color: "#DC2626" },
  "Python":      { bg: "#EEF2FF", color: "#4F46E5" },
};

const TechTrendDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [article, setArticle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    techTrendApi.detail(id)
      .then(setArticle)
      .catch((e) => setError(e.message || "기사를 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [id]);

  const tagStyle = article ? (tagColorMap[article.tag] ?? { bg: "#F3F4F6", color: "#6B7280" }) : null;
  const hasContent = article?.content && article.content.replace(/<[^>]+>/g, "").trim().length > 200;

  return (
    <div style={{ minHeight: "100vh", background: "#fff" }}>
      <TopNav />

      <div style={{ maxWidth: 780, margin: "0 auto", padding: "32px 24px 80px" }}>
        {/* 뒤로가기 */}
        <button
          onClick={() => navigate(-1)}
          style={{
            background: "transparent", border: "none",
            color: "#7B8290", fontSize: 14, fontWeight: 500,
            padding: "6px 0", marginBottom: 24,
            cursor: "pointer", fontFamily: "inherit",
          }}
        >
          ← 기술 트렌드
        </button>

        {loading && (
          <div style={{ padding: "60px 0", textAlign: "center", color: "#9BA3B2" }}>
            기사를 불러오는 중...
          </div>
        )}

        {error && (
          <div style={{
            padding: "20px 24px", background: "#FEF2F2",
            border: "1px solid #FECACA", borderRadius: 10,
            color: "#DC2626", fontSize: 14,
          }}>
            {error}
          </div>
        )}

        {article && (
          <>
            {/* 메타 */}
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 16 }}>
              <span style={{
                fontSize: 11, fontWeight: 700,
                padding: "4px 12px", borderRadius: 999,
                background: tagStyle.bg, color: tagStyle.color,
              }}>{article.tag}</span>
              <span style={{ fontSize: 13, color: "#4A7BF7", fontWeight: 600 }}>{article.source}</span>
              <span style={{ fontSize: 13, color: "#9BA3B2" }}>{article.date}</span>
            </div>

            {/* 제목 */}
            <h1 style={{
              fontSize: 34, fontWeight: 800, lineHeight: 1.3,
              color: "#1A1F2E", letterSpacing: "-0.02em",
              margin: "0 0 24px",
            }}>
              {article.title}
            </h1>

            {/* 썸네일 */}
            {article.imageUrl && (
              <img
                src={article.imageUrl}
                alt={article.title}
                onError={(e) => { e.currentTarget.style.display = "none"; }}
                style={{
                  width: "100%", maxHeight: 420, objectFit: "cover",
                  borderRadius: 12, marginBottom: 28,
                  border: "1px solid #ECEEF2",
                }}
              />
            )}

            {/* 원문 보기 버튼 */}
            <a
              href={article.url}
              target="_blank"
              rel="noopener noreferrer"
              style={{
                display: "inline-flex", alignItems: "center", gap: 6,
                padding: "10px 20px", marginBottom: 32,
                background: "#1A1F2E", color: "#fff",
                borderRadius: 10, fontSize: 14, fontWeight: 600,
                textDecoration: "none",
              }}
            >
              원문 사이트에서 읽기 <IconArrowRight size={14} />
            </a>

            {/* 본문 */}
            {hasContent ? (
              <div
                className="article-content"
                dangerouslySetInnerHTML={{ __html: article.content }}
              />
            ) : (
              <div style={{
                padding: "32px 28px", background: "#F7F9FF",
                border: "1px solid #DBEAFE", borderRadius: 12,
                fontSize: 15, color: "#3D434C", lineHeight: 1.7,
              }}>
                <p style={{ margin: "0 0 16px", fontWeight: 600, color: "#1A1F2E" }}>
                  {article.description}
                </p>
                <p style={{ margin: 0, fontSize: 14, color: "#7B8290" }}>
                  RSS에 본문이 포함되어 있지 않은 글입니다. 원문 사이트에서 전체 내용을 확인해주세요.
                </p>
              </div>
            )}

            {/* 하단 원문 바로가기 */}
            {hasContent && (
              <div style={{
                marginTop: 56, padding: "24px 28px",
                background: "#F7F9FF", border: "1px solid #DBEAFE",
                borderRadius: 12,
                display: "flex", justifyContent: "space-between", alignItems: "center",
              }}>
                <div>
                  <div style={{ fontSize: 13, color: "#7B8290", marginBottom: 4 }}>출처</div>
                  <div style={{ fontSize: 15, fontWeight: 700, color: "#1A1F2E" }}>{article.source}</div>
                </div>
                <a
                  href={article.url} target="_blank" rel="noopener noreferrer"
                  style={{
                    padding: "10px 18px", background: "#4A7BF7", color: "#fff",
                    borderRadius: 8, fontSize: 13, fontWeight: 600,
                    textDecoration: "none",
                  }}
                >
                  원문 보기
                </a>
              </div>
            )}
          </>
        )}
      </div>

      <style>{`
        .article-content {
          font-size: 16px;
          line-height: 1.8;
          color: #3D434C;
        }
        .article-content h1, .article-content h2, .article-content h3 {
          color: #1A1F2E;
          letter-spacing: -0.01em;
          margin: 32px 0 14px;
          font-weight: 700;
        }
        .article-content h1 { font-size: 26px; }
        .article-content h2 { font-size: 22px; }
        .article-content h3 { font-size: 18px; }
        .article-content p { margin: 0 0 16px; }
        .article-content a { color: #4A7BF7; text-decoration: underline; }
        .article-content img { max-width: 100%; border-radius: 8px; margin: 16px 0; height: auto; }
        .article-content ul, .article-content ol { padding-left: 24px; margin: 0 0 16px; }
        .article-content li { margin-bottom: 6px; }
        .article-content code {
          background: #F3F4F6; color: #DC2626;
          padding: 2px 6px; border-radius: 4px;
          font-size: 14px; font-family: ui-monospace, "SF Mono", Consolas, monospace;
        }
        .article-content pre {
          background: #1A1F2E; color: #E2E8F0;
          padding: 20px; border-radius: 10px;
          overflow-x: auto; margin: 20px 0;
          font-size: 14px; line-height: 1.6;
        }
        .article-content pre code {
          background: transparent; color: inherit; padding: 0;
        }
        .article-content blockquote {
          border-left: 4px solid #4A7BF7;
          padding: 10px 20px; background: #F4F7FF;
          border-radius: 0 8px 8px 0;
          margin: 20px 0; color: #3D434C;
        }
        .article-content hr {
          border: none; border-top: 1px solid #ECEEF2; margin: 32px 0;
        }
        .article-content table {
          border-collapse: collapse; width: 100%; margin: 16px 0;
        }
        .article-content th, .article-content td {
          border: 1px solid #ECEEF2; padding: 8px 12px; text-align: left;
        }
        .article-content th { background: #F7F9FF; font-weight: 700; }
      `}</style>
    </div>
  );
};

export default TechTrendDetail;