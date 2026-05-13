  import React, { createContext, useContext, useState, useCallback, useEffect } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = sessionStorage.getItem("accessToken");
    const nickname = sessionStorage.getItem("nickname");
    const username = sessionStorage.getItem("username");
    const role = sessionStorage.getItem("role");
    const jobCategoryName = sessionStorage.getItem("jobCategoryName");
    return token ? { accessToken: token, nickname, username, role, jobCategoryName } : null;
  });

  const saveAuth = useCallback((tokenResponse) => {
    sessionStorage.setItem("accessToken", tokenResponse.accessToken);
    sessionStorage.setItem("nickname", tokenResponse.nickname);
    sessionStorage.setItem("username", tokenResponse.username);
    sessionStorage.setItem("role", tokenResponse.role ?? "USER");
    if (tokenResponse.jobCategoryName) {
      sessionStorage.setItem("jobCategoryName", tokenResponse.jobCategoryName);
    } else {
      sessionStorage.removeItem("jobCategoryName");
    }
    setAuth({
      accessToken: tokenResponse.accessToken,
      nickname: tokenResponse.nickname,
      username: tokenResponse.username,
      role: tokenResponse.role ?? "USER",
      jobCategoryName: tokenResponse.jobCategoryName ?? null,
    });
  }, []);

  const logout = useCallback(() => {
    sessionStorage.clear();
    setAuth(null);
  }, []);

  useEffect(() => {
    const handleExpired = () => setAuth(null);
    window.addEventListener("auth:expired", handleExpired);
    return () => window.removeEventListener("auth:expired", handleExpired);
  }, []);

  return (
    <AuthContext.Provider value={{
      auth,
      saveAuth,
      logout,
      isLoggedIn: !!auth,
      isAdmin: auth?.role === "ADMIN",
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth는 AuthProvider 안에서 사용해야 합니다.");
  return ctx;
}