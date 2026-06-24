package com.renz.golfperformancetracker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.renz.golfperformancetracker.data.local.dao.PlayerDao
import com.renz.golfperformancetracker.data.local.dao.PlayerDetailDao
import com.renz.golfperformancetracker.data.local.dao.ShotDao
import com.renz.golfperformancetracker.data.local.dao.SyncMetadataDao
import com.renz.golfperformancetracker.data.local.entity.SyncMetadataEntity
import com.renz.golfperformancetracker.data.mapper.toDomain
import com.renz.golfperformancetracker.data.mapper.toEntity
import com.renz.golfperformancetracker.data.remote.GolfApiService
import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.PlayerStatsSummary
import com.renz.golfperformancetracker.domain.model.PlayerWithShots
import com.renz.golfperformancetracker.domain.model.Shot
import com.renz.golfperformancetracker.domain.model.ShotMetricPoint
import com.renz.golfperformancetracker.domain.model.SyncStatus
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import com.renz.golfperformancetracker.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GolfRepositoryImpl(
    private val api: GolfApiService,
    private val playerDao: PlayerDao,
    private val shotDao: ShotDao,
    private val playerDetailDao: PlayerDetailDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val networkMonitor: NetworkMonitor,
    private val applicationScope: CoroutineScope,
) : GolfRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    override val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        applicationScope.launch {
            networkMonitor.isOnline.collect { online ->
                _isOffline.update { !online }
                if (online) {
                    refreshPlayers(force = false)
                }
            }
        }
        applicationScope.launch {
            if (playerDao.count() == 0) {
                refreshPlayers(force = true)
            }
        }
    }

    override fun observePlayers(): Flow<List<Player>> =
        playerDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observePlayer(playerId: String): Flow<Player?> =
        playerDao.observeById(playerId).map { it?.toDomain() }

    override fun observePlayerWithShots(playerId: String): Flow<PlayerWithShots?> =
        playerDetailDao.observePlayerWithShots(playerId).map { entity ->
            entity?.toDomain()
        }

    override fun observePlayerStats(playerId: String): Flow<PlayerStatsSummary?> =
        combine(
            playerDao.observeById(playerId),
            shotDao.observeByPlayer(playerId),
        ) { playerEntity, shotEntities ->
            val player = playerEntity?.toDomain() ?: return@combine null
            if (shotEntities.isEmpty()) {
                return@combine PlayerStatsSummary(
                    player = player,
                    shotCount = 0,
                    avgBallSpeed = player.avgBallSpeed,
                    maxBallSpeed = player.avgBallSpeed,
                    minBallSpeed = player.avgBallSpeed,
                    avgCarryDistance = player.avgCarryDistance,
                    speedTrend = emptyList(),
                )
            }
            val speeds = shotEntities.map { it.ballSpeed }
            val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())
            PlayerStatsSummary(
                player = player,
                shotCount = shotEntities.size,
                avgBallSpeed = speeds.average(),
                maxBallSpeed = speeds.maxOrNull() ?: 0.0,
                minBallSpeed = speeds.minOrNull() ?: 0.0,
                avgCarryDistance = shotEntities.map { it.carryDistance }.average(),
                speedTrend = shotEntities
                    .sortedBy { it.recordedAt }
                    .map { shot ->
                        ShotMetricPoint(
                            label = dateFormat.format(Date(shot.recordedAt)),
                            ballSpeed = shot.ballSpeed,
                            carryDistance = shot.carryDistance,
                        )
                    },
            )
        }

    override fun pagingShots(playerId: String): Flow<PagingData<Shot>> =
        Pager(
            config = PagingConfig(
                pageSize = SHOT_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = SHOT_PAGE_SIZE,
            ),
            pagingSourceFactory = { shotDao.pagingSource(playerId) },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }

    override suspend fun refreshPlayers(force: Boolean) {
        if (_syncStatus.value == SyncStatus.SYNCING) return
        if (!force && !shouldSync(PLAYERS_SYNC_KEY)) return

        _syncStatus.value = SyncStatus.SYNCING
        runCatching {
            val syncedAt = System.currentTimeMillis()
            val remotePlayers = api.getPlayers()
            playerDao.upsertAll(remotePlayers.map { it.toEntity(syncedAt) })
            syncMetadataDao.upsert(SyncMetadataEntity(PLAYERS_SYNC_KEY, syncedAt))
            Timber.d("Synced %d players", remotePlayers.size)
            _syncStatus.value = SyncStatus.SUCCESS
        }.onFailure { error ->
            Timber.e(error, "Failed to sync players")
            val hasCachedData = playerDao.count() > 0
            _syncStatus.value = if (hasCachedData) SyncStatus.SUCCESS else SyncStatus.ERROR
        }
    }

    override suspend fun refreshPlayerShots(playerId: String, force: Boolean) {
        val syncKey = shotsSyncKey(playerId)
        if (!force && !shouldSync(syncKey)) return

        runCatching {
            val remoteShots = api.getPlayerShots(playerId)
            shotDao.deleteByPlayer(playerId)
            shotDao.upsertAll(remoteShots.map { it.toEntity() })
            syncMetadataDao.upsert(SyncMetadataEntity(syncKey, System.currentTimeMillis()))
            Timber.d("Synced %d shots for player %s", remoteShots.size, playerId)
        }.onFailure { error ->
            Timber.e(error, "Failed to sync shots for player %s", playerId)
        }
    }

    private suspend fun shouldSync(key: String): Boolean {
        val metadata = syncMetadataDao.get(key) ?: return true
        return System.currentTimeMillis() - metadata.lastSyncedAt > SYNC_INTERVAL_MS
    }

    companion object {
        private const val PLAYERS_SYNC_KEY = "players"
        private const val SYNC_INTERVAL_MS = 5 * 60 * 1000L
        private const val SHOT_PAGE_SIZE = 5

        fun shotsSyncKey(playerId: String): String = "shots_$playerId"
    }
}
