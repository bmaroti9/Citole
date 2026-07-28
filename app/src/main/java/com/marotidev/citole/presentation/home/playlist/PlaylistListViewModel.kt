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


package com.marotidev.citole.presentation.home.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.PlaylistRepository
import com.marotidev.citole.data.service.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PlaylistItem(
    val id: Int,
    val name : String,
    val tracks: List<AudioService.TrackData>,
)

@HiltViewModel
class PlaylistListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    playlistRepository: PlaylistRepository
) : ViewModel() {

    val allPlaylists = combine(
        playlistRepository.allPlaylistGroups,
        playlistRepository.allPlaylistTracks,
        audioRepository.allTracks
    ) { allPlaylistGroups, allPlaylistTracks, allTracks ->

        val groupedPlaylistTracks = allPlaylistTracks.groupBy { it.playlistId }

        allPlaylistGroups.map { playlistGroup ->
            val playlistTracks = groupedPlaylistTracks[playlistGroup.id]?.sortedBy { it.playlistIndex } ?: emptyList()
            PlaylistItem(
                id = playlistGroup.id,
                name = playlistGroup.name,
                tracks = playlistTracks.mapNotNull { track -> allTracks.find { it.id == track.trackId } }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}