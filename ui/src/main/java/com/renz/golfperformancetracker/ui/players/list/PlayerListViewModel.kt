package com.renz.golfperformancetracker.ui.players.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renz.golfperformancetracker.domain.model.SyncStatus
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerListViewModel(
    private val repository: GolfRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedClubFilter = MutableStateFlow<String?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlayerListUiState> = combine(
        repository.observePlayers(),
        searchQuery,
        selectedClubFilter,
        repository.isOffline,
        combine(repository.syncStatus, isRefreshing, errorMessage) { syncStatus, refreshing, error ->
            Triple(syncStatus, refreshing, error)
        },
    ) { players, query, clubFilter, offline, status ->
        val (syncStatus, refreshing, error) = status
        val clubs = players.map { it.club }.distinct().sorted()
        val filtered = players.filter { player ->
            val matchesQuery = query.isBlank() ||
                player.name.contains(query, ignoreCase = true) ||
                player.club.contains(query, ignoreCase = true)
            val matchesClub = clubFilter.isNullOrBlank() || player.club == clubFilter
            matchesQuery && matchesClub
        }
        PlayerListUiState(
            players = players,
            filteredPlayers = filtered,
            searchQuery = query,
            selectedClubFilter = clubFilter,
            availableClubs = clubs,
            isOffline = offline,
            syncStatus = syncStatus,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerListUiState(),
    )

    init {
        refresh(force = false)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onClubFilterSelected(club: String?) {
        selectedClubFilter.value = club
    }

    fun refresh(force: Boolean = true) {
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            repository.refreshPlayers(force = force)
            if (repository.syncStatus.value == SyncStatus.ERROR) {
                errorMessage.value = "Unable to sync. Showing cached data when available."
            }
            isRefreshing.value = false
        }
    }
}
