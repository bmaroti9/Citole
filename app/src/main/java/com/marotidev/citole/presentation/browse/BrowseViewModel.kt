package com.marotidev.citole.presentation.browse

import androidx.lifecycle.ViewModel
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    audioRepository : AudioRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

}