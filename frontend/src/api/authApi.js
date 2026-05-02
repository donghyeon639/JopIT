import { http } from "./client.js";

export const signup = (body) => http.post("/auth/signup", body);
export const login  = (body) => http.post("/auth/login",  body);