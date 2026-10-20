package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crzto3.go2gether.config.AppConfig
import com.crzto3.go2gether.data.EventSummary
import com.crzto3.go2gether.data.Session
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.ContentWidth
import com.crzto3.go2gether.ui.LEVEL_LABELS
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.QuickDate
import com.crzto3.go2gether.ui.RECOMMENDATION_REASONS
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.Tab
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.map.EventsMap
import com.crzto3.go2gether.ui.map.GeoPoint
import com.crzto3.go2gether.ui.map.MapMarker
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.EmptyBlock
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.EventCard
import com.crzto3.go2gether.ui.components.FilterChip
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.SectionTitle
import com.crzto3.go2gether.ui.components.SportKind
import com.crzto3.go2gether.ui.components.TagsRow
import com.crzto3.go2gether.ui.isoDate
import com.crzto3.go2gether.ui.quickDateRange
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val state = LocalAppState.current
    val session by state.session.state.collectAsState()
    val user = (session as? Session.State.Authorized)?.user
    val forYou = rememberLoad(user?.id) { state.api.recommendations(limit = 6) }
    val upcoming = rememberLoad { state.api.events(size = 6).items }

    ScreenScaffold {
        block {
            Column(
                Modifier.fillMaxWidth().clip(Radius.card).background(Brand.Accent).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("GO2GETHER", style = Type.display, color = Brand.OnBrand)
                Text(
                    if (user != null) "Привет, ${user.name.substringBefore(' ')}! Найдите компанию для тренировки."
                    else "Найдите компанию для спорта рядом: игры, пробежки, турниры и тренировки.",
                    style = Type.body, color = Brand.OnBrand,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppButton("Найти событие", Modifier.weight(1f), onClick = { state.nav.select(Tab.Events) })
                    if (user == null) {
                        AppButton("Войти", Modifier.weight(1f), variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Login) })
                    } else if (!user.isAdmin) {
                        AppButton("Создать", Modifier.weight(1f), variant = ButtonVariant.Accent, onClick = { state.nav.select(Tab.Create) })
                    }
                }
            }
        }

        if (user?.isAdmin != true) {
            block { SectionTitle(if (user != null) "Для вас" else "Популярное") }
            block { EventsRow(forYou.loading, forYou.error, forYou.data.orEmpty().map { it.event to it.reasons }, user != null, forYou::reload) }
        }

        block { SectionTitle("Популярные виды") }
        block {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    listOf(SportKind.Football to "Футбол", SportKind.Tennis to "Теннис"),
                    listOf(SportKind.Running to "Бег", SportKind.Chess to "Шахматы"),
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (kind, name) -> SportTile(kind, name, Modifier.weight(1f)) }
                    }
                }
            }
        }

        block { SectionTitle("Ближайшие") }
        when {
            upcoming.loading -> block { Loading() }
            upcoming.error != null -> block { ErrorBlock(upcoming.error!!) { upcoming.reload() } }
            upcoming.data.isNullOrEmpty() -> block { EmptyBlock("Пока ничего не запланировано") }
            else -> upcoming.data!!.forEach { e ->
                block(e.id) { EventCard(e, onClick = { state.nav.push(Screen.Event(e.id)) }, showAddress = user != null) }
            }
        }
        block {
            AppButton("Организации", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Organizations) })
        }
    }
}

@Composable
private fun SportTile(kind: SportKind, name: String, modifier: Modifier) {
    val state = LocalAppState.current
    Column(
        modifier.heightIn(min = 120.dp).clip(Radius.lg)
            .background(if (kind == SportKind.Chess) AppTheme.colors.card else kind.cover)
            .clickable { state.nav.select(Tab.Events, Screen.Events) }
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(Brand.OnBrand), contentAlignment = Alignment.Center) {
            Text(kind.emoji, fontSize = 24.sp)
        }
        Text(name, style = Type.cardTitle, color = if (kind == SportKind.Chess) AppTheme.colors.ink else Brand.OnBrand)
    }
}

@Composable
private fun EventsRow(
    loading: Boolean,
    error: String?,
    events: List<Pair<EventSummary, List<String>>>,
    showAddress: Boolean,
    retry: () -> Unit,
) {
    val state = LocalAppState.current
    when {
        loading -> Loading()
        error != null -> ErrorBlock(error, retry)
        events.isEmpty() -> EmptyBlock("Подборка пока пустая", "Заполните профиль — подскажем подходящие события.")
        else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events, key = { it.first.id }) { (e, reasons) ->
                EventCard(
                    e,
                    onClick = { state.nav.push(Screen.Event(e.id)) },
                    modifier = Modifier.width(280.dp),
                    showAddress = showAddress,
                    reasons = reasons.mapNotNull { RECOMMENDATION_REASONS[it] }.take(2),
                )
            }
        }
    }
}

