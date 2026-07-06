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

import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject

sealed interface SearchResultGroup {
    val score: Float

    data class Tracks(val items: List<ScoredResult<AudioService.TrackData>>) : SearchResultGroup {
        override val score get() = items.maxOf { it.score }
    }
    data class Albums(val items: List<ScoredResult<AudioService.AlbumData>>) : SearchResultGroup {
        override val score get() = items.maxOf { it.score }
    }
    data class Artists(val items: List<ScoredResult<AudioService.ArtistData>>) : SearchResultGroup {
        override val score get() = items.maxOf { it.score }
    }
}

data class ScoredResult<T>(val item: T, val score: Float)

fun scoreTargetFromQuery(query: String, target: String) : Float {

    val cleanQuery = normalizeText(query)
    val cleanTarget = normalizeText(target)

    if (cleanQuery.isEmpty() || cleanTarget.isEmpty()) return 0f

    if (cleanTarget == cleanQuery) return 1f

    if (cleanTarget.startsWith(cleanQuery)) return 0.85f

    if (cleanTarget.contains(cleanQuery)) return 0.7f

    val queryWords = cleanQuery.split("\\s+".toRegex()).filter { it.isNotEmpty() } //regex for multiple whitespaces
    val targetWords = cleanTarget.split("\\s+".toRegex()).filter { it.isNotEmpty() }

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
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
        .replace("[^a-z0-9\\s]".toRegex(), "")
        .trim()
}

@HiltViewModel
class UniversalSearchViewModel @Inject constructor(
    audioRepository : AudioRepository,
    searchQueryStateHolder: SearchQueryStateHolder,
) : ViewModel() {
    var searchResults = combine(
        searchQueryStateHolder.query,
        audioRepository.allTracks,
        audioRepository.allAlbums,
        audioRepository.allArtists,
    ) { query, allTracks, allAlbums, allArtists ->
        listOf(
            SearchResultGroup.Tracks(
                items = allTracks.map { ScoredResult(it, scoreTargetFromQuery(query, it.name)) }
            ),
            SearchResultGroup.Albums(
                items = allAlbums.map { ScoredResult(it, scoreTargetFromQuery(query, it.albumName)) }
            ),
            SearchResultGroup.Artists(
                items = allArtists.map { ScoredResult(it, scoreTargetFromQuery(query, it.name)) }
            )
        ).sortedBy { it.score }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}