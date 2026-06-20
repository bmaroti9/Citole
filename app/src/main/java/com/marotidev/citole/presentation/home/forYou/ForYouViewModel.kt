package com.marotidev.citole.presentation.home.forYou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ForYouViewModel @Inject constructor(
    audioRepository : AudioRepository,
) : ViewModel() {

    val recentlyAdded = audioRepository.allTracks.map { tracks ->
        tracks.sortedBy { it.dateAdded }.take(14)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

}