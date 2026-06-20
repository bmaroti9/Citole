package com.marotidev.citole.presentation.home.album

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.presentation.app.AlbumViewDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    audioRepository : AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val albumId = savedStateHandle.toRoute<AlbumViewDestination>().albumId

    val album = audioRepository.allAlbums.map { albums ->
        albums.find{ it.albumId == albumId}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}