package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tormessenger.R

class ItemFriendBinding private constructor(
    val root: View,
    val tvAvatar: TextView,
    val tvFriendName: TextView,
    val tvOnionAddress: TextView,
    val tvLastSeen: TextView,
    val viewStatusIndicator: View,
    val tvUnreadCount: TextView
) {
    companion object {
        fun inflate(inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean): ItemFriendBinding {
            val root = inflater.inflate(R.layout.item_friend, parent, attachToParent)
            return bind(root)
        }
        
        fun bind(root: View): ItemFriendBinding {
            return ItemFriendBinding(
                root = root,
                tvAvatar = root.findViewById(R.id.tvAvatar),
                tvFriendName = root.findViewById(R.id.tvFriendName),
                tvOnionAddress = root.findViewById(R.id.tvOnionAddress),
                tvLastSeen = root.findViewById(R.id.tvLastSeen),
                viewStatusIndicator = root.findViewById(R.id.viewStatusIndicator),
                tvUnreadCount = root.findViewById(R.id.tvUnreadCount)
            )
        }
    }
}
