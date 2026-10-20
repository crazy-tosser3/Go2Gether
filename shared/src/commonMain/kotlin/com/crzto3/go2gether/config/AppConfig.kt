package com.crzto3.go2gether.config

/**
 * Конфигурация приложения. Адрес API захардкожен здесь — единственное место,
 * которое нужно поменять, чтобы направить Android и iOS на другой сервер.
 */
object AppConfig {
    /** Адрес развёрнутого API-сервера. */
    const val API_BASE_URL = "https://hack.crzto3.ru"

    /** Префикс REST API бэка. */
    const val API_PREFIX = "/api/v1"

    /** Имя cookie, в которой бэк присылает JWT. */
    const val SESSION_COOKIE = "jwt"

    const val REQUEST_TIMEOUT_MS = 20_000L

    const val PAGE_SIZE = 20

    /** Сколько раз повторить GET, если шлюз ответил 502–504. */
    const val GATEWAY_RETRIES = 3

    /** Ключ 2ГИС (MapGL JS API + Geocoder API) — тот же, что у веб-фронта в VITE_2GIS_KEY. */
    const val TWO_GIS_KEY = "ed95edc1-4df7-4f30-8000-08d9b74b1132"

    /** Скрипт MapGL; карта работает в WebView, нативному SDK 2ГИС нужен другой тип ключа. */
    const val MAPGL_SCRIPT = "https://mapgl.2gis.com/api/js/v1"

    /** Геокодер 2ГИС: точка по адресу. */
    const val GEOCODER_URL = "https://catalog.api.2gis.com/3.0/items/geocode"

    /** Адрес, от имени которого открывается страница карты: если ключ ограничен по домену, он совпадёт с сайтом. */
    const val MAP_BASE_URL = "$API_BASE_URL/"

    /** Центр Красноярска — если у мероприятий нет координат. */
    const val DEFAULT_LAT = 56.0106
    const val DEFAULT_LNG = 92.8526

    fun twoGisLink(lat: Double, lng: Double) = "https://2gis.ru/geo/$lng,$lat"

    fun apiUrl(path: String): String = API_BASE_URL + API_PREFIX + path

    /** avatar_url приходит относительным (/api/v1/avatars/...) — дополняем адресом сервера. */
    fun absoluteUrl(url: String?): String? = when {
        url.isNullOrBlank() -> null
        url.startsWith("http") -> url
        else -> API_BASE_URL + url
    }
}
