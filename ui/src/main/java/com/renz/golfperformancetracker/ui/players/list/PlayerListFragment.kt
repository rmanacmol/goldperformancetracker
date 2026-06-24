package com.renz.golfperformancetracker.ui.players.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.renz.golfperformancetracker.ui.R
import com.renz.golfperformancetracker.ui.databinding.FragmentPlayerListBinding
import com.renz.golfperformancetracker.domain.model.SyncStatus
import kotlinx.coroutines.launch

class PlayerListFragment : Fragment() {

    private var _binding: FragmentPlayerListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerListViewModel by viewModel()
    private val playerAdapter = PlayerAdapter { player ->
        findNavController().navigate(
            R.id.action_playerList_to_playerDetail,
            bundleOf("playerId" to player.id),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlayerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playerRecycler.adapter = playerAdapter
        binding.playerRecycler.itemAnimator?.changeDuration = 300

        binding.searchInput.doAfterTextChanged { editable ->
            viewModel.onSearchQueryChanged(editable?.toString().orEmpty())
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh(force = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    playerAdapter.submitList(state.filteredPlayers)
                    binding.swipeRefresh.isRefreshing = state.isRefreshing
                    binding.offlineBanner.isVisible = state.isOffline
                    binding.emptyState.isVisible = state.isEmpty
                    binding.errorText.isVisible = state.errorMessage != null
                    binding.errorText.text = state.errorMessage
                    binding.loadingIndicator.isVisible =
                        state.syncStatus == SyncStatus.SYNCING && state.players.isEmpty()
                    renderClubFilters(state.availableClubs, state.selectedClubFilter)
                }
            }
        }
    }

    private fun renderClubFilters(clubs: List<String>, selectedClub: String?) {
        binding.clubFilterGroup.removeAllViews()
        binding.clubFilterGroup.addView(createClubChip(getString(R.string.filter_all_clubs), null, selectedClub))
        clubs.forEach { club ->
            binding.clubFilterGroup.addView(createClubChip(club, club, selectedClub))
        }
    }

    private fun createClubChip(label: String, club: String?, selectedClub: String?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            isChecked = selectedClub == club
            setOnClickListener { viewModel.onClubFilterSelected(club) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
