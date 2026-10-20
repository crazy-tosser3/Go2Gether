import { t } from '@/i18n'
import type { ApiErrorBody } from './types'

export const API_BASE = '/api/v1'

// Вход держится в cookie HttpOnly, которую ставит бэк: токен скрипту не виден и нигде не хранится.
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fields: Record<string, string>

  constructor(status: number, code: string, message: string, fields: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.fields = fields
  }

  /** Мероприятие снято или удалено (410). */
  get isGone() {
    return this.status === 410
  }

  get isUnauthorized() {
    return this.status === 401
  }
}

// Свои тексты — только когда бэк не прислал понятного сообщения; его сообщение не переводим.
const FALLBACK_STATUSES = [0, 401, 403, 404, 409, 410, 422]

function fallbackMessage(status: number) {
  return FALLBACK_STATUSES.includes(status) ? t(`apiErrors.s${status}`) : t('apiErrors.other')
}

type Query = Record<string, string | number | boolean | null | undefined>

export function buildUrl(path: string, query?: Query) {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
  }
  const qs = params.toString()
  return `${API_BASE}${path}${qs ? `?${qs}` : ''}`
}

interface RequestOptions {
  query?: Query
  body?: unknown
  signal?: AbortSignal
}

async function parseError(response: Response): Promise<ApiError> {
  let body: Partial<ApiErrorBody> | null = null
  try {
    body = (await response.json()) as Partial<ApiErrorBody>
  } catch {
    body = null
  }
  const error = body?.error
  return new ApiError(
    response.status,
    error?.code ?? `http_${response.status}`,
    error?.message || fallbackMessage(response.status),
    error?.fields ?? {},
  )
}

export async function request<T>(
  method: string,
  path: string,
  { query, body, signal }: RequestOptions = {},
): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  // Файлы уходят как multipart: заголовок с границей частей браузер ставит сам.
  const isForm = body instanceof FormData
  if (body !== undefined && !isForm) headers['Content-Type'] = 'application/json'

  let response: Response
  try {
    response = await fetch(buildUrl(path, query), {
      credentials: 'same-origin',
      method,
      headers,
      body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
      signal,
    })
  } catch (cause) {
    if (cause instanceof DOMException && cause.name === 'AbortError') throw cause
    throw new ApiError(0, 'network', fallbackMessage(0))
  }

  if (!response.ok) throw await parseError(response)
  if (response.status === 204) return undefined as T

  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const http = {
  get: <T>(path: string, options?: RequestOptions) => request<T>('GET', path, options),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('POST', path, { ...options, body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PATCH', path, { ...options, body }),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PUT', path, { ...options, body }),
  delete: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('DELETE', path, { ...options, body }),
}
