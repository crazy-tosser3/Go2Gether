package com.crzto3.go2gether.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crzto3.go2gether.ui.initials
import com.crzto3.go2gether.ui.seatsLabel
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type

enum class ButtonVariant { Primary, Accent, Ghost, Success, Danger }

/** Кнопка-«таблетка» как UiButton: одна primary в видимой области, accent — создание, ghost — второстепенное. */
@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val c = AppTheme.colors
    val active = enabled && !loading
    val (bg, fg) = when {
        !active -> c.mute to c.ink2
        variant == ButtonVariant.Primary -> c.ink to c.onInk
        variant == ButtonVariant.Accent -> Brand.Orange to Brand.OnBrand
        variant == ButtonVariant.Ghost -> c.card to c.ink
        variant == ButtonVariant.Success -> Brand.SuccessSolid to Color.White
        else -> Brand.DangerBg to Brand.DangerFg
    }
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(Radius.pill)
            .background(bg)
            .then(if (variant == ButtonVariant.Ghost && active) Modifier.border(2.dp, c.mute, Radius.pill) else Modifier)
            .clickable(enabled = active, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (loading) "Подождите…" else text, color = fg, style = Type.bodyBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, color: Color = AppTheme.colors.card, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(Radius.card).background(color).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun Pill(text: String, bg: Color = AppTheme.colors.field, fg: Color = AppTheme.colors.ink, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.clip(Radius.pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = Type.smallBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Text(
        text,
        Modifier
            .clip(Radius.pill)
            .background(if (selected) c.ink else c.card)
            .border(2.dp, if (selected) c.ink else c.mute, Radius.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = if (selected) c.onInk else c.ink,
        style = Type.smallBold,
    )
}

// Статус — всегда текстом и значком, цвет только помогает (как StatusBadge на сайте).
enum class Tone(val bg: Color, val fg: Color, val icon: String) {
    Ok(Brand.SuccessBg, Brand.SuccessFg, "✓"),
    Wait(Brand.WarningBg, Brand.WarningFg, "◷"),
    Queue(Brand.QueueBg, Brand.QueueFg, "≡"),
    No(Brand.DangerBg, Brand.DangerFg, "✕"),
    Off(Color(0xFFDADCE6), Color(0xFF4A4D5C), ""),
}

fun participationStatus(status: String): Pair<Tone, String> = when (status) {
    "confirmed" -> Tone.Ok to "Вы участник"
    "pending" -> Tone.Wait to "Ждёт решения"
    "waitlist" -> Tone.Queue to "В листе ожидания"
    "rejected" -> Tone.No to "Заявка отклонена"
    else -> Tone.Off to "Запись отменена"
}

fun eventStatus(status: String): Pair<Tone, String> = when (status) {
    "published" -> Tone.Ok to "Опубликовано"
    "unpublished" -> Tone.Off to "Снято с публикации"
    "deleted" -> Tone.Off to "Удалено"
    else -> Tone.Off to "Отменено организатором"
}

fun organizationStatus(status: String): Pair<Tone, String> = when (status) {
    "approved" -> Tone.Ok to "Допущена"
    "pending" -> Tone.Wait to "На рассмотрении"
    else -> Tone.No to "Отклонена"
}

@Composable
fun StatusBadge(status: Pair<Tone, String>) {
    val (tone, label) = status
    Pill(listOf(tone.icon, label).filter { it.isNotEmpty() }.joinToString(" "), tone.bg, tone.fg)
}

@Composable
fun SeatsBar(taken: Int, capacity: Int?) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (capacity != null && capacity > 0) {
            val fraction = (taken.toFloat() / capacity).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(10.dp).clip(Radius.pill).background(c.mute)) {
                Box(Modifier.fillMaxWidth(fraction).height(10.dp).clip(Radius.pill).background(c.ink))
            }
        }
        Text(seatsLabel(taken, capacity), style = Type.small, color = c.ink2)
    }
}

private val AVATAR_COLORS = listOf(Brand.Accent, Brand.Lilac, Brand.Orange, Brand.AccentSoft)

@Composable
fun Avatar(name: String, id: Long, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(AVATAR_COLORS[(id % AVATAR_COLORS.size).toInt()]),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), color = Brand.OnBrand, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val c = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = c.danger) } },
        singleLine = singleLine,
        minLines = minLines,
        shape = Radius.md,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = c.card,
            unfocusedContainerColor = c.field,
            focusedBorderColor = c.accentStrong,
            unfocusedBorderColor = c.mute,
            focusedLabelColor = c.accentStrong,
            unfocusedLabelColor = c.ink2,
            focusedTextColor = c.ink,
            unfocusedTextColor = c.ink,
            cursorColor = c.accentStrong,
        ),
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier, style = Type.h2, color = AppTheme.colors.ink)
}

@Composable
fun Loading(modifier: Modifier = Modifier.fillMaxWidth().padding(32.dp)) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.ink, strokeWidth = 3.dp)
    }
}

@Composable
fun ErrorBlock(message: String, onRetry: (() -> Unit)? = null) {
    AppCard(color = Brand.DangerBg) {
        Text(message, color = Brand.DangerFg, style = Type.body)
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text("Повторить", color = Brand.DangerFg, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun EmptyBlock(title: String, text: String? = null, action: (@Composable () -> Unit)? = null) {
    AppCard {
        Text(title, style = Type.bodyBold, color = AppTheme.colors.ink)
        if (text != null) Text(text, style = Type.body, color = AppTheme.colors.ink2)
        action?.invoke()
    }
}

@Composable
fun Notice(text: String, ok: Boolean = true) {
    val (bg, fg) = if (ok) Brand.SuccessBg to Brand.SuccessFg else Brand.DangerBg to Brand.DangerFg
    Text(text, Modifier.fillMaxWidth().clip(Radius.md).background(bg).padding(12.dp), color = fg, style = Type.body)
}

@Composable
fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Text(label, style = Type.small, color = AppTheme.colors.ink2)
        Text(value, style = Type.body, color = AppTheme.colors.ink)
    }
}

/** Диалог подтверждения; с [inputLabel] — ещё и поле ввода (причина, сообщение). */
@Composable
fun ConfirmDialog(
    title: String,
    text: String?,
    confirm: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    inputLabel: String? = null,
    inputRequired: Boolean = false,
    dismiss: String = "Отмена",
) {
    var input by remember { mutableStateOf("") }
    val c = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.card,
        title = { Text(title, style = Type.h2, color = c.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (text != null) Text(text, style = Type.body, color = c.ink2)
                if (inputLabel != null) AppTextField(input, { input = it }, inputLabel, singleLine = false, minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input.trim()) }, enabled = !inputRequired || input.isNotBlank()) {
                Text(confirm, fontWeight = FontWeight.Bold, color = c.ink)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismiss, color = c.ink2) } },
    )
}

@Composable
fun TagsRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        content()
    }
}
