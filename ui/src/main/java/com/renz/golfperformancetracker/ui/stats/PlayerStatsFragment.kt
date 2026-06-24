package com.renz.golfperformancetracker.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.renz.golfperformancetracker.ui.R
import com.renz.golfperformancetracker.ui.databinding.FragmentPlayerStatsBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerStatsFragment : Fragment() {

    private var _binding: FragmentPlayerStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerStatsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlayerStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loadingIndicator.isVisible = state.isLoading
                    val stats = state.stats ?: return@collect

                    binding.toolbar.title = getString(R.string.stats_title, stats.player.name)
                    binding.shotCount.text = getString(R.string.stats_shot_count, stats.shotCount)
                    binding.avgSpeed.text = getString(R.string.metric_ball_speed_value, stats.avgBallSpeed)
                    binding.maxSpeed.text = getString(R.string.stats_max_speed, stats.maxBallSpeed)
                    binding.minSpeed.text = getString(R.string.stats_min_speed, stats.minBallSpeed)
                    binding.avgDistance.text = getString(R.string.metric_carry_distance_value, stats.avgCarryDistance)

                    binding.speedChart.setData(stats.speedTrend)
                    binding.carryProgress.max = stats.speedTrend.maxOfOrNull { it.carryDistance.toInt() } ?: 100
                    binding.carryProgress.progress = stats.avgCarryDistance.toInt()

                    binding.emptyChart.isVisible = stats.speedTrend.isEmpty()
                    binding.speedChart.isVisible = stats.speedTrend.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
