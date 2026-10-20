package com.crzto3.go2gether.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Токены дизайн-системы Go2Gether из frontend/src/assets/main.css.
// В тёмной теме меняются только нейтральные цвета; бренд-цвета и статусы одинаковы.
object Brand {
    val Accent = Color(0xFFA5B4FF)
    val AccentSoft = Color(0xFFC9D3FF)
    val Orange = Color(0xFFF28C28)
    val Lilac = Color(0xFFE9B5F5)
    val Cream = Color(0xFFFFF7EC)
    val Signal = Color(0xFFE2432B)
    val OnBrand = Color(0xFF111111)

    val SuccessBg = Color(0xFFCFF3DC)
    val SuccessFg = Color(0xFF0E5A2E)
    val SuccessSolid = Color(0xFF1F8A4C)
    val WarningBg = Color(0xFFFFE7B3)
    val WarningFg = Color(0xFF6B4500)
    val QueueBg = Color(0xFFE6DDFF)
    val QueueFg = Color(0xFF4B2FA8)
    val DangerBg = Color(0xFFFFD6D1)
    val DangerFg = Color(0xFF8C1D18)
}

@Immutable
data class AppColors(
    val ink: Color,
    val ink2: Color,
    val page: Color,
    val card: Color,
    val field: Color,
    val mute: Color,
    val accentInk: Color,
    val accentStrong: Color,
    val reason: Color,
    val danger: Color,
    /** Текст на кнопке primary (фон — ink). */
    val onInk: Color,
)

private val LightColors = AppColors(
    ink = Color(0xFF111111),
    ink2 = Color(0xFF4A4D5C),
    page = Color(0xFFEEEFF4),
    card = Color(0xFFFFFFFF),
    field = Color(0xFFEEEFF4),
    mute = Color(0xFFDADCE6),
    accentInk = Color(0xFF2B3480),
    accentStrong = Color(0xFF2F3CF5),
    reason = Color(0xFFF3E0F9),
    danger = Color(0xFF8C1D18),
    onInk = Color(0xFFFFFFFF),
)

private val DarkColors = AppColors(
    ink = Color(0xFFF2F3F7),
    ink2 = Color(0xFFA9ACBD),
    page = Color(0xFF0F1016),
    card = Color(0xFF1B1D27),
    field = Color(0xFF262938),
    mute = Color(0xFF3A3D50),
    accentInk = Color(0xFFA5B4FF),
    accentStrong = Color(0xFFA5B4FF),
    reason = Color(0xFF3B2A47),
    danger = Color(0xFFFF9B8F),
    onInk = Color(0xFF111111),
)

val LocalAppColors = staticCompositionLocalOf { LightColors }

object Radius {
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(22.dp)
    val card = RoundedCornerShape(26.dp)
    val pill = RoundedCornerShape(50)
}

/** Заголовки как Exo 2 на сайте: жирный курсив, крупные — заглавными. */
object Type {
    val display = TextStyle(fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
    val h2 = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
    val cardTitle = TextStyle(fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
    val body = TextStyle(fontSize = 16.sp, lineHeight = 23.sp)
    val bodyBold = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
    val small = TextStyle(fontSize = 14.sp, lineHeight = 19.sp)
    val smallBold = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
    val tiny = TextStyle(fontSize = 11.sp, lineHeight = 13.sp)
}

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun Go2GetherTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val c = if (dark) DarkColors else LightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.ink, onPrimary = c.onInk, secondary = Brand.Orange, onSecondary = Brand.OnBrand,
            background = c.page, onBackground = c.ink, surface = c.card, onSurface = c.ink,
            surfaceVariant = c.field, onSurfaceVariant = c.ink2, outline = c.mute, error = c.danger,
            surfaceContainerHigh = c.card, surfaceContainer = c.card,
        )
    } else {
        lightColorScheme(
            primary = c.ink, onPrimary = c.onInk, secondary = Brand.Orange, onSecondary = Brand.OnBrand,
            background = c.page, onBackground = c.ink, surface = c.card, onSurface = c.ink,
            surfaceVariant = c.field, onSurfaceVariant = c.ink2, outline = c.mute, error = c.danger,
            surfaceContainerHigh = c.card, surfaceContainer = c.card,
        )
    }
    CompositionLocalProvider(LocalAppColors provides c) {
        MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
    }
}
