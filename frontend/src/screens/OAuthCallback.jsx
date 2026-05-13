import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function OAuthCallback() {
  const location = useLocation();
  const navigate = useNavigate();
  const { saveAuth } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const accessToken = params.get("accessToken");
    const needsProfileUpdate = params.get("needsProfileUpdate") === "true";

    if (!accessToken) {
      navigate("/login?reason=social_failed", { replace: true });
      return;
    }

    const authData = {
      accessToken,
      nickname: params.get("nickname") || "소셜사용자",
      username: params.get("username") || "",
      role: params.get("role") || "USER",
      jobCategoryName: params.get("jobCategoryName") || null,
    };

    saveAuth(authData);

    if (needsProfileUpdate) {
      navigate("/social-setup", { replace: true });
    } else {
      navigate("/dashboard", { replace: true });
    }
  }, [location.search, navigate, saveAuth]);

  return (
    <div style={{ minHeight: "100vh", display: "grid", placeItems: "center", background: "var(--gray-50)" }}>
      <div style={{ color: "var(--gray-700)", fontWeight: 600 }}>소셜 로그인 처리 중입니다...</div>
    </div>
  );
}

