package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.Comment
import com.crzto3.go2gether.data.EventDetails
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.data.User
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.JOIN_MODE_LABELS
import com.crzto3.go2gether.ui.LEVEL_LABELS
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.REPORT_REASON_LABELS
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.Avatar
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.ConfirmDialog
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.EventCard
import com.crzto3.go2gether.ui.components.FilterChip
import com.crzto3.go2gether.ui.components.InfoRow
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.components.Pill
import com.crzto3.go2gether.ui.components.SeatsBar
import com.crzto3.go2gether.ui.components.SectionTitle
import com.crzto3.go2gether.ui.components.SportCover
import com.crzto3.go2gether.ui.components.StatusBadge
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.components.eventStatus
import com.crzto3.go2gether.ui.components.participationStatus
import com.crzto3.go2gether.ui.formatDateTime
import com.crzto3.go2gether.ui.formatEventWhen
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

@Composable
fun EventScreen(id: Id) {
    val state = LocalAppState.current
    val session by state.session.state.collectAsState()
    val user = (session as? Session.State.Authorized)?.user
    val event = rememberLoad(id, user?.id) { state.api.event(id) }
    val comments = rememberLoad(id, user?.id) { state.api.comments(id).items }
    val similar = rememberLoad(id) { state.api.similar(id) }
    // Точку ищем только для вошедших: гостю место на карте не показываем.
    val place = rememberLoad(event.data, user?.id) { event.data?.takeIf { user != null }?.let { state.api.eventPoint(it) } }
    var reportOpen by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Мероприятие",
        actions = {
            if (user != null && !user.isAdmin && event.data?.isOrganizer == false && event.data?.status == "published") {
                TextButton(onClick = { reportOpen = true }) { Text("Пожаловаться", color = AppTheme.colors.ink2) }
            }
        },
    ) {
        val e = event.data
        when {
            e == null && event.loading -> block { Loading() }
            e == null && (event.errorStatus == 410 || event.errorStatus == 404) -> block {
                // Снятое видят только связанные с ним; остальным — «недоступно», как на сайте.
                val gone = event.errorStatus == 410
                AppCard {
                    Text((if (gone) "Мероприятие недоступно" else "Мероприятие не найдено").uppercase(), style = Type.h2, color = AppTheme.colors.ink)
                    if (gone) Text("Запись закрыта", style = Type.bodyBold, color = AppTheme.colors.ink)
                    Text(
                        if (gone) "Записаться на него нельзя. Если вы были записаны, запись сохранена в истории." else "Возможно, ссылка с ошибкой.",
                        style = Type.body, color = AppTheme.colors.ink2,
                    )
                    AppButton("Найти другие мероприятия", onClick = { state.nav.select(com.crzto3.go2gether.ui.Tab.Events) })
                }
            }
            e == null -> block { ErrorBlock(event.error ?: "Не получилось загрузить мероприятие") { event.reload() } }
            else -> {
                block { EventHeader(e, showAddress = user != null) }
                if (e.status != "published") {
                    block {
                        AppCard(color = Brand.DangerBg) {
                            Text(
                                (if (e.status == "cancelled") "Организатор отменил мероприятие" else eventStatus(e.status).second) +
                                    (e.unavailableReason?.let { ": $it" } ?: ""),
                                style = Type.bodyBold, color = Brand.DangerFg,
                            )
                            Text("Записаться на него нельзя. Вы видите страницу, потому что связаны с этим мероприятием.", style = Type.body, color = Brand.DangerFg)
                        }
                    }
                } else {
                    block { JoinPanel(e, user, onChanged = { event.reload(); comments.reload() }) }
                }
                val point = place.data
                if (user != null && point != null) {
                    block { EventPlaceMap(point.first, point.second, e.address) }
                }
                block { EventInfo(e) }
                if (e.participants.isNotEmpty()) {
                    block {
                        AppCard {
                            Text("Участники · ${e.participants.size}", style = Type.bodyBold, color = AppTheme.colors.ink)
                            e.participants.forEach { p ->
                                Row(
                                    Modifier.fillMaxWidth().clip(Radius.sm).clickable { state.nav.push(Screen.User(p.id)) }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Avatar(p.name, p.id, 34.dp)
                                    Text(p.name, style = Type.body, color = AppTheme.colors.ink)
                                }
                            }
                        }
                    }
                }
                block { SectionTitle("Обсуждение") }
                block { CommentsBlock(e, user, comments.data, comments.loading, comments.error) { comments.reload() } }
                val sim = similar.data.orEmpty()
                if (sim.isNotEmpty()) {
                    block { SectionTitle("Похожие") }
                    sim.forEach { r ->
                        block("sim-${r.event.id}") {
                            EventCard(r.event, onClick = { state.nav.push(Screen.Event(r.event.id)) }, showAddress = user != null)
                        }
                    }
                }
            }
        }
    }

    if (reportOpen) ReportDialog(id) { reportOpen = false }
}

