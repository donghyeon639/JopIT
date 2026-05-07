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

export const resumeApi = {
  feedbackFromFile: (file, jobCategory) => {
    const fd = new FormData();
    fd.append("file", file);
    if (jobCategory) fd.append("jobCategory", jobCategory);
    return postMultipart("/resume/feedback", fd);
  },
  feedbackFromText: (text, jobCategory) => {
    const fd = new FormData();
    fd.append("text", text);
    if (jobCategory) fd.append("jobCategory", jobCategory);
    return postMultipart("/resume/feedback", fd);
  },
};