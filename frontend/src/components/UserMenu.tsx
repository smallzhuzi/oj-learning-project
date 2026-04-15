import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronDown, LogOut, Shield, User, Users } from 'lucide-react'
import { useUserStore } from '@/store/userStore'

export default function UserMenu() {
  const navigate = useNavigate()
  const { user, logout } = useUserStore()
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    if (open) document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [open])

  if (!user) return null

  const initials = user.username.slice(0, 1).toUpperCase()

  const go = (path: string) => {
    setOpen(false)
    navigate(path)
  }

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setOpen((value) => !value)}
        className="flex items-center gap-2 px-2 py-1.5 rounded-lg theme-hover transition"
      >
        <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[var(--accent)] to-[var(--accent-strong)] flex items-center justify-center text-xs font-bold text-white shrink-0">
          {initials}
        </div>
        <span className="text-sm theme-muted max-w-[100px] truncate hidden sm:block">
          {user.username}
        </span>
        <ChevronDown className={`w-3.5 h-3.5 theme-hint transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-1.5 w-56 theme-modal rounded-xl overflow-hidden z-50 animate-fade-in-up">
          <div className="px-4 py-3 border-b theme-border">
            <p className="text-sm font-medium theme-text truncate">{user.username}</p>
            <p className="text-xs theme-faint truncate mt-0.5">{user.email}</p>
          </div>

          <div className="py-1">
            <button
              onClick={() => go('/profile')}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-sm theme-muted theme-hover transition"
            >
              <User className="w-4 h-4 theme-hint" />
              个人中心
            </button>

            <button
              onClick={() => go('/teams/mine')}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-sm theme-muted theme-hover transition"
            >
              <Users className="w-4 h-4 theme-hint" />
              我的队伍
            </button>

            {user.role === 'admin' && (
              <button
                onClick={() => go('/admin')}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm theme-muted theme-hover transition"
              >
                <Shield className="w-4 h-4 text-purple-400" />
                管理后台
              </button>
            )}
          </div>

          <div className="border-t theme-border py-1">
            <button
              onClick={() => {
                setOpen(false)
                logout()
                navigate('/login')
              }}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-[var(--danger)] hover:bg-[var(--danger-bg)] transition"
            >
              <LogOut className="w-4 h-4" />
              退出登录
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
