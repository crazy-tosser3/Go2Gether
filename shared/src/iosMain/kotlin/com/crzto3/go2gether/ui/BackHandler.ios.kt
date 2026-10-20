package com.crzto3.go2gether.ui

import androidx.compose.runtime.Composable

// На iOS системной кнопки «назад» нет — возврат через стрелку в шапке экрана.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
