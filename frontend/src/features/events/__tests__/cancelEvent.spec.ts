import { describe, it, expect } from 'vitest'
import { cancelReasonError } from '@/features/events/cancelEvent'

describe('cancelReasonError', () => {
  it('причина обязательна, осмысленна и не длиннее 500', () => {
    expect(cancelReasonError('  ')).toBeTruthy()
    expect(cancelReasonError('нет')).toBeTruthy()
    expect(cancelReasonError('x'.repeat(501))).toBeTruthy()
    expect(cancelReasonError(' Заболел тренер ')).toBeNull()
  })
})
