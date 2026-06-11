/*
Copyright (C) <2026>  <Balint Maroti>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

*/

package com.marotidev.citole.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreService(private val context: Context) {

    companion object {
        val CHIP_SHOW_SONGS = booleanPreferencesKey("chip_show_songs")
        val CHIP_SHOW_PODCASTS = booleanPreferencesKey("chip_show_podcasts")
        val CHIP_SHOW_AUDIOBOOKS = booleanPreferencesKey("chip_show_albums")
        val CHIP_SHOW_OTHER = booleanPreferencesKey("chip_show_other")
    }

    suspend fun saveChipShowSongs(to: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_SONGS] = to
        }
    }

    suspend fun saveChipShowPodcasts(to: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_PODCASTS] = to
        }
    }

    suspend fun saveChipShowAudiobooks(to: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_AUDIOBOOKS] = to
        }
    }

    suspend fun saveChipShowOther(to: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_OTHER] = to
        }
    }

    val chipShowSongs: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_SONGS] ?: true}

    val chipShowPodcasts: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_PODCASTS] ?: false}

    val chipShowAudiobooks: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_AUDIOBOOKS] ?: false}

    val chipShowOther: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_OTHER] ?: false}
}