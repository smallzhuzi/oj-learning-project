import { useEffect, useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useUserStore } from '@/store/userStore'
import ToastContainer from '@/components/ToastContainer'
import ConfirmDialog from '@/components/ConfirmDialog'
import ProblemListPage from './pages/ProblemListPage'
import ProblemPage from './pages/ProblemPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import AdminPage from './pages/AdminPage'
import ProblemSetPage from './pages/ProblemSetPage'
import ContestListPage from './pages/ContestListPage'
import ContestDetailPage from './pages/ContestDetailPage'
import CreateContestPage from './pages/CreateContestPage'

/** 路由守卫：无 token 则跳转登录 */
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = useUserStore((s) => s.token)
  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

/** 管理员路由守卫：非 admin 角色跳转首页 */
function AdminRoute({ children }: { children: React.ReactNode }) {
  const token = useUserStore((s) => s.token)
  const user = useUserStore((s) => s.user)
  if (!token) return <Navigate to="/login" replace />
  if (user?.role !== 'admin') return <Navigate to="/" replace />
  return <>{children}</>
}

export default function App() {
  const token = useUserStore((s) => s.token)
  const restoreSession = useUserStore((s) => s.restoreSession)
  const [restoring, setRestoring] = useState(true)

  useEffect(() => {
    if (token) {
      restoreSession().finally(() => setRestoring(false))
    } else {
      setRestoring(false)
    }
  }, [])

  if (restoring) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900 text-gray-400">
        加载中...
      </div>
    )
  }

  return (
    <>
    <ToastContainer />
    <ConfirmDialog />
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<PrivateRoute><ProblemListPage /></PrivateRoute>} />
      <Route path="/problem/:slug" element={<PrivateRoute><ProblemPage /></PrivateRoute>} />
      <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
      <Route path="/problem-sets" element={<PrivateRoute><ProblemSetPage /></PrivateRoute>} />
      <Route path="/contests" element={<PrivateRoute><ContestListPage /></PrivateRoute>} />
      <Route path="/contests/create" element={<PrivateRoute><CreateContestPage /></PrivateRoute>} />
      <Route path="/contests/:id/edit" element={<PrivateRoute><CreateContestPage /></PrivateRoute>} />
      <Route path="/contests/:id" element={<PrivateRoute><ContestDetailPage /></PrivateRoute>} />
      <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
    </>
  )
}
