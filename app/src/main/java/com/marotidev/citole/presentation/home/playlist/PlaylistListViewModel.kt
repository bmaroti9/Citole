package com.marotidev.citole.presentation.home.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.service.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

data class PlaylistItem(
    val name : String,
    val tracks: List<AudioService.TrackData>,
)

@HiltViewModel
class PlaylistListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    dataStoreRepository: DataStoreRepository,
) : ViewModel() {

    val allPlaylists = combine(
        dataStoreRepository.favoriteTrackIds,
        audioRepository.allTracks
    ) { favoriteIds, allTracks ->
        val favoriteTracks = allTracks.filter { it.id in favoriteIds }
        listOf(PlaylistItem("Favorites", favoriteTracks))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}