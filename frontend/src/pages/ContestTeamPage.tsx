import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, DoorOpen, Search, Shield, Users } from 'lucide-react'
import {
  cancelRegistration,
  createTeam,
  dissolveTeam,
  getContestDetail,
  getMyTeam,
  getTeams,
  leaveTeam,
  registerContest,
  removeTeamMember,
  transferCaptain,
  updateTeam,
} from '@/api/contest'
import ThemeToggle from '@/components/ThemeToggle'
import UserMenu from '@/components/UserMenu'
import { confirm, toast } from '@/store/uiStore'
import type { ContestDetail, ContestTeamDetail, ContestTeamLobby } from '@/types'

type TeamTab = 'mine' | 'lobby'
type LobbyFilter = 'all' | 'joinable' | 'registered' | 'full'

function TeamBadge({
  label,
  tone = 'default',
}: {
  label: string
  tone?: 'default' | 'accent' | 'success' | 'warning' | 'danger'
}) {
  const cls =
    tone === 'accent'
      ? 'theme-chip'
      : tone === 'success'
        ? 'bg-emerald-500/12 text-emerald-300'
        : tone === 'warning'
          ? 'bg-amber-500/12 text-amber-300'
          : tone === 'danger'
            ? 'bg-rose-500/12 text-rose-300'
            : 'theme-tag'

  return <span className={`rounded-full px-2.5 py-1 text-[11px] ${cls}`}>{label}</span>
}

function FlowStep({
  title,
  active,
  done,
}: {
  title: string
  active: boolean
  done: boolean
}) {
  return (
    <div className={`rounded-2xl border px-4 py-3 transition ${active || done ? 'border-[var(--accent)] bg-[var(--accent-soft)]' : 'theme-border theme-surface'}`}>
      <div className="flex items-center gap-2">
        <span className={`flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold ${done ? 'bg-emerald-500/20 text-emerald-300' : active ? 'bg-[var(--accent)] text-white' : 'theme-tag'}`}>
          {done ? '✓' : ''}
        </span>
        <span className={`text-sm font-medium ${active || done ? 'theme-text' : 'theme-faint'}`}>{title}</span>
      </div>
    </div>
  )
}

const contestStatusLabel: Record<string, string> = {
  draft: '草稿',
  registering: '报名中',
  running: '进行中',
  frozen: '封榜中',
  ended: '已结束',
  archived: '已归档',
}

