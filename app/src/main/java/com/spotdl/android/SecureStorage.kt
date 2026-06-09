package com.spotdl.android

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    companion object {
        private const val PREFS_FILE = "spotdl_secure_prefs"
        private const val KEY_SITE_URL = "site_url"
        private const val KEY_AUTH_URL = "auth_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_JELLYFIN_URL = "jellyfin_url"
        private const val KEY_IS_CONFIGURED = "is_configured"
    }

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(siteUrl: String, authUrl: String, username: String, password: String, jellyfinUrl: String = "") {
        prefs.edit()
            .putString(KEY_SITE_URL, siteUrl)
            .putString(KEY_AUTH_URL, authUrl)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_JELLYFIN_URL, jellyfinUrl)
            .putBoolean(KEY_IS_CONFIGURED, true)
            .apply()
    }

    fun getSiteUrl(): String = prefs.getString(KEY_SITE_URL, "https://spotdl.hrs-pacs.fr") ?: "https://spotdl.hrs-pacs.fr"

    fun getAuthUrl(): String = prefs.getString(KEY_AUTH_URL, "https://auth.hrs-pacs.fr") ?: "https://auth.hrs-pacs.fr"

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getPassword(): String = prefs.getString(KEY_PASSWORD, "") ?: ""

    fun getJellyfinUrl(): String = prefs.getString(KEY_JELLYFIN_URL, "https://jelly.hrs-pacs.fr") ?: "https://jelly.hrs-pacs.fr"

    fun isConfigured(): Boolean = prefs.getBoolean(KEY_IS_CONFIGURED, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
