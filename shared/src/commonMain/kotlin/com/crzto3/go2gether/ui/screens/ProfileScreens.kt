package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.ApiException
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.MyParticipation
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.data.UpdateProfileRequest
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.LEVEL_LABELS
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.Avatar
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.ConfirmDialog
import com.crzto3.go2gether.ui.components.EmptyBlock
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.EventCard
import com.crzto3.go2gether.ui.components.FilterChip
import com.crzto3.go2gether.ui.components.InfoRow
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.components.Pill
import com.crzto3.go2gether.ui.components.SectionTitle
import com.crzto3.go2gether.ui.components.StatusBadge
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.components.organizationStatus
import com.crzto3.go2gether.ui.components.participationStatus
import com.crzto3.go2gether.ui.formatDateTime
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

private enum class MineTab(val label: String) { Upcoming("Предстоящие"), Pending("Ждут решения"), Rejected("Отказы"), Archive("Архив") }

/** Раскладка по вкладкам — как features/events/myParticipations.ts. */
private fun tabOf(item: MyParticipation): MineTab {
    if (item.event.status != "published" || item.event.isPast) return MineTab.Archive
    return when (item.participation.status) {
        "confirmed" -> MineTab.Upcoming
        "pending", "waitlist" -> MineTab.Pending
        "rejected" -> MineTab.Rejected
        else -> MineTab.Archive
    }
}

@Composable
fun MyParticipationsScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val list = rememberLoad { state.api.myParticipations() }
    var tab by rememberSaveable { mutableStateOf(MineTab.Upcoming) }
    var cancelling by remember { mutableStateOf<MyParticipation?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val groups = list.data.orEmpty().groupBy(::tabOf).mapValues { (t, items) ->
        if (t == MineTab.Archive) items.sortedByDescending { it.event.startsAt } else items.sortedBy { it.event.startsAt }
    }

    ScreenScaffold(title = "Мои записи") {
        block {
            TagsRow {
                MineTab.entries.forEach { t ->
                    val n = groups[t]?.size ?: 0
                    FilterChip(if (n > 0) "${t.label} · $n" else t.label, tab == t) { tab = t }
                }
            }
        }
        error?.let { block { Notice(it, ok = false) } }
        when {
            list.loading && list.data == null -> block { Loading() }
            list.error != null -> block { ErrorBlock(list.error!!) { list.reload() } }
            else -> {
                val items = groups[tab].orEmpty()
                if (items.isEmpty()) {
                    block {
                        EmptyBlock("Здесь пока пусто", "Найдите мероприятие и запишитесь — оно появится здесь.") {
                            AppButton("К мероприятиям", onClick = { state.nav.select(com.crzto3.go2gether.ui.Tab.Events) })
                        }
                    }
                }
                items.forEach { item ->
                    block(item.participation.id) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EventCard(item.event, onClick = { state.nav.push(Screen.Event(item.event.id)) }, badge = {
                                StatusBadge(participationStatus(item.participation.status))
                            })
                            // Снятое мероприятие остаётся в записях — рядом причина снятия.
                            if (item.event.status != "published") {
                                Notice(
                                    com.crzto3.go2gether.ui.components.eventStatus(item.event.status).second +
                                        (item.event.unavailableReason?.let { ": $it" } ?: ""),
                                    ok = false,
                                )
                            }
                            item.participation.decisionReason?.takeIf { item.participation.status == "rejected" }?.let {
                                Text("Причина: «$it»", style = Type.body, color = AppTheme.colors.ink2)
                            }
                            val s = item.participation.status
                            if ((s == "confirmed" || s == "pending" || s == "waitlist") && item.event.status == "published" && !item.event.isPast) {
                                AppButton(if (s == "confirmed") "Отменить запись" else "Отозвать заявку", variant = ButtonVariant.Ghost, onClick = { cancelling = item })
                            }
                        }
                    }
                }
            }
        }
    }

    cancelling?.let { item ->
        ConfirmDialog(
            title = if (item.participation.status == "confirmed") "Отменить запись?" else "Отозвать заявку?",
            text = "Место освободится для других. Записаться снова можно, пока мероприятие не началось.",
            confirm = "Да, отменить",
            dismiss = "Не отменять",
            onConfirm = {
                cancelling = null
                scope.launch {
                    error = runCatching { state.api.leave(item.event.id) }.exceptionOrNull()?.userMessage()
                    list.reload()
                }
            },
            onDismiss = { cancelling = null },
        )
    }
}

