package com.renz.golfperformancetracker.domain.model

data class Player(
    val id: String,
    val name: String,
    val club: String,
    val avatarUrl: String?,
    val avgBallSpeed: Double,
    val avgLaunchAngle: Double,
    val avgCarryDistance: Double,
    val avgSpinRate: Double,
)

data class Shot(
    val id: String,
    val playerId: String,
    val ballSpeed: Double,
    val launchAngle: Double,
    val carryDistance: Double,
    val spinRate: Double,
    val clubType: String,
    val recordedAt: Long,
)

data class PlayerWithShots(
    val player: Player,
    val shots: List<Shot>,
)

data class ShotMetricPoint(
    val label: String,
    val ballSpeed: Double,
    val carryDistance: Double,
)

data class PlayerStatsSummary(
    val player: Player,
    val shotCount: Int,
    val avgBallSpeed: Double,
    val maxBallSpeed: Double,
    val minBallSpeed: Double,
    val avgCarryDistance: Double,
    val speedTrend: List<ShotMetricPoint>,
)

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
}
