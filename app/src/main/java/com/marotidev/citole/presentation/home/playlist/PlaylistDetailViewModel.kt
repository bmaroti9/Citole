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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.PlaylistRepository
import com.marotidev.citole.presentation.app.PlaylistViewDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    audioRepository : AudioRepository,
    savedStateHandle: SavedStateHandle,
    playlistRepository: PlaylistRepository
) : ViewModel() {
    private val playlistId = savedStateHandle.toRoute<PlaylistViewDestination>().playlistId

    val playlistGroup = playlistRepository.getPlaylistGroupFromId(playlistId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val tracks = combine(
        audioRepository.allTracks,
        playlistRepository.getPlaylistTracksFromId(playlistId)
    ) { allTracks, playlistTracks ->
        playlistTracks.mapNotNull { track -> allTracks.find { it.id == track.trackId }}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}