package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tormessenger.R

class ActivityAddFriendBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val etFriendName: TextInputEditText,
    val etOnionId: TextInputEditText,
    val btnScanQr: MaterialButton,
    val btnAddFriend: MaterialButton
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityAddFriendBinding {
            val root = inflater.inflate(R.layout.activity_add_friend, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityAddFriendBinding {
            return ActivityAddFriendBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                etFriendName = root.findViewById(R.id.etFriendName),
                etOnionId = root.findViewById(R.id.etOnionId),
                btnScanQr = root.findViewById(R.id.btnScanQr),
                btnAddFriend = root.findViewById(R.id.btnAddFriend)
            )
        }
    }
}
