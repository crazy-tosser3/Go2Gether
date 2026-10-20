package com.crzto3.go2gether.data

import com.crzto3.go2gether.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/** Движок HTTP своей платформы: OkHttp на Android, Darwin (NSURLSession) на iOS. */
expect fun httpEngine(): HttpClientEngineFactory<*>

/** Ошибка API: статус, код бэка и его сообщение (бэк присылает их уже на русском). */
class ApiException(
    val status: Int,
    val code: String,
    override val message: String,
    val fields: Map<String, String> = emptyMap(),
) : Exception(message) {
    val isUnauthorized get() = status == 401
    val isGone get() = status == 410
}

private fun fallbackMessage(status: Int) = when (status) {
    0 -> "Нет связи с сервером. Проверьте интернет и попробуйте ещё раз."
    401 -> "Нужно войти в аккаунт."
    403 -> "Недостаточно прав."
    404 -> "Не найдено."
    409 -> "Конфликт: данные уже изменились."
    410 -> "Мероприятие снято или удалено."
    422 -> "Проверьте введённые данные."
    else -> "Что-то пошло не так. Попробуйте позже."
}

fun Throwable.userMessage(): String = (this as? ApiException)?.message ?: fallbackMessage(0)

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = true
    namingStrategy = JsonNamingStrategy.SnakeCase
}

/**
 * HTTP-клиент к бэку. Бэк кладёт JWT в cookie `jwt` (HttpOnly). В приложении cookie-банка браузера нет,
 * поэтому токен забираем из Set-Cookie, храним в [TokenStorage] и шлём обратно и cookie, и Bearer.
 */
class ApiClient(engine: HttpClientEngineFactory<*> = httpEngine()) {

    var onUnauthorized: (() -> Unit)? = null

    val http = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(AppJson) }
        install(HttpTimeout) {
            requestTimeoutMillis = AppConfig.REQUEST_TIMEOUT_MS
            connectTimeoutMillis = AppConfig.REQUEST_TIMEOUT_MS
        }
    }

    val hasToken get() = TokenStorage.token != null

    fun clearToken() {
        TokenStorage.token = null
    }

    suspend fun send(
        method: HttpMethod,
        path: String,
        query: Map<String, Any?> = emptyMap(),
        body: Any? = null,
    ): HttpResponse {
        // Шлюз сервера иногда отвечает 502–504; чтение и вход безопасно повторить, остальные изменения — нет.
        val safe = method == HttpMethod.Get || path == "/auth/login"
        var attempt = 0
        while (true) {
            val response = sendOnce(method, path, query, body)
            val retriable = safe && response.status.value in 502..504 && attempt < AppConfig.GATEWAY_RETRIES
            if (!retriable) return finish(response)
            attempt++
            delay(400L * attempt)
        }
    }

    private suspend fun sendOnce(method: HttpMethod, path: String, query: Map<String, Any?>, body: Any?): HttpResponse =
        try {
            http.request(AppConfig.apiUrl(path)) {
                this.method = method
                header(HttpHeaders.Accept, "application/json")
                query.forEach { (k, v) -> if (v != null && v.toString().isNotEmpty()) parameter(k, v) }
                authorize()
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ApiException(0, "network", fallbackMessage(0))
        }

    private suspend fun finish(response: HttpResponse): HttpResponse {
        storeSession(response)
        if (!response.status.isSuccess()) throw parseError(response)
        return response
    }

    private fun HttpRequestBuilder.authorize() {
        val token = TokenStorage.token ?: return
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.Cookie, "${AppConfig.SESSION_COOKIE}=$token")
    }

    private fun storeSession(response: HttpResponse) {
        val cookie = response.setCookie().firstOrNull { it.name == AppConfig.SESSION_COOKIE } ?: return
        TokenStorage.token = cookie.value.takeIf { it.isNotEmpty() && cookie.maxAge != 0 }
    }

    private suspend fun parseError(response: HttpResponse): ApiException {
        val status = response.status.value
        val details = runCatching {
            AppJson.decodeFromString(ApiErrorBody.serializer(), response.bodyAsText()).error
        }.getOrNull()
        if (status == 401 && TokenStorage.token != null) {
            TokenStorage.token = null
            onUnauthorized?.invoke()
        }
        return ApiException(
            status = status,
            code = details?.code ?: "http_$status",
            message = details?.message?.takeIf { it.isNotBlank() } ?: fallbackMessage(status),
            fields = details?.fields.orEmpty(),
        )
    }
}
