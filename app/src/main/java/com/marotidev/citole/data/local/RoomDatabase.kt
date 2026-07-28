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
import androidx.room.ForeignKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM trackplaylog WHERE playback_duration_ms > 5000 ORDER BY playback_ended_ms DESC")
    fun getAllPlayedLogs(): Flow<List<TrackPlayLog>>

    @Query("SELECT * FROM trackplaylog WHERE queue_id = :queueId GROUP BY queue_index")
    suspend fun getAllByQueueId(queueId: Long) : List<TrackPlayLog>

    @Query("SELECT * FROM trackplaylog WHERE playback_ended_ms > :lastDate AND track_type == :type GROUP BY track_id ORDER BY SUM(playback_duration_ms) DESC LIMIT 10")
    fun getMostPlayedFromDate(lastDate: Long, type: Int) : Flow<List<TrackPlayLog>>

    @Query("SELECT * FROM trackplaylog WHERE track_id = :trackId ORDER BY playback_ended_ms DESC LIMIT 1")
    suspend fun getLastProgress(trackId: Long): TrackPlayLog

    @Query("SELECT * FROM trackplaylog WHERE track_type = :type ORDER BY playback_ended_ms DESC LIMIT 1")
    fun getLastByType(type: Int): Flow<TrackPlayLog?>

    @Query("UPDATE trackplaylog SET playback_ended_ms = :playbackEndedMs, playback_duration_ms = :playbackDurationMs WHERE track_id = :trackId AND queue_id = :queueId")
    suspend fun updateLogTimeValues(queueId: Long, trackId: Long, playbackEndedMs: Long, playbackDurationMs: Long)

    @Query("UPDATE trackplaylog SET queue_index = :newIndex WHERE track_id = :trackId AND queue_id = :queueId")
    suspend fun updateLogQueueIndex(queueId: Long, trackId: Long, newIndex: Int)

    @Query("UPDATE trackplaylog SET queue_index = queue_index - 1 WHERE queue_id = :queueId AND queue_index > :fromIndex")
    suspend fun decreaseIndexAfter(fromIndex: Int, queueId: Long, )

    @Query("DELETE FROM trackplaylog WHERE queue_index = :index AND queue_id = :queueId AND playback_duration_ms = 0")
    suspend fun deleteLogFromQueue(index: Int, queueId: Long)

    @Query("UPDATE trackplaylog SET queue_id = :newQueueId WHERE queue_index = :index AND queue_id = :queueId AND playback_duration_ms > 0")
    suspend fun detachLogFromQueue(index: Int, queueId: Long, newQueueId: Long)

    @Insert
    suspend fun insertAll(trackPlayLogs: List<TrackPlayLog>)
}

@Entity
data class PlaylistGroup(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo(name = "name") val name : String,
)

@Entity(
    primaryKeys = ["playlist_id", "track_id"], //these two combined are the key
    foreignKeys = [ForeignKey( //deletes this when the playlist is deleted
        entity = PlaylistGroup::class,
        parentColumns = ["id"],
        childColumns = ["playlist_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlaylistTrack(
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "playlist_id") val playlistId: Int,
    @ColumnInfo(name = "playlist_index") val playlistIndex: Int,
    @ColumnInfo(name = "date_added") val dateAdded: Long = System.currentTimeMillis()
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlisttrack")
    fun getAllPlaylistTracks(): Flow<List<PlaylistTrack>>

    @Query("SELECT * FROM playlistgroup")
    fun getAllPlaylistGroups(): Flow<List<PlaylistGroup>>

    @Query("SELECT * FROM playlisttrack WHERE playlist_id = :playlistId")
    fun getPlaylistTracksFromId(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("SELECT * FROM playlistgroup WHERE id = :playlistId")
    fun getPlaylistGroupFromId(playlistId: Int): Flow<PlaylistGroup>

    @Query("DELETE FROM playlisttrack WHERE track_id = :trackId AND playlist_id = :playlistId")
    suspend fun deletePlaylistTrack(trackId: Long, playlistId: Int)

    @Insert
    suspend fun insertAll(playlistTracks: List<PlaylistTrack>)
}

@Database(
    entities = [TrackPlayLog::class, PlaylistGroup::class, PlaylistTrack::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackPlayLogDao(): TrackPlayLogDao
    abstract fun playlistDao(): PlaylistDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `PlaylistGroup` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `PlaylistTrack` (`track_id` INTEGER NOT NULL, `playlist_id` INTEGER NOT NULL, `playlist_index` INTEGER NOT NULL, `date_added` INTEGER NOT NULL, PRIMARY KEY(`playlist_id`, `track_id`), FOREIGN KEY(`playlist_id`) REFERENCES `PlaylistGroup`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
        """)

        db.execSQL("""
            INSERT INTO `PlaylistGroup` (`name`) VALUES ('Favorites')
        """)
    }
}