export default function ContestTeamPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const contestId = Number(id)

  const [contest, setContest] = useState<ContestDetail | null>(null)
  const [teams, setTeams] = useState<ContestTeamLobby[]>([])
  const [myTeam, setMyTeam] = useState<ContestTeamDetail | null>(null)
  const [teamName, setTeamName] = useState('')
  const [teamDesc, setTeamDesc] = useState('')
  const [renameValue, setRenameValue] = useState('')
  const [descValue, setDescValue] = useState('')
  const [tab, setTab] = useState<TeamTab>('mine')
  const [lobbyFilter, setLobbyFilter] = useState<LobbyFilter>('all')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    void loadPage()
  }, [contestId])

  const loadPage = async () => {
    setLoading(true)
    try {
      const [contestRes, teamsRes, myTeamRes] = await Promise.all([
        getContestDetail(contestId),
        getTeams(contestId),
        getMyTeam(contestId),
      ])

      if (contestRes.code === 200) {
        if (contestRes.data.contestType !== 'team') {
          navigate(`/contests/${contestId}`, { replace: true })
          return
        }
        setContest(contestRes.data)
      }

      if (teamsRes.code === 200) {
        setTeams(teamsRes.data || [])
      }

      if (myTeamRes.code === 200) {
        setMyTeam(myTeamRes.data)
        setRenameValue(myTeamRes.data?.teamName || '')
        setDescValue(myTeamRes.data?.description || '')
        if (myTeamRes.data) {
          setTab('mine')
        }
      }
    } catch {}
    setLoading(false)
  }

  const maxTeamSize = contest?.maxTeamSize || 1
  const minTeamSize = contest?.minTeamSize || 1
  const canEditTeam = contest?.status === 'registering'
  const isLocked = !!contest && contest.status !== 'registering'
  const myTeamRegistered = !!contest?.registered
  const stageIndex = !myTeam ? 0 : !myTeamRegistered ? 1 : isLocked ? 3 : 2

  const availableTeams = useMemo(
    () => teams.filter((team) => team.memberCount < maxTeamSize),
    [teams, maxTeamSize],
  )
  const fullTeams = useMemo(
    () => teams.filter((team) => team.memberCount >= maxTeamSize),
    [teams, maxTeamSize],
  )
  const registeredTeams = useMemo(
    () => teams.filter((team) => team.registered),
    [teams],
  )

  const filteredTeams = useMemo(() => {
    const keyword = search.trim().toLowerCase()

    return teams
      .filter((team) => {
        if (lobbyFilter === 'joinable') return team.memberCount < maxTeamSize
        if (lobbyFilter === 'registered') return team.registered
        if (lobbyFilter === 'full') return team.memberCount >= maxTeamSize
        return true
      })
      .filter((team) => {
        if (!keyword) return true
        return team.teamName.toLowerCase().includes(keyword) || team.captainName.toLowerCase().includes(keyword)
      })
      .sort((a, b) => {
        const aMine = myTeam?.id === a.id ? 1 : 0
        const bMine = myTeam?.id === b.id ? 1 : 0
        if (aMine !== bMine) return bMine - aMine
        if (a.registered !== b.registered) return Number(b.registered) - Number(a.registered)
        if (a.memberCount !== b.memberCount) return b.memberCount - a.memberCount
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })
  }, [lobbyFilter, maxTeamSize, myTeam, search, teams])

  const statusSummary = useMemo(() => {
    if (!myTeam) {
      return {
        title: '未组队',
        tone: 'warning' as const,
        message: '先创建队伍或加入队伍，再由队长完成报名。',
      }
    }

    if (!myTeamRegistered) {
      return {
        title: '已组队待报名',
        tone: 'warning' as const,
        message: myTeam.captain ? '你现在可以代表队伍完成比赛报名。' : '等待队长完成比赛报名。',
      }
    }

    if (!isLocked) {
      return {
        title: '已报名',
        tone: 'success' as const,
        message: '队伍已完成报名，当前仍处于可调整阶段。',
      }
    }

    return {
      title: contest?.status === 'ended' ? '比赛结束' : '已锁队',
      tone: contest?.status === 'ended' ? 'default' as const : 'accent' as const,
      message: contest?.status === 'ended' ? '比赛已结束，当前仅保留队伍信息。' : '比赛开始后不再允许调整队伍成员和报名状态。',
    }
  }, [contest?.status, isLocked, myTeam, myTeamRegistered])

  const handleCreateTeam = async () => {
    if (!teamName.trim()) {
      toast.warning('请输入队伍名称')
      return
    }

    try {
      const res = await createTeam(contestId, teamName.trim(), teamDesc.trim() || undefined)
      if (res.code === 200) {
        toast.success('队伍创建成功')
        setTeamName('')
        setTeamDesc('')
        await loadPage()
      }
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleLeaveTeam = async () => {
    if (!myTeam) return
    if (!await confirm('确定退出当前队伍？', { type: 'warning', confirmText: '退出队伍' })) return

    try {
      await leaveTeam(contestId, myTeam.id)
      toast.success('已退出队伍')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleRenameTeam = async () => {
    if (!myTeam || !renameValue.trim()) {
      toast.warning('请输入新的队伍名称')
      return
    }

    try {
      await updateTeam(contestId, myTeam.id, renameValue.trim(), descValue.trim() || undefined)
      toast.success('队伍信息已更新')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleTransferCaptain = async (targetUserId: number) => {
    if (!myTeam) return
    if (!await confirm('确定转让队长？报名权限也会一并转移。', { type: 'warning', confirmText: '转让队长' })) return

    try {
      await transferCaptain(contestId, myTeam.id, targetUserId)
      toast.success('队长已转让')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleRemoveMember = async (targetUserId: number) => {
    if (!myTeam) return
    if (!await confirm('确定移除该成员？', { type: 'warning', confirmText: '移除成员' })) return

    try {
      await removeTeamMember(contestId, myTeam.id, targetUserId)
      toast.success('成员已移除')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleDissolveTeam = async () => {
    if (!myTeam) return
    if (!await confirm('确定解散队伍？如果队伍已报名，将同时取消报名。', { type: 'danger', confirmText: '解散队伍' })) return

    try {
      await dissolveTeam(contestId, myTeam.id)
      toast.success('队伍已解散')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleRegisterTeam = async () => {
    try {
      await registerContest(contestId)
      toast.success('队伍报名成功')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleCancelRegistration = async () => {
    if (!await confirm('确定取消当前队伍的比赛报名？', { type: 'warning', confirmText: '取消报名' })) return

    try {
      await cancelRegistration(contestId)
      toast.success('已取消队伍报名')
      await loadPage()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  if (loading) {
    return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint">加载中...</div>
  }

  if (!contest) {
    return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint">比赛不存在</div>
  }

  return (
    <div className="min-h-screen theme-bg-gradient">
      <header className="theme-header px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/teams')} className="theme-button-ghost text-sm inline-flex items-center gap-1.5">
            <ArrowLeft size={15} />
            返回我的队伍
          </button>
          <div>
            <div className="text-xs tracking-[0.12em] theme-faint">比赛队伍</div>
            <h1 className="text-lg font-semibold theme-text">{contest.title}</h1>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      <div className="max-w-6xl mx-auto px-6 py-6 space-y-5">
        <section className="theme-card rounded-2xl p-5">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="space-y-2">
              <div className="flex flex-wrap items-center gap-2">
                <TeamBadge label="组队赛按队伍报名" tone="accent" />
                <TeamBadge label={`比赛状态：${contestStatusLabel[contest.status] || contest.status}`} tone={contest.status === 'registering' ? 'success' : 'warning'} />
                <TeamBadge label={`人数：${minTeamSize}~${maxTeamSize}`} />
              </div>
              <h2 className="text-base font-semibold theme-text">队伍管理和报名入口已经收敛到同一个空间</h2>
              <p className="text-sm theme-faint">先创建或加入队伍，再由队长完成比赛报名。报名结束后会锁队，比赛开始后不允许继续调整成员。</p>
            </div>
            <div className="grid grid-cols-3 gap-3 min-w-[280px]">
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">队伍总数</div>
                <div className="mt-1 text-xl font-semibold theme-text">{teams.length}</div>
              </div>
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">可加入</div>
                <div className="mt-1 text-xl font-semibold theme-text">{availableTeams.length}</div>
              </div>
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">已报名</div>
                <div className="mt-1 text-xl font-semibold theme-text">{registeredTeams.length}</div>
              </div>
            </div>
          </div>
        </section>

        <section className="theme-card rounded-2xl p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
            <div className="theme-surface rounded-2xl px-4 py-3">
              <div className="font-medium theme-text">只有队长可以报名</div>
              <div className="mt-1 theme-faint">队员可以加入队伍，但报名和取消报名只能由队长操作。</div>
            </div>
            <div className="theme-surface rounded-2xl px-4 py-3">
              <div className="font-medium theme-text">报名结束后锁队</div>
              <div className="mt-1 theme-faint">改名、转让队长和移除成员都只允许在报名阶段内完成。</div>
            </div>
            <div className="theme-surface rounded-2xl px-4 py-3">
              <div className="font-medium theme-text">比赛开始后按队提交</div>
              <div className="mt-1 theme-faint">组队赛报名成功后，队员即可代表已报名队伍参与比赛提交。</div>
            </div>
          </div>
        </section>

        <section className="theme-card rounded-2xl p-2">
          <div className="flex flex-wrap gap-2">
            {[
              { key: 'mine' as const, label: '我的队伍' },
              { key: 'lobby' as const, label: '队伍广场' },
            ].map((item) => (
              <button
                key={item.key}
                onClick={() => setTab(item.key)}
                className={`rounded-xl px-4 py-2 text-sm transition ${tab === item.key ? 'theme-button-primary' : 'theme-button-ghost'}`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </section>

        {tab === 'mine' && (
          <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1.05fr)_minmax(0,0.95fr)] gap-5">
            <section className="space-y-5">
              <div className="theme-card rounded-2xl p-5">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <div className="text-sm font-semibold theme-text">当前状态</div>
                    <div className="mt-1 text-sm theme-faint">{statusSummary.message}</div>
                  </div>
                  <TeamBadge label={statusSummary.title} tone={statusSummary.tone} />
                </div>
                <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3">
                  <FlowStep title="创建或加入队伍" active={stageIndex === 0 || stageIndex > 0} done={stageIndex > 0} />
                  <FlowStep title="队长完成报名" active={stageIndex === 1 || stageIndex === 2} done={stageIndex > 1} />
                  <FlowStep title="锁队并进入比赛" active={stageIndex === 3} done={stageIndex === 3} />
                </div>
              </div>

              {myTeam ? (
                <>
                  <div className="theme-card rounded-2xl p-5">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div className="space-y-2 min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h2 className="text-lg font-semibold theme-text">{myTeam.teamName}</h2>
                          <TeamBadge label="我的队伍" tone="accent" />
                          {myTeam.captain && <TeamBadge label="我是队长" tone="warning" />}
                          <TeamBadge label={myTeamRegistered ? '已报名' : '待报名'} tone={myTeamRegistered ? 'success' : 'warning'} />
                          {isLocked && <TeamBadge label="已锁队" tone="default" />}
                        </div>
                        <div className="flex flex-wrap items-center gap-4 text-sm theme-faint">
                          <span>队长：{myTeam.captainName}</span>
                          <span>成员：{myTeam.memberCount}/{maxTeamSize}</span>
                          <span>创建时间：{new Date(myTeam.createdAt).toLocaleString('zh-CN')}</span>
                        </div>
                        {myTeam.description && (
                          <div className="text-sm theme-faint mt-1">{myTeam.description}</div>
                        )}
                      </div>

                      {myTeam.captain ? (
                        <button onClick={handleDissolveTeam} disabled={!canEditTeam} className="rounded-xl px-4 py-2 text-sm bg-[var(--danger)] text-white disabled:opacity-40 inline-flex items-center gap-1.5">
                          <DoorOpen size={15} />
                          解散队伍
                        </button>
                      ) : (
                        <button onClick={handleLeaveTeam} className="rounded-xl px-4 py-2 text-sm bg-[var(--danger)] text-white inline-flex items-center gap-1.5">
                          <DoorOpen size={15} />
                          退出队伍
                        </button>
                      )}
                    </div>
                  </div>

                  <div className="theme-card rounded-2xl p-5">
                    <div className="flex items-center gap-2 mb-4">
                      <Shield className="w-4 h-4 theme-accent-text" />
                      <div>
                        <div className="text-sm font-semibold theme-text">队长管理面板</div>
                        <div className="text-sm theme-faint">报名、改名、邀请码和成员操作都集中在这里。</div>
                      </div>
                    </div>

                    {myTeam.captain ? (
                      <div className="space-y-4">
                        <div className="flex flex-wrap gap-2">
                          {!myTeamRegistered ? (
                            <button onClick={handleRegisterTeam} disabled={!canEditTeam} className="theme-button-primary rounded-xl px-4 py-2 text-sm disabled:opacity-40">
                              队长报名
                            </button>
                          ) : (
                            <button onClick={handleCancelRegistration} disabled={!canEditTeam} className="theme-button-secondary rounded-xl px-4 py-2 text-sm disabled:opacity-40">
                              取消报名
                            </button>
                          )}
                        </div>

                        {!myTeamRegistered && myTeam.memberCount < minTeamSize && (
                          <div className="rounded-xl bg-amber-500/10 px-4 py-3 text-xs text-amber-300">
                            当前队伍 {myTeam.memberCount} 人，至少需要 {minTeamSize} 人才能报名。
                          </div>
                        )}

                        <div className="space-y-2">
                          <div className="text-xs theme-faint">修改队伍名称和描述</div>
                          <div className="flex flex-col sm:flex-row gap-2">
                            <input
                              value={renameValue}
                              onChange={(e) => setRenameValue(e.target.value)}
                              placeholder="队伍名称"
                              className="theme-input flex-1 rounded-xl px-3 py-2 text-sm"
                              disabled={!canEditTeam}
                            />
                            <button onClick={handleRenameTeam} disabled={!canEditTeam} className="theme-button-primary rounded-xl px-4 py-2 text-sm disabled:opacity-40">
                              保存修改
                            </button>
                          </div>
                          <textarea
                            value={descValue}
                            onChange={(e) => setDescValue(e.target.value)}
                            placeholder="队伍描述（选填，会在队伍广场展示）"
                            className="theme-input w-full rounded-xl px-3 py-2 text-sm resize-none"
                            rows={2}
                            maxLength={500}
                            disabled={!canEditTeam}
                          />
                        </div>

                        {!canEditTeam && (
                          <div className="rounded-xl bg-[var(--surface-2)] px-4 py-3 text-xs theme-faint">
                            当前不在报名阶段，队伍名称、报名状态和成员管理都已锁定。
                          </div>
                        )}
                      </div>
                    ) : (
                      <div className="rounded-xl bg-[var(--surface-2)] px-4 py-3 text-sm theme-faint">
                        你当前是队员。报名、取消报名、改名和成员管理需要队长操作。
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="theme-card rounded-2xl p-5 space-y-4">
                  <div>
                    <div className="text-sm font-semibold theme-text">你还没有队伍</div>
                    <div className="mt-1 text-sm theme-faint">可以在这里直接创建队伍，或在队伍广场申请加入已有队伍。</div>
                  </div>

                  <div className="theme-surface rounded-2xl p-4 space-y-3">
                    <div className="inline-flex items-center gap-2 text-sm font-medium theme-text">
                      <Users size={15} />
                      创建队伍
                    </div>
                    <div className="flex flex-col sm:flex-row gap-2">
                      <input
                        value={teamName}
                        onChange={(e) => setTeamName(e.target.value)}
                        placeholder="输入队伍名称"
                        className="theme-input flex-1 rounded-xl px-3 py-2 text-sm"
                        disabled={!canEditTeam}
                      />
                      <button onClick={handleCreateTeam} disabled={!canEditTeam} className="theme-button-primary rounded-xl px-4 py-2 text-sm disabled:opacity-40">
                        创建队伍
                      </button>
                    </div>
                    <textarea
                      value={teamDesc}
                      onChange={(e) => setTeamDesc(e.target.value)}
                      placeholder="队伍描述（选填，会在队伍广场展示）"
                      className="theme-input w-full rounded-xl px-3 py-2 text-sm resize-none"
                      rows={2}
                      maxLength={500}
                      disabled={!canEditTeam}
                    />
                  </div>

                  {!canEditTeam && (
                    <div className="rounded-xl bg-[var(--surface-2)] px-4 py-3 text-xs theme-faint">
                      当前不在报名阶段，不能再创建队伍或加入队伍。
                    </div>
                  )}
                </div>
              )}
            </section>

            <section className="theme-card rounded-2xl p-5">
              <div className="flex items-center gap-2 mb-4">
                <Users className="w-4 h-4 theme-accent-text" />
                <div>
                  <div className="text-sm font-semibold theme-text">成员列表</div>
                  <div className="text-sm theme-faint">队伍成员、角色和加入时间会展示在这里。</div>
                </div>
              </div>

              {myTeam ? (
                <div className="space-y-3">
                  {myTeam.members.map((member) => (
                    <div key={member.userId} className="theme-surface rounded-2xl px-4 py-3 flex items-center justify-between gap-3">
                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-medium theme-text">{member.username}</span>
                          <TeamBadge label={member.role === 'captain' ? '队长' : '队员'} tone={member.role === 'captain' ? 'warning' : 'default'} />
                          {member.userId === myTeam.captainId && <TeamBadge label="负责报名" tone="accent" />}
                        </div>
                        <div className="mt-1 text-xs theme-faint">加入时间：{new Date(member.joinedAt).toLocaleString('zh-CN')}</div>
                      </div>

                      {myTeam.captain && member.userId !== myTeam.captainId && (
                        <div className="flex flex-wrap gap-2">
                          <button onClick={() => void handleTransferCaptain(member.userId)} disabled={!canEditTeam} className="theme-button-secondary rounded-lg px-3 py-1.5 text-xs disabled:opacity-40">
                            转让队长
                          </button>
                          <button onClick={() => void handleRemoveMember(member.userId)} disabled={!canEditTeam} className="rounded-lg px-3 py-1.5 text-xs bg-[var(--danger)] text-white disabled:opacity-40">
                            移除成员
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-2xl theme-surface p-8 text-center text-sm theme-faint">
                  你还没有加入任何队伍，创建或加入后这里会显示成员信息。
                </div>
              )}
            </section>
          </div>
        )}

        {tab === 'lobby' && (
          <section className="theme-card rounded-2xl p-5 space-y-5">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
              <div>
                <div className="text-sm font-semibold theme-text">队伍广场</div>
                <div className="text-sm theme-faint">统一查看队长、人数和报名状态，快速定位可加入的队伍。</div>
              </div>

              <div className="flex flex-col sm:flex-row gap-2">
                <label className="relative">
                  <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 theme-hint" />
                  <input
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="搜索队名或队长"
                    className="theme-input rounded-xl pl-9 pr-3 py-2 text-sm w-full sm:w-56"
                  />
                </label>

                <div className="flex flex-wrap gap-2">
                  {[
                    { key: 'all' as const, label: '全部' },
                    { key: 'joinable' as const, label: '可加入' },
                    { key: 'registered' as const, label: '已报名' },
                    { key: 'full' as const, label: '已满员' },
                  ].map((item) => (
                    <button
                      key={item.key}
                      onClick={() => setLobbyFilter(item.key)}
                      className={`rounded-xl px-3 py-2 text-sm transition ${lobbyFilter === item.key ? 'theme-button-primary' : 'theme-button-ghost'}`}
                    >
                      {item.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">全部队伍</div>
                <div className="mt-1 text-lg font-semibold theme-text">{teams.length}</div>
              </div>
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">可加入</div>
                <div className="mt-1 text-lg font-semibold theme-text">{availableTeams.length}</div>
              </div>
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">已满员</div>
                <div className="mt-1 text-lg font-semibold theme-text">{fullTeams.length}</div>
              </div>
              <div className="theme-surface rounded-2xl px-4 py-3">
                <div className="text-xs theme-faint">已报名</div>
                <div className="mt-1 text-lg font-semibold theme-text">{registeredTeams.length}</div>
              </div>
            </div>

            <div className="space-y-3">
              {filteredTeams.length === 0 ? (
                <div className="rounded-2xl theme-surface p-8 text-center text-sm theme-faint">
                  没有匹配的队伍，可以切换筛选条件或自己创建一支队伍。
                </div>
              ) : (
                filteredTeams.map((team) => {
                  const isMine = myTeam?.id === team.id
                  const full = team.memberCount >= maxTeamSize
                  const canQuickJoin = !isMine && !myTeam && canEditTeam && !full

                  return (
                    <div key={team.id} className="theme-surface rounded-2xl p-4 flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                      <div className="min-w-0 space-y-2">
                        <div className="flex flex-wrap items-center gap-2">
                          <div className="text-base font-semibold theme-text">{team.teamName}</div>
                          {isMine && <TeamBadge label="我的队伍" tone="accent" />}
                          <TeamBadge label={team.registered ? '已报名' : '待报名'} tone={team.registered ? 'success' : 'warning'} />
                          <TeamBadge label={full ? '已满员' : '可加入'} tone={full ? 'warning' : 'success'} />
                        </div>

                        {team.description && (
                          <div className="text-sm theme-faint">{team.description}</div>
                        )}

                        <div className="flex flex-wrap items-center gap-4 text-sm theme-faint">
                          <span>队长：{team.captainName}</span>
                          <span>人数：{team.memberCount}/{maxTeamSize}</span>
                          <span>创建时间：{new Date(team.createdAt).toLocaleString('zh-CN')}</span>
                        </div>

                        {team.memberNames && team.memberNames.length > 0 && (
                          <div className="flex flex-wrap gap-1.5">
                            {team.memberNames.map((name, i) => (
                              <span key={i} className="rounded-full px-2 py-0.5 text-[11px] theme-tag">
                                {name}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="flex flex-wrap gap-2 shrink-0">
                        {isMine ? (
                          <button onClick={() => setTab('mine')} className="theme-button-secondary rounded-xl px-4 py-2 text-sm">
                            查看我的队伍
                          </button>
                        ) : canQuickJoin ? (
                          <button disabled className="theme-button-ghost rounded-xl px-4 py-2 text-sm opacity-60 cursor-not-allowed">
                            暂不可加入
                          </button>
                        ) : (
                          <button disabled className="theme-button-ghost rounded-xl px-4 py-2 text-sm opacity-60 cursor-not-allowed">
                            {!canEditTeam ? '已锁队' : myTeam ? '你已在其他队伍中' : full ? '已满员' : '暂不可加入'}
                          </button>
                        )}
                      </div>
                    </div>
                  )
                })
              )}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}
