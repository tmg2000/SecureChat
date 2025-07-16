package com.tormessenger.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tormessenger.R
import com.tormessenger.TorMessengerApplication
import com.tormessenger.data.database.entity.Friend
import com.tormessenger.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch

class AddFriendActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddFriendBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        
        // Check if we received data from QR scan
        handleQRScanResult()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.add_friend_title)
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnScanQr.setOnClickListener {
            val intent = Intent(this, QRScanActivity::class.java)
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE)
        }
        
        binding.btnAddFriend.setOnClickListener {
            addFriend()
        }
    }
    
    private fun handleQRScanResult() {
        val scannedData = intent.getStringExtra("scanned_data")
        if (scannedData != null) {
            // Parse scanned QR data
            val parts = scannedData.split("|")
            if (parts.size >= 2) {
                binding.etOnionId.setText(parts[0])
                // parts[1] would be the public key
            }
        }
    }
    
    private fun addFriend() {
        val friendName = binding.etFriendName.text.toString().trim()
        val onionId = binding.etOnionId.text.toString().trim()
        
        if (friendName.isEmpty()) {
            binding.etFriendName.error = "Friend name is required"
            return
        }
        
        if (onionId.isEmpty()) {
            binding.etOnionId.error = "Onion ID is required"
            return
        }
        
        if (!isValidOnionAddress(onionId)) {
            binding.etOnionId.error = getString(R.string.invalid_onion_address)
            return
        }
        
        lifecycleScope.launch {
            try {
                val database = (application as TorMessengerApplication).database
                
                // Check if friend already exists
                val existingFriend = database.friendDao().getFriend(onionId)
                if (existingFriend != null) {
                    Toast.makeText(this@AddFriendActivity, getString(R.string.friend_already_exists), Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Create new friend
                val friend = Friend(
                    onionAddress = onionId,
                    displayName = friendName,
                    publicKey = "", // TODO: Get public key from handshake
                    isOnline = false
                )
                
                database.friendDao().insertFriend(friend)
                
                Toast.makeText(this@AddFriendActivity, getString(R.string.friend_added_successfully), Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@AddFriendActivity, "Error adding friend: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun isValidOnionAddress(address: String): Boolean {
        // Basic validation for onion address
        return address.endsWith(".onion") && address.length > 7
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            val scannedData = data?.getStringExtra("scanned_data")
            if (scannedData != null) {
                // Parse scanned QR data
                val parts = scannedData.split("|")
                if (parts.isNotEmpty()) {
                    binding.etOnionId.setText(parts[0])
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    companion object {
        private const val QR_SCAN_REQUEST_CODE = 1001
    }
}
