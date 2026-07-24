package com.marotidev.citole.presentation.home.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.presentation.app.PlaylistViewDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    audioRepository : AudioRepository,
    savedStateHandle: SavedStateHandle,
    recommendationRepository: RecommendationRepository
) : ViewModel() {
    private val playlistName = savedStateHandle.toRoute<PlaylistViewDestination>().playlistName

//    val playlistItems = audioRepository.allTracks.map { tracks ->
//        tracks.filter { it.id  }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )
//
//    val similarAlbums = combine(
//        audioRepository.allAlbums,
//        audioRepository.allTracks,
//        album
//    ) { albums, tracks, album ->
//        recommendationRepository.findSimilarAlbums(album, albums, tracks, 8)
//    }
//        .flowOn(Dispatchers.Default)
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
}