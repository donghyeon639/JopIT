import { http } from "./client.js";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

async function postMultipart(path, formData) {
  const token = sessionStorage.getItem("accessToken");
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  });

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

export const interviewApi = {
  createSession: (file, jobCategory, interviewType) => {
    const fd = new FormData();
    fd.append("file", file);
    fd.append("jobCategory", jobCategory);
    fd.append("interviewType", interviewType);
    return postMultipart("/interview/sessions", fd);
  },

  getSession: (sessionId) => http.get(`/interview/sessions/${sessionId}`),

  submitAnswer: (sessionId, questionId, transcript) =>
    http.post(`/interview/sessions/${sessionId}/questions/${questionId}/answer`, { transcript }),

  complete: (sessionId) => http.post(`/interview/sessions/${sessionId}/complete`),

  listSessions: () => http.get("/interview/sessions"),
};
