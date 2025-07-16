package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tormessenger.R
import android.widget.TextView
import android.widget.LinearLayout

class ActivityMainBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val tvTorStatus: TextView,
    val tvOnionAddress: TextView,
    val btnToggleTor: MaterialButton,
    val btnShareId: MaterialButton,
    val btnAddFriend: MaterialButton,
    val rvFriends: RecyclerView,
    val layoutEmptyState: LinearLayout
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityMainBinding {
            val root = inflater.inflate(R.layout.activity_main, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityMainBinding {
            return ActivityMainBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                tvTorStatus = root.findViewById(R.id.tvTorStatus),
                tvOnionAddress = root.findViewById(R.id.tvOnionAddress),
                btnToggleTor = root.findViewById(R.id.btnToggleTor),
                btnShareId = root.findViewById(R.id.btnShareId),
                btnAddFriend = root.findViewById(R.id.btnAddFriend),
                rvFriends = root.findViewById(R.id.rvFriends),
                layoutEmptyState = root.findViewById(R.id.layoutEmptyState)
            )
        }
    }
}
