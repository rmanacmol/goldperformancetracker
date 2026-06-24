package com.renz.golfperformancetracker.ui.players.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.renz.golfperformancetracker.ui.R
import com.renz.golfperformancetracker.ui.databinding.FragmentPlayerDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerDetailFragment : Fragment() {

    private var _binding: FragmentPlayerDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerDetailViewModel by viewModel()
    private val shotAdapter = ShotPagingAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlayerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_stats -> {
                    val playerId = viewModel.uiState.value.player?.id ?: return@setOnMenuItemClickListener true
                    findNavController().navigate(
                        R.id.action_playerDetail_to_playerStats,
                        bundleOf("playerId" to playerId),
                    )
                    true
                }
                else -> false
            }
        }
        binding.shotsRecycler.adapter = shotAdapter

        binding.statsCard.setOnClickListener {
            viewModel.toggleStatsExpanded()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shotsPagingFlow.collectLatest { pagingData ->
                    shotAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            shotAdapter.loadStateFlow.collect { loadStates ->
                binding.shotsLoading.isVisible = loadStates.refresh is LoadState.Loading &&
                    shotAdapter.itemCount == 0
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loadingIndicator.isVisible = state.isLoading
                    binding.offlineBanner.isVisible = state.isOffline

                    val player = state.player ?: return@collect
                    binding.toolbar.title = player.name
                    binding.playerName.text = player.name
                    binding.playerClub.text = player.club
                    binding.avgBallSpeed.text = getString(R.string.metric_ball_speed_value, player.avgBallSpeed)
                    binding.avgLaunchAngle.text = getString(R.string.metric_launch_angle_value, player.avgLaunchAngle)
                    binding.avgCarryDistance.text = getString(R.string.metric_carry_distance_value, player.avgCarryDistance)
                    binding.avgSpinRate.text = getString(R.string.metric_spin_rate_value, player.avgSpinRate)
                    binding.detailMetricBar.setMetrics(
                        speed = player.avgBallSpeed,
                        launchAngle = player.avgLaunchAngle,
                        carryDistance = player.avgCarryDistance,
                    )

                    Glide.with(binding.playerAvatar)
                        .load(player.avatarUrl)
                        .placeholder(R.drawable.ic_golf_avatar_placeholder)
                        .error(R.drawable.ic_golf_avatar_placeholder)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.playerAvatar)

                    val motionLayout = binding.statsMotionLayout
                    val target = if (state.statsExpanded) R.id.expanded else R.id.collapsed
                    if (motionLayout.currentState != target) {
                        motionLayout.transitionToState(target)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
