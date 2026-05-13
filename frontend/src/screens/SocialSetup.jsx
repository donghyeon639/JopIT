import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Logo, IconUser, IconArrowRight } from "../components/Components.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { socialSetup } from "../api/authApi.js";
import { JOB_CATEGORIES } from "../constants/jobs.js";

const SocialSetup = () => {
  const navigate = useNavigate();
  const { user, saveAuth } = useAuth();
  const [selectedJob, setSelectedJob] = useState(0);
  const [nickname, setNickname] = useState(user?.nickname || "");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!nickname.trim()) {
      setError("닉네임을 입력해주세요.");
      return;
    }
    
    setError("");
    setLoading(true);
    try {
      const result = await socialSetup({
        nickname: nickname.trim(),
        jobCategoryName: JOB_CATEGORIES[selectedJob].value,
      });
      saveAuth(result);
      navigate("/dashboard", { replace: true });
    } catch (e) {
      setError(e.message || "프로필 설정 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: "100vh", display: "flex",
      alignItems: "center", justifyContent: "center",
      background: "var(--gray-50)"
    }}>
      <div style={{
        width: "100%", maxWidth: 520,
        padding: "48px 52px",
        background: "#fff",
        borderRadius: 16,
        boxShadow: "0 4px 24px rgba(0,0,0,0.08)",
        margin: "0 16px"
      }}>
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 32 }}>
          <Logo />
        </div>

        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-0.025em",
            color: "var(--gray-900)", marginBottom: 8 }}>
            추가 정보 입력
          </h1>
          <p style={{ fontSize: 14, color: "var(--gray-500)", lineHeight: 1.6 }}>
            환영합니다! 맞춤형 면접 준비를 위해 직군과 닉네임을 설정해주세요.
          </p>
        </div>

        {error && (
          <div style={{
            marginBottom: 16, padding: "10px 14px",
            background: "#FEF2F2", border: "1px solid #FECACA",
            borderRadius: 8, color: "#DC2626", fontSize: 13
          }}>
            {error}
          </div>
        )}

        <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
          <div>
            <label style={{ display: "block", marginBottom: 8,
              fontSize: 12, fontWeight: 600, color: "var(--gray-700)" }}>
              닉네임
            </label>
            <div style={{ position: "relative" }}>
              <div style={{ position: "absolute", left: 12, top: "50%",
                transform: "translateY(-50%)", color: "var(--gray-400)" }}>
                <IconUser size={15} />
              </div>
              <input className="input" type="text" placeholder="닉네임 입력"
                value={nickname} onChange={(e) => setNickname(e.target.value)}
                style={{ paddingLeft: 38 }} />
            </div>
          </div>

          <div>
            <label style={{ display: "block", marginBottom: 8,
              fontSize: 12, fontWeight: 600, color: "var(--gray-700)" }}>
              관심 직군 (대분류)
            </label>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 8 }}>
              {JOB_CATEGORIES.map((j, i) => (
                <button key={j.value} onClick={() => setSelectedJob(i)} style={{
                  padding: "10px 6px",
                  border: "1.5px solid",
                  borderColor: selectedJob === i ? "var(--blue-600)" : "var(--gray-200)",
                  background: selectedJob === i ? "var(--blue-50)" : "#fff",
                  color: selectedJob === i ? "var(--blue-700)" : "var(--gray-600)",
                  borderRadius: 8, textAlign: "center",
                  fontSize: 12, fontWeight: 600, cursor: "pointer",
                  transition: "all 0.15s", fontFamily: "inherit"
                }}>{j.label}</button>
              ))}
            </div>
          </div>

          <button className="btn btn-primary btn-lg"
            style={{ width: "100%", marginTop: 8, fontSize: 15, fontWeight: 700, opacity: loading ? 0.7 : 1 }}
            onClick={handleSubmit}
            disabled={loading}>
            {loading ? "처리 중..." : "설정 완료"}
            {!loading && <IconArrowRight size={15} />}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SocialSetup;
