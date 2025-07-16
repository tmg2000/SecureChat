package com.tormessenger.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.tormessenger.R
import com.tormessenger.databinding.ActivityQrScanBinding

class QRScanActivity : AppCompatActivity(), DecoratedBarcodeView.TorchListener {
    
    private lateinit var binding: ActivityQrScanBinding
    private lateinit var captureManager: CaptureManager
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        
        if (checkCameraPermission()) {
            initializeScanner()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.scan_friend_qr)
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun initializeScanner() {
        captureManager = CaptureManager(this, binding.barcodeScanner)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        captureManager.decode()
        
        binding.barcodeScanner.setTorchListener(this)
        
        // Set up scan result callback
        binding.barcodeScanner.decodeContinuous { result ->
            val scannedData = result.text
            if (isValidQRData(scannedData)) {
                val resultIntent = Intent().apply {
                    putExtra("scanned_data", scannedData)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.invalid_qr_code), Toast.LENGTH_SHORT).show()
                // Continue scanning
                binding.barcodeScanner.resume()
            }
        }
    }
    
    private fun isValidQRData(data: String): Boolean {
        // Basic validation for QR data format: "onion_address|public_key"
        val parts = data.split("|")
        return parts.size >= 1 && parts[0].endsWith(".onion")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanner()
            } else {
                Toast.makeText(this, getString(R.string.error_camera_permission), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (::captureManager.isInitialized) {
            captureManager.onResume()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::captureManager.isInitialized) {
            captureManager.onPause()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::captureManager.isInitialized) {
            captureManager.onDestroy()
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::captureManager.isInitialized) {
            captureManager.onSaveInstanceState(outState)
        }
    }
    
    override fun onTorchOn() {
        // Handle torch on
    }
    
    override fun onTorchOff() {
        // Handle torch off
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
