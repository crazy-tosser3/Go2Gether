package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.ApiException
import com.crzto3.go2gether.data.CreateEventRequest
import com.crzto3.go2gether.data.EventDetails
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.data.UpdateEventRequest
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.LEVEL_LABELS
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.map.GeoPoint
import com.crzto3.go2gether.ui.map.LocationPicker
import com.crzto3.go2gether.ui.map.geocode
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.EmptyBlock
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.FilterChip
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.inputDate
import com.crzto3.go2gether.ui.inputTime
import com.crzto3.go2gether.ui.isFuture
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type
import com.crzto3.go2gether.ui.toIso
import kotlinx.coroutines.launch

/** Поля формы — как EventFormState в frontend/src/utils/eventForm.ts. */
private data class EventFormState(
    val title: String = "",
    val description: String = "",
    val sportId: Id? = null,
    /** Своё направление, которого нет в справочнике. */
    val newSport: String? = null,
    val sportName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val city: String = "",
    val address: String = "",
    val locationNote: String = "",
    val level: String? = null,
    val joinMode: String? = null,
    val capacity: String = "",
    val requirements: String = "",
    val equipment: String = "",
    val organizationId: Id? = null,
    /** Точка на карте; без неё мероприятие не видно на карте. */
    val point: GeoPoint? = null,
)

private fun formFrom(e: EventDetails) = EventFormState(
    title = e.title,
    description = e.description.orEmpty(),
    sportId = e.sport.id,
    sportName = e.sport.name,
    date = inputDate(e.startsAt),
    startTime = inputTime(e.startsAt),
    endTime = e.endsAt?.let(::inputTime).orEmpty(),
    city = e.city.orEmpty(),
    address = e.address.orEmpty(),
    locationNote = e.locationNote.orEmpty(),
    level = e.level,
    joinMode = e.joinMode,
    capacity = e.capacity?.toString().orEmpty(),
    requirements = e.requirements.orEmpty(),
    equipment = e.equipment.orEmpty(),
    organizationId = if (e.organizer.type == "organization") e.organizer.id else null,
    point = if (e.lat != null && e.lng != null) GeoPoint(e.lat, e.lng) else null,
)

private fun validate(f: EventFormState, original: EventFormState?): Map<String, String> = buildMap {
    if (f.title.isBlank()) put("title", "Укажите название")
    else if (f.title.trim().length > 120) put("title", "Не длиннее 120 символов")
    if (f.sportId == null && f.newSport.isNullOrBlank()) put("sport", "Выберите направление или впишите своё")
    if (f.date.isBlank()) put("date", "Укажите дату")
    if (f.startTime.isBlank()) put("startTime", "Укажите время начала")
    val start = toIso(f.date, f.startTime)
    if (f.date.isNotBlank() && f.startTime.isNotBlank() && start == null) put("date", "Дата ДД.ММ.ГГГГ, время ЧЧ:ММ")
    val timeChanged = original == null || f.date != original.date || f.startTime != original.startTime
    if (start != null && timeChanged && !isFuture(start)) put("startTime", "Время начала уже прошло")
    if (start != null && f.endTime.isNotBlank()) {
        val end = toIso(f.date, f.endTime)
        if (end == null || end <= start) put("endTime", "Окончание позже начала")
    }
    if (f.address.isBlank()) put("address", "Укажите место")
    if (f.joinMode == null) put("joinMode", "Выберите, как записываться")
    val cap = f.capacity.trim()
    if (cap.isNotEmpty()) {
        val n = cap.toIntOrNull()
        if (n == null || n < 1) put("capacity", "Целое число, не меньше 1")
        else if (n > 1000) put("capacity", "Не больше 1000")
    }
    // PATCH бэка не стирает поля: число мест, окончание и уровень можно изменить, но не убрать.
    if (original != null) {
        if (original.capacity.isNotBlank() && cap.isEmpty()) put("capacity", "Убрать ограничение нельзя — укажите число")
        if (original.endTime.isNotBlank() && f.endTime.isBlank()) put("endTime", "Окончание можно изменить, но не убрать")
        if (original.level != null && f.level == null) put("level", "Уровень можно изменить, но не убрать")
    }
}

