package com.marotidev.citole.presentation.album

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
class AlbumListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    var filteredAlbums = combine(
        audioRepository.allTracks,
        dataStoreRepository.chipSortChip,
        combine(
            dataStoreRepository.chipShowSongs,
            dataStoreRepository.chipShowPodcasts,
            dataStoreRepository.chipShowAudiobooks,
            dataStoreRepository.chipShowOther,
        ) { songs, podcasts, audiobooks, other ->
            listOf<Boolean>(songs, podcasts, audiobooks, other)
        }

    ) { allTracks, sortChip, types ->
        allTracks
            .filterByType(types[0], types[1], types[2], types[3])
            .sortByChip(sortChip)
    }

    fun List<AudioService.TrackData>.sortByChip(sortChip: SortChip): List<AudioService.TrackData> {
        return if (sortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { track ->
                when (sortChip) {
                    SortChip.Name -> track.name
                    SortChip.Album -> track.albumName
                    SortChip.Artist -> track.rawArtist
                }
            }
        }
    }

    fun List<AudioService.TrackData>.filterByType(
        showSongs: Boolean,
        showPodcasts: Boolean,
        showAudiobooks: Boolean,
        showOther: Boolean,
    ) : List<AudioService.TrackData> {
        return this.filter { track ->
            when (track.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.TrackData>.filterByQuery(query: String) : List<AudioService.TrackData> {
        return this.filter { track ->
            track.name.contains(query, ignoreCase = true)
                    || track.rawArtist.contains(query, ignoreCase = true)
                    || track.albumName.contains(query, ignoreCase = true)
        }
    }

}