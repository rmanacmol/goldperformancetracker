package com.renz.golfperformancetracker.ui.players.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.renz.golfperformancetracker.ui.R
import com.renz.golfperformancetracker.ui.databinding.ItemPlayerBinding
import com.renz.golfperformancetracker.domain.model.Player

class PlayerAdapter(
    private val onPlayerClicked: (Player) -> Unit,
) : ListAdapter<Player, PlayerAdapter.PlayerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding, onPlayerClicked)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlayerViewHolder(
        private val binding: ItemPlayerBinding,
        private val onPlayerClicked: (Player) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Player) {
            binding.playerName.text = player.name
            binding.playerClub.text = player.club
            binding.playerAvgSpeed.text = binding.root.context.getString(
                R.string.metric_ball_speed_value,
                player.avgBallSpeed,
            )
            binding.playerAvgDistance.text = binding.root.context.getString(
                R.string.metric_carry_distance_value,
                player.avgCarryDistance,
            )
            binding.metricBar.setMetrics(
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

            binding.root.setOnClickListener { onPlayerClicked(player) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean = oldItem == newItem
    }
}
