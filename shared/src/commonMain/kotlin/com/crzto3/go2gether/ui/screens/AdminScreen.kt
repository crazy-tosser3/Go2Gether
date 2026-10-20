package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.ORG_TYPE_LABELS
import com.crzto3.go2gether.ui.REPORT_REASON_LABELS
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.ConfirmDialog
import com.crzto3.go2gether.ui.components.EmptyBlock
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.FilterChip
import com.crzto3.go2gether.ui.components.InfoRow
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.components.StatusBadge
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.components.eventStatus
import com.crzto3.go2gether.ui.components.organizationStatus
import com.crzto3.go2gether.ui.formatDateTime
import com.crzto3.go2gether.ui.formatEventWhen
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

private enum class AdminTab(val label: String) {
    Dashboard("Обзор"), Organizations("Организации"), Events("Мероприятия"), Reports("Жалобы")
}

private val ORG_FILTERS = listOf("pending" to "Ждут решения", "approved" to "Допущены", "rejected" to "Отклонены", null to "Все")
private val EVENT_FILTERS = listOf("published" to "Опубликованы", "unpublished" to "Сняты", "deleted" to "Удалены", null to "Все")

/** Действие модерации, ждущее причины в диалоге. */
private data class Pending(val kind: String, val id: Id, val title: String)

/** Причину увидят организатор и участники, поэтому она обязательна и осмысленна (features/admin/events.ts). */
private fun reasonError(text: String): String? = when {
    text.isBlank() -> "Напишите причину"
    text.trim().length < 5 -> "Причина слишком короткая"
    text.trim().length > 500 -> "Не длиннее 500 символов"
    else -> null
}

