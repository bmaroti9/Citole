package com.marotidev.citole.presentation.app

import androidx.lifecycle.ViewModel
import com.marotidev.citole.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    audioRepository: AudioRepository
) : ViewModel() {
    val startDestination = if (audioRepository.checkHasAudioPermission()) {
        TracksViewDestination
    } else {
        OnboardViewDestination
    }
}