package com.example.kiskibreakkab.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    object PreferencesKeys {
        val SELECTED_DAY = stringPreferencesKey("selected_day")
        val SELECTED_SLOT = intPreferencesKey("selected_slot")
        val SELECTED_BLOCK = stringPreferencesKey("selected_block")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    val selectedDay: Flow<String?> = dataStore.data.map { it[PreferencesKeys.SELECTED_DAY] }
    val selectedSlot: Flow<Int?> = dataStore.data.map { it[PreferencesKeys.SELECTED_SLOT] }
    val selectedBlock: Flow<String?> = dataStore.data.map { it[PreferencesKeys.SELECTED_BLOCK] }
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.IS_DARK_THEME] ?: true }

    suspend fun saveTheme(isDark: Boolean) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun saveSelectedDay(day: String) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.SELECTED_DAY] = day
        }
    }

    suspend fun saveSelectedSlot(slot: Int) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.SELECTED_SLOT] = slot
        }
    }

    suspend fun saveSelectedBlock(block: String?) {
        dataStore.edit { preferences: MutablePreferences ->
            if (block != null) {
                preferences[PreferencesKeys.SELECTED_BLOCK] = block
            } else {
                preferences.remove(PreferencesKeys.SELECTED_BLOCK)
            }
        }
    }
}
