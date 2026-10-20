import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { http as mswHttp, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { ApiError, buildUrl, http } from '@/api/http'

const server = setupServer()

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => {
  server.resetHandlers()
})
afterAll(() => server.close())

describe('buildUrl', () => {
  it('пропускает пустые параметры', () => {
    expect(buildUrl('/events', { q: 'бег', sport: '', page: 2, city: undefined })).toBe(
      '/api/v1/events?q=%D0%B1%D0%B5%D0%B3&page=2',
    )
  })
})

describe('http', () => {
  it('держит вход в cookie от сервера и разбирает JSON', async () => {
    let cookie: string | null = null
    server.use(
      mswHttp.post('*/api/v1/auth/login', () =>
        HttpResponse.json({ ok: true }, { headers: { 'Set-Cookie': 'jwt=abc; Path=/; HttpOnly' } }),
      ),
      mswHttp.get('*/api/v1/ping', ({ request }) => {
        cookie = request.headers.get('cookie')
        return HttpResponse.json({ ok: true })
      }),
    )
    await http.post('/auth/login')
    await expect(http.get('/ping')).resolves.toEqual({ ok: true })
    expect(cookie).toContain('jwt=abc')
  })

  it('превращает ответ с ошибкой в ApiError с полями', async () => {
    server.use(
      mswHttp.post('*/api/v1/events', () =>
        HttpResponse.json(
          {
            error: {
              code: 'validation',
              message: 'Проверьте поля',
              fields: { title: 'Укажите название' },
            },
          },
          { status: 422 },
        ),
      ),
    )
    const error = await http.post('/events', {}).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 422, code: 'validation', message: 'Проверьте поля' })
    expect((error as ApiError).fields).toEqual({ title: 'Укажите название' })
  })

  it('отмечает 410 как недоступное мероприятие и подставляет текст по умолчанию', async () => {
    server.use(mswHttp.get('*/api/v1/events/1', () => new HttpResponse(null, { status: 410 })))
    const error = (await http.get('/events/1').catch((e: unknown) => e)) as ApiError
    expect(error.isGone).toBe(true)
    expect(error.message).toBe('Мероприятие недоступно.')
  })

  it('сообщает о сетевой ошибке по-русски', async () => {
    server.use(mswHttp.get('*/api/v1/ping', () => HttpResponse.error()))
    const error = (await http.get('/ping').catch((e: unknown) => e)) as ApiError
    expect(error.status).toBe(0)
    expect(error.message).toMatch(/Нет связи/)
  })

  it('возвращает undefined на 204', async () => {
    server.use(mswHttp.post('*/api/v1/auth/logout', () => new HttpResponse(null, { status: 204 })))
    await expect(http.post('/auth/logout')).resolves.toBeUndefined()
  })
})