@Composable
private fun EventHeader(e: EventDetails, showAddress: Boolean) {
    val state = LocalAppState.current
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().clip(Radius.card).background(c.card)) {
        SportCover(e.sport, height = 140)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TagsRow {
                Pill(e.sport.name)
                e.level?.let { LEVEL_LABELS[it] }?.let { Pill(it) }
                Pill(JOIN_MODE_LABELS[e.joinMode] ?: e.joinMode)
                if (e.status != "published") StatusBadge(eventStatus(e.status))
                if (e.isPast) Pill("Прошло")
            }
            Text(formatEventWhen(e.startsAt, e.endsAt), style = Type.bodyBold, color = c.accentInk)
            Text(e.title.uppercase(), style = Type.display, color = c.ink)
            val place = if (showAddress) {
                val address = e.address?.takeIf { it.isNotBlank() }
                val city = e.city?.takeIf { it.isNotBlank() }
                // Бэк часто пишет город прямо в адресе — не повторяем его.
                if (address != null && city != null && address.contains(city, ignoreCase = true)) address
                else listOfNotNull(city, address).joinToString(", ")
            }
            else e.city?.let { "$it · точный адрес увидят вошедшие" } ?: "Точный адрес увидят вошедшие"
            if (place.isNotBlank()) Text("📍 $place", style = Type.body, color = c.ink2)
            if (showAddress && !e.locationNote.isNullOrBlank()) Text(e.locationNote, style = Type.small, color = c.ink2)
            SeatsBar(e.confirmedCount, e.capacity)
            Row(
                Modifier.clip(Radius.sm).clickable {
                    state.nav.push(if (e.organizer.type == "organization") Screen.Organization(e.organizer.id) else Screen.User(e.organizer.id))
                }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Avatar(e.organizer.name, e.organizer.id, 36.dp)
                Column {
                    Text(if (e.organizer.type == "organization") "Организатор · организация" else "Организатор", style = Type.small, color = c.ink2)
                    Text(e.organizer.name, style = Type.bodyBold, color = c.ink)
                }
            }
        }
    }
}

/** Место на карте и ссылка на маршрут в 2ГИС — только вошедшим. */
@Composable
private fun EventPlaceMap(lat: Double, lng: Double, address: String?) {
    val uri = androidx.compose.ui.platform.LocalUriHandler.current
    AppCard {
        Text("Место на карте", style = Type.bodyBold, color = AppTheme.colors.ink)
        address?.let { Text(it, style = Type.small, color = AppTheme.colors.ink2) }
        com.crzto3.go2gether.ui.map.EventsMap(
            listOf(com.crzto3.go2gether.ui.map.MapMarker(0, com.crzto3.go2gether.ui.map.GeoPoint(lat, lng))),
            center = com.crzto3.go2gether.ui.map.GeoPoint(lat, lng),
            zoom = 15.0,
        )
        AppButton("Открыть в 2ГИС и построить маршрут", variant = ButtonVariant.Ghost, onClick = {
            uri.openUri(com.crzto3.go2gether.config.AppConfig.twoGisLink(lat, lng))
        })
    }
}

@Composable
private fun EventInfo(e: EventDetails) {
    if (listOf(e.description, e.requirements, e.equipment, e.organizerContacts).all { it.isNullOrBlank() }) return
    AppCard {
        InfoRow("Описание", e.description)
        InfoRow("Требования", e.requirements)
        InfoRow("Что взять с собой", e.equipment)
        InfoRow("Контакты организатора", e.organizerContacts)
        if (e.isEdited) Text("Изменено организатором", style = Type.small, color = AppTheme.colors.ink2)
    }
}