@Composable
fun ProfileScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val session by state.session.state.collectAsState()
    val auth = session as? Session.State.Authorized ?: return
    val user = auth.user
    val c = AppTheme.colors
    val sports = rememberLoad { state.api.sports() }
    var logoutOpen by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Профиль") {
        block {
            Column(
                Modifier.fillMaxWidth().clip(Radius.card).background(Brand.Lilac).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Avatar(user.name, user.id, 64.dp)
                    Column(Modifier.weight(1f)) {
                        Text(user.name.uppercase(), style = Type.h2, color = Brand.OnBrand)
                        Text(user.email, style = Type.small, color = Brand.OnBrand)
                    }
                }
                TagsRow {
                    if (user.isAdmin) Pill("Администратор", Brand.OnBrand, androidx.compose.ui.graphics.Color.White)
                    user.city?.let { Pill("📍 $it", androidx.compose.ui.graphics.Color.White, Brand.OnBrand) }
                    user.level?.let { LEVEL_LABELS[it] }?.let { Pill(it, androidx.compose.ui.graphics.Color.White, Brand.OnBrand) }
                    if (user.isDemo) Pill("Демо", androidx.compose.ui.graphics.Color.White, Brand.OnBrand)
                }
                user.bio?.takeIf { it.isNotBlank() }?.let { Text(it, style = Type.body, color = Brand.OnBrand) }
            }
        }
        val mySports = sports.data.orEmpty().filter { it.id in user.sportIds }
        if (mySports.isNotEmpty()) {
            block { AppCard { Text("Виды спорта", style = Type.bodyBold, color = c.ink); TagsRow { mySports.forEach { Pill(it.name) } } } }
        }
        block {
            AppCard {
                MenuRow("Редактировать профиль") { state.nav.push(Screen.EditProfile) }
                MenuRow("Уведомления") { state.nav.push(Screen.Notifications) }
                if (!user.isAdmin) {
                    MenuRow("Мои мероприятия") { state.nav.push(Screen.MyEvents) }
                    MenuRow("Моя организация") { state.nav.push(Screen.MyOrganizations) }
                } else {
                    MenuRow("Админка") { state.nav.push(Screen.Admin) }
                }
                MenuRow("Организации") { state.nav.push(Screen.Organizations) }
            }
        }
        if (auth.organizations.isNotEmpty()) {
            block {
                AppCard {
                    Text("Организации", style = Type.bodyBold, color = c.ink)
                    auth.organizations.forEach { o ->
                        Row(
                            Modifier.fillMaxWidth().clickable { state.nav.push(Screen.Organization(o.id)) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(o.name, Modifier.weight(1f), style = Type.body, color = c.ink)
                            o.status?.let { StatusBadge(organizationStatus(it)) }
                        }
                    }
                }
            }
        }
        block {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (user.isDemo) {
                    AppButton("Сменить роль", variant = ButtonVariant.Ghost, onClick = {
                        scope.launch {
                            state.session.logout()
                            state.nav.select(com.crzto3.go2gether.ui.Tab.Profile, Screen.Login)
                        }
                    })
                }
                AppButton("Выйти", variant = ButtonVariant.Ghost, onClick = { logoutOpen = true })
            }
        }
    }

    if (logoutOpen) {
        ConfirmDialog(
            title = "Выйти из аккаунта?",
            text = null,
            confirm = "Выйти",
            onConfirm = {
                logoutOpen = false
                scope.launch {
                    state.session.logout()
                    state.nav.select(com.crzto3.go2gether.ui.Tab.Home)
                }
            },
            onDismiss = { logoutOpen = false },
        )
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(Radius.md).background(c.field).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), style = Type.bodyBold, color = c.ink)
        Text("›", style = Type.h2, color = c.ink2)
    }
}

