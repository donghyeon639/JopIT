import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext.jsx";
import Landing from "./screens/Landing.jsx";
import LevelCheck from "./screens/LevelCheck.jsx";
import Auth from "./screens/Auth.jsx";
import Dashboard from "./screens/Dashboard.jsx";
import QuestionList from "./screens/QuestionList.jsx";
import Solve from "./screens/Solve.jsx";
import AIFeedback from "./screens/AIFeedback.jsx";
import Community from "./screens/Community.jsx";
import AdminRoute from "./components/admin/AdminRoute.jsx";
import AdminDashboard from "./screens/admin/AdminDashboard.jsx";
import AdminQuestions from "./screens/admin/AdminQuestions.jsx";
import AdminQuestionForm from "./screens/admin/AdminQuestionForm.jsx";
import AdminCategories from "./screens/admin/AdminCategories.jsx";
import AdminUsers from "./screens/admin/AdminUsers.jsx";
import AdminPreview from "./screens/admin/AdminPreview.jsx";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* 일반 사용자 라우트 */}
        <Route path="/" element={<Landing />} />
        <Route path="/levelcheck" element={<LevelCheck />} />
        <Route path="/signup" element={<Auth mode="signup" />} />
        <Route path="/login" element={<Auth mode="login" />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/questions" element={<QuestionList />} />
        <Route path="/solve" element={<Solve />} />
        <Route path="/feedback" element={<AIFeedback />} />
        <Route path="/community" element={<Community />} />

        {/* 관리자 전용 라우트 */}
        <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/questions" element={<AdminRoute><AdminQuestions /></AdminRoute>} />
        <Route path="/admin/questions/new" element={<AdminRoute><AdminQuestionForm /></AdminRoute>} />
        <Route path="/admin/questions/:id" element={<AdminRoute><AdminQuestionForm /></AdminRoute>} />
        <Route path="/admin/categories" element={<AdminRoute><AdminCategories /></AdminRoute>} />
        <Route path="/admin/users" element={<AdminRoute><AdminUsers /></AdminRoute>} />
        <Route path="/admin/preview" element={<AdminRoute><AdminPreview /></AdminRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}