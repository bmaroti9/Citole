package com.marotidev.citole.presentation.onboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val audioRepository : AudioRepository,
) : ViewModel() {
    val count = 8

    val artworkUris = audioRepository.allTracks.map {
        val tracks = it.shuffled()
        List(count) { index ->
            if (index < tracks.size) tracks[index].artworkUri else Uri.EMPTY
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = List(count) {Uri.EMPTY}
    )

    fun onPermissionGranted() {
        audioRepository.fetchOrUpdateTracks()
    }

    fun checkHasAudioPermission() : Boolean {
        return audioRepository.checkHasAudioPermission()
    }
}