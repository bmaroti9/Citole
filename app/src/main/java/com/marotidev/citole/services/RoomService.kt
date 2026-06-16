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

package com.marotidev.citole.services

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class TrackPlayLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "playback_ended_ms") val playbackEndedMs: Long,
    @ColumnInfo(name = "playback_duration_ms") val playbackDurationMs: Long,
    @ColumnInfo(name = "track_type") val trackType: Int,
)

@Dao
interface TrackPlayLogDao {
    @Query("SELECT * FROM trackplaylog")
    suspend fun getAll(): List<TrackPlayLog>

    @Query("SELECT * FROM trackplaylog WHERE track_id = :trackId ORDER BY playback_ended_ms DESC LIMIT 1")
    suspend fun getLastProgress(trackId: Long): TrackPlayLog

    @Query("SELECT * FROM trackplaylog WHERE track_type = :type ORDER BY playback_ended_ms DESC LIMIT 1")
    suspend fun getLastByType(trackId: Long, type: AudioService.AudioType): TrackPlayLog

    @Insert
    suspend fun insertAll(vararg trackPlayLogs: TrackPlayLog)
}

@Database(entities = [TrackPlayLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackPlayLogDao(): TrackPlayLogDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app-database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}

