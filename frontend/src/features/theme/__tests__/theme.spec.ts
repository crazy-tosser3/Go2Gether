import { beforeEach, describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import { applyTheme, initTheme, parseTheme, readTheme, useTheme } from '@/features/theme/theme'

beforeEach(() => {
  localStorage.clear()
  delete document.documentElement.dataset.theme
})

describe('theme', () => {
  it('неизвестное значение — как в системе', () => {
    expect(parseTheme('dark')).toBe('dark')
    expect(parseTheme('sepia')).toBe('system')
    expect(parseTheme(null)).toBe('system')
  })

  it('«как в системе» снимает атрибут, явный выбор его ставит', () => {
    const root = document.createElement('html')
    applyTheme('dark', root)
    expect(root.dataset.theme).toBe('dark')
    applyTheme('system', root)
    expect(root.dataset.theme).toBeUndefined()
  })

  it('выбор применяется и запоминается', async () => {
    localStorage.setItem('theme', 'light')
    initTheme()
    expect(document.documentElement.dataset.theme).toBe('light')
    const theme = useTheme()
    theme.value = 'dark'
    await nextTick()
    expect(document.documentElement.dataset.theme).toBe('dark')
    expect(readTheme()).toBe('dark')
    theme.value = 'system'
    await nextTick()
    expect(localStorage.getItem('theme')).toBeNull()
    expect(document.documentElement.dataset.theme).toBeUndefined()
  })
})
