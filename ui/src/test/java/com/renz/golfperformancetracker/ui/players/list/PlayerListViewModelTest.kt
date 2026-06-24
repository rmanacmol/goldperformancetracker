package com.renz.golfperformancetracker.ui.players.list

import app.cash.turbine.test
import com.renz.golfperformancetracker.domain.model.Player
import com.renz.golfperformancetracker.domain.model.SyncStatus
import com.renz.golfperformancetracker.domain.repository.GolfRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: GolfRepository = mockk(relaxed = true)
    private val syncStatus = MutableStateFlow(SyncStatus.IDLE)
    private val isOffline = MutableStateFlow(false)

    private val samplePlayers = listOf(
        Player("1", "Jordan Lee", "Driver", null, 168.4, 12.5, 265.0, 2450.0),
        Player("2", "Mia Chen", "7 Iron", null, 122.8, 18.2, 158.4, 6200.0),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observePlayers() } returns flowOf(samplePlayers)
        every { repository.syncStatus } returns syncStatus
        every { repository.isOffline } returns isOffline
        coEvery { repository.refreshPlayers(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filters players by search query`() = runTest {
        val viewModel = PlayerListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(2, awaitItem().filteredPlayers.size)
            viewModel.onSearchQueryChanged("Mia")
            advanceUntilIdle()
            val filtered = awaitItem()
            assertEquals(1, filtered.filteredPlayers.size)
            assertEquals("Mia Chen", filtered.filteredPlayers.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filters players by club`() = runTest {
        val viewModel = PlayerListViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClubFilterSelected("Driver")
            advanceUntilIdle()
            val filtered = awaitItem()
            assertEquals(1, filtered.filteredPlayers.size)
            assertEquals("Driver", filtered.filteredPlayers.first().club)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh triggers repository sync`() = runTest {
        val viewModel = PlayerListViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh(force = true)
        advanceUntilIdle()

        coVerify(atLeast = 1) { repository.refreshPlayers(force = true) }
    }
}
