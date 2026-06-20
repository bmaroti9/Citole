package com.marotidev.citole.presentation.home.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.app.ArtistViewDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    audioRepository : AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val artistName = savedStateHandle.toRoute<ArtistViewDestination>().artistName

    val artist = audioRepository.allArtists.map { artists ->
        artists.find{ it.name == artistName}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}