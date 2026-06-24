package com.renz.golfperformancetracker.ui.players.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.renz.golfperformancetracker.domain.model.Shot
import com.renz.golfperformancetracker.ui.R
import com.renz.golfperformancetracker.ui.databinding.ItemShotBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShotPagingAdapter : PagingDataAdapter<Shot, ShotPagingAdapter.ShotViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShotViewHolder {
        val binding = ItemShotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShotViewHolder(binding, dateFormat)
    }

    override fun onBindViewHolder(holder: ShotViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    class ShotViewHolder(
        private val binding: ItemShotBinding,
        private val dateFormat: SimpleDateFormat,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shot: Shot) {
            binding.shotClub.text = shot.clubType
            binding.shotBallSpeed.text = binding.root.context.getString(
                R.string.metric_ball_speed_value,
                shot.ballSpeed,
            )
            binding.shotLaunchAngle.text = binding.root.context.getString(
                R.string.metric_launch_angle_value,
                shot.launchAngle,
            )
            binding.shotCarryDistance.text = binding.root.context.getString(
                R.string.metric_carry_distance_value,
                shot.carryDistance,
            )
            binding.shotSpinRate.text = binding.root.context.getString(
                R.string.metric_spin_rate_value,
                shot.spinRate,
            )
            binding.shotTimestamp.text = dateFormat.format(Date(shot.recordedAt))
            binding.shotMetricBar.setMetrics(
                speed = shot.ballSpeed,
                launchAngle = shot.launchAngle,
                carryDistance = shot.carryDistance,
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Shot>() {
        override fun areItemsTheSame(oldItem: Shot, newItem: Shot): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Shot, newItem: Shot): Boolean = oldItem == newItem
    }
}
