package com.crzto3.go2gether.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.crzto3.go2gether.config.AppConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/** Мост страница → приложение: window.webkit.messageHandlers.app.postMessage(...). */
private class MessageHandler(var onMessage: (String) -> Unit) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        (didReceiveScriptMessage.body as? String)?.let(onMessage)
    }
}

/** WKWebView и последняя загруженная страница — чтобы не перезагружать карту без изменений. */
private class MapHolder(val view: WKWebView, val handler: MessageHandler) {
    var loaded: String? = null
}

@OptIn(ExperimentalForeignApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformWebMap(html: String, modifier: Modifier, onMessage: (String) -> Unit) {
    val callback by rememberUpdatedState(onMessage)
    val holder = remember {
        val handler = MessageHandler { callback(it) }
        val config = WKWebViewConfiguration().apply { userContentController.addScriptMessageHandler(handler, "app") }
        MapHolder(WKWebView(frame = CGRectZero.readValue(), configuration = config), handler)
    }
    UIKitView(
        factory = { holder.view },
        modifier = modifier,
        update = {
            if (holder.loaded != html) {
                holder.loaded = html
                it.loadHTMLString(html, baseURL = NSURL(string = AppConfig.MAP_BASE_URL))
            }
        },
        onRelease = { it.configuration.userContentController.removeScriptMessageHandlerForName("app") },
        // Жесты отдаём карте сразу, иначе их перехватывает прокрутка экрана.
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative),
    )
}
