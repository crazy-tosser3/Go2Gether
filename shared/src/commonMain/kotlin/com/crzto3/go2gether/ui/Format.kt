package com.crzto3.go2gether.ui

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Подписи и форматы — как в frontend/src/i18n/ru.ts и utils/datetime.ts.

val LEVEL_LABELS = mapOf("beginner" to "Новичок", "amateur" to "Любитель", "pro" to "Профи")

val JOIN_MODE_LABELS = mapOf("open" to "Свободная запись", "approval" to "По заявке")

val ORG_TYPE_LABELS = mapOf(
    "club" to "Клуб", "community" to "Сообщество", "federation" to "Федерация", "company" to "Компания",
)

val REPORT_REASON_LABELS = mapOf(
    "spam" to "Спам", "fraud" to "Мошенничество", "offensive" to "Оскорбления",
    "dangerous" to "Опасно", "misleading" to "Вводит в заблуждение", "other" to "Другое",
)

val RECOMMENDATION_REASONS = mapOf(
    "sport" to "ваш вид спорта", "level" to "ваш уровень", "city" to "в вашем городе",
    "nearby" to "рядом", "time" to "удобное время", "popular" to "популярное", "soon" to "скоро",
    "age" to "ваш возраст", "seats" to "больше свободных мест",
)

private val MONTHS = listOf("янв.", "февр.", "мар.", "апр.", "мая", "июн.", "июл.", "авг.", "сент.", "окт.", "нояб.", "дек.")
private val WEEKDAYS = mapOf(
    DayOfWeek.MONDAY to "пн", DayOfWeek.TUESDAY to "вт", DayOfWeek.WEDNESDAY to "ср", DayOfWeek.THURSDAY to "чт",
    DayOfWeek.FRIDAY to "пт", DayOfWeek.SATURDAY to "сб", DayOfWeek.SUNDAY to "вс",
)

private val tz get() = TimeZone.currentSystemDefault()

fun parseLocal(iso: String): LocalDateTime? =
    runCatching { Instant.parse(iso).toLocalDateTime(tz) }.getOrElse {
        runCatching { LocalDateTime.parse(iso.removeSuffix("Z")) }.getOrNull()
    }

fun today(): LocalDate = Clock.System.now().toLocalDateTime(tz).date

private fun two(n: Int) = n.toString().padStart(2, '0')

fun formatTime(iso: String): String = parseLocal(iso)?.let { "${two(it.hour)}:${two(it.minute)}" } ?: ""

fun formatDay(date: LocalDate): String {
    val day = "${WEEKDAYS[date.dayOfWeek]}, ${date.day} ${MONTHS[date.month.ordinal]}"
    return day.replaceFirstChar { it.uppercase() }
}

/** «Сегодня · 19:00», «Завтра · 07:30–09:00», «Сб, 24 окт. · 10:00». */
fun formatEventWhen(startsAt: String, endsAt: String? = null): String {
    val start = parseLocal(startsAt) ?: return startsAt
    val diff = today().daysUntil(start.date)
    val day = when (diff) {
        0 -> "Сегодня"
        1 -> "Завтра"
        else -> formatDay(start.date)
    }
    val range = if (endsAt != null) "${formatTime(startsAt)}–${formatTime(endsAt)}" else formatTime(startsAt)
    return "$day · $range"
}

fun formatDateTime(iso: String?): String {
    val d = iso?.let(::parseLocal) ?: return ""
    return "${d.day} ${MONTHS[d.month.ordinal]} ${d.year}, ${two(d.hour)}:${two(d.minute)}"
}

/** Ввод «ДД.ММ.ГГГГ» и «ЧЧ:ММ» → ISO в UTC для бэка. */
fun toIso(date: String, time: String): String? = runCatching {
    val (d, m, y) = date.trim().split('.').map { it.toInt() }
    val (h, min) = time.trim().split(':').map { it.toInt() }
    LocalDateTime(y, m, d, h, min).toInstant(tz).toString()
}.getOrNull()

fun formatInputDate(date: LocalDate) = "${two(date.day)}.${two(date.month.ordinal + 1)}.${date.year}"

/** Дата и время мероприятия для полей формы: «ДД.ММ.ГГГГ» и «ЧЧ:ММ» в местном поясе. */
fun inputDate(iso: String?): String = iso?.let(::parseLocal)?.let { formatInputDate(it.date) }.orEmpty()
fun inputTime(iso: String?): String = iso?.let(::parseLocal)?.let { "${two(it.hour)}:${two(it.minute)}" }.orEmpty()

fun isFuture(iso: String): Boolean = runCatching { Instant.parse(iso) > Clock.System.now() }.getOrDefault(true)

fun isoDate(date: LocalDate) = date.toString()

enum class QuickDate(val label: String) { TODAY("Сегодня"), TOMORROW("Завтра"), WEEKEND("Выходные") }

/** Диапазон для быстрых фильтров; выходные — ближайшие суббота и воскресенье. */
fun quickDateRange(kind: QuickDate): Pair<LocalDate, LocalDate> {
    val t = today()
    return when (kind) {
        QuickDate.TODAY -> t to t
        QuickDate.TOMORROW -> t.plus(DatePeriod(days = 1)).let { it to it }
        QuickDate.WEEKEND -> {
            val dow = t.dayOfWeek.ordinal // пн = 0
            val toSat = if (dow == 6) -1 else 5 - dow
            val sat = t.plus(DatePeriod(days = toSat))
            val sun = sat.plus(DatePeriod(days = 1))
            (if (sat < t) t else sat) to sun
        }
    }
}

/** Русские формы слова по числу. */
fun plural(n: Int, one: String, few: String, many: String): String {
    val m10 = n % 10
    val m100 = n % 100
    return when {
        m10 == 1 && m100 != 11 -> one
        m10 in 2..4 && (m100 < 12 || m100 > 14) -> few
        else -> many
    }
}

fun participantsLabel(n: Int) = "$n ${plural(n, "участник", "участника", "участников")}"
fun placesLabel(n: Int) = "$n ${plural(n, "место", "места", "мест")}"

fun seatsShort(taken: Int, capacity: Int?): String {
    if (capacity == null || capacity <= 0) return participantsLabel(taken)
    val left = (capacity - taken).coerceAtLeast(0)
    return if (left == 0) "мест нет" else "ещё ${placesLabel(left)}"
}

fun seatsLabel(taken: Int, capacity: Int?): String {
    if (capacity == null || capacity <= 0) return participantsLabel(taken)
    val left = (capacity - taken).coerceAtLeast(0)
    return if (left == 0) "$taken из $capacity · мест нет" else "$taken из $capacity · осталось ${placesLabel(left)}"
}

fun initials(name: String): String =
    name.split(' ', '-').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
