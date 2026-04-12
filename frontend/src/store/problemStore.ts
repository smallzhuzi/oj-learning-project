import { create } from 'zustand'
import type { Problem, ProblemQueryParams, PageResult } from '@/types'
import { getProblems, getProblemBySlug } from '@/api/problem'

interface ProblemState {
  /** 题目列表 */
  problems: Problem[]
  /** 总数 */
  total: number
  /** 当前页 */
  currentPage: number
  /** 是否加载中 */
  loading: boolean
  /** 当前查看的题目详情 */
  currentProblem: Problem | null
  /** 当前选中的 OJ 平台 */
  ojPlatform: string

  /** 加载题目列表 */
  fetchProblems: (params: ProblemQueryParams) => Promise<void>
  /** 加载题目详情 */
  fetchProblemDetail: (slug: string, ojPlatform?: string) => Promise<void>
  /** 设置 OJ 平台 */
  setOjPlatform: (platform: string) => void
  /** 设置当前页 */
  setCurrentPage: (page: number) => void
}

export const useProblemStore = create<ProblemState>((set, get) => ({
  problems: [],
  total: 0,
  currentPage: 1,
  loading: false,
  currentProblem: null,
  ojPlatform: 'leetcode',

  fetchProblems: async (params) => {
    set({ loading: true })
    try {
      const res = await getProblems(params)
      if (res.code === 200) {
        const page = res.data as PageResult<Problem>
        set({
          problems: page.records,
          total: page.total,
          currentPage: page.current,
        })
      }
    } finally {
      set({ loading: false })
    }
  },

  fetchProblemDetail: async (slug, ojPlatform) => {
    set({ loading: true })
    try {
      const platform = ojPlatform || get().ojPlatform
      const res = await getProblemBySlug(slug, platform)
      if (res.code === 200) {
        set({ currentProblem: res.data })
      }
    } finally {
      set({ loading: false })
    }
  },

  setOjPlatform: (platform) => set({ ojPlatform: platform }),
  setCurrentPage: (page) => set({ currentPage: page }),
}))
