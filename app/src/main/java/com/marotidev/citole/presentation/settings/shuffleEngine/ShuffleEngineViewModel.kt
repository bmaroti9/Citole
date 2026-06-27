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

package com.marotidev.citole.presentation.settings.shuffleEngine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShuffleEngineViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    var discoveryRadiusValue by mutableFloatStateOf(0f)
        private set

    var queueTrajectoryValue by mutableFloatStateOf(0f)
        private set

    init {
        viewModelScope.launch {
            discoveryRadiusValue = dataStoreRepository.shuffleDiscoveryRadius.first()
            queueTrajectoryValue = dataStoreRepository.shuffleQueueTrajectory.first()
        }
    }

    fun updateDiscoveryRadiusSliderValue(to: Float) {
        discoveryRadiusValue = to
    }

    fun updateQueueTrajectorySliderValue(to: Float) {
        queueTrajectoryValue = to
    }

    fun updateDataStoreDiscoveryRadiusSliderValue() {
        viewModelScope.launch {
            dataStoreRepository.saveShuffleDiscoveryRadius(discoveryRadiusValue)
        }
    }

    fun updateDataStoreQueueTrajectorySliderValue() {
        viewModelScope.launch {
            dataStoreRepository.saveShuffleQueueTrajectory(queueTrajectoryValue)
        }
    }
}