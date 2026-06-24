package com.renz.golfperformancetracker.ui.stats

import com.renz.golfperformancetracker.domain.model.PlayerStatsSummary

data class PlayerStatsUiState(
    val stats: PlayerStatsSummary? = null,
    val isLoading: Boolean = true,
)
