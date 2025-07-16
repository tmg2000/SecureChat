package com.tormessenger.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.tormessenger.R
import com.tormessenger.databinding.ActivityShareIdBinding
import com.tormessenger.service.TorService

class ShareIdActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityShareIdBinding
    private var onionAddress: String? = null
    private var publicKey: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareIdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        loadUserData()
        generateQRCode()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.share_your_id)
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCopyId.setOnClickListener {
            copyIdToClipboard()
        }
        
        binding.btnShareVia.setOnClickListener {
            shareId()
        }
    }
    
    private fun loadUserData() {
        // In a real implementation, this would get data from TorService
        // For now, we'll use placeholder data
        onionAddress = "example123abc456def.onion"
        publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
        
        binding.tvOnionAddress.text = onionAddress
    }
    
    private fun generateQRCode() {
        val qrData = "$onionAddress|$publicKey"
        
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400)
            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun copyIdToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Onion ID", onionAddress)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.id_copied), Toast.LENGTH_SHORT).show()
    }
    
    private fun shareId() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "My Tor Messenger ID: $onionAddress")
            putExtra(Intent.EXTRA_SUBJECT, "Tor Messenger ID")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
