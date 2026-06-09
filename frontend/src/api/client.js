const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

function isAuthPath(path) {
  return path.startsWith("/auth/");
}

// 동시 요청이 여러 개 401을 받아도 팝업·이동은 한 번만 처리하기 위한 가드.
let handlingSessionExpiry = false;

function handleSessionExpired() {
  if (handlingSessionExpiry) return;
  handlingSessionExpiry = true;

  sessionStorage.clear();
  if (typeof window === "undefined") return;

  window.dispatchEvent(new Event("auth:expired"));

  const currentPath = window.location.pathname;
  if (currentPath === "/login" || currentPath === "/signup") {
    handlingSessionExpiry = false;
    return;
  }

  // 팝업으로 로그아웃 사실을 한 번 알린 뒤 로그인 화면으로 이동.
  // (이동은 하드 네비게이션이라 React 모달은 즉시 사라지므로 차단형 alert을 쓴다.)
  window.alert("로그인이 만료되어 로그아웃되었습니다. 다시 로그인해주세요.");
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
    handleSessionExpired();
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
  patch:  (path, body, options) => request(path, { method: "PATCH",  body: body == null ? undefined : JSON.stringify(body), ...options }),
  delete: (path, options)       => request(path, { method: "DELETE", ...options }),
};