import { useNavigate, useLocation } from 'react-router-dom'
import { Trophy, Users, UserCheck } from 'lucide-react'

const navItems = [
  { path: '/contests', label: '比赛列表', icon: Trophy },
  { path: '/teams', label: '队伍空间', icon: Users },
  { path: '/teams/mine', label: '我的队伍', icon: UserCheck },
]

export default function ContestSidebar() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <aside className="w-48 shrink-0 theme-sidebar border-r theme-border h-full py-4 px-2 space-y-1">
      {navItems.map((item) => {
        const active = location.pathname === item.path
        const Icon = item.icon
        return (
          <button
            key={item.path}
            onClick={() => navigate(item.path)}
            className={`w-full flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm transition ${
              active
                ? 'bg-[var(--accent-soft)] text-[var(--accent)] font-medium'
                : 'theme-faint hover:bg-[var(--hover-bg)] hover:text-[var(--text-primary)]'
            }`}
          >
            <Icon size={16} />
            {item.label}
          </button>
        )
      })}
    </aside>
  )
}
