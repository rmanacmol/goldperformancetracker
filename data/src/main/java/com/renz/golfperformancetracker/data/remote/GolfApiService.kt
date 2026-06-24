package com.renz.golfperformancetracker.data.remote

import com.renz.golfperformancetracker.data.remote.dto.PlayerDto
import com.renz.golfperformancetracker.data.remote.dto.ShotDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GolfApiService {

    @GET("players")
    suspend fun getPlayers(): List<PlayerDto>

    @GET("shots")
    suspend fun getShots(@Query("playerId") playerId: String? = null): List<ShotDto>

    @GET("players/{playerId}/shots")
    suspend fun getPlayerShots(@Path("playerId") playerId: String): List<ShotDto>
}
