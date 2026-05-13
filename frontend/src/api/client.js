const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

function isAuthPath(path) {
  return path.startsWith("/auth/");
}

function redirectToLoginIfNeeded() {
  if (typeof window === "undefined") return;
  const currentPath = window.location.pathname;
  if (currentPath === "/login" || currentPath === "/signup") return;
  window.location.href = "/login?reason=expired";
}

async function request(path, options = {}) {
  const token = sessionStorage.getItem("accessToken");
  const includeAuthHeader = token && !isAuthPath(path);
  const headers = {
    "Content-Type": "application/json",
    ...(includeAuthHeader ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // 204 No Content 등 body 없는 응답 처리
  const data = res.status !== 204 ? await res.json().catch(() => ({})) : {};

  if (res.status === 401 && !isAuthPath(path)) {
    sessionStorage.clear();
    if (typeof window !== "undefined") {
      window.dispatchEvent(new Event("auth:expired"));
    }
    redirectToLoginIfNeeded();
  }

  if (!res.ok) {
    const msg =
      data.message ||
      (typeof data === "object" ? Object.values(data)[0] : null) ||
      "요청에 실패했습니다.";
    throw new Error(msg);
  }

  return data;
}

export const http = {
  get:    (path, options)       => request(path, { method: "GET", ...options }),
  post:   (path, body, options) => request(path, { method: "POST",   body: JSON.stringify(body), ...options }),
  put:    (path, body, options) => request(path, { method: "PUT",    body: JSON.stringify(body), ...options }),
  delete: (path, options)       => request(path, { method: "DELETE", ...options }),
};