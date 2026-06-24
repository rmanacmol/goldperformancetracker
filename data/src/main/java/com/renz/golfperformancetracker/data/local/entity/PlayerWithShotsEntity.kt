package com.renz.golfperformancetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlayerWithShotsEntity(
    @Embedded val player: PlayerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playerId",
    )
    val shots: List<ShotEntity>,
)
