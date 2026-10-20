import { describe, it, expect } from 'vitest'
import type { Comment, EventSummary } from '@/api'
import {
  moderationActions,
  moderationReasonError,
  parseEventStatusFilter,
  replaceComment,
  replaceEventStatus,
} from '@/features/admin/events'

describe('moderationActions', () => {
  it('опубликованное можно снять или удалить', () => {
    expect(moderationActions('published')).toEqual(['unpublish', 'delete'])
  })
  it('снятое — вернуть или удалить', () => {
    expect(moderationActions('unpublished')).toEqual(['restore', 'delete'])
  })
  it('удалённое и отменённое не трогаем', () => {
    expect(moderationActions('deleted')).toEqual([])
    expect(moderationActions('cancelled')).toEqual([])
  })
})

describe('parseEventStatusFilter', () => {
  it('по умолчанию — опубликованные', () => {
    expect(parseEventStatusFilter(undefined)).toBe('published')
    expect(parseEventStatusFilter('deleted')).toBe('deleted')
  })
})

describe('замены в списках', () => {
  it('меняет статус только у нужного мероприятия', () => {
    const list = [
      { id: 1, status: 'published' },
      { id: 2, status: 'published' },
    ] as EventSummary[]
    expect(replaceEventStatus(list, 2, 'unpublished').map((e) => e.status)).toEqual([
      'published',
      'unpublished',
    ])
  })
  it('подменяет скрытое сообщение ответом сервера', () => {
    const list = [
      { id: 1, is_hidden: false },
      { id: 2, is_hidden: false },
    ] as Comment[]
    const next = replaceComment(list, { id: 2, is_hidden: true } as Comment)
    expect(next.map((c) => c.is_hidden)).toEqual([false, true])
  })
})

describe('moderationReasonError', () => {
  it('причина обязательна', () => {
    expect(moderationReasonError('')).not.toBeNull()
    expect(moderationReasonError('ok')).not.toBeNull()
    expect(moderationReasonError('Площадка закрыта на ремонт')).toBeNull()
  })
})
