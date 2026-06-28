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

package com.marotidev.citole.data.repository

import com.marotidev.citole.data.domain.SimilarityGraphBuilder
import com.marotidev.citole.data.service.AudioService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.PriorityQueue
import javax.inject.Inject
import kotlin.random.Random

class RecommendationRepository @Inject constructor(
    trackLogRepository: TrackLogRepository,
    private val audioRepository: AudioRepository,
    dataStoreRepository: DataStoreRepository
) {

    val explorationFactor = dataStoreRepository.shuffleDiscoveryRadius.map {
        0.1f + it * 4f
    }

    val trajectoryRange = dataStoreRepository.shuffleQueueTrajectory.map {
        Pair(((it - 0.5f) * 2f).coerceIn(0f, 1f), (it * 2f).coerceIn(0f, 1f))
    }

    private var similarityGraph : Flow<Map<Long, Map<Long, Float>>> = combine(
        audioRepository.allTracks,
        audioRepository.allAlbums,
        audioRepository.allArtists,
        trackLogRepository.allLogs
    ) { tracks, albums, artists, allLogs ->
        SimilarityGraphBuilder()
            .apply {
                connectBySharedArtist(artists)
                connectBySharedAlbum(albums)
                connectBySharedQueueLog(allLogs, tracks)
                connectBySharedReleaseYear(tracks)
            }
            .build()
    }

    suspend fun generateQueueFromSeed(seedId: Long, count: Int) : List<AudioService.TrackData> {
        val currentExplorationFactor = explorationFactor.first()
        val currentTrajectoryRange = trajectoryRange.first()

        val graph : Map<Long, Map<Long, Float>> = similarityGraph.first()

        val picked = mutableListOf(seedId)

        for (i in 1..200) {
            var currentNode = picked
                .filterIndexed { index, lng ->  (picked.size - 1) * currentTrajectoryRange.first <= index && index <= (picked.size - 1) * currentTrajectoryRange.second}
                .randomOrNull()

            for (j in 1..100) {
                graph[currentNode]?.let { node ->
                    val totalWeight = node.values.sumOf { it.toDouble() }.toFloat()
                    val r = Random.nextFloat() * totalWeight

                    var sum = 0f
                    val newNode = node.entries.firstOrNull { entry ->
                        sum += entry.value
                        sum >= r
                    }

                    newNode?.let {
                        currentNode = newNode.key
                        if (newNode.value / totalWeight * currentExplorationFactor < Random.nextFloat() && it.key !in picked) {
                            break
                        }
                    }
                }
            }

            if (currentNode != null && currentNode !in picked) {
                picked.add(currentNode)
                if (picked.size == count) {
                    break
                }
            }
        }

        val selectedTracks = picked.mapNotNull {
            audioRepository.findTrackById(it)
        }

        return selectedTracks
    }

    suspend fun findSimilarArtists(artist: AudioService.ArtistData?, artists: List<AudioService.ArtistData>,
        tracks: List<AudioService.TrackData>, count: Int) : List<AudioService.ArtistData> {
        if (artist == null) return emptyList()

        val trackMap = tracks.associateBy { it.id }
        val artistMap = artists.associateBy { it.name }

        val ids = artist.tracks.map { Pair(it.id, 1f) }

        val maxWeights = findMaxWeightsFromStartingIds(ids)

        val selectedArtists = mutableMapOf<String, Float>()

        maxWeights.entries.forEach { weight ->
            trackMap[weight.key]?.let { track ->
                track.artists.forEach { a ->
                    selectedArtists.merge(a, weight.value, Float::plus)
                }
            }
        }

        return selectedArtists.toList().sortedByDescending { it.second }.take(count).mapNotNull { a ->
            if (a.first != artist.name) artistMap[a.first] else null
        }
    }

    suspend fun findSimilarAlbums(album: AudioService.AlbumData?, albums: List<AudioService.AlbumData>,
                                   tracks: List<AudioService.TrackData>, count: Int) : List<AudioService.AlbumData> {
        if (album == null) return emptyList()

        val trackMap = tracks.associateBy { it.id }
        val albumMap = albums.associateBy { it.albumId }

        val ids = album.tracks.map { Pair(it.id, 1f) }

        val maxWeights = findMaxWeightsFromStartingIds(ids)

        val selectedAlbums = mutableMapOf<Long, Float>()

        maxWeights.entries.forEach { weight ->
            trackMap[weight.key]?.let { track ->
                selectedAlbums.merge(track.albumId, weight.value, Float::plus)
            }
        }

        return selectedAlbums.toList().sortedByDescending { it.second }.take(count).mapNotNull { a ->
            if (a.first != album.albumId) albumMap[a.first] else null
        }
    }

    suspend fun findMaxWeightsFromStartingIds(ids: List<Pair<Long, Float>>) : Map<Long, Float> {
        //sort of reverse Dijkstra where we are looking for the largest weights

        val graph : Map<Long, Map<Long, Float>> = similarityGraph.first()

        val pq = PriorityQueue<Pair<Long, Float>> {a, b -> b.second.compareTo(a.second)}
        pq.addAll(ids)

        val maxWeights = mutableMapOf<Long, Float>()
        ids.forEach { maxWeights[it.first] = it.second }

        for (k in 0..500) {
            //guaranteed to be the best way to get there since all weights beneath it are smaller
            val top = pq.poll() ?: break

            if (top.second < 0.005f || top.second != maxWeights[top.first]) continue

            graph[top.first]?.let { node ->
                val totalWeight = node.values.sumOf { it.toDouble() }.toFloat()

                node.entries.forEach { n ->
                    val newWeight = n.value / totalWeight * top.second

                    if (newWeight > (maxWeights[n.key] ?: 0f)) {
                        pq.add(Pair(n.key, newWeight))
                        maxWeights[n.key] = newWeight
                    }
                }
            }
        }

        return maxWeights
    }
}