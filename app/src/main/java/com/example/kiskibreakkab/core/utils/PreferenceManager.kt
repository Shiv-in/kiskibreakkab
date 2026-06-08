package com.example.kiskibreakkab.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        val SELECTED_DEPT = stringPreferencesKey("selected_dept")
    }

    val selectedDay: Flow<String?> = dataStore.data.map { it[PreferencesKeys.SELECTED_DAY] }
    val selectedSlot: Flow<Int?> = dataStore.data.map { it[PreferencesKeys.SELECTED_SLOT] }
    val selectedBlock: Flow<String?> = dataStore.data.map { it[PreferencesKeys.SELECTED_BLOCK] }
    val selectedDept: Flow<String?> = dataStore.data.map { it[PreferencesKeys.SELECTED_DEPT] }

    suspend fun saveSelectedDay(day: String) {
        dataStore.edit { it[PreferencesKeys.SELECTED_DAY] = day }
    }

    suspend fun saveSelectedSlot(slot: Int) {
        dataStore.edit { it[PreferencesKeys.SELECTED_SLOT] = slot }
    }

    suspend fun saveSelectedBlock(block: String?) {
        dataStore.edit { 
            if (block != null) it[PreferencesKeys.SELECTED_BLOCK] = block
            else it.remove(PreferencesKeys.SELECTED_BLOCK)
        }
    }

    suspend fun saveSelectedDept(dept: String?) {
        dataStore.edit {
            if (dept != null) it[PreferencesKeys.SELECTED_DEPT] = dept
            else it.remove(PreferencesKeys.SELECTED_DEPT)
        }
    }
}
