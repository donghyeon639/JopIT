import { http } from "./client.js";

// 문제 관리
export const adminQuestions = {
  list:   ()           => http.get("/admin/questions"),
  detail: (id)         => http.get(`/admin/questions/${id}`),
  create: (data)       => http.post("/admin/questions", data),
  update: (id, data)   => http.put(`/admin/questions/${id}`, data),
  remove: (id)         => http.delete(`/admin/questions/${id}`),
};

// 직군 카테고리 관리 (사용자 프로필용)
export const adminCategories = {
  list:         ()                   => http.get("/admin/categories"),
  create:       (data)               => http.post("/admin/categories", data),
  remove:       (id)                 => http.delete(`/admin/categories/${id}`),
  listDetails:  (categoryId)         => http.get(`/admin/categories/${categoryId}/details`),
  createDetail: (categoryId, data)   => http.post(`/admin/categories/${categoryId}/details`, data),
  removeDetail: (detailId)           => http.delete(`/admin/categories/details/${detailId}`),
};

// 질문 카테고리 관리 (CS 주제별 분류)
export const adminQuestionCategories = {
  list:   ()           => http.get("/admin/question-categories"),
  create: (data)       => http.post("/admin/question-categories", data),
  update: (id, data)   => http.put(`/admin/question-categories/${id}`, data),
  remove: (id)         => http.delete(`/admin/question-categories/${id}`),
};

// 사용자 관리
export const adminUsers = {
  list:       ()              => http.get("/admin/users"),
  changeRole: (id, role)      => http.put(`/admin/users/${id}/role?role=${role}`, {}),
};