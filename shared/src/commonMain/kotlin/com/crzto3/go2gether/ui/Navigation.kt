package com.crzto3.go2gether.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.crzto3.go2gether.data.Api
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.data.userMessage

/** Экраны приложения — повторяют маршруты frontend/src/router. */
sealed interface Screen {
    data object Home : Screen
    data object Events : Screen
    data object CreateEvent : Screen
    data object MyParticipations : Screen
    data object Profile : Screen
    data object EditProfile : Screen
    data object MyEvents : Screen
    data object Organizations : Screen
    data object MyOrganizations : Screen
    data object Notifications : Screen
    data object Login : Screen
    data object Register : Screen
    data object Admin : Screen
    data class Event(val id: Id) : Screen
    data class ManageEvent(val id: Id) : Screen
    /** Форма мероприятия: новое, правка своего ([editId]) или повтор прошлого ([repeatId]). */
    data class EventForm(val editId: Id? = null, val repeatId: Id? = null, val organizationId: Id? = null) : Screen
    data class Organization(val id: Id) : Screen
    data class User(val id: Id) : Screen
}

enum class Tab(val label: String, val icon: String, val root: Screen) {
    Home("Главная", "⌂", Screen.Home),
    Events("События", "☰", Screen.Events),
    Create("Создать", "+", Screen.CreateEvent),
    Mine("Мои записи", "▦", Screen.MyParticipations),
    Profile("Профиль", "◉", Screen.Profile),
}

/** Стек экранов на вкладку: переключение вкладки начинает стек заново, как переход по нижней панели сайта. */
class Navigator {
    var tab by mutableStateOf(Tab.Home)
        private set
    val stack = mutableStateListOf<Screen>(Screen.Home)
    val current: Screen get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    fun select(tab: Tab, root: Screen = tab.root) {
        this.tab = tab
        stack.clear()
        stack.add(root)
    }

    fun push(screen: Screen) {
        if (stack.lastOrNull() != screen) stack.add(screen)
    }

    /** Заменить текущий экран — после создания мероприятия открываем его вместо формы. */
    fun replace(screen: Screen) {
        stack.removeAt(stack.lastIndex)
        stack.add(screen)
    }

    fun back(): Boolean {
        if (!canGoBack) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}

class AppState(val api: Api, val session: Session, val nav: Navigator)

val LocalAppState = staticCompositionLocalOf<AppState> { error("AppState не задан") }

/** Результат загрузки для экрана: данные, ошибка и перезапрос. */
class Load<T> internal constructor() {
    var data by mutableStateOf<T?>(null)
        internal set
    var error by mutableStateOf<String?>(null)
        internal set
    var loading by mutableStateOf(true)
        internal set
    /** HTTP-статус последней ошибки (0 — нет связи): 404 и 410 экраны показывают по-своему. */
    var errorStatus by mutableIntStateOf(0)
        internal set
    internal var version by mutableIntStateOf(0)

    fun reload() {
        version++
    }
}

@Composable
fun <T> rememberLoad(vararg keys: Any?, block: suspend () -> T): Load<T> {
    val load = remember(*keys) { Load<T>() }
    LaunchedEffect(load, load.version) {
        load.loading = true
        load.error = null
        try {
            load.data = block()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            load.errorStatus = (e as? com.crzto3.go2gether.data.ApiException)?.status ?: 0
            load.error = e.userMessage()
        } finally {
            load.loading = false
        }
    }
    return load
}
