package com.marotidev.citole.presentation.artist

import androidx.lifecycle.ViewModel
import com.marotidev.citole.SortChip
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.AudioService.AudioType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class ArtistListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    var filteredArtists = combine(
        audioRepository.allArtists,
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

    ) { allArtists, sortChip, sortReversed, types ->
        allArtists
            .filterByType(types[0], types[1], types[2], types[3])
            .sortByChip(sortChip)
            .reverseIf(sortReversed)
    }

    fun List<AudioService.ArtistData>.sortByChip(sortChip: SortChip): List<AudioService.ArtistData> {
        return if (sortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { artist ->
                when (sortChip) {
                    SortChip.Name -> artist.name
                    SortChip.Album -> artist.albums.firstOrNull()?.albumName
                    SortChip.Artist -> artist.name
                }
            }
        }
    }

    fun List<AudioService.ArtistData>.filterByType(
        showSongs: Boolean,
        showPodcasts: Boolean,
        showAudiobooks: Boolean,
        showOther: Boolean,
    ) : List<AudioService.ArtistData> {
        return this.filter { artist ->
            when (artist.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.ArtistData>.filterByQuery(query: String) : List<AudioService.ArtistData> {
        return this.filter { artist ->
            artist.name.contains(query, ignoreCase = true)
                    || artist.tracks.any {it.name.contains(query, ignoreCase = true)}
                    || artist.allAlbums.any {it.albumName.contains(query, ignoreCase = true)}
        }
    }

    fun List<AudioService.ArtistData>.reverseIf(reverse: Boolean) : List<AudioService.ArtistData> {
        return if (reverse) {
            this.reversed()
        } else {
            this
        }
    }

}