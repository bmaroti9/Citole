package com.marotidev.citole.presentation.home.album

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.presentation.browse.SortChip
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.AudioService.AudioType
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    dataStoreRepository: DataStoreRepository,
    searchQueryStateHolder: SearchQueryStateHolder
) : ViewModel() {

    var filteredAlbums = combine(
        searchQueryStateHolder.query,
        audioRepository.allAlbums,
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

    ) {query, allAlbums, sortChip, sortReversed, types ->
        allAlbums
            .filterByQuery(query)
            .filterByType(types[0], types[1], types[2], types[3])
            .sortByChip(sortChip)
            .reverseIf(sortReversed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun List<AudioService.AlbumData>.sortByChip(sortChip: SortChip): List<AudioService.AlbumData> {
        return if (sortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { album ->
                when (sortChip) {
                    SortChip.Name -> album.albumName
                    SortChip.Album -> album.albumName
                    SortChip.Artist -> album.ownerArtists.joinToString(", ")
                }
            }
        }
    }

    fun List<AudioService.AlbumData>.filterByType(
        showSongs: Boolean,
        showPodcasts: Boolean,
        showAudiobooks: Boolean,
        showOther: Boolean,
    ) : List<AudioService.AlbumData> {
        return this.filter { album ->
            when (album.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.AlbumData>.filterByQuery(query: String) : List<AudioService.AlbumData> {
        return this.filter { album ->
            album.albumName.contains(query, ignoreCase = true)
                    || album.ownerArtists.any {it.contains(query, ignoreCase = true)}
        }
    }


    fun List<AudioService.AlbumData>.reverseIf(reverse: Boolean) : List<AudioService.AlbumData> {
        return if (reverse) {
            this.reversed()
        } else {
            this
        }
    }

}