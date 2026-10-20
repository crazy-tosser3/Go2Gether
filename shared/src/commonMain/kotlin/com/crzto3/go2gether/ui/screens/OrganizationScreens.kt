package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.clickable
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
import com.crzto3.go2gether.data.ApiException
import com.crzto3.go2gether.data.CreateOrganizationRequest
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.Organization
import com.crzto3.go2gether.data.UpdateOrganizationRequest
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.ORG_TYPE_LABELS
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.Avatar
import com.crzto3.go2gether.ui.components.ButtonVariant
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
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

@Composable
fun OrganizationsScreen() {
    val state = LocalAppState.current
    var query by rememberSaveable { mutableStateOf("") }
    var applied by rememberSaveable { mutableStateOf("") }
    val list = rememberLoad(applied) { state.api.organizations(q = applied.ifBlank { null }, size = 50).items }

    ScreenScaffold(title = "Организации") {
        block {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(query, { query = it }, "Поиск", Modifier.weight(1f), placeholder = "Клуб, федерация…")
                Box(Modifier.width(110.dp)) { AppButton("Найти", onClick = { applied = query.trim() }) }
            }
        }
        when {
            list.loading && list.data == null -> block { Loading() }
            list.error != null -> block { ErrorBlock(list.error!!) { list.reload() } }
            list.data.isNullOrEmpty() -> block { EmptyBlock("Организаций не нашлось") }
            else -> list.data!!.forEach { o -> block(o.id) { OrgCard(o) { state.nav.push(Screen.Organization(o.id)) } } }
        }
    }
}

@Composable
private fun OrgCard(o: Organization, showStatus: Boolean = false, onClick: () -> Unit) {
    val c = AppTheme.colors
    AppCard(Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(o.name, o.id, 48.dp)
            Column(Modifier.weight(1f)) {
                Text(o.name, style = Type.cardTitle, color = c.ink)
                Text(listOfNotNull(ORG_TYPE_LABELS[o.type], o.city).joinToString(" · "), style = Type.small, color = c.ink2)
            }
        }
        if (showStatus) StatusBadge(organizationStatus(o.status))
        o.sportsDescription?.takeIf { it.isNotBlank() }?.let { Text(it, style = Type.body, color = c.ink2, maxLines = 2) }
    }
}

@Composable
fun OrganizationScreen(id: Id) {
    val state = LocalAppState.current
    val org = rememberLoad(id) { state.api.organization(id) }
    val events = rememberLoad(id) { state.api.organizationEvents(id) }
    val c = AppTheme.colors

    ScreenScaffold(title = "Организация") {
        val o = org.data
        when {
            o == null && org.loading -> block { Loading() }
            o == null && org.errorStatus == 404 -> block {
                EmptyBlock("Страница не найдена", "Организации нет или она ещё не прошла проверку.") {
                    AppButton("Все организации", onClick = { state.nav.push(Screen.Organizations) })
                }
            }
            o == null -> block { ErrorBlock(org.error ?: "Не найдено") { org.reload() } }
            else -> {
                block {
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Avatar(o.name, o.id, 64.dp)
                            Column(Modifier.weight(1f)) {
                                Text(o.name.uppercase(), style = Type.h2, color = c.ink)
                                TagsRow {
                                    ORG_TYPE_LABELS[o.type]?.let { Pill(it) }
                                    o.city?.let { Pill("📍 $it") }
                                    if (o.status != "approved") StatusBadge(organizationStatus(o.status))
                                }
                            }
                        }
                        InfoRow("О нас", o.description)
                        InfoRow("Виды спорта", o.sportsDescription)
                        InfoRow("Контакты", o.contacts)
                        InfoRow("Сайт", o.websiteUrl)
                        InfoRow("Причина отказа", o.rejectReason)
                    }
                }
                block { SectionTitle("Мероприятия") }
                when {
                    events.loading && events.data == null -> block { Loading() }
                    events.error != null -> block { ErrorBlock(events.error!!) { events.reload() } }
                    events.data.isNullOrEmpty() -> block { EmptyBlock("Ближайших мероприятий нет") }
                    else -> events.data!!.forEach { e -> block(e.id) { EventCard(e, onClick = { state.nav.push(Screen.Event(e.id)) }) } }
                }
            }
        }
    }
}

