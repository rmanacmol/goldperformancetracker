package com.renz.golfperformancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val club: String,
    val avatarUrl: String?,
    val avgBallSpeed: Double,
    val avgLaunchAngle: Double,
    val avgCarryDistance: Double,
    val avgSpinRate: Double,
    val lastSyncedAt: Long,
)

@Entity(
    tableName = "shots",
    primaryKeys = ["id"],
)
data class ShotEntity(
    val id: String,
    val playerId: String,
    val ballSpeed: Double,
    val launchAngle: Double,
    val carryDistance: Double,
    val spinRate: Double,
    val clubType: String,
    val recordedAt: Long,
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSyncedAt: Long,
)
