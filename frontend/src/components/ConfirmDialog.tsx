import { useConfirmStore } from '@/store/uiStore'
import { AlertTriangle, Info, XCircle } from 'lucide-react'

const typeConfig = {
  info: {
    icon: <Info size={20} style={{ color: 'var(--info)' }} />,
    confirmClass: 'theme-button-blue',
  },
  warning: {
    icon: <AlertTriangle size={20} style={{ color: 'var(--warning)' }} />,
    confirmClass: 'bg-gradient-to-r from-amber-500 to-amber-600 text-white hover:brightness-110',
  },
  danger: {
    icon: <XCircle size={20} style={{ color: 'var(--danger)' }} />,
    confirmClass: 'theme-button-danger',
  },
}

export default function ConfirmDialog() {
  const { visible, options, close } = useConfirmStore()

  if (!visible) return null

  const type = options.type || 'info'
  const config = typeConfig[type]

  return (
    <div className="fixed inset-0 z-[9998] flex items-center justify-center theme-overlay" onClick={() => close(false)}>
      <div
        className="theme-modal w-[400px] rounded-2xl overflow-hidden animate-scale-in"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-6">
          <div className="flex items-start gap-3">
            <div className="shrink-0 mt-0.5">{config.icon}</div>
            <div className="flex-1">
              {options.title && (
                <h3 className="font-medium theme-text mb-1">{options.title}</h3>
              )}
              <p className="text-sm theme-muted leading-relaxed">{options.message}</p>
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-2 px-6 py-4 border-t theme-border" style={{ background: 'var(--hover-bg)' }}>
          <button
            onClick={() => close(false)}
            className="px-4 py-2 text-sm theme-button-secondary rounded-xl transition"
          >
            {options.cancelText || '取消'}
          </button>
          <button
            onClick={() => close(true)}
            className={`px-4 py-2 text-sm rounded-xl transition ${config.confirmClass}`}
          >
            {options.confirmText || '确定'}
          </button>
        </div>
      </div>
    </div>
  )
}
