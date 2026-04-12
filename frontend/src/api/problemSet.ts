import request from './request'
import type {
  ApiResult,
  PageResult,
  ProblemSet,
  ProblemSetItemDetail,
  CreateProblemSetParams,
  QuickGenerateParams,
} from '@/types'

/** 创建题单（手动选题） */
export function createProblemSet(data: CreateProblemSetParams) {
  return request.post<any, ApiResult<ProblemSet>>('/problem-sets', data)
}

/** 快速组题 */
export function quickGenerate(data: QuickGenerateParams) {
  return request.post<any, ApiResult<ProblemSet>>('/problem-sets/quick-generate', data)
}

/** 获取我的题单列表 */
export function getMyProblemSets(pageNum = 1, pageSize = 20) {
  return request.get<any, ApiResult<PageResult<ProblemSet>>>('/problem-sets', {
    params: { pageNum, pageSize },
  })
}

/** 获取公开题单列表 */
export function getPublicProblemSets(pageNum = 1, pageSize = 20) {
  return request.get<any, ApiResult<PageResult<ProblemSet>>>('/problem-sets/public', {
    params: { pageNum, pageSize },
  })
}

/** 获取题单详情 */
export function getProblemSetDetail(id: number) {
  return request.get<any, ApiResult<ProblemSet>>(`/problem-sets/${id}`)
}

/** 获取题单内所有题目 */
export function getProblemSetItems(id: number) {
  return request.get<any, ApiResult<ProblemSetItemDetail[]>>(`/problem-sets/${id}/items`)
}

/** 更新题单信息 */
export function updateProblemSet(id: number, data: Partial<CreateProblemSetParams>) {
  return request.put<any, ApiResult<void>>(`/problem-sets/${id}`, data)
}

/** 删除题单 */
export function deleteProblemSet(id: number) {
  return request.delete<any, ApiResult<void>>(`/problem-sets/${id}`)
}

/** 向题单添加题目 */
export function addItemToProblemSet(id: number, problemSlug: string, score = 100) {
  return request.post<any, ApiResult<void>>(`/problem-sets/${id}/items`, null, {
    params: { problemSlug, score },
  })
}

/** 从题单移除题目 */
export function removeItemFromProblemSet(setId: number, itemId: number) {
  return request.delete<any, ApiResult<void>>(`/problem-sets/${setId}/items/${itemId}`)
}

/** 调整题单题目顺序 */
export function reorderProblemSetItems(setId: number, itemIds: number[]) {
  return request.put<any, ApiResult<void>>(`/problem-sets/${setId}/items/reorder`, itemIds)
}
