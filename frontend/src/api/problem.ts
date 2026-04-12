import request from './request'
import type { ApiResult, PageResult, Problem, ProblemQueryParams } from '@/types'

/** 分页查询题目列表 */
export function getProblems(params: ProblemQueryParams) {
  return request.get<any, ApiResult<PageResult<Problem>>>('/problems', { params })
}

/** 根据 slug 获取题目详情 */
export function getProblemBySlug(slug: string, ojPlatform = 'leetcode') {
  return request.get<any, ApiResult<Problem>>(`/problems/${slug}`, {
    params: { ojPlatform },
  })
}

/** 从远程 OJ 同步题目 */
export function syncProblems(skip = 0, limit = 50, ojPlatform = 'leetcode') {
  return request.post<any, ApiResult<number>>('/problems/sync', null, {
    params: { skip, limit, ojPlatform },
  })
}
