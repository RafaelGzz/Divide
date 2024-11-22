package com.ragl.divide.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ragl.divide.ui.utils.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_START_DESTINATION = stringPreferencesKey("start_destination")
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
        const val TAG = "PreferencesRepositoryImpl"
    }

    val startDestinationFlow: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_START_DESTINATION] ?: Screen.Login.route
        }

    val darkModeFlow: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            it[KEY_DARK_MODE]
        }

    suspend fun saveStartDestination(startDestination: String) {
        dataStore.edit {
            it[KEY_START_DESTINATION] = startDestination
        }
    }

    suspend fun saveDarkMode(darkMode: Boolean) {
        dataStore.edit {
            it[KEY_DARK_MODE] = darkMode.toString()
        }
    }
}