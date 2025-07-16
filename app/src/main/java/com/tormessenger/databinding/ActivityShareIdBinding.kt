package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.tormessenger.R

class ActivityShareIdBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val ivQrCode: ImageView,
    val tvOnionAddress: TextView,
    val btnCopyId: MaterialButton,
    val btnShareVia: MaterialButton
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityShareIdBinding {
            val root = inflater.inflate(R.layout.activity_share_id, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityShareIdBinding {
            return ActivityShareIdBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                ivQrCode = root.findViewById(R.id.ivQrCode),
                tvOnionAddress = root.findViewById(R.id.tvOnionAddress),
                btnCopyId = root.findViewById(R.id.btnCopyId),
                btnShareVia = root.findViewById(R.id.btnShareVia)
            )
        }
    }
}
