package com.renz.golfperformancetracker.ui.di

import androidx.lifecycle.SavedStateHandle
import com.renz.golfperformancetracker.ui.players.detail.PlayerDetailViewModel
import com.renz.golfperformancetracker.ui.players.list.PlayerListViewModel
import com.renz.golfperformancetracker.ui.stats.PlayerStatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel { PlayerListViewModel(get()) }
    viewModel { (handle: SavedStateHandle) -> PlayerDetailViewModel(handle, get()) }
    viewModel { (handle: SavedStateHandle) -> PlayerStatsViewModel(handle, get()) }
}
