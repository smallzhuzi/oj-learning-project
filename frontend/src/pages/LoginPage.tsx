import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2, Mail } from 'lucide-react'
import { useUserStore } from '@/store/userStore'
import { sendCode, resetPassword } from '@/api/email'
import ThemeToggle from '@/components/ThemeToggle'

type Tab = 'login' | 'register' | 'forgot'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, register } = useUserStore()

  const [tab, setTab] = useState<Tab>('login')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPwd, setConfirmPwd] = useState('')
  const [verifyCode, setVerifyCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPwd, setConfirmNewPwd] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [codeSending, setCodeSending] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const countdownRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const resetFields = () => {
    setUsername('')
    setEmail('')
    setPassword('')
    setConfirmPwd('')
    setVerifyCode('')
    setNewPassword('')
    setConfirmNewPwd('')
    setError('')
    setSuccess('')
  }

  const switchTab = (t: Tab) => {
    setTab(t)
    resetFields()
    setCountdown(0)
    if (countdownRef.current) clearInterval(countdownRef.current)
  }

  // 倒计时逻辑
  useEffect(() => {
    if (countdown <= 0 && countdownRef.current) {
      clearInterval(countdownRef.current)
      countdownRef.current = null
    }
  }, [countdown])

  useEffect(() => {
    return () => {
      if (countdownRef.current) clearInterval(countdownRef.current)
    }
  }, [])

  /** 发送验证码 */
  const handleSendCode = async () => {
    if (!email.trim()) {
      setError('请先输入邮箱')
      return
    }
    setCodeSending(true)
    setError('')
    try {
      const res = await sendCode(email.trim())
      if (res.code === 200) {
        setCountdown(60)
        countdownRef.current = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) return 0
            return prev - 1
          })
        }, 1000)
      } else {
        setError(res.message || '发送失败')
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || '发送失败')
    } finally {
      setCodeSending(false)
    }
  }

  /** 登录 */
  const handleLogin = async () => {
    if (!username.trim() || !password.trim()) {
      setError('请填写用户名和密码')
      return
    }
    setLoading(true)
    setError('')
    const ok = await login({ username: username.trim(), password })
    setLoading(false)
    if (ok) {
      navigate('/', { replace: true })
    } else {
      setError('用户名或密码错误')
    }
  }

  /** 注册 */
  const handleRegister = async () => {
    if (!username.trim() || !email.trim() || !password.trim()) {
      setError('请填写所有字段')
      return
    }
    if (username.trim().length < 3 || username.trim().length > 20) {
      setError('用户名长度需在 3-20 之间')
      return
    }
    if (password.length < 6) {
      setError('密码长度至少 6 位')
      return
    }
    if (password !== confirmPwd) {
      setError('两次输入的密码不一致')
      return
    }
    if (!verifyCode.trim()) {
      setError('请输入邮箱验证码')
      return
    }
    setLoading(true)
    setError('')
    const ok = await register({
      username: username.trim(),
      email: email.trim(),
      password,
      code: verifyCode.trim(),
    })
    setLoading(false)
    if (ok) {
      navigate('/', { replace: true })
    } else {
      setError('注册失败，用户名或邮箱可能已被使用，或验证码错误')
    }
  }

  /** 忘记密码 */
  const handleResetPassword = async () => {
    if (!email.trim()) {
      setError('请输入邮箱')
      return
    }
    if (!verifyCode.trim()) {
      setError('请输入验证码')
      return
    }
    if (!newPassword.trim() || newPassword.length < 6) {
      setError('新密码长度至少 6 位')
      return
    }
    if (newPassword !== confirmNewPwd) {
      setError('两次输入的密码不一致')
      return
    }
    setLoading(true)
    setError('')
    try {
      const res = await resetPassword({
        email: email.trim(),
        code: verifyCode.trim(),
        newPassword,
      })
      if (res.code === 200) {
        setSuccess('密码重置成功，请使用新密码登录')
        setTimeout(() => switchTab('login'), 1500)
      } else {
        setError(res.message || '重置失败')
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || '重置失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (tab === 'login') handleLogin()
    else if (tab === 'register') handleRegister()
    else handleResetPassword()
  }

  /** 验证码发送按钮 */
  const codeButton = (
    <button
      type="button"
      onClick={handleSendCode}
      disabled={codeSending || countdown > 0}
      className="shrink-0 px-3 py-2 text-xs theme-button-primary rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
    >
      {codeSending ? (
        <Loader2 className="w-3 h-3 animate-spin" />
      ) : countdown > 0 ? (
        `${countdown}s`
      ) : (
        '发送验证码'
      )}
    </button>
  )

  const inputClass = "w-full px-3 py-2 text-sm theme-input rounded-lg"

  return (
    <div className="min-h-screen flex items-center justify-center theme-bg-gradient">
      {/* 右上角主题切换 */}
      <div className="fixed top-4 right-4 z-50">
        <ThemeToggle />
      </div>

      <div className="w-full max-w-sm p-8 theme-panel rounded-2xl">

        {/* 标题 */}
        <h1 className="text-2xl font-bold text-center theme-text mb-6">程序智能在线评测系统</h1>

        {/* Tab 切换 */}
        <div className="flex mb-6 rounded-xl p-1" style={{ background: 'var(--hover-bg)' }}>
          <button
            onClick={() => switchTab('login')}
            className={`flex-1 py-2 text-sm font-medium rounded-lg transition ${
              tab === 'login' ? 'theme-button-primary' : 'theme-button-ghost'
            }`}
          >
            登录
          </button>
          <button
            onClick={() => switchTab('register')}
            className={`flex-1 py-2 text-sm font-medium rounded-lg transition ${
              tab === 'register' ? 'theme-button-primary' : 'theme-button-ghost'
            }`}
          >
            注册
          </button>
        </div>

        {/* 表单 */}
        <form onSubmit={handleSubmit} className="space-y-4">

          {/* ==================== 登录 ==================== */}
          {tab === 'login' && (
            <>
              <div>
                <label className="block text-xs theme-faint mb-1">用户名</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="请输入用户名"
                  className={inputClass}
                />
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">密码</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="请输入密码"
                  className={inputClass}
                />
              </div>
              <div className="flex justify-end">
                <button
                  type="button"
                  onClick={() => switchTab('forgot')}
                  className="text-xs theme-accent-text hover:brightness-110 transition"
                >
                  忘记密码?
                </button>
              </div>
            </>
          )}

          {/* ==================== 注册 ==================== */}
          {tab === 'register' && (
            <>
              <div>
                <label className="block text-xs theme-faint mb-1">用户名</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="请输入用户名（3-20 个字符）"
                  className={inputClass}
                />
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">邮箱</label>
                <div className="flex gap-2">
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="请输入邮箱"
                    className={`${inputClass} flex-1`}
                  />
                  {codeButton}
                </div>
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">验证码</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-hint" />
                  <input
                    type="text"
                    value={verifyCode}
                    onChange={(e) => setVerifyCode(e.target.value)}
                    placeholder="请输入 6 位验证码"
                    maxLength={6}
                    className={`${inputClass} pl-10`}
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">密码</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="请输入密码（至少 6 位）"
                  className={inputClass}
                />
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">确认密码</label>
                <input
                  type="password"
                  value={confirmPwd}
                  onChange={(e) => setConfirmPwd(e.target.value)}
                  placeholder="请再次输入密码"
                  className={inputClass}
                />
              </div>
            </>
          )}

          {/* ==================== 忘记密码 ==================== */}
          {tab === 'forgot' && (
            <>
              <div>
                <label className="block text-xs theme-faint mb-1">注册邮箱</label>
                <div className="flex gap-2">
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="请输入注册时使用的邮箱"
                    className={`${inputClass} flex-1`}
                  />
                  {codeButton}
                </div>
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">验证码</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-hint" />
                  <input
                    type="text"
                    value={verifyCode}
                    onChange={(e) => setVerifyCode(e.target.value)}
                    placeholder="请输入 6 位验证码"
                    maxLength={6}
                    className={`${inputClass} pl-10`}
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">新密码</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="请输入新密码（至少 6 位）"
                  className={inputClass}
                />
              </div>
              <div>
                <label className="block text-xs theme-faint mb-1">确认新密码</label>
                <input
                  type="password"
                  value={confirmNewPwd}
                  onChange={(e) => setConfirmNewPwd(e.target.value)}
                  placeholder="请再次输入新密码"
                  className={inputClass}
                />
              </div>
              <div>
                <button
                  type="button"
                  onClick={() => switchTab('login')}
                  className="text-xs theme-accent-text hover:brightness-110 transition"
                >
                  返回登录
                </button>
              </div>
            </>
          )}

          {error && <p className="text-sm" style={{ color: 'var(--danger)' }}>{error}</p>}
          {success && <p className="text-sm" style={{ color: 'var(--success)' }}>{success}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 theme-button-primary text-sm font-medium rounded-xl disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center justify-center gap-2"
          >
            {loading && <Loader2 className="w-4 h-4 animate-spin" />}
            {tab === 'login' ? '登录' : tab === 'register' ? '注册' : '重置密码'}
          </button>
        </form>
      </div>
    </div>
  )
}
