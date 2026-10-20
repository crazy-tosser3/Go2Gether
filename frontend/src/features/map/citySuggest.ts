import { mapKey } from './mapgl'

export interface CitySuggestion {
  name: string
  region: string | null
}

type SuggestItem = {
  name?: string
  type?: string
  subtype?: string
  adm_div?: { type?: string; name?: string }[]
}

/** Только города и посёлки России; одинаковые «город + регион» — один раз. */
export function parseCitySuggest(body: unknown): CitySuggestion[] {
  const items = (body as { result?: { items?: SuggestItem[] } })?.result?.items ?? []
  const seen = new Set<string>()
  const out: CitySuggestion[] = []
  for (const i of items) {
    if (!i.name || i.type !== 'adm_div') continue
    const country = i.adm_div?.find((a) => a.type === 'country')?.name
    if (country && country !== 'Россия') continue
    const region = i.adm_div?.find((a) => a.type === 'region')?.name ?? null
    const key = `${i.name}|${region}`
    if (seen.has(key)) continue
    seen.add(key)
    out.push({ name: i.name, region })
  }
  return out
}

/** Подсказки городов по началу названия — подсказчик 2ГИС по ключу карты. */
export async function suggestCities(
  query: string,
  signal?: AbortSignal,
): Promise<CitySuggestion[]> {
  const key = mapKey()
  const q = query.trim()
  if (!key || q.length < 2) return []
  const url = new URL('https://catalog.api.2gis.com/3.0/suggests')
  url.searchParams.set('q', q)
  url.searchParams.set('type', 'adm_div.city,adm_div.settlement')
  url.searchParams.set('fields', 'items.adm_div')
  url.searchParams.set('page_size', '8')
  url.searchParams.set('key', key)
  const res = await fetch(url, { signal })
  if (!res.ok) throw new Error(`suggest ${res.status}`)
  return parseCitySuggest(await res.json())
}
