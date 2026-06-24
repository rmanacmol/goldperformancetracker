package com.renz.golfperformancetracker.ui.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PlayerStatsViewModel(
    savedStateHandle: SavedStateHandle,
    repository: GolfRepository,
) : ViewModel() {

    private val playerId: String = checkNotNull(savedStateHandle["playerId"])

    val uiState: StateFlow<PlayerStatsUiState> = repository.observePlayerStats(playerId)
        .map { stats ->
            PlayerStatsUiState(
                stats = stats,
                isLoading = stats == null,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerStatsUiState(),
        )
}
