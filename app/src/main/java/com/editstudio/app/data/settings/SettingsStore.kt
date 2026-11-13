package com.editstudio.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "edit_studio_settings")

class SettingsStore(private val context: Context) {
    companion object {
        val KEY_CONTENT_SOURCE_URL = stringPreferencesKey("content_source_url")
        val KEY_IS_SIMPLE_MODE = booleanPreferencesKey("is_simple_mode")
        val KEY_LOW_QUALITY_PREVIEW = booleanPreferencesKey("low_quality_preview")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val KEY_GUIDED_TOUR_DONE = booleanPreferencesKey("guided_tour_done")
        val KEY_FAVORITE_PRESETS = stringPreferencesKey("favorite_presets")
        val KEY_RECENT_PRESETS = stringPreferencesKey("recent_presets")
        val KEY_FAVORITE_EFFECTS = stringPreferencesKey("favorite_effects")
        val KEY_RECENT_EFFECTS = stringPreferencesKey("recent_effects")

        const val DEFAULT_CONTENT_URL = "https://raw.githubusercontent.com/editstudio/manifest/main/manifest.json"
    }

    val contentSourceUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CONTENT_SOURCE_URL] ?: DEFAULT_CONTENT_URL
    }

    val isSimpleMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_SIMPLE_MODE] ?: true
    }

    val isLowQualityPreview: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOW_QUALITY_PREVIEW] ?: false
    }

    val isWifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIFI_ONLY] ?: false
    }

    val isGuidedTourDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GUIDED_TOUR_DONE] ?: false
    }

    val favoritePresets: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_PRESETS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    val recentPresets: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_RECENT_PRESETS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    val favoriteEffects: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_EFFECTS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    val recentEffects: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_RECENT_EFFECTS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setContentSourceUrl(url: String) {
        context.dataStore.edit { prefs -> prefs[KEY_CONTENT_SOURCE_URL] = url }
    }

    suspend fun setSimpleMode(isSimple: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_IS_SIMPLE_MODE] = isSimple }
    }

    suspend fun setLowQualityPreview(lowQuality: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_LOW_QUALITY_PREVIEW] = lowQuality }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_WIFI_ONLY] = wifiOnly }
    }

    suspend fun setGuidedTourDone(done: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_GUIDED_TOUR_DONE] = done }
    }

    suspend fun toggleFavoritePreset(presetId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_PRESETS]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (!current.add(presetId)) current.remove(presetId)
            prefs[KEY_FAVORITE_PRESETS] = current.joinToString(",")
        }
    }

    suspend fun addRecentPreset(presetId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_PRESETS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            current.remove(presetId)
            current.add(0, presetId)
            if (current.size > 20) current.removeAt(current.size - 1)
            prefs[KEY_RECENT_PRESETS] = current.joinToString(",")
        }
    }

    suspend fun toggleFavoriteEffect(effectId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_EFFECTS]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (!current.add(effectId)) current.remove(effectId)
            prefs[KEY_FAVORITE_EFFECTS] = current.joinToString(",")
        }
    }

    suspend fun addRecentEffect(effectId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_EFFECTS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            current.remove(effectId)
            current.add(0, effectId)
            if (current.size > 20) current.removeAt(current.size - 1)
            prefs[KEY_RECENT_EFFECTS] = current.joinToString(",")
        }
    }
}