/** Свои организации и заявка на новую — как MyOrganizationsView и OrgForm. */
@Composable
fun MyOrganizationsScreen() {
    val state = LocalAppState.current
    val list = rememberLoad { state.api.myOrganizations() }
    // null — форма закрыта; Organization с id 0 — новая заявка; иначе — исправление отклонённой.
    var editing by remember { mutableStateOf<Organization?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    val c = AppTheme.colors

    ScreenScaffold(title = "Моя организация") {
        notice?.let { block { Notice(it) } }
        val form = editing
        if (form != null) {
            block(form.id) {
                OrgForm(form, onDone = { msg ->
                    editing = null
                    notice = msg
                    list.reload()
                }, onCancel = { editing = null })
            }
            return@ScreenScaffold
        }
        when {
            list.loading && list.data == null -> block { Loading() }
            list.error != null -> block { ErrorBlock(list.error!!) { list.reload() } }
            list.data.isNullOrEmpty() -> block {
                EmptyBlock("У вас пока нет организации", "Клуб, сообщество или федерация после проверки смогут публиковать мероприятия от своего имени.") {
                    AppButton("Подать заявку", variant = ButtonVariant.Accent, onClick = { editing = Organization(id = 0) })
                }
            }
            else -> {
                list.data!!.forEach { o ->
                    block(o.id) {
                        AppCard {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Avatar(o.name, o.id, 48.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(o.name, style = Type.cardTitle, color = c.ink)
                                    Text(listOfNotNull(ORG_TYPE_LABELS[o.type], o.city).joinToString(" · "), style = Type.small, color = c.ink2)
                                }
                            }
                            StatusBadge(organizationStatus(o.status))
                            when (o.status) {
                                "pending" -> Text("Администратор проверяет заявку. Публиковать мероприятия можно будет после допуска.", style = Type.body, color = c.ink2)
                                "rejected" -> {
                                    o.rejectReason?.let { Text("Причина: $it", style = Type.body, color = c.ink) }
                                    AppButton("Исправить и отправить снова", onClick = { editing = o })
                                }
                                else -> {
                                    AppButton("+ Мероприятие от организации", variant = ButtonVariant.Accent, onClick = {
                                        state.nav.push(Screen.EventForm(organizationId = o.id))
                                    })
                                    AppButton("Страница организации", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Organization(o.id)) })
                                }
                            }
                        }
                    }
                }
                block { AppButton("+ Ещё одна", variant = ButtonVariant.Ghost, onClick = { editing = Organization(id = 0) }) }
            }
        }
    }
}

@Composable
private fun OrgForm(initial: Organization, onDone: (String) -> Unit, onCancel: () -> Unit) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val isNew = initial.id == 0L
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var type by rememberSaveable { mutableStateOf(initial.type) }
    var city by rememberSaveable { mutableStateOf(initial.city.orEmpty()) }
    var contacts by rememberSaveable { mutableStateOf(initial.contacts.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial.description.orEmpty()) }
    var sportsDescription by rememberSaveable { mutableStateOf(initial.sportsDescription.orEmpty()) }
    var website by rememberSaveable { mutableStateOf(initial.websiteUrl.orEmpty()) }
    var consent by rememberSaveable { mutableStateOf(!isNew) }
    var fields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val c = AppTheme.colors

    AppCard {
        Text(if (isNew) "Новая организация" else "Исправить заявку", style = Type.bodyBold, color = c.ink)
        Text("После проверки администратором можно будет проводить мероприятия от её имени.", style = Type.small, color = c.ink2)
        AppTextField(name, { name = it }, "Название *", error = fields["name"])
        Text("Тип", style = Type.smallBold, color = c.ink)
        TagsRow { ORG_TYPE_LABELS.forEach { (k, v) -> FilterChip(v, type == k) { type = k } } }
        AppTextField(sportsDescription, { sportsDescription = it }, "Направления", placeholder = "Футбол, мини-футбол", error = fields["sports_description"])
        AppTextField(city, { city = it }, "Город", error = fields["city"])
        AppTextField(contacts, { contacts = it }, "Контакты для участников *", placeholder = "Телефон, Telegram, почта", error = fields["contacts"])
        AppTextField(description, { description = it }, "О вас", singleLine = false, minLines = 3, error = fields["description"])
        AppTextField(website, { website = it }, "Сайт", error = fields["website_url"])
        if (isNew) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Checkbox(
                    consent, { consent = it },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = c.ink, checkmarkColor = c.onInk),
                )
                Text("Согласен на обработку персональных данных", style = Type.small, color = c.ink)
            }
        }
        error?.let { Notice(it, ok = false) }
        AppButton(if (isNew) "Отправить на проверку" else "Отправить снова", loading = saving, enabled = consent, onClick = {
            fields = buildMap {
                if (name.isBlank()) put("name", "Введите название")
                if (contacts.isBlank()) put("contacts", "Укажите контакты")
            }
            if (fields.isNotEmpty()) return@AppButton
            saving = true
            error = null
            scope.launch {
                try {
                    if (isNew) {
                        state.api.createOrganization(
                            CreateOrganizationRequest(
                                name = name.trim(), type = type, contacts = contacts.trim(),
                                description = description.trim().ifBlank { null },
                                sportsDescription = sportsDescription.trim().ifBlank { null },
                                city = city.trim().ifBlank { null },
                                websiteUrl = website.trim().ifBlank { null },
                            ),
                        )
                    } else {
                        state.api.updateOrganization(
                            initial.id,
                            UpdateOrganizationRequest(
                                name = name.trim(), type = type, contacts = contacts.trim(),
                                description = description.trim(), sportsDescription = sportsDescription.trim(),
                                city = city.trim(), websiteUrl = website.trim(),
                            ),
                        )
                        // Повторная подача переводит заявку обратно в «На рассмотрении».
                        state.api.resubmitOrganization(initial.id)
                    }
                    state.session.refresh()
                    onDone("Заявка отправлена. Ответ появится здесь.")
                } catch (e: Exception) {
                    fields = (e as? ApiException)?.fields.orEmpty()
                    error = e.userMessage()
                } finally {
                    saving = false
                }
            }
        })
        AppButton("Отмена", variant = ButtonVariant.Ghost, onClick = onCancel)
    }
}
