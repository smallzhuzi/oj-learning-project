import { useConfirmStore } from '@/store/uiStore'
import { AlertTriangle, Info, XCircle } from 'lucide-react'

const typeConfig = {
  info: {
    icon: <Info size={20} className="text-blue-400" />,
    confirmClass: 'bg-blue-600 hover:bg-blue-700',
  },
  warning: {
    icon: <AlertTriangle size={20} className="text-yellow-400" />,
    confirmClass: 'bg-yellow-600 hover:bg-yellow-700',
  },
  danger: {
    icon: <XCircle size={20} className="text-red-400" />,
    confirmClass: 'bg-red-600 hover:bg-red-700',
  },
}

export default function ConfirmDialog() {
  const { visible, options, close } = useConfirmStore()

  if (!visible) return null

  const type = options.type || 'info'
  const config = typeConfig[type]

  return (
    <div className="fixed inset-0 z-[9998] flex items-center justify-center bg-black/60 backdrop-blur-sm" onClick={() => close(false)}>
      <div
        className="bg-gray-800 rounded-xl border border-gray-700 shadow-2xl w-[400px] overflow-hidden animate-scale-in"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-6">
          <div className="flex items-start gap-3">
            <div className="shrink-0 mt-0.5">{config.icon}</div>
            <div className="flex-1">
              {options.title && (
                <h3 className="font-medium text-gray-100 mb-1">{options.title}</h3>
              )}
              <p className="text-sm text-gray-400 leading-relaxed">{options.message}</p>
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-2 px-6 py-4 bg-gray-850 border-t border-gray-700">
          <button
            onClick={() => close(false)}
            className="px-4 py-2 text-sm text-gray-300 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors"
          >
            {options.cancelText || '取消'}
          </button>
          <button
            onClick={() => close(true)}
            className={`px-4 py-2 text-sm text-white rounded-lg transition-colors ${config.confirmClass}`}
          >
            {options.confirmText || '确定'}
          </button>
        </div>
      </div>
    </div>
  )
}