/** Список мероприятий: поиск, быстрые даты, уровень, режим записи и подгрузка страниц. */
@Composable
fun EventsScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val session by state.session.state.collectAsState()
    val authorized = session is Session.State.Authorized

    var query by rememberSaveable { mutableStateOf("") }
    var appliedQuery by rememberSaveable { mutableStateOf("") }
    var quick by rememberSaveable { mutableStateOf<QuickDate?>(null) }
    var level by rememberSaveable { mutableStateOf<String?>(null) }
    var joinMode by rememberSaveable { mutableStateOf<String?>(null) }
    var sport by rememberSaveable { mutableStateOf<Long?>(null) }
    var city by rememberSaveable { mutableStateOf("") }
    var appliedCity by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(EventSort.Soon) }
    val sports = rememberLoad { state.api.sports() }
    var showMap by rememberSaveable { mutableStateOf(false) }

    val items = remember { mutableStateListOf<EventSummary>() }
    var page by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableIntStateOf(0) }

    suspend fun load(p: Int) {
        loading = true
        error = null
        try {
            val range = quick?.let(::quickDateRange)
            val res = state.api.events(
                q = appliedQuery.ifBlank { null },
                dateFrom = range?.first?.let(::isoDate),
                dateTo = range?.second?.let(::isoDate),
                level = level,
                joinMode = joinMode,
                sport = sport,
                city = appliedCity.ifBlank { null },
                page = p,
                size = AppConfig.PAGE_SIZE,
            )
            if (p == 0) items.clear()
            items.addAll(res.items)
            page = res.page
            totalPages = res.totalPages
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.userMessage()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(appliedQuery, quick, level, joinMode, sport, appliedCity, version) { load(0) }

    ScreenScaffold(title = "Мероприятия") {
        block {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(query, { query = it }, "Поиск", Modifier.weight(1f), placeholder = "Футбол, пробежка, турнир…")
                    Box(Modifier.width(110.dp)) {
                        AppButton("Найти", onClick = { appliedQuery = query.trim(); appliedCity = city.trim() })
                    }
                }
                AppTextField(city, { city = it }, "Город", placeholder = "Любой")
                val sportList = sports.data.orEmpty()
                if (sportList.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { FilterChip("Все направления", sport == null) { sport = null } }
                        items(sportList, key = { it.id }) { s -> FilterChip(s.name, sport == s.id) { sport = if (sport == s.id) null else s.id } }
                    }
                }
                TagsRow {
                    QuickDate.entries.forEach { q -> FilterChip(q.label, quick == q) { quick = if (quick == q) null else q } }
                }
                TagsRow {
                    LEVEL_LABELS.forEach { (k, v) -> FilterChip(v, level == k) { level = if (level == k) null else k } }
                    FilterChip("По заявке", joinMode == "approval") { joinMode = if (joinMode == "approval") null else "approval" }
                    FilterChip("Свободная запись", joinMode == "open") { joinMode = if (joinMode == "open") null else "open" }
                }
                Text("Сначала", style = Type.smallBold, color = AppTheme.colors.ink2)
                TagsRow { EventSort.entries.forEach { o -> FilterChip(o.label, sort == o) { sort = o } } }
                TagsRow {
                    FilterChip("Список", !showMap) { showMap = false }
                    FilterChip("Карта", showMap) { showMap = true }
                }
            }
        }
        if (showMap) {
            block {
                if (!authorized) {
                    // Место встречи на карте — только вошедшим, как на сайте.
                    EmptyBlock("Карта доступна после входа", "Так место встречи не видно случайным людям.") {
                        AppButton("Войти", onClick = { state.nav.push(Screen.Login) })
                    }
                } else {
                    val markers = items.mapNotNull { e ->
                        if (e.lat != null && e.lng != null) MapMarker(e.id, GeoPoint(e.lat, e.lng)) else null
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventsMap(markers, height = 460.dp, onMarker = { id -> state.nav.push(Screen.Event(id)) })
                        Text(
                            "На карте ${markers.size} из ${items.size}: показаны мероприятия с точкой. Нажмите на метку, чтобы открыть.",
                            style = Type.small, color = AppTheme.colors.ink2,
                        )
                    }
                }
            }
        }
        if (error != null && items.isEmpty()) {
            block { ErrorBlock(error!!) { version++ } }
        } else if (!loading && items.isEmpty()) {
            block { EmptyBlock("Ничего не нашлось", "Попробуйте изменить фильтры или поиск.") }
        }
        // Сортировки в API нет — сортируем загруженное, как features/events/sort.ts.
        if (!showMap) sortEvents(items, sort).forEach { e ->
            block(e.id) { EventCard(e, onClick = { state.nav.push(Screen.Event(e.id)) }, showAddress = authorized) }
        }
        if (loading) block { Loading() }
        if (!loading && page + 1 < totalPages) {
            block {
                AppButton("Показать ещё", variant = ButtonVariant.Ghost, onClick = { scope.launch { load(page + 1) } })
            }
        }
    }
}

@Composable
fun EventList(
    events: List<EventSummary>,
    empty: String,
    authorized: Boolean = true,
) {
    val state = LocalAppState.current
    Column(ContentWidth, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (events.isEmpty()) EmptyBlock(empty)
        events.forEach { e -> EventCard(e, onClick = { state.nav.push(Screen.Event(e.id)) }, showAddress = authorized) }
    }
}


enum class EventSort(val label: String) { Soon("ближайшие"), Seats("больше свободных мест"), Popular("популярные"), New("новые") }

fun sortEvents(events: List<EventSummary>, sort: EventSort): List<EventSummary> {
    fun free(e: EventSummary) = e.capacity?.let { (it - e.confirmedCount).coerceAtLeast(0) } ?: Int.MAX_VALUE
    return when (sort) {
        EventSort.Soon -> events.sortedBy { it.startsAt }
        EventSort.Seats -> events.sortedWith(compareByDescending<EventSummary> { free(it) }.thenBy { it.startsAt })
        EventSort.Popular -> events.sortedWith(compareByDescending<EventSummary> { it.confirmedCount }.thenBy { it.startsAt })
        EventSort.New -> events.sortedByDescending { it.id }
    }
}
