package com.renz.golfperformancetracker.ui.players.detail

import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.SyncStatus

data class PlayerDetailUiState(
    val player: Player? = null,
    val isOffline: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isLoading: Boolean = true,
    val statsExpanded: Boolean = false,
)
