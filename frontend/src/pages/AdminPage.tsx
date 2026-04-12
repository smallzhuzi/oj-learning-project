import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, ArrowLeft, UserPlus, RotateCcw, Ban, Trash2, X, RefreshCw } from 'lucide-react'
import { getUsers, toggleUserStatus, resetUserPassword, deleteUser, createUser } from '@/api/admin'
import { syncProblems } from '@/api/problem'
import { toast, confirm } from '@/store/uiStore'
import type { UserInfo } from '@/types'

export default function AdminPage() {
  const navigate = useNavigate()

  const [users, setUsers] = useState<UserInfo[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [size] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)

  // 添加用户弹窗
  const [showCreate, setShowCreate] = useState(false)
  const [createForm, setCreateForm] = useState({ username: '', email: '', password: '', role: 'user' })
  const [createLoading, setCreateLoading] = useState(false)

  // 重置密码结果弹窗
  const [resetResult, setResetResult] = useState<{ username: string; password: string } | null>(null)

  // 题库同步
  const [syncPlatform, setSyncPlatform] = useState('luogu')
  const [syncing, setSyncing] = useState(false)
  const [syncResult, setSyncResult] = useState<string | null>(null)

  const totalPages = Math.ceil(total / size)

  /** 加载用户列表 */
  const loadUsers = (p = page) => {
    setLoading(true)
    setPage(p)
    getUsers(p, size, keyword)
      .then((res) => {
        if (res.code === 200) {
          setUsers(res.data.records)
          setTotal(res.data.total)
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadUsers(1)
  }, [])

  /** 切换用户状态 */
  const handleToggleStatus = async (user: UserInfo) => {
    const action = user.status === 'active' ? '禁用' : '启用'
    if (!await confirm(`确定要${action}用户 ${user.username} 吗？`, { type: 'warning', confirmText: action })) return
    toggleUserStatus(user.id)
      .then((res) => {
        if (res.code === 200) loadUsers()
        else toast.error(res.message)
      })
      .catch(() => toast.error('操作失败'))
  }

  /** 重置密码 */
  const handleResetPassword = async (user: UserInfo) => {
    if (!await confirm(`确定要重置用户 ${user.username} 的密码吗？`, { type: 'warning', confirmText: '重置密码' })) return
    resetUserPassword(user.id)
      .then((res) => {
        if (res.code === 200) {
          setResetResult({ username: user.username, password: res.data })
        } else {
          toast.error(res.message)
        }
      })
      .catch(() => toast.error('操作失败'))
  }

  /** 删除用户 */
  const handleDelete = async (user: UserInfo) => {
    if (!await confirm(`确定要删除用户 ${user.username} 吗？此操作不可撤销！`, { type: 'danger', confirmText: '删除' })) return
    deleteUser(user.id)
      .then((res) => {
        if (res.code === 200) loadUsers()
        else toast.error(res.message)
      })
      .catch(() => toast.error('操作失败'))
  }

  /** 添加用户 */
  const handleCreate = () => {
    if (!createForm.username || !createForm.email || !createForm.password) {
      toast.warning('请填写完整信息')
      return
    }
    setCreateLoading(true)
    createUser(createForm)
      .then((res) => {
        if (res.code === 200) {
          setShowCreate(false)
          setCreateForm({ username: '', email: '', password: '', role: 'user' })
          loadUsers(1)
        } else {
          toast.error(res.message)
        }
      })
      .catch(() => toast.error('创建失败'))
      .finally(() => setCreateLoading(false))
  }

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100">
      <div className="max-w-6xl mx-auto px-6 py-8">

        {/* 顶部导航 */}
        <div className="mb-8 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-400 hover:text-gray-200 border border-gray-600 rounded-lg hover:bg-gray-700 transition"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              返回题库
            </button>
            <h1 className="text-xl font-bold text-gray-100">管理后台</h1>
          </div>
          <div className="flex items-center gap-3">
            {/* 题库同步 */}
            <select
              value={syncPlatform}
              onChange={(e) => setSyncPlatform(e.target.value)}
              className="px-3 py-2 bg-gray-800 border border-gray-600 rounded-lg text-sm text-gray-200 focus:outline-none"
            >
              <option value="leetcode">LeetCode</option>
              <option value="luogu">洛谷</option>
            </select>
            <button
              onClick={async () => {
                setSyncing(true)
                setSyncResult(null)
                try {
                  const res = await syncProblems(0, 50, syncPlatform)
                  if (res.code === 200) setSyncResult(`成功同步 ${res.data} 道新题目`)
                  else setSyncResult(`同步失败: ${res.message}`)
                } catch { setSyncResult('同步失败') }
                finally { setSyncing(false) }
              }}
              disabled={syncing}
              className="flex items-center gap-1.5 px-4 py-2 border border-gray-600 text-gray-300 rounded-lg text-sm hover:bg-gray-700 disabled:opacity-50 transition"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${syncing ? 'animate-spin' : ''}`} />
              {syncing ? '同步中...' : '同步题库'}
            </button>
            {syncResult && <span className="text-xs text-gray-400">{syncResult}</span>}
            <button
              onClick={() => setShowCreate(true)}
              className="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 transition"
            >
              <UserPlus className="w-4 h-4" />
              添加用户
            </button>
          </div>
        </div>

        {/* 搜索栏 */}
        <div className="mb-6 flex gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
            <input
              type="text"
              placeholder="按用户名或邮箱搜索..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadUsers(1)}
              className="w-full pl-10 pr-4 py-2 border border-gray-600 rounded-lg bg-gray-800 text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <button
            onClick={() => loadUsers(1)}
            className="px-6 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 transition"
          >
            搜索
          </button>
        </div>

        {/* 用户列表表格 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-800/50 border-b border-gray-700">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">用户名</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">邮箱</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">角色</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">状态</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-500">加载中...</td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-500">暂无用户数据</td>
                </tr>
              ) : (
                users.map((u) => (
                  <tr key={u.id} className="hover:bg-gray-700/50 transition">
                    <td className="px-6 py-4 text-sm text-gray-400">{u.id}</td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-100">{u.username}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{u.email}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs rounded-full font-medium ${
                        u.role === 'admin' ? 'text-purple-300 bg-purple-900/30' : 'text-blue-300 bg-blue-900/30'
                      }`}>
                        {u.role === 'admin' ? '管理员' : '普通用户'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs rounded-full font-medium ${
                        u.status === 'active' ? 'text-green-400 bg-green-900/30' : 'text-red-400 bg-red-900/30'
                      }`}>
                        {u.status === 'active' ? '正常' : '已禁用'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleToggleStatus(u)}
                          className={`flex items-center gap-1 px-2 py-1 text-xs rounded border transition ${
                            u.status === 'active'
                              ? 'text-amber-400 border-amber-700 hover:bg-amber-900/30'
                              : 'text-green-400 border-green-700 hover:bg-green-900/30'
                          }`}
                          title={u.status === 'active' ? '禁用' : '启用'}
                        >
                          <Ban className="w-3 h-3" />
                          {u.status === 'active' ? '禁用' : '启用'}
                        </button>
                        <button
                          onClick={() => handleResetPassword(u)}
                          className="flex items-center gap-1 px-2 py-1 text-xs text-blue-400 border border-blue-700 rounded hover:bg-blue-900/30 transition"
                          title="重置密码"
                        >
                          <RotateCcw className="w-3 h-3" />
                          重置密码
                        </button>
                        {u.role !== 'admin' && (
                          <button
                            onClick={() => handleDelete(u)}
                            className="flex items-center gap-1 px-2 py-1 text-xs text-red-400 border border-red-700 rounded hover:bg-red-900/30 transition"
                            title="删除"
                          >
                            <Trash2 className="w-3 h-3" />
                            删除
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* 分页 */}
        {totalPages >= 1 && (
          <div className="mt-5 flex items-center justify-between">
            <span className="text-sm text-gray-500">共 {total} 位用户</span>
            <div className="flex items-center gap-1">
              <button disabled={page <= 1} onClick={() => loadUsers(1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition" title="首页">
                <ChevronsLeft className="w-4 h-4" />
              </button>
              <button disabled={page <= 1} onClick={() => loadUsers(page - 1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition" title="上一页">
                <ChevronLeft className="w-4 h-4" />
              </button>
              {(() => {
                const pages: (number | '...')[] = []
                const range = 2
                const left = Math.max(1, page - range)
                const right = Math.min(totalPages, page + range)
                if (left > 1) { pages.push(1); if (left > 2) pages.push('...') }
                for (let i = left; i <= right; i++) pages.push(i)
                if (right < totalPages) { if (right < totalPages - 1) pages.push('...'); pages.push(totalPages) }
                return pages.map((p, idx) =>
                  p === '...' ? (
                    <span key={`dot-${idx}`} className="w-8 text-center text-gray-500 text-sm select-none">...</span>
                  ) : (
                    <button key={p} onClick={() => loadUsers(p)}
                      className={`w-8 h-8 text-sm rounded transition ${p === page ? 'bg-indigo-600 text-white' : 'border border-gray-600 hover:bg-gray-700 text-gray-300'}`}>
                      {p}
                    </button>
                  )
                )
              })()}
              <button disabled={page >= totalPages} onClick={() => loadUsers(page + 1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition" title="下一页">
                <ChevronRight className="w-4 h-4" />
              </button>
              <button disabled={page >= totalPages} onClick={() => loadUsers(totalPages)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition" title="末页">
                <ChevronsRight className="w-4 h-4" />
              </button>
            </div>
            <span className="text-sm text-gray-500">第 {page} / {totalPages} 页</span>
          </div>
        )}
      </div>

      {/* 添加用户弹窗 */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/60" onClick={() => setShowCreate(false)} />
          <div className="relative bg-gray-800 rounded-xl border border-gray-700 shadow-2xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-100">添加用户</h2>
              <button onClick={() => setShowCreate(false)} className="p-1 rounded hover:bg-gray-700 text-gray-500 hover:text-gray-300 transition">
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">用户名</label>
                <input
                  type="text" value={createForm.username}
                  onChange={(e) => setCreateForm((f) => ({ ...f, username: e.target.value }))}
                  className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="3-20 个字符"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">邮箱</label>
                <input
                  type="email" value={createForm.email}
                  onChange={(e) => setCreateForm((f) => ({ ...f, email: e.target.value }))}
                  className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="user@example.com"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">密码</label>
                <input
                  type="text" value={createForm.password}
                  onChange={(e) => setCreateForm((f) => ({ ...f, password: e.target.value }))}
                  className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="6-50 个字符"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">角色</label>
                <select
                  value={createForm.role}
                  onChange={(e) => setCreateForm((f) => ({ ...f, role: e.target.value }))}
                  className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="user">普通用户</option>
                  <option value="admin">管理员</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setShowCreate(false)}
                className="px-4 py-2 text-sm text-gray-400 border border-gray-600 rounded-lg hover:bg-gray-700 transition"
              >
                取消
              </button>
              <button
                onClick={handleCreate}
                disabled={createLoading}
                className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition"
              >
                {createLoading ? '创建中...' : '确认创建'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 重置密码结果弹窗 */}
      {resetResult && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/60" onClick={() => setResetResult(null)} />
          <div className="relative bg-gray-800 rounded-xl border border-gray-700 shadow-2xl w-full max-w-sm p-6">
            <h2 className="text-lg font-semibold text-gray-100 mb-4">密码已重置</h2>
            <p className="text-sm text-gray-400 mb-2">
              用户 <span className="text-gray-200 font-medium">{resetResult.username}</span> 的新密码为：
            </p>
            <div className="bg-gray-700 rounded-lg px-4 py-3 font-mono text-lg text-center text-indigo-300 select-all mb-4">
              {resetResult.password}
            </div>
            <p className="text-xs text-gray-500 mb-4">请将新密码告知用户，此密码仅显示一次。</p>
            <div className="flex justify-end">
              <button
                onClick={() => setResetResult(null)}
                className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
              >
                知道了
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
