package com.crzto3.go2gether.ui.map

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.crzto3.go2gether.config.AppConfig

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
actual fun PlatformWebMap(html: String, modifier: Modifier, onMessage: (String) -> Unit) {
    val callback by rememberUpdatedState(onMessage)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // Мост страница → приложение; вызывается не на главном потоке, поэтому переносим в него.
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun post(message: String) {
                            post { callback(message) }
                        }
                    },
                    "Android",
                )
                // Жесты на карте не должны прокручивать экран вокруг неё.
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }
            }
        },
        update = { view ->
            if (view.tag != html) {
                view.tag = html
                view.loadDataWithBaseURL(AppConfig.MAP_BASE_URL, html, "text/html", "utf-8", null)
            }
        },
        onRelease = { it.destroy() },
    )
}
