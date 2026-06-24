package com.renz.golfperformancetracker.data.mapper

import com.renz.golfperformancetracker.data.local.entity.PlayerEntity
import com.renz.golfperformancetracker.data.local.entity.PlayerWithShotsEntity
import com.renz.golfperformancetracker.data.local.entity.ShotEntity
import com.renz.golfperformancetracker.data.remote.dto.PlayerDto
import com.renz.golfperformancetracker.data.remote.dto.ShotDto
import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.PlayerWithShots
import com.renz.golfperformancetracker.domain.model.Shot

fun PlayerDto.toEntity(syncedAt: Long): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    club = club,
    avatarUrl = avatarUrl,
    avgBallSpeed = avgBallSpeed,
    avgLaunchAngle = avgLaunchAngle,
    avgCarryDistance = avgCarryDistance,
    avgSpinRate = avgSpinRate,
    lastSyncedAt = syncedAt,
)

fun ShotDto.toEntity(): ShotEntity = ShotEntity(
    id = id,
    playerId = playerId,
    ballSpeed = ballSpeed,
    launchAngle = launchAngle,
    carryDistance = carryDistance,
    spinRate = spinRate,
    clubType = clubType,
    recordedAt = recordedAt,
)

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    club = club,
    avatarUrl = avatarUrl,
    avgBallSpeed = avgBallSpeed,
    avgLaunchAngle = avgLaunchAngle,
    avgCarryDistance = avgCarryDistance,
    avgSpinRate = avgSpinRate,
)

fun ShotEntity.toDomain(): Shot = Shot(
    id = id,
    playerId = playerId,
    ballSpeed = ballSpeed,
    launchAngle = launchAngle,
    carryDistance = carryDistance,
    spinRate = spinRate,
    clubType = clubType,
    recordedAt = recordedAt,
)

fun PlayerWithShotsEntity.toDomain(): PlayerWithShots = PlayerWithShots(
    player = player.toDomain(),
    shots = shots.map { it.toDomain() },
)