/** Админка: обзор, организации, мероприятия, жалобы — как разделы /admin на сайте. */
@Composable
fun AdminScreen() {
    val state = LocalAppState.current
    val user = state.session.user
    if (user?.isAdmin != true) {
        ScreenScaffold(title = "Админка") { block { EmptyBlock("Раздел только для администратора") } }
        return
    }
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(AdminTab.Dashboard) }
    var orgFilter by rememberSaveable { mutableStateOf<String?>("pending") }
    var eventFilter by rememberSaveable { mutableStateOf<String?>("published") }
    var eventQuery by rememberSaveable { mutableStateOf("") }
    var eventApplied by rememberSaveable { mutableStateOf("") }
    val dashboard = rememberLoad { state.api.adminDashboard() }
    val orgs = rememberLoad(orgFilter) { state.api.adminOrganizations(orgFilter).items }
    val events = rememberLoad(eventFilter, eventApplied) { state.api.adminEvents(eventFilter, eventApplied.ifBlank { null }).items }
    val reports = rememberLoad { state.api.adminReports("pending").items }
    var dialog by remember { mutableStateOf<Pending?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    fun act(done: String, block: suspend () -> Unit) {
        error = null
        notice = null
        scope.launch {
            try {
                block()
                notice = done
            } catch (e: Exception) {
                error = e.userMessage()
            }
            orgs.reload(); events.reload(); reports.reload(); dashboard.reload()
        }
    }

    ScreenScaffold(title = "Админка") {
        block { TagsRow { AdminTab.entries.forEach { t -> FilterChip(t.label, tab == t) { tab = t } } } }
        notice?.let { block { Notice(it) } }
        error?.let { block { Notice(it, ok = false) } }
        when (tab) {
            AdminTab.Dashboard -> {
                val d = dashboard.data
                when {
                    d == null && dashboard.loading -> block { Loading() }
                    d == null -> block { ErrorBlock("Не получилось загрузить сводку.") { dashboard.reload() } }
                    else -> listOf(
                        listOf("ждут допуска" to d.pendingOrganizations, "новых жалоб" to d.pendingReports),
                        listOf("опубликовано" to d.publishedEvents, "пользователей" to d.users),
                        listOf("снято с публикации" to d.unpublishedEvents, "отменено организаторами" to d.cancelledEvents),
                        listOf("организаций допущено" to d.approvedOrganizations, "скрыто сообщений" to d.hiddenComments),
                    ).forEachIndexed { i, row ->
                        block("d$i") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { (label, value) ->
                                    val hot = i == 0 && value > 0
                                    Column(Modifier.weight(1f)) {
                                        AppCard(color = if (hot) Brand.Cream else AppTheme.colors.card) {
                                            Text(value.toString(), style = Type.display, color = if (hot) Brand.OnBrand else AppTheme.colors.ink)
                                            Text(label, style = Type.small, color = if (hot) Brand.OnBrand else AppTheme.colors.ink2)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.Organizations -> {
                block { TagsRow { ORG_FILTERS.forEach { (k, v) -> FilterChip(v, orgFilter == k) { orgFilter = k } } } }
                when {
                    orgs.loading && orgs.data == null -> block { Loading() }
                    orgs.error != null -> block { ErrorBlock(orgs.error!!) { orgs.reload() } }
                    orgs.data.isNullOrEmpty() -> block { EmptyBlock("Организаций нет") }
                    else -> orgs.data!!.forEach { o ->
                        block("o${o.id}") {
                            AppCard {
                                Text(o.name, style = Type.cardTitle, color = AppTheme.colors.ink)
                                StatusBadge(organizationStatus(o.status))
                                Text(listOfNotNull(ORG_TYPE_LABELS[o.type], o.city).joinToString(" · "), style = Type.small, color = AppTheme.colors.ink2)
                                InfoRow("Владелец", o.owner?.name)
                                InfoRow("Контакты", o.contacts)
                                InfoRow("Подана", formatDateTime(o.createdAt))
                                InfoRow("О себе", o.description)
                                InfoRow("Причина отказа", o.rejectReason)
                                if (o.status == "pending") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AppButton("Допустить", Modifier.weight(1f), variant = ButtonVariant.Success, onClick = {
                                            act("Организация допущена.") { state.api.approveOrganization(o.id) }
                                        })
                                        AppButton("Отклонить", Modifier.weight(1f), variant = ButtonVariant.Ghost, onClick = {
                                            dialog = Pending("org", o.id, o.name)
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.Events -> {
                block {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTextField(eventQuery, { eventQuery = it }, "Поиск по названию", Modifier.weight(1f))
                            Box(Modifier.width(110.dp)) { AppButton("Найти", onClick = { eventApplied = eventQuery.trim() }) }
                        }
                        TagsRow { EVENT_FILTERS.forEach { (k, v) -> FilterChip(v, eventFilter == k) { eventFilter = k } } }
                    }
                }
                when {
                    events.loading && events.data == null -> block { Loading() }
                    events.error != null -> block { ErrorBlock(events.error!!) { events.reload() } }
                    events.data.isNullOrEmpty() -> block { EmptyBlock("Мероприятий нет") }
                    else -> events.data!!.forEach { e ->
                        block("e${e.id}") {
                            AppCard {
                                Text(e.title, style = Type.cardTitle, color = AppTheme.colors.ink)
                                StatusBadge(eventStatus(e.status))
                                Text("${formatEventWhen(e.startsAt)} · ${e.organizer.name}", style = Type.small, color = AppTheme.colors.ink2)
                                e.unavailableReason?.let { Text("Причина: $it", style = Type.body, color = AppTheme.colors.ink) }
                                AppButton("Открыть", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Event(e.id)) })
                                // Что можно сделать в этом статусе — по правилам бэка (moderationActions).
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    when (e.status) {
                                        "published" -> {
                                            AppButton("Снять", Modifier.weight(1f), onClick = { dialog = Pending("unpublish", e.id, e.title) })
                                            AppButton("Удалить", Modifier.weight(1f), variant = ButtonVariant.Danger, onClick = { dialog = Pending("delete", e.id, e.title) })
                                        }
                                        "unpublished" -> {
                                            AppButton("Вернуть", Modifier.weight(1f), variant = ButtonVariant.Success, onClick = {
                                                act("Мероприятие снова опубликовано.") { state.api.restoreEvent(e.id) }
                                            })
                                            AppButton("Удалить", Modifier.weight(1f), variant = ButtonVariant.Danger, onClick = { dialog = Pending("delete", e.id, e.title) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.Reports -> when {
                reports.loading && reports.data == null -> block { Loading() }
                reports.error != null -> block { ErrorBlock("Не получилось загрузить жалобы.") { reports.reload() } }
                reports.data.isNullOrEmpty() -> block { EmptyBlock("Жалоб нет") }
                else -> reports.data!!.forEach { r ->
                    block("r${r.id}") {
                        AppCard {
                            Text(REPORT_REASON_LABELS[r.reason] ?: r.reason, style = Type.bodyBold, color = AppTheme.colors.ink)
                            InfoRow("Пожаловался", r.reporter.name)
                            InfoRow("Подана", formatDateTime(r.createdAt))
                            InfoRow("Комментарий", r.comment)
                            Text("${r.event.title} · ${formatEventWhen(r.event.startsAt)}", style = Type.smallBold, color = AppTheme.colors.accentInk)
                            AppButton("Открыть мероприятие", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Event(r.event.id)) })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppButton("Принять и снять", Modifier.weight(1f), onClick = { dialog = Pending("accept", r.id, r.event.title) })
                                AppButton("Отклонить", Modifier.weight(1f), variant = ButtonVariant.Ghost, onClick = { dialog = Pending("rejectReport", r.id, r.event.title) })
                            }
                        }
                    }
                }
            }
        }
    }

    dialog?.let { p ->
        val (title, text, confirm) = when (p.kind) {
            "org" -> Triple("Отклонить «${p.title}»?", "Владелец увидит причину и сможет исправить заявку.", "Отклонить")
            "unpublish" -> Triple("Снять с публикации?", "Мероприятие пропадёт из ленты, организатор и участники увидят причину.", "Снять")
            "delete" -> Triple("Удалить мероприятие?", "Вернуть удалённое через приложение нельзя. Организатор и участники увидят причину.", "Удалить")
            "accept" -> Triple(
                "Принять жалобу и снять мероприятие?",
                "Мероприятие пропадёт из ленты, организатор и участники увидят причину. Остальные жалобы на него закроются.",
                "Принять и снять",
            )
            else -> Triple("Отклонить жалобу?", "Мероприятие останется опубликованным, автор жалобы получит ваш ответ.", "Отклонить")
        }
        val optional = p.kind == "rejectReport"
        ConfirmDialog(
            title = title,
            text = text,
            confirm = confirm,
            inputLabel = if (optional) "Ответ автору жалобы (необязательно)" else "Причина",
            inputRequired = !optional,
            onConfirm = { reason ->
                if (!optional) reasonError(reason)?.let { err ->
                    error = err
                    dialog = null
                    return@ConfirmDialog
                }
                dialog = null
                when (p.kind) {
                    "org" -> act("Организация отклонена.") { state.api.rejectOrganization(p.id, reason) }
                    "unpublish" -> act("Мероприятие снято с публикации.") { state.api.unpublishEvent(p.id, reason) }
                    "delete" -> act("Мероприятие удалено.") { state.api.deleteEvent(p.id, reason) }
                    "accept" -> act("Жалоба принята, мероприятие снято.") { state.api.acceptReport(p.id, reason) }
                    else -> act("Жалоба отклонена.") { state.api.rejectReport(p.id, reason.ifBlank { null }) }
                }
            },
            onDismiss = { dialog = null },
        )
    }
}
