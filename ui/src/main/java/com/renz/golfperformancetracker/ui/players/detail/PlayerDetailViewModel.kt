package com.renz.golfperformancetracker.ui.players.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.renz.golfperformancetracker.domain.model.SyncStatus
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GolfRepository,
) : ViewModel() {

    private val playerId: String = checkNotNull(savedStateHandle["playerId"])
    private val statsExpanded = MutableStateFlow(false)

    val shotsPagingFlow = repository.pagingShots(playerId).cachedIn(viewModelScope)

    val uiState: StateFlow<PlayerDetailUiState> = combine(
        repository.observePlayer(playerId),
        repository.isOffline,
        repository.syncStatus,
        statsExpanded,
    ) { player, offline, syncStatus, expanded ->
        PlayerDetailUiState(
            player = player,
            isOffline = offline,
            syncStatus = syncStatus,
            isLoading = player == null && syncStatus == SyncStatus.SYNCING,
            statsExpanded = expanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerDetailUiState(),
    )

    init {
        viewModelScope.launch {
            repository.refreshPlayerShots(playerId, force = true)
        }
    }

    fun toggleStatsExpanded() {
        statsExpanded.update { !it }
    }
}
