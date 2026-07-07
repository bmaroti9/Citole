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

package com.marotidev.citole.presentation.home.forYou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer
import javax.inject.Inject

private val diacriticsRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
private val nonAlphanumericRegex = "[^a-z0-9\\s]".toRegex()
private val whitespaceRegex = "\\s+".toRegex()

sealed interface SearchResultGroup {
    val score: Float

    data class Tracks(val items: List<ScoredResult<AudioService.TrackData>>) : SearchResultGroup {
        override val score get() = items.maxOfOrNull { it.score } ?: 0f
    }
    data class Albums(val items: List<ScoredResult<AudioService.AlbumData>>) : SearchResultGroup {
        override val score get() = items.maxOfOrNull { it.score } ?: 0f
    }
    data class Artists(val items: List<ScoredResult<AudioService.ArtistData>>) : SearchResultGroup {
        override val score get() = items.maxOfOrNull { it.score } ?: 0f
    }
}

data class ScoredResult<T>(val item: T, val score: Float)
data class NormalizedItem<T>(val item: T, val normalized: String)

fun scoreTargetFromQuery(cleanQuery : String, cleanTarget: String) : Float {

    if (cleanQuery.isEmpty() || cleanTarget.isEmpty()) return 0f

    if (cleanTarget == cleanQuery) return 1f

    if (cleanTarget.startsWith(cleanQuery)) return 0.85f

    if (cleanTarget.contains(cleanQuery)) return 0.7f

    val queryWords = cleanQuery.split(whitespaceRegex).filter { it.isNotEmpty() } //regex for multiple whitespaces
    val targetWords = cleanTarget.split(whitespaceRegex).filter { it.isNotEmpty() }

    var matchedWordsCount = 0
    queryWords.forEach { word ->
        if (targetWords.any { it.startsWith(word) || it.contains(word) }) {
            matchedWordsCount++
        }
    }

    return matchedWordsCount * 0.5f / queryWords.size

}

private fun normalizeText(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized
        .replace(diacriticsRegex, "")
        .lowercase()
        .replace(nonAlphanumericRegex, "")
        .trim()
}

@HiltViewModel
class UniversalSearchViewModel @Inject constructor(
    audioRepository : AudioRepository,
    searchQueryStateHolder: SearchQueryStateHolder,
) : ViewModel() {

    var normalizedTrackTitles = audioRepository.allTracks.map {
        it.map { track -> NormalizedItem(track, normalizeText(track.title)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var normalizedAlbumTitles = audioRepository.allAlbums.map {
        it.map { album -> NormalizedItem(album, normalizeText(album.albumName)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var normalizedArtistNames = audioRepository.allArtists.map {
        it.map { artist -> NormalizedItem(artist, normalizeText(artist.name)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var searchResults = combine(
        searchQueryStateHolder.query,
        normalizedTrackTitles,
        normalizedAlbumTitles,
        normalizedArtistNames,
    ) { query, normalizedTracks, normalizedAlbums, normalizedArtists ->

        val cleanQuery = normalizeText(query)

        listOf(
            SearchResultGroup.Tracks(
                items = normalizedTracks
                    .map {
                        ScoredResult(
                            it.item,
                            scoreTargetFromQuery(cleanQuery, it.normalized)
                        )
                    }
                    .filter { it.score > 0.2f }
            ),
            SearchResultGroup.Albums(
                items = normalizedAlbums
                    .map {
                        ScoredResult(
                            it.item,
                            scoreTargetFromQuery(cleanQuery, it.normalized)
                        )
                    }
                    .filter { it.score > 0.2f }
            ),
            SearchResultGroup.Artists(
                items = normalizedArtists
                    .map {
                        ScoredResult(it.item,
                            scoreTargetFromQuery(cleanQuery, it.normalized)
                        )
                    }
                    .filter { it.score > 0.2f }
            )
        ).sortedByDescending { it.score }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}