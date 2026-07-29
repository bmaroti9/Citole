package com.marotidev.citole.data.domain

import com.marotidev.citole.data.local.TrackPlayLog
import com.marotidev.citole.data.service.AudioService
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

data class SimilarityGraph(
    val edges : Map<Long, Map<Long, Float>>,
    val nodes : Map<Long, Float>
)

class SimilarityGraphBuilder {

    val sharedArtistWeight = 13f
    val sharedAlbumWeight = 20f
    val sharedQueueWeight = 3f

    //a map of the trackIds with the weight of the connection
    private val edges = mutableMapOf<Long, MutableMap<Long, Float>>()
    private val nodes = mutableMapOf<Long, Float>()

    fun addEdge(id1: Long, id2: Long, weight: Float) {
        if (id1 == id2) return
        edges.getOrPut(id1) { mutableMapOf() }.merge(id2, weight, Float::plus)
        //edges.getOrPut(id2) { mutableMapOf() }.merge(id1, weight, Float::plus)
    }

    fun connectBySharedArtist(artists: List<AudioService.ArtistData>) {
        artists.forEach { artist ->
            artist.tracks.forEach { a ->
                artist.tracks.forEach { b ->
                    addEdge(a.id, b.id, sharedArtistWeight)
                }
            }
        }
    }

    fun connectBySharedAlbum(albums: List<AudioService.AlbumData>) {
        albums.forEach { album ->
            album.tracks.forEach { a ->
                album.tracks.forEach { b ->
                    addEdge(a.id, b.id, sharedAlbumWeight)
                }
            }
        }
    }

    fun connectBySharedQueueLog(allLogs: List<TrackPlayLog>) {
        allLogs.groupBy { it.queueId }.forEach { (_, logs) ->
            logs.forEach { a ->
                logs.forEach { b ->
                    if (a.trackType == b.trackType) {
                        addEdge(a.trackId, b.trackId, sharedQueueWeight)
                    }
                }
            }
        }
    }

    fun flattenByArtistSize(artists: List<AudioService.ArtistData>) {
        val normalizationMap = mutableMapOf<Long, Float>()
        artists.forEach { artist ->
            val normalizeFactor = 1 / sqrt(artist.tracks.size * 1f)
            artist.tracks.forEach {
                normalizationMap[it.id] = normalizeFactor
            }
        }

        edges.forEach { (trackId, _) ->
            val factor = normalizationMap[trackId] ?: 1f
            nodes[trackId] = (nodes[trackId] ?: 0.04f) * factor
        }
    }

    fun weighNodesByLogs(allLogs: List<TrackPlayLog>, tracks: List<AudioService.TrackData>) {
        val trackDurationMap = tracks.associate { it.id to it.duration }

        val playbackSums = mutableMapOf<Long, Float>()

        allLogs.forEach { log ->
            val duration = trackDurationMap[log.trackId] ?: return@forEach

            val ageInDays = (System.currentTimeMillis() - log.playbackEndedMs) / 86400000f
            val recencyWeight = 0.1f + exp(-ageInDays / 14f) * 0.9f
            val completionRate = (log.playbackDurationMs * 1f / duration).coerceIn(0f, 1f)

            playbackSums.merge(log.trackId, recencyWeight * completionRate, Float::plus)
        }

        val smoothSums = playbackSums.mapValues { ln(1.0 + it.value).toFloat() }
        val maxScore = smoothSums.values.maxOrNull() ?: 1f

        smoothSums.forEach { (id, score) ->
            nodes[id] = (score / maxScore).coerceAtLeast(0.05f)
        }
    }

    fun build() = SimilarityGraph(edges, nodes)
}