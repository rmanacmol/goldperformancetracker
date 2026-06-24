package com.renz.golfperformancetracker.mapper

import com.renz.golfperformancetracker.data.mapper.toDomain
import com.renz.golfperformancetracker.data.mapper.toEntity
import com.renz.golfperformancetracker.data.remote.dto.PlayerDto
import com.renz.golfperformancetracker.data.remote.dto.ShotDto
import org.junit.Assert.assertEquals
import org.junit.Test

class GolfMappersTest {

    @Test
    fun `player dto maps to domain entity and back`() {
        val dto = PlayerDto(
            id = "1",
            name = "Jordan Lee",
            club = "Driver",
            avatarUrl = "https://example.com/a.png",
            avgBallSpeed = 168.4,
            avgLaunchAngle = 12.5,
            avgCarryDistance = 265.0,
            avgSpinRate = 2450.0,
        )

        val entity = dto.toEntity(syncedAt = 100L)
        val domain = entity.toDomain()

        assertEquals("Jordan Lee", domain.name)
        assertEquals("Driver", domain.club)
        assertEquals(168.4, domain.avgBallSpeed, 0.001)
    }

    @Test
    fun `shot dto maps to domain`() {
        val dto = ShotDto(
            id = "s1",
            playerId = "1",
            ballSpeed = 170.2,
            launchAngle = 12.1,
            carryDistance = 268.0,
            spinRate = 2500.0,
            clubType = "Driver",
            recordedAt = 1_714_000_000_000L,
        )

        val domain = dto.toEntity().toDomain()

        assertEquals("s1", domain.id)
        assertEquals("Driver", domain.clubType)
        assertEquals(170.2, domain.ballSpeed, 0.001)
    }
}
