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

package com.marotidev.citole.data.repository

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.marotidev.citole.presentation.browse.SortChip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val application: Application
) {

    val Context.dataStore by preferencesDataStore(name = "data_store")

    companion object {
        val CHIP_SHOW_SONGS = booleanPreferencesKey("chip_show_songs")
        val CHIP_SHOW_PODCASTS = booleanPreferencesKey("chip_show_podcasts")
        val CHIP_SHOW_AUDIOBOOKS = booleanPreferencesKey("chip_show_albums")
        val CHIP_SHOW_OTHER = booleanPreferencesKey("chip_show_other")
        val CHIP_SORT_CHIP = intPreferencesKey("chip_sort_chip")
        val CHIP_SORT_REVERSED = booleanPreferencesKey("chip_sort_reversed")
        val SHUFFLE_DISCOVERY_RADIUS = floatPreferencesKey("shuffle_discovery_radius")
    }

    suspend fun saveChipShowSongs(to: Boolean) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_SONGS] = to
        }
    }

    suspend fun saveChipShowPodcasts(to: Boolean) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_PODCASTS] = to
        }
    }

    suspend fun saveChipShowAudiobooks(to: Boolean) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_AUDIOBOOKS] = to
        }
    }

    suspend fun saveChipShowOther(to: Boolean) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SHOW_OTHER] = to
        }
    }

    suspend fun saveChipSortChip(to: SortChip) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SORT_CHIP] = to.ordinal
        }
    }

    suspend fun saveChipSortReversed(to: Boolean) {
        application.dataStore.edit { preferences ->
            preferences[CHIP_SORT_REVERSED] = to
        }
    }

    suspend fun saveShuffleDiscoveryRadius(to: Float) {
        application.dataStore.edit { preferences ->
            preferences[SHUFFLE_DISCOVERY_RADIUS] = to
        }
    }

    val chipShowSongs: Flow<Boolean> = application.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_SONGS] ?: true}

    val chipShowPodcasts: Flow<Boolean> = application.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_PODCASTS] ?: false}

    val chipShowAudiobooks: Flow<Boolean> = application.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_AUDIOBOOKS] ?: false}

    val chipShowOther: Flow<Boolean> = application.dataStore.data
        .map { preferences -> preferences[CHIP_SHOW_OTHER] ?: false}

    val chipSortChip: Flow<SortChip> = application.dataStore.data.map { preferences ->
        SortChip.entries[preferences[CHIP_SORT_CHIP] ?: 0]
    }

    val chipSortReversed: Flow<Boolean> = application.dataStore.data
        .map { preferences -> preferences[CHIP_SORT_REVERSED] ?: false}

    val shuffleDiscoveryRadius: Flow<Float> = application.dataStore.data.map { preferences ->
        preferences[SHUFFLE_DISCOVERY_RADIUS] ?: 0.5f
    }
}