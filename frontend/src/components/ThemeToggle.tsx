import { Sun, Moon } from 'lucide-react'
import { useThemeStore } from '@/store/uiStore'

/**
 * 全局主题切换按钮
 * 可放置在任意页面的导航栏中
 */
export default function ThemeToggle({ className = '' }: { className?: string }) {
  const { theme, setTheme } = useThemeStore()
  const isDark = theme === 'dark'

  return (
    <button
      type="button"
      onClick={() => setTheme(isDark ? 'light' : 'dark')}
      className={`relative inline-flex items-center justify-center w-9 h-9 rounded-xl theme-button-secondary overflow-hidden ${className}`}
      title={isDark ? '切换到白天主题' : '切换到黑夜主题'}
      aria-label={isDark ? '切换到白天主题' : '切换到黑夜主题'}
    >
      <Sun
        className={`w-4 h-4 absolute theme-toggle-icon ${isDark ? 'theme-toggle-icon-active' : 'theme-toggle-icon-enter'}`}
        style={{ color: isDark ? '#fbbf24' : undefined }}
      />
      <Moon
        className={`w-4 h-4 absolute theme-toggle-icon ${!isDark ? 'theme-toggle-icon-active' : 'theme-toggle-icon-enter'}`}
        style={{ color: !isDark ? '#6366f1' : undefined }}
      />
    </button>
  )
}
