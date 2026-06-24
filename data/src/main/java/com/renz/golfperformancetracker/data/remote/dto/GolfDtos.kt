package com.renz.golfperformancetracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "club") val club: String,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
    @Json(name = "avgBallSpeed") val avgBallSpeed: Double,
    @Json(name = "avgLaunchAngle") val avgLaunchAngle: Double,
    @Json(name = "avgCarryDistance") val avgCarryDistance: Double,
    @Json(name = "avgSpinRate") val avgSpinRate: Double,
)

@JsonClass(generateAdapter = true)
data class ShotDto(
    @Json(name = "id") val id: String,
    @Json(name = "playerId") val playerId: String,
    @Json(name = "ballSpeed") val ballSpeed: Double,
    @Json(name = "launchAngle") val launchAngle: Double,
    @Json(name = "carryDistance") val carryDistance: Double,
    @Json(name = "spinRate") val spinRate: Double,
    @Json(name = "clubType") val clubType: String,
    @Json(name = "recordedAt") val recordedAt: Long,
)
