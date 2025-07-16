package com.tormessenger.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tormessenger.R
import com.tormessenger.TorMessengerApplication
import com.tormessenger.data.database.entity.Message
import com.tormessenger.databinding.ActivityChatBinding
import com.tormessenger.service.MessageService
import com.tormessenger.ui.adapter.MessagesAdapter
import kotlinx.coroutines.launch
import java.util.*

class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var friendAddress: String
    private lateinit var friendName: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        friendAddress = intent.getStringExtra("friend_address") ?: ""
        friendName = intent.getStringExtra("friend_name") ?: ""
        
        if (friendAddress.isEmpty()) {
            finish()
            return
        }
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeMessages()
        markMessagesAsRead()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = friendName
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messagesAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun observeMessages() {
        val database = (application as TorMessengerApplication).database
        database.messageDao().getMessagesForFriend(friendAddress).observe(this) { messages ->
            messagesAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }
    }
    
    private fun markMessagesAsRead() {
        lifecycleScope.launch {
            val database = (application as TorMessengerApplication).database
            database.messageDao().markMessagesAsRead(friendAddress)
        }
    }
    
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return
        
        binding.etMessage.text?.clear()
        
        lifecycleScope.launch {
            try {
                val messageId = UUID.randomUUID().toString()
                
                // Start message service to handle sending
                val intent = Intent(this@ChatActivity, MessageService::class.java).apply {
                    action = "SEND_MESSAGE"
                    putExtra("friend_address", friendAddress)
                    putExtra("message_content", messageText)
                    putExtra("message_id", messageId)
                }
                startService(intent)
                
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, getString(R.string.error_message_send_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
