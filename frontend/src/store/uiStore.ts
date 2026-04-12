import { create } from 'zustand'

// ==================== Toast ====================

export interface ToastItem {
  id: string
  type: 'success' | 'error' | 'info' | 'warning'
  message: string
}

interface ToastStore {
  toasts: ToastItem[]
  addToast: (type: ToastItem['type'], message: string, duration?: number) => void
  removeToast: (id: string) => void
}

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  addToast: (type, message, duration = 3000) => {
    const id = Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
    set((s) => ({ toasts: [...s.toasts, { id, type, message }] }))
    setTimeout(() => {
      set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) }))
    }, duration)
  },
  removeToast: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}))

/** 便捷方法 */
export const toast = {
  success: (msg: string, duration?: number) => useToastStore.getState().addToast('success', msg, duration),
  error: (msg: string, duration?: number) => useToastStore.getState().addToast('error', msg, duration),
  info: (msg: string, duration?: number) => useToastStore.getState().addToast('info', msg, duration),
  warning: (msg: string, duration?: number) => useToastStore.getState().addToast('warning', msg, duration),
}

// ==================== Confirm ====================

interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: 'info' | 'warning' | 'danger'
}

interface ConfirmStore {
  visible: boolean
  options: ConfirmOptions
  resolve: ((value: boolean) => void) | null
  showConfirm: (options: ConfirmOptions) => Promise<boolean>
  close: (result: boolean) => void
}

export const useConfirmStore = create<ConfirmStore>((set, get) => ({
  visible: false,
  options: { message: '' },
  resolve: null,
  showConfirm: (options) => {
    return new Promise<boolean>((resolve) => {
      set({ visible: true, options, resolve })
    })
  },
  close: (result) => {
    const { resolve } = get()
    if (resolve) resolve(result)
    set({ visible: false, resolve: null })
  },
}))

/** 便捷方法：await confirm('确定？') */
export const confirm = (message: string, options?: Partial<ConfirmOptions>) =>
  useConfirmStore.getState().showConfirm({ message, ...options })