private fun toCreate(f: EventFormState) = CreateEventRequest(
    title = f.title.trim(),
    description = f.description.trim(),
    sportId = f.sportId,
    sportName = if (f.sportId == null) f.newSport?.trim() else null,
    startsAt = toIso(f.date, f.startTime)!!,
    endsAt = f.endTime.takeIf { it.isNotBlank() }?.let { toIso(f.date, it) },
    city = f.city.trim().ifBlank { null },
    address = f.address.trim(),
    locationNote = f.locationNote.trim().ifBlank { null },
    level = f.level,
    requirements = f.requirements.trim().ifBlank { null },
    equipment = f.equipment.trim().ifBlank { null },
    joinMode = f.joinMode!!,
    capacity = f.capacity.trim().toIntOrNull(),
    organizationId = f.organizationId,
    lat = f.point?.lat,
    lng = f.point?.lng,
)

/** Только изменённые поля — как toEventUpdate. */
private fun toUpdate(f: EventFormState, o: EventFormState): UpdateEventRequest {
    fun text(new: String, old: String) = new.trim().takeIf { it != old.trim() }
    val timeChanged = f.date != o.date || f.startTime != o.startTime
    return UpdateEventRequest(
        title = text(f.title, o.title),
        description = text(f.description, o.description),
        city = text(f.city, o.city),
        address = text(f.address, o.address),
        locationNote = text(f.locationNote, o.locationNote),
        requirements = text(f.requirements, o.requirements),
        equipment = text(f.equipment, o.equipment),
        sportId = f.sportId.takeIf { it != null && it != o.sportId },
        sportName = if (f.sportId == null) f.newSport?.trim() else null,
        startsAt = if (timeChanged) toIso(f.date, f.startTime) else null,
        endsAt = if (f.endTime.isNotBlank() && (f.endTime != o.endTime || f.date != o.date)) toIso(f.date, f.endTime) else null,
        level = f.level.takeIf { it != o.level },
        joinMode = f.joinMode.takeIf { it != o.joinMode },
        capacity = f.capacity.trim().takeIf { it.isNotEmpty() && it != o.capacity }?.toIntOrNull(),
        lat = f.point?.takeIf { it != o.point }?.lat,
        lng = f.point?.takeIf { it != o.point }?.lng,
    )
}

@Composable
fun EventFormScreen(editId: Id? = null, repeatId: Id? = null, organizationId: Id? = null) {
    val state = LocalAppState.current
    val session by state.session.state.collectAsState()
    val auth = session as? Session.State.Authorized
    val title = when {
        editId != null -> "Изменить мероприятие"
        repeatId != null -> "Повтор мероприятия"
        else -> "Новое мероприятие"
    }

    // Администратор не создаёт мероприятия — как noAdmin в роутере сайта.
    if (auth?.user?.isAdmin == true) {
        ScreenScaffold(title = title) {
            block {
                EmptyBlock("Администратор не создаёт мероприятия") {
                    AppButton("В админку", onClick = { state.nav.push(Screen.Admin) })
                }
            }
        }
        return
    }

    val sourceId = editId ?: repeatId
    val source = rememberLoad(sourceId) { sourceId?.let { state.api.event(it) } }
    if (sourceId != null && source.data == null) {
        ScreenScaffold(title = title) {
            block { if (source.loading) Loading() else ErrorBlock(source.error ?: "Не найдено") { source.reload() } }
        }
        return
    }
    val approved = auth?.organizations.orEmpty().filter { it.status == "approved" }
    val original = source.data?.let { e ->
        formFrom(e).let { f -> if (f.point == null) state.api.coords[e.id]?.let { (lat, lng) -> f.copy(point = GeoPoint(lat, lng)) } ?: f else f }
    }
    val initial = when {
        original == null -> EventFormState(city = auth?.user?.city.orEmpty(), organizationId = organizationId?.takeIf { id -> approved.any { it.id == id } })
        // Повтор: всё, кроме даты и времени; от имени организации — только если она ещё допущена.
        repeatId != null -> original.copy(
            date = "", startTime = "", endTime = "",
            organizationId = original.organizationId?.takeIf { id -> approved.any { it.id == id } },
        )
        else -> original
    }
    EventFormContent(title, initial, if (editId != null) original else null, editId, approved.map { it.id to it.name })
}

