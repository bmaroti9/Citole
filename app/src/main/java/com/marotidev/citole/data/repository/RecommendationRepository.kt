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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class RecommendationRepository @Inject constructor(
    trackLogRepository: TrackLogRepository,
    private val audioRepository: AudioRepository
) {

    val explorationFactor = 1.5f

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
            }
            .build()
    }

    suspend fun generateQueueFromSeed(seedId: Long, count: Int) : List<AudioService.TrackData> {

        val graph : Map<Long, Map<Long, Float>> = similarityGraph.first()

        val picked = mutableListOf(seedId)

        for (i in 1..200) {
            var currentNode = picked.last()

            for (j in 1..100) {
                graph[currentNode]?.let { node ->
                    val totalWeight = node.values.sumOf { it.toDouble() }.toFloat()
                    val r = Random.nextFloat() * totalWeight

                    var sum = 0f
                    val newNode = node.entries.firstOrNull { entry ->
                        println("$sum, ${entry.value}, $r, ${entry.key}")
                        sum += entry.value
                        sum >= r
                    }

                    newNode?.let {
                        currentNode = newNode.key
                        if (newNode.value / totalWeight / explorationFactor < Random.nextFloat() && it.key !in picked) {
                            break
                        }
                    }
                }
            }

            if (currentNode !in picked) {
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
}