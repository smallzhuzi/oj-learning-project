import request from './request'
import type { ApiResult, CodeDraft, SaveDraftParams } from '@/types'

/** 保存草稿（upsert） */
export function saveDraft(data: SaveDraftParams) {
  return request.put<any, ApiResult<void>>('/drafts', data)
}

/** 获取某题所有语言的草稿 */
export function getDrafts(problemSlug: string) {
  return request.get<any, ApiResult<CodeDraft[]>>(`/drafts/${problemSlug}`)
}
