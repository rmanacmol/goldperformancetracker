package com.renz.golfperformancetracker.domain.repository

import androidx.paging.PagingData
import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.PlayerStatsSummary
import com.renz.golfperformancetracker.domain.model.PlayerWithShots
import com.renz.golfperformancetracker.domain.model.Shot
import com.renz.golfperformancetracker.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GolfRepository {

    fun observePlayers(): Flow<List<Player>>

    fun observePlayer(playerId: String): Flow<Player?>

    fun observePlayerWithShots(playerId: String): Flow<PlayerWithShots?>

    fun observePlayerStats(playerId: String): Flow<PlayerStatsSummary?>

    fun pagingShots(playerId: String): Flow<PagingData<Shot>>

    val syncStatus: StateFlow<SyncStatus>

    val isOffline: StateFlow<Boolean>

    suspend fun refreshPlayers(force: Boolean = false)

    suspend fun refreshPlayerShots(playerId: String, force: Boolean = false)
}
