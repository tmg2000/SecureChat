package com.tormessenger.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tormessenger.R
import com.tormessenger.data.database.entity.Message
import com.tormessenger.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(message.timestamp)
            
            if (message.isOutgoing) {
                // Show sent message
                binding.layoutSentMessage.visibility = android.view.View.VISIBLE
                binding.layoutReceivedMessage.visibility = android.view.View.GONE
                
                binding.tvSentMessage.text = message.content
                binding.tvSentTime.text = timeString
                
                // Set delivery status icon
                val statusIcon = if (message.isDelivered) {
                    R.drawable.ic_check
                } else if (message.retryCount > 0) {
                    R.drawable.ic_pending
                } else {
                    R.drawable.ic_pending
                }
                binding.ivDeliveryStatus.setImageResource(statusIcon)
                
                val statusColor = if (message.isDelivered) {
                    R.color.status_online
                } else {
                    R.color.status_offline
                }
                binding.ivDeliveryStatus.imageTintList = 
                    android.content.res.ColorStateList.valueOf(
                        binding.root.context.getColor(statusColor)
                    )
                
            } else {
                // Show received message
                binding.layoutSentMessage.visibility = android.view.View.GONE
                binding.layoutReceivedMessage.visibility = android.view.View.VISIBLE
                
                binding.tvReceivedMessage.text = message.content
                binding.tvReceivedTime.text = timeString
            }
        }
    }
    
    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
