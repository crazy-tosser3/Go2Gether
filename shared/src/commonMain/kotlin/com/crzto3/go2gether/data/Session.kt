package com.crzto3.go2gether.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Кто вошёл. Восстанавливается при запуске по сохранённому токену через /auth/me. */
class Session(private val api: Api) {

    sealed interface State {
        data object Loading : State
        data object Guest : State
        data class Authorized(val user: User, val organizations: List<OrganizationShort>) : State
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    val user: User? get() = (state.value as? State.Authorized)?.user

    init {
        api.client.onUnauthorized = { _state.value = State.Guest }
    }

    suspend fun restore() {
        if (!api.client.hasToken) {
            _state.value = State.Guest
            return
        }
        refresh()
    }

    suspend fun refresh() {
        _state.value = try {
            val me = api.me()
            State.Authorized(me.user, me.organizations)
        } catch (e: ApiException) {
            if (e.isUnauthorized) api.client.clearToken()
            // Нет сети — не выкидываем из аккаунта, просто остаёмся гостем до следующей попытки.
            State.Guest
        }
    }

    suspend fun loginDemo(id: Id) {
        api.loginDemo(id)
        refresh()
    }

    suspend fun login(email: String, password: String) {
        api.login(email, password)
        refresh()
    }

    suspend fun register(name: String, email: String, password: String) {
        api.register(RegisterRequest(name, email, password, consent = true))
        refresh()
    }

    fun updateUser(user: User) {
        val current = state.value as? State.Authorized ?: return
        _state.value = current.copy(user = user)
    }

    suspend fun logout() {
        runCatching { api.logout() }
        api.client.clearToken()
        _state.value = State.Guest
    }
}
