import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  IconUser, IconLock, IconMail, IconGoogle, IconGithub, IconKakao, IconArrowRight,
  Logo
} from "../components/Components.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { signup as apiSignup, login as apiLogin } from "../api/authApi.js";
import { JOB_CATEGORIES } from "../constants/jobs.js";

const Auth = ({ mode = "signup" }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { saveAuth } = useAuth();
  const isSignup = mode === "signup";
  const redirectAfterLogin = location.state?.from || "/dashboard";
  const sessionExpired = !isSignup && new URLSearchParams(location.search).get("reason") === "expired";
  const socialFailed = !isSignup && new URLSearchParams(location.search).get("reason") === "social_failed";

  const [selectedJob, setSelectedJob] = useState(0);
  const [form, setForm] = useState({ username: "", password: "", nickname: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const socialBasePath = import.meta.env.VITE_API_BASE_URL || "/api";

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    setError("");
  };

  const handleSubmit = async () => {
    setError("");
    setLoading(true);
    try {
      let result;
      if (isSignup) {
        await apiSignup({
          username: form.username,
          password: form.password,
          nickname: form.nickname,
          jobCategoryName: JOB_CATEGORIES[selectedJob].value,
        });
        navigate("/login");
        return;
      }
      result = await apiLogin({ username: form.username, password: form.password });
      saveAuth(result);
      navigate(redirectAfterLogin, { replace: true });
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSocialLogin = (provider) => {
    window.location.href = `${socialBasePath}/social/authorization/${provider}`;
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
        {/* Logo */}
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 32 }}>
          <Logo />
        </div>

        {/* Header */}
        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-0.025em",
            color: "var(--gray-900)", marginBottom: 8 }}>
            {isSignup ? "시작해볼까요" : "다시 만나서 반가워요"}
          </h1>
          <p style={{ fontSize: 14, color: "var(--gray-500)", lineHeight: 1.6 }}>
            {isSignup
              ? "아이디만 있으면 바로 면접 준비를 시작할 수 있어요."
              : "로그인하고 어제 풀던 문제를 이어서 풀어보세요."}
          </p>
        </div>

        {/* Social buttons */}
        <div style={{ display: "flex", flexDirection: "column", gap: 10, marginBottom: 24 }}>
          <button className="btn btn-outline btn-lg"
            style={{ width: "100%", justifyContent: "center", gap: 10 }}
            onClick={() => handleSocialLogin("google")}>
            <IconGoogle /> Google로 {isSignup ? "시작하기" : "로그인"}
          </button>
          <button className="btn btn-outline btn-lg"
            style={{ width: "100%", justifyContent: "center", gap: 10 }}
            onClick={() => handleSocialLogin("github")}>
            <IconGithub /> GitHub으로 {isSignup ? "시작하기" : "로그인"}
          </button>
          <button className="btn btn-lg"
            style={{ width: "100%", justifyContent: "center", gap: 10, background: "#FEE500", color: "#191919" }}
            onClick={() => handleSocialLogin("kakao")}>
            <IconKakao /> 카카오로 {isSignup ? "시작하기" : "로그인"}
          </button>
        </div>


        {/* Error message */}
        {socialFailed && !error && (
          <div style={{
            marginBottom: 16, padding: "10px 14px",
            background: "#FEF2F2", border: "1px solid #FECACA",
            borderRadius: 8, color: "#DC2626", fontSize: 13
          }}>
            소셜 로그인에 실패했습니다. 다시 시도해주세요.
          </div>
        )}

        {sessionExpired && !error && (
          <div style={{
            marginBottom: 16, padding: "10px 14px",
            background: "#EFF6FF", border: "1px solid #BFDBFE",
            borderRadius: 8, color: "#1D4ED8", fontSize: 13
          }}>
            세션이 만료되었습니다. 다시 로그인해주세요.
          </div>
        )}

        {error && (
          <div style={{
            marginBottom: 16, padding: "10px 14px",
            background: "#FEF2F2", border: "1px solid #FECACA",
            borderRadius: 8, color: "#DC2626", fontSize: 13
          }}>
            {error}
          </div>
        )}

        {/* Form fields */}
        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {isSignup && (
            <Field label="닉네임" name="nickname" icon={<IconUser size={15} />}
              placeholder="예: 김개발" value={form.nickname} onChange={handleChange}
              onKeyDown={(e) => e.key === "Enter" && handleSubmit()} />
          )}
          <Field label="아이디" name="username" icon={<IconUser size={15} />}
            placeholder="사용할 아이디 입력"
            value={form.username} onChange={handleChange}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()} />
          <Field label="비밀번호" name="password" icon={<IconLock size={15} />}
            type="password" placeholder={isSignup ? "8자 이상 입력" : "비밀번호"}
            value={form.password} onChange={handleChange}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()} />

          {isSignup && (
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
          )}

          <button className="btn btn-primary btn-lg"
            style={{ width: "100%", marginTop: 4, fontSize: 15, fontWeight: 700, opacity: loading ? 0.7 : 1 }}
            onClick={handleSubmit}
            disabled={loading}>
            {loading ? "처리 중..." : (isSignup ? "무료로 시작하기" : "로그인")}
            {!loading && <IconArrowRight size={15} />}
          </button>
        </div>

        {/* Footer links */}
        <div style={{ marginTop: 24, textAlign: "center" }}>
          {isSignup ? (
            <p style={{ fontSize: 13, color: "var(--gray-500)" }}>
              이미 계정이 있어요?{" "}
              <span onClick={() => navigate("/login")}
                style={{ color: "var(--blue-600)", fontWeight: 600, cursor: "pointer" }}>
                로그인
              </span>
            </p>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10, alignItems: "center" }}>
              <span style={{ fontSize: 13, color: "var(--blue-600)", fontWeight: 500, cursor: "pointer" }}>
                비밀번호를 잊으셨나요?
              </span>
              <p style={{ fontSize: 13, color: "var(--gray-500)", margin: 0 }}>
                아직 계정이 없어요?{" "}
                <span onClick={() => navigate("/signup")}
                  style={{ color: "var(--blue-600)", fontWeight: 600, cursor: "pointer" }}>
                  회원가입
                </span>
              </p>
            </div>
          )}
        </div>

        {isSignup && (
          <p style={{ fontSize: 11, color: "var(--gray-400)", textAlign: "center",
            marginTop: 20, lineHeight: 1.6 }}>
            가입 시{" "}
            <span style={{ textDecoration: "underline", cursor: "pointer" }}>이용약관</span>
            {" "}및{" "}
            <span style={{ textDecoration: "underline", cursor: "pointer" }}>개인정보 처리방침</span>
            에 동의하게 됩니다.
          </p>
        )}
      </div>
    </div>
  );
};

const Field = ({ label, name, icon, type = "text", placeholder, value, onChange, onKeyDown }) => (
  <div>
    <label style={{ display: "block", marginBottom: 6,
      fontSize: 12, fontWeight: 600, color: "var(--gray-700)" }}>{label}</label>
    <div style={{ position: "relative" }}>
      <div style={{ position: "absolute", left: 12, top: "50%",
        transform: "translateY(-50%)", color: "var(--gray-400)" }}>{icon}</div>
      <input className="input" type={type} name={name} placeholder={placeholder}
        value={value} onChange={onChange} onKeyDown={onKeyDown} style={{ paddingLeft: 38 }} />
    </div>
  </div>
);

export default Auth;