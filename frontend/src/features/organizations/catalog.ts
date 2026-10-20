import type { Organization, OrganizationType } from '@/api'

export type TypeFilter = OrganizationType | 'all'

/** Тип API пока не фильтрует — отбираем на клиенте среди загруженных. */
export function filterByType(list: Organization[], type: TypeFilter): Organization[] {
  return type === 'all' ? list : list.filter((o) => o.type === type)
}

/** Ссылку из данных показываем только если это http(s) — иначе это может быть javascript:. */
export function safeHref(url: string | null | undefined): string | null {
  if (!url) return null
  try {
    const u = new URL(url.trim())
    return u.protocol === 'http:' || u.protocol === 'https:' ? u.href : null
  } catch {
    return null
  }
}

export function shortHost(href: string): string {
  return new URL(href).host.replace(/^www\./, '')
}
