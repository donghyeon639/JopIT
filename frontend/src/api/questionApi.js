import { http } from "./client.js";

function buildQuestionListQuery({ categoryId, difficulty, page = 0, size = 10 } = {}) {
  const params = new URLSearchParams();
  if (categoryId) params.set("categoryId", categoryId);
  if (difficulty) params.set("difficulty", difficulty);
  params.set("page", page);
  params.set("size", size);
  return params.toString();
}

export const questionApi = {
  list:            (opts = {})           => http.get(`/questions?${buildQuestionListQuery(opts)}`),
  detail:          (id)                  => http.get(`/questions/${id}`),
  listAnswers:     (questionId)          => http.get(`/questions/${questionId}/answers`),
  createAnswer:    (questionId, content) => http.post(`/questions/${questionId}/answers`, { content }),
  getAnswer:       (answerId)            => http.get(`/answers/${answerId}`),
  requestFeedback: (answerId)            => http.post(`/answers/${answerId}/feedback`, {}),
  myAnswers:       ()                    => http.get(`/answers/me`),
};

export const communityApi = {
  feed: () => http.get(`/answers`),
};

export const commentApi = {
  list:   (answerId)          => http.get(`/answers/${answerId}/comments`),
  create: (answerId, content) => http.post(`/answers/${answerId}/comments`, { content }),
  delete: (commentId)         => http.delete(`/comments/${commentId}`),
};

export const categoryApi = {
  list: () => http.get("/categories"),
};

export const meApi = {
  stats: () => http.get("/me/stats"),
};

export const techTrendApi = {
  list:   ()   => http.get("/tech-trends"),
  detail: (id) => http.get(`/tech-trends/${id}`),
};