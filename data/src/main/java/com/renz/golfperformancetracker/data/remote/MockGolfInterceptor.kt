package com.renz.golfperformancetracker.data.remote

import com.renz.golfperformancetracker.data.remote.dto.PlayerDto
import com.renz.golfperformancetracker.data.remote.dto.ShotDto
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serves embedded demo JSON when the remote MockAPI endpoint is unavailable.
 * Keeps the assessment runnable without requiring a hosted API setup.
 */
class MockGolfInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            val response = chain.proceed(request)
            if (response.isSuccessful) {
                response
            } else {
                response.close()
                mockResponse(request.url.encodedPath)
            }
        } catch (_: Exception) {
            mockResponse(request.url.encodedPath)
        }
    }

    private fun mockResponse(path: String): Response {
        val body = when {
            path.contains("/players/") && path.contains("/shots") -> {
                val playerId = path.substringAfter("/players/").substringBefore("/shots")
                shotsJsonForPlayer(playerId)
            }
            path.contains("/players") -> playersJson()
            path.contains("/shots") -> shotsJson()
            else -> "[]"
        }
        return Response.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(chainRequest(path))
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun chainRequest(path: String) = okhttp3.Request.Builder()
        .url("https://mock.local$path")
        .build()

    private fun playersJson(): String = JSONArray(
        listOf(
            playerJson("1", "Jordan Lee", "Driver", 168.4, 12.5, 265.0, 2450.0),
            playerJson("2", "Mia Chen", "7 Iron", 122.8, 18.2, 158.4, 6200.0),
            playerJson("3", "Alex Rivera", "Driver", 171.2, 11.8, 272.5, 2380.0),
            playerJson("4", "Sam Patel", "Pitching Wedge", 94.5, 26.4, 112.0, 9100.0),
            playerJson("5", "Taylor Brooks", "3 Wood", 156.3, 14.1, 235.8, 4100.0),
        ),
    ).toString()

    private fun shotsJsonForPlayer(playerId: String): String =
        JSONArray(generateShotsForPlayer(playerId)).toString()

    private fun shotsJson(): String {
        val allShots = (1..5).flatMap { generateShotsForPlayer(it.toString()) }
        return JSONArray(allShots).toString()
    }

    private fun generateShotsForPlayer(playerId: String): List<JSONObject> {
        val profile = playerShotProfiles[playerId] ?: return emptyList()
        return (1..12).map { index ->
            val speedDelta = (index % 6) * 1.4 - 2.5
            val distanceDelta = (index % 4) * 2.0 - 3.0
            shotJson(
                id = "s${playerId}_$index",
                playerId = playerId,
                ballSpeed = profile.baseSpeed + speedDelta,
                launchAngle = profile.launchAngle + (index % 3) * 0.3,
                carryDistance = profile.carry + distanceDelta,
                spinRate = profile.spin + (index * 25),
                clubType = profile.club,
                recordedAt = BASE_TIMESTAMP + (index * 86_400_000L),
            )
        }
    }

    private data class PlayerShotProfile(
        val club: String,
        val baseSpeed: Double,
        val launchAngle: Double,
        val carry: Double,
        val spin: Double,
    )

    private val playerShotProfiles = mapOf(
        "1" to PlayerShotProfile("Driver", 168.0, 12.3, 265.0, 2450.0),
        "2" to PlayerShotProfile("7 Iron", 122.5, 18.1, 158.0, 6200.0),
        "3" to PlayerShotProfile("Driver", 171.0, 11.7, 272.0, 2380.0),
        "4" to PlayerShotProfile("Pitching Wedge", 94.5, 26.2, 112.0, 9100.0),
        "5" to PlayerShotProfile("3 Wood", 156.0, 14.0, 236.0, 4100.0),
    )

    private fun playerJson(
        id: String,
        name: String,
        club: String,
        avgBallSpeed: Double,
        avgLaunchAngle: Double,
        avgCarryDistance: Double,
        avgSpinRate: Double,
    ): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("club", club)
        .put("avatarUrl", "https://i.pravatar.cc/150?u=$id")
        .put("avgBallSpeed", avgBallSpeed)
        .put("avgLaunchAngle", avgLaunchAngle)
        .put("avgCarryDistance", avgCarryDistance)
        .put("avgSpinRate", avgSpinRate)

    private fun shotJson(
        id: String,
        playerId: String,
        ballSpeed: Double,
        launchAngle: Double,
        carryDistance: Double,
        spinRate: Double,
        clubType: String,
        recordedAt: Long,
    ): JSONObject = JSONObject()
        .put("id", id)
        .put("playerId", playerId)
        .put("ballSpeed", ballSpeed)
        .put("launchAngle", launchAngle)
        .put("carryDistance", carryDistance)
        .put("spinRate", spinRate)
        .put("clubType", clubType)
        .put("recordedAt", recordedAt)

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private const val BASE_TIMESTAMP = 1_714_000_000_000L
    }
}
