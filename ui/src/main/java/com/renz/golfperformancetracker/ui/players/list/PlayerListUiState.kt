package com.renz.golfperformancetracker.ui.players.list

import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.SyncStatus

data class PlayerListUiState(
    val players: List<Player> = emptyList(),
    val filteredPlayers: List<Player> = emptyList(),
    val searchQuery: String = "",
    val selectedClubFilter: String? = null,
    val availableClubs: List<String> = emptyList(),
    val isOffline: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = filteredPlayers.isEmpty() && syncStatus != SyncStatus.SYNCING
}
