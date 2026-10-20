package com.crzto3.go2gether.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crzto3.go2gether.data.EventSummary
import com.crzto3.go2gether.data.Sport
import com.crzto3.go2gether.ui.LEVEL_LABELS
import com.crzto3.go2gether.ui.formatEventWhen
import com.crzto3.go2gether.ui.seatsShort
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type

enum class SportKind(val emoji: String, val cover: Color) {
    Football("⚽", Brand.Orange),
    Tennis("🎾", Brand.Lilac),
    Running("🏃", Brand.Accent),
    Chess("♟️", Brand.AccentSoft),
    Bike("🚴", Brand.Lilac),
    Custom("🏅", Brand.AccentSoft),
}

// Как sportIcon.ts: рисунок выбираем по названию направления.
private val RULES = listOf(
    Regex("футбол") to SportKind.Football,
    Regex("теннис|бадминтон|сквош|пинг") to SportKind.Tennis,
    Regex("шахмат|шашк|го\\b") to SportKind.Chess,
    Regex("вело|bmx") to SportKind.Bike,
    Regex("бег|пробеж|марафон|трейл") to SportKind.Running,
)

fun sportKind(sport: Sport): SportKind {
    val name = sport.name.lowercase().replace('ё', 'е')
    return RULES.firstOrNull { it.first.containsMatchIn(name) }?.second ?: SportKind.Custom
}

fun sportEmoji(sport: Sport): String = sport.icon?.takeIf { it.isNotBlank() } ?: sportKind(sport).emoji

/** Обложка в точку с крупной иконкой вида спорта. */
@Composable
fun SportCover(sport: Sport, modifier: Modifier = Modifier, height: Int = 112) {
    val kind = sportKind(sport)
    Box(modifier.fillMaxWidth().height(height.dp).background(kind.cover)) {
        Canvas(Modifier.matchParentSize()) {
            val step = 9.dp.toPx()
            var y = step / 2
            while (y < size.height) {
                var x = step / 2
                while (x < size.width) {
                    drawCircle(Color(0x33111111), radius = 1.5.dp.toPx(), center = Offset(x, y))
                    x += step
                }
                y += step
            }
        }
        Text(
            sportEmoji(sport),
            Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 14.dp),
            fontSize = (height * 0.7f).sp,
        )
    }
}

@Composable
fun EventCard(
    event: EventSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddress: Boolean = true,
    reasons: List<String> = emptyList(),
    badge: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth().clip(Radius.card).background(c.card).clickable(onClick = onClick)) {
        Box {
            SportCover(event.sport)
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Pill(event.sport.name, Color.White, Brand.OnBrand, Modifier.weight(1f, fill = false))
                if (event.joinMode == "approval") Pill("По заявке", Brand.OnBrand, Color.White)
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatEventWhen(event.startsAt), style = Type.smallBold, color = c.accentInk)
            Text(event.title, style = Type.cardTitle, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val place = if (showAddress) event.address?.takeIf { it.isNotBlank() } ?: event.city else event.city
            if (!place.isNullOrBlank()) {
                Text(place, style = Type.body, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TagsRow {
                event.level?.let { LEVEL_LABELS[it] }?.let { Pill(it) }
                Pill(seatsShort(event.confirmedCount, event.capacity))
                if (event.status != "published") StatusBadge(eventStatus(event.status))
                reasons.forEach { Pill("★ $it", c.reason) }
                badge?.invoke()
            }
            Text(event.organizer.name, style = Type.small, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
