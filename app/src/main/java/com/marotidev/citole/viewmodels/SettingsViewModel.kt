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

package com.marotidev.citole.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.services.DataStoreService
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreService = DataStoreService(application)

//    val chipShowSongs = service.chipShowSongs
//    val chipShowPodcasts = service.chipShowPodcasts
//    val chipShowAudiobooks = service.chipShowAudiobooks
//    val chipShowOther = service.chipShowOther
//
//    fun setChipShowSongs(to: Boolean) {
//        viewModelScope.launch {
//            service.saveChipShowSongs(to)
//        }
//    }
//
//    fun setChipShowPodcasts(to: Boolean) {
//        viewModelScope.launch {
//            service.saveChipShowPodcasts(to)
//        }
//    }
//
//    fun setChipShowAudiobooks(to: Boolean) {
//        viewModelScope.launch {
//            service.saveChipShowAudiobooks(to)
//        }
//    }
//
//    fun setChipShowOther(to: Boolean) {
//        viewModelScope.launch {
//            service.saveChipShowOther(to)
//        }
//    }
}