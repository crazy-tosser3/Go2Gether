package com.crzto3.go2gether.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crzto3.go2gether.data.Api
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.screens.AdminScreen
import com.crzto3.go2gether.ui.screens.EventFormScreen
import com.crzto3.go2gether.ui.screens.EditProfileScreen
import com.crzto3.go2gether.ui.screens.EventScreen
import com.crzto3.go2gether.ui.screens.EventsScreen
import com.crzto3.go2gether.ui.screens.HomeScreen
import com.crzto3.go2gether.ui.screens.LoginScreen
import com.crzto3.go2gether.ui.screens.ManageEventScreen
import com.crzto3.go2gether.ui.screens.MyEventsScreen
import com.crzto3.go2gether.ui.screens.MyOrganizationsScreen
import com.crzto3.go2gether.ui.screens.MyParticipationsScreen
import com.crzto3.go2gether.ui.screens.NotificationsScreen
import com.crzto3.go2gether.ui.screens.OrganizationScreen
import com.crzto3.go2gether.ui.screens.OrganizationsScreen
import com.crzto3.go2gether.ui.screens.ProfileScreen
import com.crzto3.go2gether.ui.screens.RegisterScreen
import com.crzto3.go2gether.ui.screens.UserScreen
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Go2GetherTheme
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
fun App() {
    val state = remember {
        val api = Api()
        AppState(api, Session(api), Navigator())
    }
    LaunchedEffect(Unit) { state.session.restore() }

    Go2GetherTheme {
        androidx.compose.runtime.CompositionLocalProvider(LocalAppState provides state) {
            val sessionState by state.session.state.collectAsState()
            Box(Modifier.fillMaxSize().background(AppTheme.colors.page)) {
                if (sessionState is Session.State.Loading) {
                    Loading(Modifier.fillMaxSize())
                } else {
                    MainLayout(state.nav, sessionState)
                }
            }
        }
    }
}

@Composable
private fun MainLayout(nav: Navigator, session: Session.State) {
    val holder = rememberSaveableStateHolder()
    PlatformBackHandler(enabled = nav.canGoBack) { nav.back() }

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val screen = nav.current
            // Ключ — экран и его глубина в стеке: при возврате восстанавливается прокрутка.
            holder.SaveableStateProvider("${nav.tab}/${nav.stack.size}/$screen") {
                ScreenContent(screen)
            }
        }
        TabBar(nav, isAdmin = (session as? Session.State.Authorized)?.user?.isAdmin == true)
    }
}

@Composable
private fun ScreenContent(screen: Screen) {
    when (screen) {
        Screen.Home -> HomeScreen()
        Screen.Events -> EventsScreen()
        Screen.CreateEvent -> RequireAuth { EventFormScreen() }
        Screen.MyParticipations -> RequireAuth { MyParticipationsScreen() }
        Screen.Profile -> RequireAuth { ProfileScreen() }
        Screen.EditProfile -> RequireAuth { EditProfileScreen() }
        Screen.MyEvents -> RequireAuth { MyEventsScreen() }
        Screen.MyOrganizations -> RequireAuth { MyOrganizationsScreen() }
        Screen.Notifications -> RequireAuth { NotificationsScreen() }
        Screen.Admin -> RequireAuth { AdminScreen() }
        Screen.Organizations -> OrganizationsScreen()
        Screen.Login -> LoginScreen()
        Screen.Register -> RegisterScreen()
        is Screen.Event -> EventScreen(screen.id)
        is Screen.ManageEvent -> RequireAuth { ManageEventScreen(screen.id) }
        is Screen.EventForm -> RequireAuth { EventFormScreen(screen.editId, screen.repeatId, screen.organizationId) }
        is Screen.Organization -> OrganizationScreen(screen.id)
        is Screen.User -> UserScreen(screen.id)
    }
}

/** Разделы для вошедших: гостю на их месте показываем вход, после входа экран откроется сам. */
@Composable
fun RequireAuth(content: @Composable () -> Unit) {
    val state = LocalAppState.current
    val session by state.session.state.collectAsState()
    if (session is Session.State.Authorized) content() else LoginScreen(embedded = true)
}

@Composable
private fun TabBar(nav: Navigator, isAdmin: Boolean) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .shadow(16.dp, Radius.lg)
            .clip(Radius.lg)
            .background(c.card)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Tab.entries.forEach { tab ->
            val selected = nav.tab == tab
            Column(
                Modifier.weight(1f).clip(Radius.md).clickable {
                    if (tab == Tab.Create && isAdmin) nav.select(tab, Screen.Admin) else nav.select(tab)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (tab == Tab.Create) {
                    // Администратор не создаёт мероприятия — в центре у него админка.
                    Box(
                        Modifier.offset(y = (-6).dp).size(52.dp).clip(CircleShape)
                            .background(if (isAdmin) c.ink else Brand.Orange),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (isAdmin) "⛨" else "+",
                            color = if (isAdmin) c.onInk else Brand.OnBrand,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(if (isAdmin) "Админка" else tab.label, style = Type.tiny, fontWeight = FontWeight.Bold, color = c.ink)
                } else {
                    Text(tab.icon, fontSize = 20.sp, color = if (selected) c.ink else c.ink2)
                    Text(
                        tab.label,
                        style = Type.tiny,
                        color = if (selected) c.ink else c.ink2,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Каркас экрана: шапка со стрелкой назад и прокручиваемый список содержимого. */
@Composable
fun ScreenScaffold(
    title: String? = null,
    actions: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val nav = LocalAppState.current.nav
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize()) {
        if (title != null || nav.canGoBack) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (nav.canGoBack) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(c.card).clickable { nav.back() },
                        contentAlignment = Alignment.Center,
                    ) { Text("‹", fontSize = 30.sp, color = c.ink, modifier = Modifier.offset(y = (-2).dp)) }
                }
                Text(
                    (title ?: "").uppercase(),
                    Modifier.weight(1f).padding(horizontal = 10.dp),
                    style = Type.h2,
                    color = c.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                actions?.invoke()
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

/** Ограничение ширины на планшете — карточки не растягиваются на весь экран. */
val ContentWidth = Modifier.widthIn(max = 640.dp).fillMaxWidth()

/** Элемент экрана по ширине контента. */
fun LazyListScope.block(key: Any? = null, content: @Composable () -> Unit) {
    item(key) { Box(ContentWidth) { content() } }
}
