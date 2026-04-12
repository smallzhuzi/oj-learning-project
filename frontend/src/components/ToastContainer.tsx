import { useToastStore, type ToastItem } from '@/store/uiStore'
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react'

const iconMap: Record<ToastItem['type'], React.ReactNode> = {
  success: <CheckCircle size={16} className="text-green-400" />,
  error: <XCircle size={16} className="text-red-400" />,
  info: <Info size={16} className="text-blue-400" />,
  warning: <AlertTriangle size={16} className="text-yellow-400" />,
}

const bgMap: Record<ToastItem['type'], string> = {
  success: 'border-green-800/50 bg-green-950/80',
  error: 'border-red-800/50 bg-red-950/80',
  info: 'border-blue-800/50 bg-blue-950/80',
  warning: 'border-yellow-800/50 bg-yellow-950/80',
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
          className={`pointer-events-auto flex items-center gap-2.5 px-4 py-3 rounded-lg border backdrop-blur-sm shadow-lg min-w-[280px] max-w-[420px] animate-slide-in ${bgMap[t.type]}`}
        >
          {iconMap[t.type]}
          <span className="flex-1 text-sm text-gray-200">{t.message}</span>
          <button
            onClick={() => removeToast(t.id)}
            className="text-gray-500 hover:text-gray-300 shrink-0"
          >
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  )
}
