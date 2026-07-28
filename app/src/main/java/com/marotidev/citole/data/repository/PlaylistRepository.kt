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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    var allPlaylistGroups: Flow<List<PlaylistGroup>> = playlistDao.getAllPlaylistGroups()
    var allPlaylistTracks: Flow<List<PlaylistTrack>> = playlistDao.getAllPlaylistTracks()

    fun getPlaylistTracksFromId(id: Int) : Flow<List<PlaylistTrack>>{
        return playlistDao.getPlaylistTracksFromId(id)
    }

    fun getPlaylistGroupFromId(id: Int) : Flow<PlaylistGroup>{
        return playlistDao.getPlaylistGroupFromId(id)
    }

    suspend fun removeFromPlaylist(trackId: Long, queueId: Int) {
        playlistDao.deletePlaylistTrack(trackId, queueId)
    }

    suspend fun addToPlaylist(playlistTrack: PlaylistTrack) {
        playlistDao.insertAll(listOf(playlistTrack))
    }
}