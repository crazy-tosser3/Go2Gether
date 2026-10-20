package com.crzto3.go2gether.data

import android.content.Context
import android.content.SharedPreferences
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpEngine(): HttpClientEngineFactory<*> = OkHttp

actual object KeyValueStore {
    private var prefs: SharedPreferences? = null

    /** Вызывается из MainActivity до первого обращения к хранилищу. */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("go2gether", Context.MODE_PRIVATE)
    }

    actual fun getString(key: String): String? = prefs?.getString(key, null)

    actual fun putString(key: String, value: String?) {
        prefs?.edit()?.apply { if (value == null) remove(key) else putString(key, value) }?.apply()
    }
}
