package com.marotidev.citole.presentation.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val searchQueryStateHolder: SearchQueryStateHolder
) : ViewModel() {

    var query by mutableStateOf("")

    var showSongs by mutableStateOf(true)
    var showPodcasts by mutableStateOf(false)
    var showAudiobooks by mutableStateOf(false)
    var showOther by mutableStateOf(false)

    var selectedSortChip by mutableStateOf(SortChip.Name)

    var reverseSortOrder by mutableStateOf(false)

    fun setChipShowSongs(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowSongs(to) }
    }

    fun setChipShowPodcasts(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowPodcasts(to) }
    }

    fun setChipShowAudiobooks(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowAudiobooks(to) }
    }

    fun setChipShowOther(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowOther(to) }
    }

    fun setSortChipSort(to: SortChip) {
        viewModelScope.launch { dataStoreRepository.saveChipSortChip(to) }
    }

    fun onReverseSortOrderChanged(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipSortReversed(to) }
    }

    fun onQueryChange(to: String) {
        searchQueryStateHolder.updateQuery(to)
    }

    init {
        combine(
            searchQueryStateHolder.query,
            dataStoreRepository.chipSortChip,
            dataStoreRepository.chipSortReversed,
            combine(
                dataStoreRepository.chipShowSongs,
                dataStoreRepository.chipShowPodcasts,
                dataStoreRepository.chipShowAudiobooks,
                dataStoreRepository.chipShowOther,
            ) { songs, podcasts, audiobooks, other ->
                listOf(songs, podcasts, audiobooks, other)
            }
        ) { queryState, sortChip, sortReversed, types ->
            query = queryState
            selectedSortChip = sortChip
            reverseSortOrder = sortReversed
            showSongs = types[0]
            showPodcasts = types[1]
            showAudiobooks = types[2]
            showOther = types[3]
        }.launchIn(viewModelScope)
    }
}