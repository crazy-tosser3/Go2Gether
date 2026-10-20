package com.crzto3.go2gether.data

/** Хранилище строк платформы: SharedPreferences на Android, NSUserDefaults на iOS. */
expect object KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
}

object TokenStorage {
    private const val KEY = "session_token"

    var token: String?
        get() = KeyValueStore.getString(KEY)
        set(value) = KeyValueStore.putString(KEY, value)
}
