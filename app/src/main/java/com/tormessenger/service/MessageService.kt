package com.tormessenger.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tormessenger.TorMessengerApplication
import com.tormessenger.crypto.CryptoManager
import com.tormessenger.data.database.entity.Message
import com.tormessenger.ui.MainActivity
import kotlinx.coroutines.*
import java.util.*

class MessageService : Service() {
    
    companion object {
        private const val TAG = "MessageService"
        private const val NOTIFICATION_ID = 1002
        private const val RETRY_INTERVAL = 30000L // 30 seconds
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var retryJob: Job? = null
    private lateinit var database: com.tormessenger.data.database.AppDatabase
    private val cryptoManager = CryptoManager()
    
    override fun onCreate() {
        super.onCreate()
        database = (application as TorMessengerApplication).database
        startForeground(NOTIFICATION_ID, createNotification())
        startRetryLoop()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SEND_MESSAGE" -> {
                val friendAddress = intent.getStringExtra("friend_address")
                val messageContent = intent.getStringExtra("message_content")
                val messageId = intent.getStringExtra("message_id")
                
                if (friendAddress != null && messageContent != null && messageId != null) {
                    sendMessage(friendAddress, messageContent, messageId)
                }
            }
            "RETRY_MESSAGES" -> retryPendingMessages()
        }
        return START_STICKY
    }
    
    private fun startRetryLoop() {
        retryJob = serviceScope.launch {
            while (isActive) {
                try {
                    retryPendingMessages()
                    delay(RETRY_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in retry loop", e)
                    delay(RETRY_INTERVAL)
                }
            }
        }
    }
    
    private fun sendMessage(friendAddress: String, messageContent: String, messageId: String) {
        serviceScope.launch {
            try {
                val friend = database.friendDao().getFriend(friendAddress)
                if (friend == null) {
                    Log.e(TAG, "Friend not found: $friendAddress")
                    return@launch
                }
                
                // Encrypt message
                val publicKey = cryptoManager.stringToPublicKey(friend.publicKey)
                val encryptedMessage = cryptoManager.encryptMessage(messageContent, publicKey)
                
                // Create message entity
                val message = Message(
                    id = messageId,
                    friendOnionAddress = friendAddress,
                    content = messageContent,
                    encryptedContent = "${encryptedMessage.encryptedContent}|${encryptedMessage.encryptedKey}|${encryptedMessage.iv}",
                    timestamp = Date(),
                    isOutgoing = true,
                    isDelivered = false
                )
                
                // Save to database
                database.messageDao().insertMessage(message)
                
                // Try to send immediately
                val success = attemptMessageSend(friendAddress, encryptedMessage)
                if (success) {
                    database.messageDao().markAsDelivered(messageId)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }
    
    private suspend fun retryPendingMessages() {
        try {
            val pendingMessages = database.messageDao().getPendingMessages()
            
            for (message in pendingMessages) {
                try {
                    // Parse encrypted content
                    val parts = message.encryptedContent.split("|")
                    if (parts.size == 3) {
                        val encryptedMessage = CryptoManager.EncryptedMessage(
                            encryptedContent = parts[0],
                            encryptedKey = parts[1],
                            iv = parts[2]
                        )
                        
                        val success = attemptMessageSend(message.friendOnionAddress, encryptedMessage)
                        if (success) {
                            database.messageDao().markAsDelivered(message.id)
                        } else {
                            database.messageDao().incrementRetryCount(message.id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error retrying message ${message.id}", e)
                    database.messageDao().incrementRetryCount(message.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in retry pending messages", e)
        }
    }
    
    private suspend fun attemptMessageSend(friendAddress: String, encryptedMessage: CryptoManager.EncryptedMessage): Boolean {
        return try {
            // In a real implementation, this would use the TorService to send the message
            // For now, we'll simulate the sending process
            
            // Check if friend is online (simplified check)
            val friend = database.friendDao().getFriend(friendAddress)
            if (friend?.isOnline == true) {
                // Simulate successful send
                delay(1000) // Simulate network delay
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attempting to send message", e)
            false
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, TorMessengerApplication.MESSAGE_SERVICE_CHANNEL_ID)
            .setContentTitle("Message Service")
            .setContentText("Handling message delivery")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        retryJob?.cancel()
        serviceScope.cancel()
    }
}
