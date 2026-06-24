package com.renz.golfperformancetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.renz.golfperformancetracker.data.local.dao.PlayerDao
import com.renz.golfperformancetracker.data.local.dao.PlayerDetailDao
import com.renz.golfperformancetracker.data.local.dao.ShotDao
import com.renz.golfperformancetracker.data.local.dao.SyncMetadataDao
import com.renz.golfperformancetracker.data.local.entity.PlayerEntity
import com.renz.golfperformancetracker.data.local.entity.ShotEntity
import com.renz.golfperformancetracker.data.local.entity.SyncMetadataEntity

@Database(
    entities = [
        PlayerEntity::class,
        ShotEntity::class,
        SyncMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class GolfDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun shotDao(): ShotDao

    abstract fun playerDetailDao(): PlayerDetailDao

    abstract fun syncMetadataDao(): SyncMetadataDao
}
