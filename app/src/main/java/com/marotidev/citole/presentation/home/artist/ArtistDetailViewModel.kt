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

package com.marotidev.citole.presentation.home.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.presentation.app.ArtistViewDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    audioRepository : AudioRepository,
    savedStateHandle: SavedStateHandle,
    recommendationRepository: RecommendationRepository
) : ViewModel() {
    private val artistName = savedStateHandle.toRoute<ArtistViewDestination>().artistName

    val artist = audioRepository.allArtists.map { artists ->
        artists.find{ it.name == artistName}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val similarArtists = combine(
        audioRepository.allArtists,
        audioRepository.allTracks,
        artist
    ) { artists, tracks, artist ->
        recommendationRepository.findSimilarArtists(artist, artists, tracks, 8)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}