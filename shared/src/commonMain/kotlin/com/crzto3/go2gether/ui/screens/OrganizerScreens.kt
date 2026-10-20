package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.ApiException
import com.crzto3.go2gether.data.CreateEventRequest
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.Participant
import com.crzto3.go2gether.data.Session
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
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.components.Pill
import com.crzto3.go2gether.ui.components.SeatsBar
import com.crzto3.go2gether.ui.components.SectionTitle
import com.crzto3.go2gether.ui.components.StatusBadge
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.components.participationStatus
import com.crzto3.go2gether.ui.formatInputDate
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Type
import com.crzto3.go2gether.ui.toIso
import com.crzto3.go2gether.ui.today
import kotlinx.coroutines.launch

@Composable
fun MyEventsScreen() {
    val state = LocalAppState.current
    val events = rememberLoad { state.api.myEvents() }
    ScreenScaffold(title = "Мои мероприятия") {
        when {
            events.loading && events.data == null -> block { Loading() }
            events.error != null -> block { ErrorBlock(events.error!!) { events.reload() } }
            events.data.isNullOrEmpty() -> block {
                EmptyBlock("Вы ещё ничего не организовали", "Соберите компанию — это пара минут.") {
                    AppButton("Создать мероприятие", variant = ButtonVariant.Accent, onClick = { state.nav.push(Screen.EventForm()) })
                }
            }
            else -> {
                // Сначала те, где ждут ответа, потом по дате; архив — прошедшие, снятые и отменённые.
                val all = events.data!!.sortedWith(
                    compareByDescending<com.crzto3.go2gether.data.OrganizerEvent> { it.pendingCount > 0 }.thenBy { it.event.startsAt },
                )
                val upcoming = all.filter { it.event.status == "published" && !it.event.isPast }
                val archive = all - upcoming.toSet()
                listOf("Предстоящие" to upcoming, "Архив" to archive).filter { it.second.isNotEmpty() }.forEach { (title, list) ->
                    block(title) { SectionTitle(title) }
                    list.forEach { item ->
                        block(item.event.id) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EventCard(item.event, onClick = { state.nav.push(Screen.Event(item.event.id)) }, badge = {
                                    if (item.pendingCount > 0) Pill("Заявок: ${item.pendingCount}", Brand.WarningBg, Brand.WarningFg)
                                    if (item.waitlistedCount > 0) Pill("В очереди: ${item.waitlistedCount}", Brand.QueueBg, Brand.QueueFg)
                                })
                                if (title == "Архив") {
                                    AppButton("Повторить", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.EventForm(repeatId = item.event.id)) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class ManageTab(val label: String) { Requests("Заявки"), Participants("Участники"), Rejected("Отказы") }

private val REJECT_REASONS = listOf("Группа уже собрана", "Не подходит уровень", "Другое время")

/** Заявки и участники — как EventManageView: вкладки «Заявки», «Участники», «Отказы». */
@Composable
fun ManageEventScreen(id: Id) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val participants = rememberLoad(id) { state.api.participants(id) }
    val requests = rememberLoad(id) {
        (state.api.requests(id, "pending") + state.api.requests(id, "waitlist")).sortedBy { it.createdAt.orEmpty() }
    }
    val rejected = rememberLoad(id) { state.api.requests(id, "rejected").sortedByDescending { it.createdAt.orEmpty() } }
    var tab by rememberSaveable { mutableStateOf(ManageTab.Requests) }
    var rejecting by remember { mutableStateOf<Participant?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun act(block: suspend () -> Unit) {
        error = null
        scope.launch {
            try {
                block()
            } catch (e: Exception) {
                error = e.userMessage()
            }
            participants.reload()
            requests.reload()
            rejected.reload()
        }
    }

    ScreenScaffold(title = "Заявки и участники") {
        block {
            TagsRow {
                ManageTab.entries.forEach { t ->
                    val n = when (t) {
                        ManageTab.Requests -> requests.data?.size
                        ManageTab.Participants -> participants.data?.participants?.count { it.status == "confirmed" }
                        ManageTab.Rejected -> rejected.data?.size
                    }
                    FilterChip(if (n != null) "${t.label} · $n" else t.label, tab == t) { tab = t }
                }
            }
        }
        error?.let { block { Notice(it, ok = false) } }
        participants.data?.let { list -> block { AppCard { SeatsBar(list.count, list.capacity) } } }
        when (tab) {
            ManageTab.Requests -> when {
                requests.loading && requests.data == null -> block { Loading() }
                requests.error != null -> block { ErrorBlock(requests.error!!) { requests.reload() } }
                requests.data.isNullOrEmpty() -> block { EmptyBlock("Новых заявок нет") }
                else -> requests.data!!.forEach { p ->
                    block("r-${p.participationId}") {
                        ParticipantCard(p) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppButton("Принять", Modifier.weight(1f), variant = ButtonVariant.Success, onClick = { act { state.api.approve(p.participationId) } })
                                AppButton("Отклонить", Modifier.weight(1f), variant = ButtonVariant.Ghost, onClick = { rejecting = p })
                            }
                        }
                    }
                }
            }
            ManageTab.Participants -> {
                val list = participants.data
                when {
                    participants.loading && list == null -> block { Loading() }
                    participants.error != null -> block { ErrorBlock(participants.error!!) { participants.reload() } }
                    list != null -> {
                        val confirmed = list.participants.filter { it.status == "confirmed" }
                        if (confirmed.isEmpty()) block { EmptyBlock("Пока никто не записался") }
                        confirmed.forEach { p -> block("p-${p.participationId}") { ParticipantCard(p) {} } }
                    }
                }
            }
            ManageTab.Rejected -> when {
                rejected.loading && rejected.data == null -> block { Loading() }
                rejected.error != null -> block { ErrorBlock(rejected.error!!) { rejected.reload() } }
                rejected.data.isNullOrEmpty() -> block { EmptyBlock("Отказов нет") }
                else -> rejected.data!!.forEach { p ->
                    block("x-${p.participationId}") {
                        ParticipantCard(p) {
                            p.decisionReason?.let { Text("Причина: «$it»", style = Type.body, color = AppTheme.colors.ink2) }
                        }
                    }
                }
            }
        }
    }

    rejecting?.let { p -> RejectDialog(p, onDismiss = { rejecting = null }) { reason ->
        rejecting = null
        act { state.api.reject(p.participationId, reason) }
    } }
}

/** Отказ: готовые причины и комментарий — всё необязательно. */
@Composable
private fun RejectDialog(p: Participant, onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var preset by remember { mutableStateOf<String?>(null) }
    var comment by remember { mutableStateOf("") }
    val c = AppTheme.colors
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.card,
        title = { Text("Отклонить заявку: ${p.user.name}", style = Type.h2, color = c.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TagsRow { REJECT_REASONS.forEach { r -> FilterChip(r, preset == r) { preset = if (preset == r) null else r } } }
                AppTextField(comment, { comment = it }, "Комментарий", singleLine = false, minLines = 2)
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                onConfirm(listOfNotNull(preset, comment.trim().ifBlank { null }).joinToString(". ").ifBlank { null })
            }) { Text("Отклонить", color = c.ink, style = Type.bodyBold) }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Отмена", color = c.ink2) } },
    )
}

@Composable
private fun ParticipantCard(p: Participant, actions: @Composable () -> Unit) {
    val state = LocalAppState.current
    val c = AppTheme.colors
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(p.user.name, p.user.id)
            Column(Modifier.weight(1f)) {
                Text(p.user.name, style = Type.bodyBold, color = c.ink)
                p.email?.let { Text(it, style = Type.small, color = c.ink2) }
            }
            StatusBadge(participationStatus(p.status))
        }
        p.createdAt?.let { Text("Подана ${com.crzto3.go2gether.ui.formatDateTime(it)}", style = Type.small, color = c.ink2) }
        p.message?.takeIf { it.isNotBlank() }?.let { Text("«$it»", style = Type.body, color = c.ink2) }
        androidx.compose.material3.TextButton(onClick = { state.nav.push(Screen.User(p.user.id)) }) {
            Text("Профиль", color = c.accentInk)
        }
        actions()
    }
}
