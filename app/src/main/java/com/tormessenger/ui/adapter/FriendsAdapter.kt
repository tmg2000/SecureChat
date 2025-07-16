package com.tormessenger.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tormessenger.R
import com.tormessenger.data.database.entity.Friend
import com.tormessenger.databinding.ItemFriendBinding
import java.text.SimpleDateFormat
import java.util.*

class FriendsAdapter(
    private val onFriendClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class FriendViewHolder(
        private val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(friend: Friend) {
            binding.apply {
                tvFriendName.text = friend.displayName
                tvOnionAddress.text = friend.onionAddress
                
                // Set avatar with first letter of name
                tvAvatar.text = friend.displayName.firstOrNull()?.toString()?.uppercase() ?: "?"
                
                // Set status indicator
                val statusColor = if (friend.isOnline) {
                    R.color.status_online
                } else {
                    R.color.status_offline
                }
                viewStatusIndicator.backgroundTintList = 
                    android.content.res.ColorStateList.valueOf(
                        root.context.getColor(statusColor)
                    )
                
                // Set last seen text
                if (friend.isOnline) {
                    tvLastSeen.text = root.context.getString(R.string.online)
                } else {
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val lastSeenDate = Date(friend.lastSeen)
                    tvLastSeen.text = root.context.getString(R.string.last_seen, dateFormat.format(lastSeenDate))
                }
                
                // TODO: Set unread message count
                tvUnreadCount.visibility = android.view.View.GONE
                
                root.setOnClickListener {
                    onFriendClick(friend)
                }
            }
        }
    }
    
    private class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.onionAddress == newItem.onionAddress
        }
        
        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
