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

package com.marotidev.citole.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.marotidev.citole.data.service.AudioService

@Entity
data class TrackPlayLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "queue_id") val queueId: Long,

    @ColumnInfo(name = "queue_index") val queueIndex: Int,

    @ColumnInfo(name = "playback_ended_ms") val playbackEndedMs: Long = 0,
    @ColumnInfo(name = "playback_duration_ms") val playbackDurationMs: Long = 0,

    @ColumnInfo(name = "track_type") val trackType: Int,
)

@Dao
interface TrackPlayLogDao {
    @Query("SELECT * FROM trackplaylog")
    suspend fun getAll(): List<TrackPlayLog>

    @Query("SELECT * FROM trackplaylog WHERE queue_id = :queueId GROUP BY queue_index")
    suspend fun getAllByQueueId(queueId: Long) : List<TrackPlayLog>

    @Query("SELECT * FROM trackplaylog WHERE track_id = :trackId ORDER BY playback_ended_ms DESC LIMIT 1")
    suspend fun getLastProgress(trackId: Long): TrackPlayLog

    @Query("SELECT * FROM trackplaylog WHERE track_type = :type ORDER BY playback_ended_ms DESC LIMIT 1")
    suspend fun getLastByType(type: Int): TrackPlayLog?

    @Query("UPDATE trackplaylog SET playback_ended_ms = :playbackEndedMs, playback_duration_ms = :playbackDurationMs WHERE track_id = :trackId AND queue_id = :queueId")
    suspend fun updateLogTimeValues(queueId: Long, trackId: Long, playbackEndedMs: Long, playbackDurationMs: Long)

    @Query("UPDATE trackplaylog SET queue_index = :newIndex WHERE track_id = :trackId AND queue_id = :queueId")
    suspend fun updateLogQueueIndex(queueId: Long, trackId: Long, newIndex: Int)

    @Query("UPDATE trackplaylog SET queue_index = queue_index - 1 WHERE queue_id = :queueId AND queue_index > :fromIndex")
    suspend fun decreaseIndexAfter(fromIndex: Int, queueId: Long, )

    @Query("DELETE FROM trackplaylog WHERE queue_index = :index AND queue_id = :queueId")
    suspend fun deleteLogFromQueue(index: Int, queueId: Long)

    @Insert
    suspend fun insertAll(trackPlayLogs: List<TrackPlayLog>)
}

@Database(entities = [TrackPlayLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackPlayLogDao(): TrackPlayLogDao
}

