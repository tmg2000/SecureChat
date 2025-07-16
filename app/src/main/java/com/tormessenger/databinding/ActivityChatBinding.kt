package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tormessenger.R

class ActivityChatBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val rvMessages: RecyclerView,
    val etMessage: TextInputEditText,
    val btnSend: MaterialButton
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityChatBinding {
            val root = inflater.inflate(R.layout.activity_chat, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityChatBinding {
            return ActivityChatBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                rvMessages = root.findViewById(R.id.rvMessages),
                etMessage = root.findViewById(R.id.etMessage),
                btnSend = root.findViewById(R.id.btnSend)
            )
        }
    }
}
