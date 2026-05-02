import { http } from "./client.js";

export const questionApi = {
  list:            (categoryId)          => http.get(`/questions${categoryId ? `?categoryId=${categoryId}` : ""}`),
  detail:          (id)                  => http.get(`/questions/${id}`),
  listAnswers:     (questionId)          => http.get(`/questions/${questionId}/answers`),
  createAnswer:    (questionId, content) => http.post(`/questions/${questionId}/answers`, { content }),
  getAnswer:       (answerId)            => http.get(`/answers/${answerId}`),
  requestFeedback: (answerId)            => http.post(`/answers/${answerId}/feedback`, {}),
  myAnswers:       ()                    => http.get(`/answers/me`),
};

export const categoryApi = {
  list: () => http.get("/categories"),
};