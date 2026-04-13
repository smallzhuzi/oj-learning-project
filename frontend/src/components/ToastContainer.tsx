import { useToastStore, type ToastItem } from '@/store/uiStore'
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react'

const iconMap: Record<ToastItem['type'], React.ReactNode> = {
  success: <CheckCircle size={16} style={{ color: 'var(--success)' }} />,
  error: <XCircle size={16} style={{ color: 'var(--danger)' }} />,
  info: <Info size={16} style={{ color: 'var(--info)' }} />,
  warning: <AlertTriangle size={16} style={{ color: 'var(--warning)' }} />,
}

const bgMap: Record<ToastItem['type'], string> = {
  success: 'toast-success',
  error: 'toast-error',
  info: 'toast-info',
  warning: 'toast-warning',
}

export default function ToastContainer() {
  const toasts = useToastStore((s) => s.toasts)
  const removeToast = useToastStore((s) => s.removeToast)

  if (toasts.length === 0) return null

  return (
    <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`pointer-events-auto flex items-center gap-2.5 px-4 py-3 rounded-xl shadow-lg min-w-[280px] max-w-[420px] animate-slide-in ${bgMap[t.type]}`}
        >
          {iconMap[t.type]}
          <span className="flex-1 text-sm theme-text">{t.message}</span>
          <button
            onClick={() => removeToast(t.id)}
            className="theme-hint hover:text-[var(--text-primary)] shrink-0 transition"
          >
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  )
}
