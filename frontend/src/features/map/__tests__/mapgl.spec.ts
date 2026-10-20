import { afterEach, describe, expect, it, vi } from 'vitest'

afterEach(() => {
  vi.unstubAllEnvs()
  vi.resetModules()
})

describe('loadMapgl', () => {
  it('без ключа не ходит в 2ГИС и сообщает причину', async () => {
    vi.stubEnv('VITE_2GIS_KEY', '')
    const { loadMapgl, MapUnavailableError } = await import('../mapgl')
    await expect(loadMapgl()).rejects.toBeInstanceOf(MapUnavailableError)
    await expect(loadMapgl()).rejects.toMatchObject({ reason: 'no-key' })
  })
})
