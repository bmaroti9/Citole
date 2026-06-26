package com.marotidev.citole.data.domain

import com.marotidev.citole.data.local.TrackPlayLog
import com.marotidev.citole.data.service.AudioService

class SimilarityGraphBuilder {

    val sharedArtistWeight = 10f
    val sharedAlbumWeight = 15f
    val sharedQueueWeight = 5f

    //a map of the trackIds with the weight of the connection
    private val edges = mutableMapOf<Long, MutableMap<Long, Float>>()

    fun addEdge(id1: Long, id2: Long, weight: Float) {
        if (id1 == id2) return
        edges.getOrPut(id1) { mutableMapOf() }.merge(id2, weight, Float::plus)
        edges.getOrPut(id2) { mutableMapOf() }.merge(id1, weight, Float::plus)
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

    fun connectBySharedQueueLog(allLogs: List<TrackPlayLog>, tracks: List<AudioService.TrackData>) {
        allLogs.groupBy { it.queueId }.forEach { (id, logs) ->
            logs.forEach { a ->
                val totalDuration = tracks.find { it.id == a.trackId }?.duration
                val durationMultiplier = totalDuration?.let {
                    a.playbackDurationMs / it
                } ?: 0
                logs.forEach { b ->
                    addEdge(a.trackId, b.trackId, sharedQueueWeight * durationMultiplier)
                }
            }
        }
    }

    fun build() = edges
}