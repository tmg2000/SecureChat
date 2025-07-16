package com.tormessenger.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.tormessenger.R

class ActivityQrScanBinding private constructor(
    val root: View,
    val toolbar: Toolbar,
    val barcodeScanner: DecoratedBarcodeView
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityQrScanBinding {
            val root = inflater.inflate(R.layout.activity_qr_scan, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityQrScanBinding {
            return ActivityQrScanBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                barcodeScanner = root.findViewById(R.id.barcodeScanner)
            )
        }
    }
}
