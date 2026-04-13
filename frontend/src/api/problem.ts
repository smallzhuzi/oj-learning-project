import request from './request'
import type { ApiResult, PageResult, Problem, ProblemQueryParams, ProblemTagOption } from '@/types'

export function getProblems(params: ProblemQueryParams) {
  const query = {
    ...params,
    tags: params.tags && params.tags.length > 0 ? params.tags.join(',') : undefined,
  }
  return request.get<any, ApiResult<PageResult<Problem>>>('/problems', { params: query })
}

export function getProblemBySlug(slug: string, ojPlatform = 'leetcode') {
  return request.get<any, ApiResult<Problem>>(`/problems/${slug}`, {
    params: { ojPlatform },
  })
}

export function searchProblemTags(ojPlatform = 'leetcode', keyword?: string, pageNum = 1, pageSize = 20) {
  return request.get<any, ApiResult<PageResult<ProblemTagOption>>>('/problems/tags', {
    params: { ojPlatform, keyword, pageNum, pageSize },
  })
}

export function syncProblems(skip = 0, limit = 50, ojPlatform = 'leetcode') {
  return request.post<any, ApiResult<number>>('/problems/sync', null, {
    params: { skip, limit, ojPlatform },
  })
}