/** Блок записи — порядок проверок как в features/events/participation.ts. */
@Composable
private fun JoinPanel(e: EventDetails, user: User?, onChanged: () -> Unit) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<String?>(null) }
    val c = AppTheme.colors

    fun run(done: String, block: suspend () -> Unit) {
        busy = true
        error = null
        notice = null
        scope.launch {
            try {
                block()
                notice = done
                onChanged()
            } catch (ex: Exception) {
                error = ex.userMessage()
            } finally {
                busy = false
            }
        }
    }

    val mine = e.myParticipation
    AppCard {
        when {
            e.status != "published" -> Text("Запись закрыта.", style = Type.body, color = c.ink2)
            user == null -> {
                Text("Чтобы записаться, войдите в аккаунт.", style = Type.body, color = c.ink2)
                AppButton("Войти и записаться", onClick = { state.nav.push(Screen.Login) })
            }
            user.isAdmin -> Text("Администратор не записывается на мероприятия.", style = Type.body, color = c.ink2)
            e.isOrganizer -> {
                Text("Вы организатор этого мероприятия.", style = Type.body, color = c.ink)
                AppButton("Заявки и участники", onClick = { state.nav.push(Screen.ManageEvent(e.id)) })
                if (!e.isPast) {
                    AppButton("Изменить", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.EventForm(editId = e.id)) })
                    AppButton("Отменить мероприятие", variant = ButtonVariant.Ghost, enabled = !busy, onClick = { dialog = "cancelEvent" })
                }
            }
            mine?.status == "confirmed" -> {
                StatusBadge(participationStatus("confirmed"))
                Text("Ждём вас: ${formatEventWhen(e.startsAt, e.endsAt).lowercase()}.", style = Type.bodyBold, color = c.ink)
                AppButton("Отменить запись", variant = ButtonVariant.Ghost, enabled = !busy, onClick = { dialog = "leave" })
            }
            mine?.status == "pending" || mine?.status == "waitlist" -> {
                StatusBadge(participationStatus(mine.status))
                Text("Организатор рассмотрит заявку. Решение появится здесь и в «Моих записях».", style = Type.body, color = c.ink2)
                AppButton("Отозвать заявку", variant = ButtonVariant.Ghost, enabled = !busy, onClick = { dialog = "leave" })
            }
            e.isPast -> Text("Мероприятие уже прошло.", style = Type.body, color = c.ink2)
            e.capacity != null && e.confirmedCount >= e.capacity -> Text("Мест больше нет.", style = Type.body, color = c.ink2)
            e.joinMode == "open" -> {
                Text("Свободная запись: вы сразу станете участником.", style = Type.body, color = c.ink2)
                AppButton("Записаться", loading = busy, onClick = { run("Вы записаны.") { state.api.join(e.id) } })
            }
            else -> {
                if (mine?.status == "rejected") {
                    StatusBadge(participationStatus("rejected"))
                    mine.decisionReason?.let { Text("Причина: «$it»", style = Type.body, color = c.ink2) }
                }
                Text("Запись по заявке: организатор примет или отклонит её. До решения вы не участник.", style = Type.body, color = c.ink2)
                AppButton(
                    if (mine?.status == "rejected") "Подать заявку снова" else "Подать заявку",
                    loading = busy,
                    onClick = { dialog = "apply" },
                )
            }
        }
        notice?.let { Notice(it) }
        error?.let { Notice(it, ok = false) }
    }

    when (dialog) {
        "apply" -> ConfirmDialog(
            title = "Заявка на участие",
            text = "Можно коротко рассказать о себе — организатору будет проще решить.",
            confirm = "Отправить заявку",
            inputLabel = "Сообщение организатору (необязательно)",
            onConfirm = { msg ->
                dialog = null
                run("Заявка отправлена. Решение организатора появится здесь.") { state.api.join(e.id, msg.ifBlank { null }) }
            },
            onDismiss = { dialog = null },
        )
        "leave" -> ConfirmDialog(
            title = if (mine?.status == "confirmed") "Отменить запись?" else "Отозвать заявку?",
            text = "Место освободится для других. Записаться снова можно, пока мероприятие не началось.",
            confirm = "Да, отменить",
            dismiss = "Не отменять",
            onConfirm = {
                dialog = null
                run("Запись отменена.") { state.api.leave(e.id) }
            },
            onDismiss = { dialog = null },
        )
        "cancelEvent" -> ConfirmDialog(
            title = "Отменить мероприятие?",
            text = "Запись закроется, все записавшиеся увидят причину. Вернуть мероприятие нельзя.",
            confirm = "Да, отменить",
            dismiss = "Не отменять",
            inputLabel = "Причина",
            inputRequired = true,
            onConfirm = { reason ->
                dialog = null
                run("Мероприятие отменено. Участники увидят причину.") { state.api.cancelEvent(e.id, reason) }
            },
            onDismiss = { dialog = null },
        )
    }
}

