const BASE_URL = "http://localhost:8080/api";

async function request(path, options = {}) {
  const token = sessionStorage.getItem("accessToken");
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // 204 No Content 등 body 없는 응답 처리
  const data = res.status !== 204 ? await res.json().catch(() => ({})) : {};

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