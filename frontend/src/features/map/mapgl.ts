import type { load } from '@2gis/mapgl'
import { t } from '@/i18n'

export type MapglApi = Awaited<ReturnType<typeof load>>

export class MapUnavailableError extends Error {
  constructor(readonly reason: 'no-key' | 'unsupported' | 'load-failed') {
    super(reason)
  }
}

export const mapKey = () => import.meta.env.VITE_2GIS_KEY?.trim() ?? ''

let loading: Promise<MapglApi> | null = null

/** Скрипт MapGL грузим с серверов 2ГИС только когда карта действительно нужна. */
export function loadMapgl(): Promise<MapglApi> {
  if (!mapKey()) return Promise.reject(new MapUnavailableError('no-key'))
  loading ??= import('@2gis/mapgl')
    .then(async ({ load }) => {
      const api = await load()
      if (!api.isSupported()) throw new MapUnavailableError('unsupported')
      return api
    })
    .catch((error: unknown) => {
      loading = null
      throw error instanceof MapUnavailableError ? error : new MapUnavailableError('load-failed')
    })
  return loading
}

// Геттеры: текст берётся из словаря в момент показа, на текущем языке.
export const UNAVAILABLE_TEXT: Record<MapUnavailableError['reason'], string> = {
  get 'no-key'() {
    return t('map.noKey')
  },
  get unsupported() {
    return t('map.unsupported')
  },
  get 'load-failed'() {
    return t('map.loadFailed')
  },
}
