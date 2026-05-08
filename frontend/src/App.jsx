import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext.jsx";
import Landing from "./screens/Landing.jsx";
import LevelCheck from "./screens/LevelCheck.jsx";
import Auth from "./screens/Auth.jsx";
import Dashboard from "./screens/Dashboard.jsx";
import QuestionList from "./screens/QuestionList.jsx";
import Solve from "./screens/Solve.jsx";
import AIFeedback from "./screens/AIFeedback.jsx";
import ResumeFeedback from "./screens/ResumeFeedback.jsx";
import MyAnswers from "./screens/MyAnswers.jsx";
import LearningStatus from "./screens/LearningStatus.jsx";
import Community from "./screens/Community.jsx";
import AnswerDetail from "./screens/AnswerDetail.jsx";
import TechTrendDetail from "./screens/TechTrendDetail.jsx";
import ProtectedRoute from "./components/common/ProtectedRoute.jsx";
import AdminRoute from "./components/admin/AdminRoute.jsx";
import AdminDashboard from "./screens/admin/AdminDashboard.jsx";
import AdminQuestions from "./screens/admin/AdminQuestions.jsx";
import AdminQuestionForm from "./screens/admin/AdminQuestionForm.jsx";
import AdminCategories from "./screens/admin/AdminCategories.jsx";
import AdminUsers from "./screens/admin/AdminUsers.jsx";
import AdminPreview from "./screens/admin/AdminPreview.jsx";

function HomeRoute() {
  const { isLoggedIn } = useAuth();
  return isLoggedIn ? <Dashboard /> : <Landing />;
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* 비로그인 접근 가능 — 첫 화면 / 회원가입·로그인 / 레벨 체크 */}
        <Route path="/" element={<HomeRoute />} />
        <Route path="/levelcheck" element={<LevelCheck />} />
        <Route path="/signup" element={<Auth mode="signup" />} />
        <Route path="/login" element={<Auth mode="login" />} />

        {/* 로그인 필요 — 학습 / 커뮤니티 */}
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/questions" element={<ProtectedRoute><QuestionList /></ProtectedRoute>} />
        <Route path="/solve" element={<ProtectedRoute><Solve /></ProtectedRoute>} />
        <Route path="/feedback" element={<ProtectedRoute><AIFeedback /></ProtectedRoute>} />
        <Route path="/resume" element={<ProtectedRoute><ResumeFeedback /></ProtectedRoute>} />
        <Route path="/my/answers" element={<ProtectedRoute><MyAnswers /></ProtectedRoute>} />
        <Route path="/my/status" element={<ProtectedRoute><LearningStatus /></ProtectedRoute>} />
        <Route path="/community" element={<ProtectedRoute><Community /></ProtectedRoute>} />
        <Route path="/answer" element={<ProtectedRoute><AnswerDetail /></ProtectedRoute>} />
        <Route path="/tech-trends/:id" element={<ProtectedRoute><TechTrendDetail /></ProtectedRoute>} />

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