import type { ProblemTag } from '@/types'

export function getProblemTagLabel(tag: ProblemTag | null | undefined) {
  if (!tag) return ''
  return tag.label || tag.name || tag.sourceName || tag.key || tag.slug || ''
}

export function getProblemTagKey(tag: ProblemTag | null | undefined, index = 0) {
  if (!tag) return `tag-${index}`
  return String(tag.id || tag.key || tag.slug || tag.label || tag.name || index)
}
