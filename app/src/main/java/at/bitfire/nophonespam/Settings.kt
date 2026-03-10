package at.bitfire.nophonespam

import android.content.Context

class Settings(context: Context) {

    private val pref = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    var blockHiddenNumbers: Boolean
        get() = pref.getBoolean(PREF_BLOCK_HIDDEN_NUMBERS, false)
        set(value) { pref.edit().putBoolean(PREF_BLOCK_HIDDEN_NUMBERS, value).apply() }

    var showNotifications: Boolean
        get() = pref.getBoolean(PREF_NOTIFICATIONS, true)
        set(value) { pref.edit().putBoolean(PREF_NOTIFICATIONS, value).apply() }

    companion object {
        private const val PREF_BLOCK_HIDDEN_NUMBERS = "blockHiddenNumbers"
        private const val PREF_NOTIFICATIONS = "notifications"
    }
}
