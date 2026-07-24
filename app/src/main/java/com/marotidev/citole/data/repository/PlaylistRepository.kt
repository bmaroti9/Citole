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

import com.marotidev.citole.data.local.PlaylistDao
import com.marotidev.citole.data.local.PlaylistGroup
import com.marotidev.citole.data.local.PlaylistTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    var allPlaylists: MutableStateFlow<List<PlaylistGroup>> = MutableStateFlow(emptyList())

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun fetchAllPlaylists() {
        serviceScope.launch {
            allPlaylists.value = playlistDao.getAllPlaylistGroups()
        }
    }

    suspend fun getTracksFromPlaylist(id: Int) : List<PlaylistTrack>{
        return playlistDao.getTracksFromPlaylist(id)
    }
}