@Composable
private fun CommentsBlock(
    e: EventDetails,
    user: User?,
    comments: List<Comment>?,
    loading: Boolean,
    error: String?,
    reload: () -> Unit,
) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<Comment?>(null) }
    var editing by remember { mutableStateOf<Comment?>(null) }
    var hiding by remember { mutableStateOf<Comment?>(null) }
    var sending by remember { mutableStateOf(false) }
    var sendError by remember { mutableStateOf<String?>(null) }
    val c = AppTheme.colors
    // Владелец допущенной организации отвечает в обсуждении её события от её имени.
    val asOrganizationId = if (e.isOrganizer && e.organizer.type == "organization") e.organizer.id else null

    fun act(block: suspend () -> Unit, onDone: () -> Unit = {}) {
        sending = true
        sendError = null
        scope.launch {
            try {
                block()
                onDone()
                reload()
            } catch (ex: Exception) {
                sendError = (ex as? com.crzto3.go2gether.data.ApiException)?.message ?: "Не получилось отправить. Попробуйте ещё раз."
            } finally {
                sending = false
            }
        }
    }

    AppCard {
        comments?.let { Text("${it.size} ${com.crzto3.go2gether.ui.plural(it.size, "сообщение", "сообщения", "сообщений")}", style = Type.small, color = c.ink2) }
        when {
            loading && comments == null -> Loading()
            error != null && comments == null -> ErrorBlock("Не получилось загрузить обсуждение.", reload)
            comments.isNullOrEmpty() -> Text("Вопросов пока нет. Спросите о том, что важно перед записью.", style = Type.body, color = c.ink2)
            else -> buildThread(comments).forEach { (root, replies) ->
                CommentItem(root, user, e, onReply = { replyTo = root; editing = null }, onEdit = { editing = root; text = root.text.orEmpty() },
                    onDelete = { act({ state.api.deleteComment(root.id) }) }, onHide = { hiding = root })
                replies.forEach { reply ->
                    Column(Modifier.padding(start = 24.dp)) {
                        CommentItem(reply, user, e, onReply = { replyTo = root; editing = null }, onEdit = { editing = reply; text = reply.text.orEmpty() },
                            onDelete = { act({ state.api.deleteComment(reply.id) }) }, onHide = { hiding = reply })
                    }
                }
            }
        }
        when {
            e.status != "published" -> Text("Обсуждение закрыто: мероприятие снято с публикации.", style = Type.body, color = c.ink2)
            user == null -> Text(
                "Войдите, чтобы задать вопрос.",
                Modifier.clickable { state.nav.push(Screen.Login) },
                style = Type.bodyBold, color = c.accentInk,
            )
            user.isAdmin -> Unit
            else -> {
                val target = editing ?: replyTo
                target?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (editing != null) "Изменение сообщения" else "Ответ для ${it.author.name}", Modifier.weight(1f), style = Type.small, color = c.ink2)
                        TextButton(onClick = { replyTo = null; editing = null; text = "" }) { Text(if (editing != null) "Отмена" else "Не отвечать", color = c.ink2) }
                    }
                }
                AppTextField(
                    text, { text = it }, "Текст сообщения",
                    placeholder = if (e.isOrganizer) "Ответьте участникам или добавьте уточнение…" else "Задать вопрос организатору…",
                    singleLine = false, minLines = 2,
                )
                if (asOrganizationId != null && editing == null) Text("Сообщение будет от имени организации.", style = Type.small, color = c.ink2)
                sendError?.let { Notice(it, ok = false) }
                AppButton(if (editing != null) "Сохранить" else "Отправить ↑", loading = sending, onClick = {
                    val body = text.trim()
                    if (body.isEmpty()) {
                        sendError = "Напишите сообщение."
                        return@AppButton
                    }
                    val edit = editing
                    act(
                        {
                            if (edit != null) state.api.updateComment(edit.id, body)
                            else state.api.addComment(e.id, body, replyTo?.id, asOrganizationId)
                        },
                        onDone = { text = ""; replyTo = null; editing = null },
                    )
                })
            }
        }
    }

    hiding?.let { cm ->
        ConfirmDialog(
            title = "Скрыть сообщение?",
            text = "Автор и участники увидят, что сообщение скрыто модератором.",
            confirm = "Скрыть",
            inputLabel = "Причина",
            inputRequired = true,
            onConfirm = { reason -> hiding = null; act({ state.api.hideComment(cm.id, reason) }) },
            onDismiss = { hiding = null },
        )
    }
}

