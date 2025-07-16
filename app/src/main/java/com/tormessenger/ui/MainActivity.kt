package com.tormessenger.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tormessenger.R
import com.tormessenger.TorMessengerApplication
import com.tormessenger.databinding.ActivityMainBinding
import com.tormessenger.service.TorService
import com.tormessenger.ui.adapter.FriendsAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TorService.TorConnectionListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var friendsAdapter: FriendsAdapter
    private var torService: TorService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TorService.TorServiceBinder
            torService = binder.getService()
            torService?.setConnectionListener(this@MainActivity)
            isBound = true
            updateTorStatus()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            torService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeFriends()
        
        // Bind to TorService
        val intent = Intent(this, TorService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter { friend ->
            // Open chat with friend
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("friend_address", friend.onionAddress)
            intent.putExtra("friend_name", friend.displayName)
            startActivity(intent)
        }
        
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = friendsAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnToggleTor.setOnClickListener {
            if (torService?.isConnected() == true) {
                torService?.stopTorConnection()
            } else {
                torService?.startTorConnection()
            }
        }
        
        binding.btnShareId.setOnClickListener {
            val intent = Intent(this, ShareIdActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnAddFriend.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeFriends() {
        val database = (application as TorMessengerApplication).database
        database.friendDao().getAllFriends().observe(this) { friends ->
            friendsAdapter.submitList(friends)
            binding.layoutEmptyState.visibility = if (friends.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }
    
    private fun updateTorStatus() {
        val isConnected = torService?.isConnected() ?: false
        
        if (isConnected) {
            binding.tvTorStatus.text = getString(R.string.tor_status_connected)
            binding.btnToggleTor.text = getString(R.string.disconnect_from_tor)
            binding.btnShareId.isEnabled = true
            
            val onionAddress = torService?.getOnionAddress()
            if (onionAddress != null) {
                binding.tvOnionAddress.text = getString(R.string.your_onion_id) + ": $onionAddress"
                binding.tvOnionAddress.visibility = android.view.View.VISIBLE
            }
        } else {
            binding.tvTorStatus.text = getString(R.string.tor_status_disconnected)
            binding.btnToggleTor.text = getString(R.string.connect_to_tor)
            binding.btnShareId.isEnabled = false
            binding.tvOnionAddress.visibility = android.view.View.GONE
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Open settings
                true
            }
            R.id.action_about -> {
                // Show about dialog
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // TorService.TorConnectionListener implementation
    override fun onTorConnected(onionAddress: String) {
        runOnUiThread {
            updateTorStatus()
            Toast.makeText(this, getString(R.string.tor_connected_notification), Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onTorDisconnected() {
        runOnUiThread {
            updateTorStatus()
            Toast.makeText(this, getString(R.string.tor_disconnected_notification), Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onTorError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onMessageReceived(fromAddress: String, message: String) {
        // Handle incoming message
        lifecycleScope.launch {
            // Process and save incoming message
            // This would decrypt the message and save it to database
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
