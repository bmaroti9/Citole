package com.marotidev.citole.services

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class TrackPlayLog(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "playback_started") val playbackStarted: Long,
    @ColumnInfo(name = "playback_duration_ms") val playbackDurationMs: Int
)

//@Dao
//interface TrackPlayLogDao {
//    @Query("SELECT * FROM trackplaylog")
//    fun getAll(): List<TrackPlayLog>
//
//    @Query("SELECT * FROM trackplaylog WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM trackplaylog WHERE track_id LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findById(id: Long): TrackPlayLog
//
//    @Insert
//    fun insertAll(vararg users: User)
//
//    @Delete
//    fun delete(user: User)
//}