/** Один уровень вложенности: ответы на ответы — в ветке исходного вопроса (features/comments/thread.ts). */
private fun buildThread(comments: List<Comment>): List<Pair<Comment, List<Comment>>> {
    val byId = comments.associateBy { it.id }
    fun rootOf(c: Comment): Comment {
        var cur = c
        val seen = mutableSetOf<Long>()
        while (cur.parentId != null && byId.containsKey(cur.parentId) && seen.add(cur.id)) cur = byId.getValue(cur.parentId!!)
        return cur
    }
    val sorted = comments.sortedBy { it.createdAt.orEmpty() }
    val roots = sorted.filter { rootOf(it).id == it.id }
    return roots.map { r -> r to sorted.filter { it.id != r.id && rootOf(it).id == r.id } }
}

@Composable
private fun CommentItem(
    comment: Comment,
    user: User?,
    e: EventDetails,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit,
) {
    val c = AppTheme.colors
    val mine = user != null && user.id == comment.author.id
    Column(
        Modifier.fillMaxWidth().clip(Radius.md)
            .background(if (comment.isOrganizer) c.reason else androidx.compose.ui.graphics.Color.Transparent)
            .padding(if (comment.isOrganizer) 8.dp else 0.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(comment.asOrganization?.name ?: comment.author.name, comment.asOrganization?.id ?: comment.author.id, 34.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(comment.asOrganization?.name ?: comment.author.name, style = Type.smallBold, color = c.ink)
                    if (comment.isOrganizer) Pill("Организатор", Brand.OnBrand, androidx.compose.ui.graphics.Color.White)
                }
                Text(
                    formatDateTime(comment.createdAt) + (if (mine) " · вы" else "") + (if (comment.isEdited) " · изменено" else ""),
                    style = Type.tiny, color = c.ink2,
                )
                if (comment.isHidden) {
                    Text("Сообщение скрыто модератором" + (comment.hiddenReason?.let { ": $it" } ?: ""), style = Type.small, color = c.ink2)
                } else {
                    Text(comment.text.orEmpty(), style = Type.body, color = c.ink)
                }
                if (user != null && e.status == "published" && !comment.isHidden) {
                    Row {
                        if (!user.isAdmin) CommentAction("Ответить", c.accentInk, onReply)
                        if (mine) {
                            CommentAction("Изменить", c.accentInk, onEdit)
                            CommentAction("Удалить", c.danger, onDelete)
                        }
                        if (user.isAdmin) CommentAction("Скрыть", c.danger, onHide)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentAction(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(label, Modifier.clip(Radius.sm).clickable(onClick = onClick).padding(vertical = 6.dp, horizontal = 6.dp), style = Type.smallBold, color = color)
}

@Composable
private fun ReportDialog(eventId: Id, onClose: () -> Unit) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    var reason by remember { mutableStateOf("other") }
    var comment by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val c = AppTheme.colors
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onClose,
        containerColor = c.card,
        title = { Text("Пожаловаться на мероприятие", style = Type.h2, color = c.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TagsRow { REPORT_REASON_LABELS.forEach { (k, v) -> FilterChip(v, reason == k) { reason = k } } }
                AppTextField(comment, { comment = it }, "Комментарий (необязательно)", singleLine = false, minLines = 2)
                result?.let { Notice(it.second, it.first) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (result?.first == true) return@TextButton onClose()
                scope.launch {
                    result = try {
                        state.api.report(eventId, reason, comment.trim().ifBlank { null })
                        true to "✓ Жалоба отправлена"
                    } catch (e: Exception) {
                        false to e.userMessage()
                    }
                }
            }) { Text(if (result?.first == true) "Готово" else "Отправить жалобу", fontWeight = FontWeight.Bold, color = c.ink) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Отмена", color = c.ink2) } },
    )
}