@Composable
fun EditProfileScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val user = state.session.user ?: return
    val sports = rememberLoad { state.api.sports() }
    var name by rememberSaveable { mutableStateOf(user.name) }
    var city by rememberSaveable { mutableStateOf(user.city.orEmpty()) }
    var bio by rememberSaveable { mutableStateOf(user.bio.orEmpty()) }
    var level by rememberSaveable { mutableStateOf(user.level) }
    var sportIds by remember { mutableStateOf(user.sportIds.toSet()) }
    var saving by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var fields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    ScreenScaffold(title = "Редактирование") {
        block {
            AppCard {
                AppTextField(name, { name = it }, "Имя", error = fields["name"])
                AppTextField(city, { city = it }, "Город", error = fields["city"])
                AppTextField(bio, { bio = it }, "О себе", singleLine = false, minLines = 3, error = fields["bio"])
                Text("Уровень", style = Type.smallBold, color = AppTheme.colors.ink)
                TagsRow { LEVEL_LABELS.forEach { (k, v) -> FilterChip(v, level == k) { level = k } } }
                Text("Виды спорта", style = Type.smallBold, color = AppTheme.colors.ink)
                when {
                    sports.loading -> Loading()
                    sports.error != null -> ErrorBlock(sports.error!!) { sports.reload() }
                    else -> TagsRow {
                        sports.data.orEmpty().forEach { s ->
                            FilterChip(s.name, s.id in sportIds) { sportIds = if (s.id in sportIds) sportIds - s.id else sportIds + s.id }
                        }
                    }
                }
                result?.let { Notice(it.second, it.first) }
                AppButton("Сохранить", loading = saving, onClick = {
                    if (name.isBlank()) {
                        fields = mapOf("name" to "Введите имя")
                        return@AppButton
                    }
                    saving = true
                    result = null
                    fields = emptyMap()
                    scope.launch {
                        try {
                            val updated = state.api.updateMe(
                                UpdateProfileRequest(name.trim(), city.trim(), bio.trim(), level, sportIds.toList()),
                            )
                            state.session.updateUser(updated)
                            result = true to "Профиль сохранён."
                        } catch (e: Exception) {
                            fields = (e as? ApiException)?.fields.orEmpty()
                            result = false to e.userMessage()
                        } finally {
                            saving = false
                        }
                    }
                })
            }
        }
    }
}

@Composable
fun UserScreen(id: Id) {
    val state = LocalAppState.current
    val user = rememberLoad(id) { state.api.user(id) }
    val c = AppTheme.colors
    ScreenScaffold(title = "Профиль") {
        val u = user.data
        when {
            u == null && user.loading -> block { Loading() }
            u == null -> block { ErrorBlock(user.error ?: "Не найдено") { user.reload() } }
            else -> {
                block {
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Avatar(u.name, u.id, 64.dp)
                            Column {
                                Text(u.name.uppercase(), style = Type.h2, color = c.ink)
                                u.city?.let { Text("📍 $it", style = Type.small, color = c.ink2) }
                            }
                        }
                        TagsRow {
                            u.level?.let { LEVEL_LABELS[it] }?.let { Pill(it) }
                            u.sports.forEach { Pill(it.name) }
                        }
                        u.bio?.takeIf { it.isNotBlank() }?.let { Text(it, style = Type.body, color = c.ink) }
                    }
                }
                if (u.createdEvents.isNotEmpty()) {
                    block { SectionTitle("Организует") }
                    u.createdEvents.forEach { e -> block(e.id) { EventCard(e, onClick = { state.nav.push(Screen.Event(e.id)) }) } }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val list = rememberLoad { state.api.notifications() }
    val c = AppTheme.colors
    ScreenScaffold(title = "Уведомления") {
        when {
            list.loading && list.data == null -> block { Loading() }
            list.error != null -> block { ErrorBlock(list.error!!) { list.reload() } }
            list.data.isNullOrEmpty() -> block { EmptyBlock("Уведомлений нет") }
            else -> list.data!!.forEach { n ->
                block(n.id) {
                    AppCard(
                        Modifier.clickable {
                            scope.launch {
                                if (!n.isRead) runCatching { state.api.readNotification(n.id) }
                                n.eventId?.let { state.nav.push(Screen.Event(it)) } ?: list.reload()
                            }
                        },
                        color = if (n.isRead) c.card else Brand.Cream,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!n.isRead) androidx.compose.foundation.layout.Box(Modifier.size(10.dp).clip(CircleShape).background(Brand.Signal))
                            Text(n.title, style = Type.bodyBold, color = if (n.isRead) c.ink else Brand.OnBrand)
                        }
                        n.body?.let { Text(it, style = Type.body, color = if (n.isRead) c.ink2 else Brand.OnBrand) }
                        Text(formatDateTime(n.createdAt), style = Type.tiny, color = if (n.isRead) c.ink2 else Brand.OnBrand)
                    }
                }
            }
        }
    }
}

@Composable
internal fun InfoCard(vararg rows: Pair<String, String?>) {
    AppCard { rows.forEach { (l, v) -> InfoRow(l, v) } }
}
