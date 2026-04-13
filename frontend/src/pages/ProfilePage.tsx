import { useEffect, useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft, Save, Loader2, Eye, EyeOff, Send, LogOut, Mail, Lock, Settings,
  BarChart3, TrendingUp, Target, RefreshCw, CheckCircle2, XCircle,
} from 'lucide-react'
import { useUserStore } from '@/store/userStore'
import { updateProfile, changePassword, getOjConfigs, saveOjConfig } from '@/api/user'
import { getUserStats } from '@/api/submission'
import { getUserProfile, updateUserProfile, analyzeUserProfile } from '@/api/userProfile'
import { sendCode } from '@/api/email'
import ThemeToggle from '@/components/ThemeToggle'
import type { UserOjConfig, UserStats, UserProfile } from '@/types'

type OjTab = 'leetcode' | 'luogu'
type PageTab = 'overview' | 'settings'

const skillLabels: Record<string, string> = {
  beginner: '入门', intermediate: '进阶', advanced: '高级', expert: '专家',
}

export default function ProfilePage() {
  const navigate = useNavigate()
  const { user, logout, restoreSession } = useUserStore()

  const [pageTab, setPageTab] = useState<PageTab>('overview')

  // 做题统计
  const [stats, setStats] = useState<UserStats | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)

  // 能力画像
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [profileAnalyzing, setProfileAnalyzing] = useState(false)
  const [skillLevel, setSkillLevel] = useState('')
  const [targetLevel, setTargetLevel] = useState('')
  const [profileSaving, setProfileSaving] = useState(false)
  const [profileMsg2, setProfileMsg2] = useState('')

  // 个人信息
  const [email, setEmail] = useState('')
  const [username, setUsername] = useState('')
  const [originalEmail, setOriginalEmail] = useState('')
  const [emailCode, setEmailCode] = useState('')
  const [codeCooldown, setCodeCooldown] = useState(0)
  const cooldownRef = useRef<ReturnType<typeof setInterval>>(undefined)
  const [codeSending, setCodeSending] = useState(false)
  const [profileLoading, setProfileLoading] = useState(false)
  const [profileMsg, setProfileMsg] = useState('')

  // 修改密码
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPwd, setConfirmNewPwd] = useState('')
  const [pwdLoading, setPwdLoading] = useState(false)
  const [pwdMsg, setPwdMsg] = useState('')
  const [showOldPwd, setShowOldPwd] = useState(false)

  // OJ 配置
  const [ojTab, setOjTab] = useState<OjTab>('leetcode')
  const [ojConfigs, setOjConfigs] = useState<UserOjConfig[]>([])
  const [lcCookie, setLcCookie] = useState('')
  const [lcCsrf, setLcCsrf] = useState('')
  const [lgUid, setLgUid] = useState('')
  const [lgClientId, setLgClientId] = useState('')
  const [ojLoading, setOjLoading] = useState(false)
  const [ojMsg, setOjMsg] = useState('')

  // 初始化
  useEffect(() => {
    if (user) {
      setUsername(user.username)
      setEmail(user.email)
      setOriginalEmail(user.email)
    }
    loadOjConfigs()
    loadStats()
    loadProfile()
  }, [user])

  useEffect(() => {
    return () => { if (cooldownRef.current) clearInterval(cooldownRef.current) }
  }, [])

  const loadStats = async () => {
    setStatsLoading(true)
    try {
      const res = await getUserStats()
      if (res.code === 200) setStats(res.data)
    } catch { /* 静默 */ }
    finally { setStatsLoading(false) }
  }

  const loadProfile = async () => {
    try {
      const res = await getUserProfile()
      if (res.code === 200 && res.data) {
        setProfile(res.data)
        setSkillLevel(res.data.skillLevel || 'beginner')
        setTargetLevel(res.data.targetLevel || '')
      }
    } catch { /* 静默 */ }
  }

  const handleAnalyzeProfile = async () => {
    setProfileAnalyzing(true)
    try {
      const res = await analyzeUserProfile()
      if (res.code === 200 && res.data) {
        setProfile(res.data)
        setProfileMsg2('画像分析完成')
      }
    } catch { setProfileMsg2('分析失败') }
    finally { setProfileAnalyzing(false) }
  }

  const handleSaveProfile2 = async () => {
    setProfileSaving(true)
    setProfileMsg2('')
    try {
      const res = await updateUserProfile({ skillLevel, targetLevel: targetLevel || undefined })
      if (res.code === 200) {
        setProfile(res.data)
        setProfileMsg2('保存成功')
      } else { setProfileMsg2(res.message || '保存失败') }
    } catch { setProfileMsg2('保存失败') }
    finally { setProfileSaving(false) }
  }

  const loadOjConfigs = async () => {
    try {
      const res = await getOjConfigs()
      if (res.code === 200 && res.data) {
        setOjConfigs(res.data)
        for (const c of res.data) {
          if (c.ojPlatform === 'leetcode') {
            setLcCookie(c.cookieValue || '')
            setLcCsrf(c.csrfToken || '')
          } else if (c.ojPlatform === 'luogu') {
            const cookie = c.cookieValue || ''
            const uidMatch = cookie.match(/_uid=([^;]+)/)
            const cidMatch = cookie.match(/__client_id=([^;]+)/)
            setLgUid(uidMatch ? uidMatch[1] : '')
            setLgClientId(cidMatch ? cidMatch[1] : '')
          }
        }
      }
    } catch { /* 静默 */ }
  }

  const emailChanged = email !== originalEmail

  const handleSendCode = async () => {
    if (!email || email === originalEmail) return
    setCodeSending(true)
    try {
      const res = await sendCode(email)
      if (res.code === 200) {
        setCodeCooldown(60)
        cooldownRef.current = setInterval(() => {
          setCodeCooldown((c) => {
            if (c <= 1) { clearInterval(cooldownRef.current!); return 0 }
            return c - 1
          })
        }, 1000)
      } else { setProfileMsg(res.message || '发送失败') }
    } catch (err: any) { setProfileMsg(err?.message || '发送失败') }
    finally { setCodeSending(false) }
  }

  const handleSaveProfile = async () => {
    if (emailChanged && !emailCode) { setProfileMsg('修改邮箱需要输入验证码'); return }
    setProfileLoading(true); setProfileMsg('')
    try {
      const res = await updateProfile({ email, code: emailChanged ? emailCode : undefined })
      if (res.code === 200) {
        setProfileMsg('保存成功'); setEmailCode(''); setOriginalEmail(email)
        await restoreSession()
      } else { setProfileMsg(res.message || '保存失败') }
    } catch (err: any) { setProfileMsg(err?.message || '保存失败') }
    finally { setProfileLoading(false) }
  }

  const handleChangePassword = async () => {
    if (!oldPassword || !newPassword) { setPwdMsg('请填写旧密码和新密码'); return }
    if (newPassword.length < 6) { setPwdMsg('新密码长度至少 6 位'); return }
    if (newPassword !== confirmNewPwd) { setPwdMsg('两次输入的密码不一致'); return }
    setPwdLoading(true); setPwdMsg('')
    try {
      const res = await changePassword({ oldPassword, newPassword })
      if (res.code === 200) {
        setPwdMsg('密码修改成功'); setOldPassword(''); setNewPassword(''); setConfirmNewPwd('')
      } else { setPwdMsg(res.message || '修改失败') }
    } catch (err: any) { setPwdMsg(err?.message || '修改失败') }
    finally { setPwdLoading(false) }
  }

  const handleSaveOjConfig = async () => {
    setOjLoading(true); setOjMsg('')
    try {
      if (ojTab === 'leetcode') {
        const res = await saveOjConfig({ ojPlatform: 'leetcode', cookieValue: lcCookie, csrfToken: lcCsrf })
        setOjMsg(res.code === 200 ? '配置保存成功' : (res.message || '保存失败'))
      } else {
        const cookieStr = `_uid=${lgUid}; __client_id=${lgClientId}`
        const res = await saveOjConfig({ ojPlatform: 'luogu', cookieValue: cookieStr, csrfToken: '' })
        setOjMsg(res.code === 200 ? '配置保存成功' : (res.message || '保存失败'))
      }
    } catch (err: any) { setOjMsg(err?.message || '保存失败') }
    finally { setOjLoading(false) }
  }

  const initials = user?.username?.slice(0, 1).toUpperCase() || '?'
  const inputClass = "w-full px-3 py-2.5 text-sm theme-input rounded-lg transition"
  const labelClass = "block text-xs font-medium theme-faint mb-1.5"

  // 计算近 30 天趋势图最大值
  const maxDaily = stats?.recentDaily?.reduce((m, d) => Math.max(m, d.count), 0) || 1

  return (
    <div className="min-h-screen theme-bg-gradient">
      <div className="max-w-3xl mx-auto px-6 py-8">

        {/* 顶部导航 */}
        <div className="flex items-center justify-between mb-6">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-2 text-sm theme-button-ghost transition"
          >
            <ArrowLeft className="w-4 h-4" />
            返回题库
          </button>
          <div className="flex items-center gap-3">
            <ThemeToggle />
            <div className="flex theme-card-solid rounded-xl p-0.5">
              <button
                onClick={() => setPageTab('overview')}
                className={`px-4 py-1.5 text-xs font-medium rounded-lg transition ${
                  pageTab === 'overview' ? 'theme-button-primary' : 'theme-button-ghost'
                }`}
              >
                数据概览
              </button>
              <button
                onClick={() => setPageTab('settings')}
                className={`px-4 py-1.5 text-xs font-medium rounded-lg transition ${
                  pageTab === 'settings' ? 'theme-button-primary' : 'theme-button-ghost'
                }`}
              >
                账号设置
              </button>
            </div>
          </div>
        </div>

        {/* 用户卡片（始终显示） */}
        <div className="theme-card rounded-2xl p-6 mb-6">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-full bg-gradient-to-br from-[var(--accent)] to-[var(--accent-strong)] flex items-center justify-center text-xl font-bold text-white shrink-0">
              {initials}
            </div>
            <div className="flex-1 min-w-0">
              <h1 className="text-lg font-semibold theme-text truncate">{user?.username}</h1>
              <p className="text-sm theme-faint truncate">{user?.email}</p>
              <div className="flex items-center gap-2 mt-1.5">
                <span className={`px-2 py-0.5 text-[11px] font-medium rounded-full ${
                  user?.role === 'admin' ? 'bg-purple-500/15 text-purple-400' : 'bg-[var(--accent-soft)] theme-accent-text'
                }`}>
                  {user?.role === 'admin' ? '管理员' : '普通用户'}
                </span>
                {profile && (
                  <span className="px-2 py-0.5 text-[11px] font-medium rounded-full theme-tag">
                    {skillLabels[profile.skillLevel] || profile.skillLevel}
                  </span>
                )}
              </div>
            </div>
            <button
              onClick={() => { logout(); navigate('/login') }}
              className="flex items-center gap-2 px-4 py-2 text-sm theme-button-secondary rounded-xl hover:text-[var(--danger)] hover:border-[var(--danger)] transition shrink-0"
            >
              <LogOut className="w-4 h-4" />
              退出
            </button>
          </div>
        </div>

        {pageTab === 'overview' ? (
          <>
            {/* ===== 做题统计 ===== */}
            {statsLoading ? (
              <div className="flex items-center justify-center py-16 theme-faint">
                <Loader2 className="w-5 h-5 animate-spin mr-2" /> 加载统计数据...
              </div>
            ) : stats ? (
              <>
                {/* 总计三指标 */}
                <div className="grid grid-cols-3 gap-4 mb-6">
                  {[
                    { label: '解题总数', value: stats.total.solved, color: 'var(--success)' },
                    { label: '提交总数', value: stats.total.submitted, color: 'var(--info)' },
                    { label: '通过率', value: `${stats.total.acceptanceRate}%`, color: 'var(--warning)' },
                  ].map((item) => (
                    <div key={item.label} className="theme-card rounded-2xl p-5 text-center">
                      <div className="text-2xl font-bold" style={{ color: item.color }}>{item.value}</div>
                      <div className="text-xs theme-faint mt-1">{item.label}</div>
                    </div>
                  ))}
                </div>

                {/* 按平台统计 */}
                <section className="theme-card rounded-2xl p-6 mb-6">
                  <div className="flex items-center gap-2 mb-4">
                    <BarChart3 className="w-4 h-4 theme-accent-text" />
                    <h2 className="text-sm font-semibold theme-text">平台做题统计</h2>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    {Object.entries(stats.platforms).map(([platform, s]) => (
                      <div key={platform} className="theme-surface rounded-xl p-4">
                        <div className="flex items-center justify-between mb-3">
                          <span className="text-sm font-medium theme-text">
                            {platform === 'leetcode' ? 'LeetCode' : '洛谷'}
                          </span>
                          <span className="text-xs theme-faint">通过率 {s.acceptanceRate}%</span>
                        </div>
                        <div className="flex items-baseline gap-3">
                          <div>
                            <span className="text-xl font-bold" style={{ color: 'var(--success)' }}>{s.solved}</span>
                            <span className="text-xs theme-faint ml-1">题通过</span>
                          </div>
                          <div>
                            <span className="text-lg font-semibold theme-muted">{s.submitted}</span>
                            <span className="text-xs theme-faint ml-1">次提交</span>
                          </div>
                        </div>

                        {/* 难度分布 */}
                        {stats.difficulties[platform] && (
                          <div className="mt-3 space-y-1.5">
                            {Object.entries(stats.difficulties[platform]).map(([diff, count]) => {
                              const total = Object.values(stats.difficulties[platform]).reduce((a, b) => a + b, 0)
                              const pct = total > 0 ? (count / total * 100) : 0
                              const barColor = diff === 'Easy' || diff === '入门' || diff === '普及-' ? 'var(--success)'
                                : diff === 'Hard' || diff === '省选/NOI-' || diff === 'NOI/NOI+/CTSC' ? 'var(--danger)'
                                : 'var(--warning)'
                              return (
                                <div key={diff} className="flex items-center gap-2 text-xs">
                                  <span className="w-20 theme-faint truncate">{diff}</span>
                                  <div className="flex-1 h-2 rounded-full overflow-hidden" style={{ background: 'var(--hover-bg)' }}>
                                    <div
                                      className="h-full rounded-full transition-all"
                                      style={{ width: `${Math.max(pct, 2)}%`, background: barColor }}
                                    />
                                  </div>
                                  <span className="w-6 text-right theme-faint">{count}</span>
                                </div>
                              )
                            })}
                          </div>
                        )}
                      </div>
                    ))}
                    {Object.keys(stats.platforms).length === 0 && (
                      <div className="col-span-2 text-center py-8 theme-faint text-sm">
                        暂无提交记录，去题库开始做题吧
                      </div>
                    )}
                  </div>
                </section>

                {/* 近 30 天提交趋势 */}
                {stats.recentDaily.length > 0 && (
                  <section className="theme-card rounded-2xl p-6 mb-6">
                    <div className="flex items-center gap-2 mb-4">
                      <TrendingUp className="w-4 h-4 theme-accent-text" />
                      <h2 className="text-sm font-semibold theme-text">近 30 天提交趋势</h2>
                    </div>
                    <div className="flex items-end gap-[3px] h-24">
                      {stats.recentDaily.map((d) => (
                        <div
                          key={d.date}
                          className="flex-1 rounded-t cursor-default group relative transition-colors"
                          style={{
                            height: `${Math.max((d.count / maxDaily) * 100, 4)}%`,
                            background: `color-mix(in srgb, var(--accent) 65%, transparent)`,
                          }}
                          title={`${d.date}：${d.count} 次提交`}
                        >
                          <div className="absolute -top-6 left-1/2 -translate-x-1/2 hidden group-hover:block text-[10px] theme-text rounded px-1.5 py-0.5 whitespace-nowrap" style={{ background: 'var(--card-bg-solid)' }}>
                            {d.date.slice(5)} · {d.count}次
                          </div>
                        </div>
                      ))}
                    </div>
                    <div className="flex justify-between mt-1.5 text-[10px] theme-hint">
                      <span>{stats.recentDaily[0]?.date.slice(5)}</span>
                      <span>{stats.recentDaily[stats.recentDaily.length - 1]?.date.slice(5)}</span>
                    </div>
                  </section>
                )}
              </>
            ) : (
              <div className="text-center py-16 theme-faint text-sm">无法加载统计数据</div>
            )}

            {/* ===== 能力画像 ===== */}
            <section className="theme-card rounded-2xl p-6 mb-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <Target className="w-4 h-4 theme-accent-text" />
                  <h2 className="text-sm font-semibold theme-text">能力画像</h2>
                </div>
                <button
                  onClick={handleAnalyzeProfile}
                  disabled={profileAnalyzing}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-xs theme-button-secondary rounded-xl disabled:opacity-50 transition"
                >
                  {profileAnalyzing ? <Loader2 className="w-3 h-3 animate-spin" /> : <RefreshCw className="w-3 h-3" />}
                  刷新分析
                </button>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label className={labelClass}>自评水平</label>
                  <select value={skillLevel} onChange={(e) => setSkillLevel(e.target.value)} className={inputClass}>
                    <option value="beginner">入门</option>
                    <option value="intermediate">进阶</option>
                    <option value="advanced">高级</option>
                    <option value="expert">专家</option>
                  </select>
                </div>
                <div>
                  <label className={labelClass}>目标水平</label>
                  <select value={targetLevel} onChange={(e) => setTargetLevel(e.target.value)} className={inputClass}>
                    <option value="">未设置</option>
                    <option value="beginner">入门</option>
                    <option value="intermediate">进阶</option>
                    <option value="advanced">高级</option>
                    <option value="expert">专家</option>
                  </select>
                </div>
              </div>

              {profile && (
                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div>
                    <label className={labelClass}>擅长领域</label>
                    <div className="flex flex-wrap gap-1.5">
                      {profile.strongTags && profile.strongTags.length > 0 ? (
                        profile.strongTags.map((t) => (
                          <span key={t} className="px-2 py-0.5 text-[11px] theme-status-accepted rounded-full">{t}</span>
                        ))
                      ) : (
                        <span className="text-xs theme-hint">暂未分析</span>
                      )}
                    </div>
                  </div>
                  <div>
                    <label className={labelClass}>薄弱领域</label>
                    <div className="flex flex-wrap gap-1.5">
                      {profile.weakTags && profile.weakTags.length > 0 ? (
                        profile.weakTags.map((t) => (
                          <span key={t} className="px-2 py-0.5 text-[11px] theme-status-error rounded-full">{t}</span>
                        ))
                      ) : (
                        <span className="text-xs theme-hint">暂未分析</span>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {profileMsg2 && (
                <p className={`text-xs mb-3 ${profileMsg2.includes('成功') || profileMsg2.includes('完成') ? 'text-[var(--success)]' : 'text-[var(--danger)]'}`}>
                  {profileMsg2}
                </p>
              )}
              <div className="flex justify-end">
                <button
                  onClick={handleSaveProfile2}
                  disabled={profileSaving}
                  className="flex items-center gap-2 px-4 py-2 theme-button-primary text-sm rounded-xl disabled:opacity-50 transition"
                >
                  {profileSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                  保存画像
                </button>
              </div>
            </section>
          </>
        ) : (
          <>
            {/* ===== 账号设置 Tab ===== */}

            {/* 个人信息 */}
            <section className="theme-card rounded-2xl p-6 mb-6">
              <div className="flex items-center gap-2 mb-5">
                <Mail className="w-4 h-4 theme-accent-text" />
                <h2 className="text-sm font-semibold theme-text">个人信息</h2>
              </div>
              <div className="space-y-4">
                <div>
                  <label className={labelClass}>用户名</label>
                  <input type="text" value={username} disabled className={`${inputClass} opacity-60 cursor-not-allowed`} />
                  <p className="text-[11px] theme-hint mt-1">用户名注册后不可修改</p>
                </div>
                <div>
                  <label className={labelClass}>邮箱</label>
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className={inputClass} />
                </div>
                {emailChanged && (
                  <div className="theme-surface rounded-xl p-3">
                    <label className={labelClass}>验证码（发送到新邮箱）</label>
                    <div className="flex gap-2">
                      <input type="text" value={emailCode} onChange={(e) => setEmailCode(e.target.value)} placeholder="请输入 6 位验证码" maxLength={6} className={`${inputClass} flex-1`} />
                      <button onClick={handleSendCode} disabled={codeCooldown > 0 || codeSending}
                        className="px-4 py-2 text-sm theme-button-primary rounded-xl disabled:opacity-50 transition whitespace-nowrap flex items-center gap-1.5">
                        {codeSending ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Send className="w-3.5 h-3.5" />}
                        {codeCooldown > 0 ? `${codeCooldown}s` : '发送验证码'}
                      </button>
                    </div>
                  </div>
                )}
                {profileMsg && <p className={`text-xs ${profileMsg.includes('成功') ? 'text-[var(--success)]' : 'text-[var(--danger)]'}`}>{profileMsg}</p>}
                <div className="flex justify-end">
                  <button onClick={handleSaveProfile} disabled={profileLoading}
                    className="flex items-center gap-2 px-5 py-2 theme-button-primary text-sm rounded-xl disabled:opacity-50 transition">
                    {profileLoading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                    保存信息
                  </button>
                </div>
              </div>
            </section>

            {/* 修改密码 */}
            <section className="theme-card rounded-2xl p-6 mb-6">
              <div className="flex items-center gap-2 mb-5">
                <Lock className="w-4 h-4 theme-accent-text" />
                <h2 className="text-sm font-semibold theme-text">修改密码</h2>
              </div>
              <div className="space-y-4">
                <div>
                  <label className={labelClass}>当前密码</label>
                  <div className="relative">
                    <input type={showOldPwd ? 'text' : 'password'} value={oldPassword} onChange={(e) => setOldPassword(e.target.value)} placeholder="请输入当前密码" className={inputClass} />
                    <button type="button" onClick={() => setShowOldPwd(!showOldPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 theme-hint hover:text-[var(--text-primary)] transition">
                      {showOldPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className={labelClass}>新密码</label>
                    <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="至少 6 位" className={inputClass} />
                  </div>
                  <div>
                    <label className={labelClass}>确认新密码</label>
                    <input type="password" value={confirmNewPwd} onChange={(e) => setConfirmNewPwd(e.target.value)} placeholder="再次输入新密码" className={inputClass} />
                  </div>
                </div>
                {pwdMsg && <p className={`text-xs ${pwdMsg.includes('成功') ? 'text-[var(--success)]' : 'text-[var(--danger)]'}`}>{pwdMsg}</p>}
                <div className="flex justify-end">
                  <button onClick={handleChangePassword} disabled={pwdLoading}
                    className="flex items-center gap-2 px-5 py-2 theme-button-primary text-sm rounded-xl disabled:opacity-50 transition">
                    {pwdLoading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Lock className="w-3.5 h-3.5" />}
                    修改密码
                  </button>
                </div>
              </div>
            </section>

            {/* OJ 平台配置 */}
            <section className="theme-card rounded-2xl p-6">
              <div className="flex items-center gap-2 mb-1">
                <Settings className="w-4 h-4 theme-accent-text" />
                <h2 className="text-sm font-semibold theme-text">OJ 平台配置</h2>
              </div>
              <p className="text-xs theme-faint mb-5 ml-6">配置个人 OJ 凭证后，提交代码将使用你的账号。未配置时使用平台公共账号。</p>

              <div className="flex mb-5 rounded-xl p-1" style={{ background: 'var(--hover-bg)' }}>
                <button onClick={() => { setOjTab('leetcode'); setOjMsg('') }}
                  className={`flex-1 py-2 text-xs font-medium rounded-lg transition ${ojTab === 'leetcode' ? 'theme-button-primary' : 'theme-button-ghost'}`}>
                  LeetCode
                </button>
                <button onClick={() => { setOjTab('luogu'); setOjMsg('') }}
                  className={`flex-1 py-2 text-xs font-medium rounded-lg transition ${ojTab === 'luogu' ? 'theme-button-primary' : 'theme-button-ghost'}`}>
                  洛谷
                </button>
              </div>

              <div className="space-y-4">
                {ojTab === 'leetcode' ? (
                  <>
                    <div>
                      <label className={labelClass}>LeetCode Cookie</label>
                      <textarea value={lcCookie} onChange={(e) => setLcCookie(e.target.value)}
                        placeholder="浏览器 F12 → Network → Headers → Cookie，粘贴完整字符串"
                        rows={4} className={`${inputClass} resize-none`} />
                    </div>
                    <div>
                      <label className={labelClass}>CSRF Token</label>
                      <input type="text" value={lcCsrf} onChange={(e) => setLcCsrf(e.target.value)} placeholder="csrftoken 的值" className={inputClass} />
                    </div>
                  </>
                ) : (
                  <>
                    <div>
                      <label className={labelClass}>洛谷 _uid</label>
                      <input type="text" value={lgUid} onChange={(e) => setLgUid(e.target.value)}
                        placeholder="浏览器 F12 → Application → Cookies → _uid 的值"
                        className={inputClass} />
                    </div>
                    <div>
                      <label className={labelClass}>洛谷 __client_id</label>
                      <input type="text" value={lgClientId} onChange={(e) => setLgClientId(e.target.value)}
                        placeholder="浏览器 F12 → Application → Cookies → __client_id 的值"
                        className={inputClass} />
                    </div>
                  </>
                )}

                {ojMsg && <p className={`text-xs ${ojMsg.includes('成功') ? 'text-[var(--success)]' : 'text-[var(--danger)]'}`}>{ojMsg}</p>}
                <div className="flex justify-end">
                  <button onClick={handleSaveOjConfig} disabled={ojLoading}
                    className="flex items-center gap-2 px-5 py-2 theme-button-primary text-sm rounded-xl disabled:opacity-50 transition">
                    {ojLoading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                    保存配置
                  </button>
                </div>
              </div>
            </section>
          </>
        )}
      </div>
    </div>
  )
}