@Composable
private fun EventFormContent(
    title: String,
    initial: EventFormState,
    original: EventFormState?,
    editId: Id?,
    organizations: List<Pair<Id, String>>,
) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf(initial) }
    var sportQuery by rememberSaveable { mutableStateOf(initial.sportName) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val sports = rememberLoad { state.api.sports() }
    var mapVersion by remember { mutableStateOf(0) }
    var geocoding by remember { mutableStateOf(false) }
    var geoNote by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val c = AppTheme.colors

    fun submit() {
        errors = validate(form, original)
        if (errors.isNotEmpty()) {
            error = "Проверьте отмеченные поля."
            return
        }
        saving = true
        error = null
        scope.launch {
            try {
                val saved = if (editId != null && original != null) {
                    state.api.updateEvent(editId, toUpdate(form, original))
                } else {
                    state.api.createEvent(toCreate(form))
                }
                state.nav.replace(Screen.Event(saved.id))
            } catch (e: Exception) {
                // Ошибки полей от бэка — к соответствующим полям (как mapServerErrors).
                val keys = mapOf(
                    "sport_id" to "sport", "sport_name" to "sport", "starts_at" to "startTime", "ends_at" to "endTime",
                    "join_mode" to "joinMode", "location_note" to "locationNote", "organization_id" to "organizationId",
                )
                errors = (e as? ApiException)?.fields.orEmpty().mapKeys { keys[it.key] ?: it.key }
                error = e.userMessage()
            } finally {
                saving = false
            }
        }
    }

    ScreenScaffold(title = title) {
        if (original != null) {
            block { Notice("После публикации нельзя убрать число мест, окончание и уровень — только изменить.", ok = true) }
        }
        block {
            AppCard {
                Text("Что и когда", style = Type.bodyBold, color = c.ink)
                AppTextField(form.title, { form = form.copy(title = it) }, "Название *", placeholder = "Футбол в парке", error = errors["title"])
                // Направление — поле с подсказками и пунктом «+ Добавить «…»», как UiCombobox.
                AppTextField(
                    sportQuery,
                    {
                        sportQuery = it
                        form = form.copy(sportId = null, newSport = null)
                    },
                    "Направление *",
                    placeholder = "Начните вводить",
                    error = errors["sport"],
                )
                if (form.sportId == null && form.newSport == null) {
                    val q = sportQuery.trim().lowercase()
                    val all = sports.data.orEmpty()
                    val matches = all.filter { q.isEmpty() || it.name.lowercase().contains(q) }.take(12)
                    when {
                        sports.loading -> Loading()
                        sports.error != null -> ErrorBlock(sports.error!!) { sports.reload() }
                        else -> TagsRow {
                            matches.forEach { s ->
                                FilterChip(s.name, false) {
                                    form = form.copy(sportId = s.id, newSport = null)
                                    sportQuery = s.name
                                }
                            }
                            if (q.isNotEmpty() && all.none { it.name.lowercase() == q }) {
                                FilterChip("+ Добавить «${sportQuery.trim()}»", false) {
                                    form = form.copy(sportId = null, newSport = sportQuery.trim())
                                }
                            }
                        }
                    }
                } else if (form.newSport != null) {
                    Text("✦ Новое направление", style = Type.smallBold, color = c.accentInk)
                }
                AppTextField(form.date, { form = form.copy(date = it) }, "Дата * (ДД.ММ.ГГГГ)", error = errors["date"], keyboardType = KeyboardType.Number)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTextField(form.startTime, { form = form.copy(startTime = it) }, "Начало *", Modifier.weight(1f), placeholder = "19:00", error = errors["startTime"], keyboardType = KeyboardType.Number)
                    AppTextField(form.endTime, { form = form.copy(endTime = it) }, "Окончание", Modifier.weight(1f), placeholder = "21:00", error = errors["endTime"], keyboardType = KeyboardType.Number)
                }
                AppTextField(form.description, { form = form.copy(description = it) }, "Описание", singleLine = false, minLines = 3, error = errors["description"])
            }
        }
        block {
            AppCard {
                Text("Где", style = Type.bodyBold, color = c.ink)
                AppTextField(form.city, { form = form.copy(city = it) }, "Город", error = errors["city"])
                AppTextField(form.address, { form = form.copy(address = it) }, "Адрес *", placeholder = "Парк Горького, поле 2", error = errors["address"])
                AppTextField(form.locationNote, { form = form.copy(locationNote = it) }, "Как найти", placeholder = "У главного входа", error = errors["locationNote"])
                Text("Точка на карте", style = Type.smallBold, color = c.ink)
                Text("Найдите по адресу или нажмите на карту. Без точки мероприятие не появится на карте.", style = Type.small, color = c.ink2)
                LocationPicker(form.point, mapVersion) { p ->
                    form = form.copy(point = p)
                    geoNote = null
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppButton("Найти по адресу", Modifier.weight(1f), variant = ButtonVariant.Ghost, loading = geocoding, enabled = form.address.isNotBlank(), onClick = {
                        geocoding = true
                        geoNote = null
                        scope.launch {
                            try {
                                val found = geocode(state.api.client, form.address, form.city)
                                if (found == null) {
                                    geoNote = false to "Адрес не нашёлся на карте — поставьте точку вручную."
                                } else {
                                    form = form.copy(point = found)
                                    mapVersion++
                                    geoNote = true to "Точка найдена по адресу."
                                }
                            } catch (e: Exception) {
                                geoNote = false to "Поиск по адресу недоступен. Поставьте точку на карте."
                            } finally {
                                geocoding = false
                            }
                        }
                    })
                    if (form.point != null && original?.point == null) {
                        AppButton("Убрать точку", Modifier.weight(1f), variant = ButtonVariant.Ghost, onClick = {
                            form = form.copy(point = null)
                            mapVersion++
                        })
                    }
                }
                geoNote?.let { Notice(it.second, it.first) }
            }
        }
        block {
            AppCard {
                Text("Запись", style = Type.bodyBold, color = c.ink)
                Text("Как записываться *", style = Type.smallBold, color = c.ink)
                JoinModeOption("Свободно", "Сразу участник", form.joinMode == "open") { form = form.copy(joinMode = "open") }
                JoinModeOption("По заявке", "Вы решаете, кого принять", form.joinMode == "approval") { form = form.copy(joinMode = "approval") }
                errors["joinMode"]?.let { Text(it, style = Type.small, color = c.danger) }
                Text("Уровень", style = Type.smallBold, color = c.ink)
                TagsRow {
                    FilterChip("Любой", form.level == null) { form = form.copy(level = null) }
                    LEVEL_LABELS.forEach { (k, v) -> FilterChip(v, form.level == k) { form = form.copy(level = k) } }
                }
                errors["level"]?.let { Text(it, style = Type.small, color = c.danger) }
                AppTextField(
                    form.capacity, { v -> form = form.copy(capacity = v.filter(Char::isDigit)) },
                    "Сколько мест", placeholder = "Пусто — без ограничения", error = errors["capacity"], keyboardType = KeyboardType.Number,
                )
                AppTextField(form.equipment, { form = form.copy(equipment = it) }, "Что взять с собой", singleLine = false)
                AppTextField(form.requirements, { form = form.copy(requirements = it) }, "Требования", singleLine = false)
            }
        }
        if (editId == null && organizations.isNotEmpty()) {
            block {
                AppCard {
                    Text("От чьего имени", style = Type.bodyBold, color = c.ink)
                    TagsRow {
                        FilterChip("От себя", form.organizationId == null) { form = form.copy(organizationId = null) }
                        organizations.forEach { (id, name) -> FilterChip(name, form.organizationId == id) { form = form.copy(organizationId = id) } }
                    }
                }
            }
        }
        block {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                error?.let { Notice(it, ok = false) }
                AppButton(if (editId != null) "Сохранить" else "Опубликовать", variant = ButtonVariant.Accent, loading = saving, onClick = ::submit)
                if (state.nav.canGoBack) AppButton("Отмена", variant = ButtonVariant.Ghost, onClick = { state.nav.back() })
            }
        }
    }

}

@Composable
private fun JoinModeOption(title: String, hint: String, selected: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().clip(Radius.md)
            .background(if (selected) Brand.AccentSoft else c.field)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text((if (selected) "● " else "○ ") + title, style = Type.bodyBold, color = if (selected) Brand.OnBrand else c.ink)
        Text(hint, style = Type.small, color = if (selected) Brand.OnBrand else c.ink2)
    }
}
