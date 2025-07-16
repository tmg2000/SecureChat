package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tormessenger.R

class ItemMessageBinding private constructor(
    val root: View,
    val layoutSentMessage: LinearLayout,
    val layoutReceivedMessage: LinearLayout,
    val tvSentMessage: TextView,
    val tvSentTime: TextView,
    val ivDeliveryStatus: ImageView,
    val tvReceivedMessage: TextView,
    val tvReceivedTime: TextView
) {
    companion object {
        fun inflate(inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean): ItemMessageBinding {
            val root = inflater.inflate(R.layout.item_message, parent, attachToParent)
            return bind(root)
        }
        
        fun bind(root: View): ItemMessageBinding {
            return ItemMessageBinding(
                root = root,
                layoutSentMessage = root.findViewById(R.id.layoutSentMessage),
                layoutReceivedMessage = root.findViewById(R.id.layoutReceivedMessage),
                tvSentMessage = root.findViewById(R.id.tvSentMessage),
                tvSentTime = root.findViewById(R.id.tvSentTime),
                ivDeliveryStatus = root.findViewById(R.id.ivDeliveryStatus),
                tvReceivedMessage = root.findViewById(R.id.tvReceivedMessage),
                tvReceivedTime = root.findViewById(R.id.tvReceivedTime)
            )
        }
    }
}
