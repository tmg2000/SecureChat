package com.tormessenger.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tormessenger.R
import com.tormessenger.TorMessengerApplication
import com.tormessenger.ui.MainActivity
import info.guardianproject.netcipher.proxy.OrbotHelper
import kotlinx.coroutines.*
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyPair
import java.util.concurrent.ConcurrentHashMap

class TorService : Service() {
    
    companion object {
        private const val TAG = "TorService"
        private const val NOTIFICATION_ID = 1001
        private const val TOR_PORT = 9050
        private const val HIDDEN_SERVICE_PORT = 8080
    }
    
    private val binder = TorServiceBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isConnected = false
    private var onionAddress: String? = null
    private var keyPair: KeyPair? = null
    private var serverSocket: ServerSocket? = null
    private val activeConnections = ConcurrentHashMap<String, Socket>()
    
    private var connectionListener: TorConnectionListener? = null
    
    interface TorConnectionListener {
        fun onTorConnected(onionAddress: String)
        fun onTorDisconnected()
        fun onTorError(error: String)
        fun onMessageReceived(fromAddress: String, message: String)
    }
    
    inner class TorServiceBinder : Binder() {
        fun getService(): TorService = this@TorService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TOR" -> startTorConnection()
            "STOP_TOR" -> stopTorConnection()
        }
        return START_STICKY
    }
    
    fun setConnectionListener(listener: TorConnectionListener) {
        this.connectionListener = listener
    }
    
    fun startTorConnection() {
        if (isConnected) return
        
        serviceScope.launch {
            try {
                if (!OrbotHelper.isOrbotInstalled(this@TorService)) {
                    connectionListener?.onTorError("Orbot is not installed")
                    return@launch
                }
                
                if (!OrbotHelper.isOrbotRunning(this@TorService)) {
                    val intent = OrbotHelper.getOrbotStartIntent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    
                    // Wait for Orbot to start
                    var attempts = 0
                    while (!OrbotHelper.isOrbotRunning(this@TorService) && attempts < 30) {
                        delay(1000)
                        attempts++
                    }
                    
                    if (!OrbotHelper.isOrbotRunning(this@TorService)) {
                        connectionListener?.onTorError("Failed to start Orbot")
                        return@launch
                    }
                }
                
                // Generate or load key pair
                keyPair = generateOrLoadKeyPair()
                
                // Start hidden service
                startHiddenService()
                
                isConnected = true
                onionAddress?.let { address ->
                    connectionListener?.onTorConnected(address)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Tor connection", e)
                connectionListener?.onTorError("Failed to connect to Tor: ${e.message}")
            }
        }
    }
    
    fun stopTorConnection() {
        serviceScope.launch {
            try {
                isConnected = false
                serverSocket?.close()
                activeConnections.values.forEach { it.close() }
                activeConnections.clear()
                connectionListener?.onTorDisconnected()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping Tor connection", e)
            }
        }
    }
    
    private suspend fun generateOrLoadKeyPair(): KeyPair {
        val sharedPrefs = getSharedPreferences("tor_keys", Context.MODE_PRIVATE)
        val privateKeyString = sharedPrefs.getString("private_key", null)
        val publicKeyString = sharedPrefs.getString("public_key", null)
        
        return if (privateKeyString != null && publicKeyString != null) {
            // Load existing key pair
            val cryptoManager = com.tormessenger.crypto.CryptoManager()
            val privateKey = cryptoManager.stringToPrivateKey(privateKeyString)
            val publicKey = cryptoManager.stringToPublicKey(publicKeyString)
            KeyPair(publicKey, privateKey)
        } else {
            // Generate new key pair
            val cryptoManager = com.tormessenger.crypto.CryptoManager()
            val newKeyPair = cryptoManager.generateKeyPair()
            
            // Save key pair
            sharedPrefs.edit()
                .putString("private_key", cryptoManager.privateKeyToString(newKeyPair.private))
                .putString("public_key", cryptoManager.publicKeyToString(newKeyPair.public))
                .apply()
            
            newKeyPair
        }
    }
    
    private suspend fun startHiddenService() {
        try {
            serverSocket = ServerSocket(HIDDEN_SERVICE_PORT)
            
            // Generate onion address (simplified - in real implementation this would be more complex)
            onionAddress = generateOnionAddress()
            
            // Start accepting connections
            serviceScope.launch {
                while (isConnected && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { socket ->
                            handleClientConnection(socket)
                        }
                    } catch (e: IOException) {
                        if (isConnected) {
                            Log.e(TAG, "Error accepting connection", e)
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting hidden service", e)
            throw e
        }
    }
    
    private fun generateOnionAddress(): String {
        // Simplified onion address generation
        // In a real implementation, this would derive from the private key
        val randomPart = (1..16).map { ('a'..'z').random() }.joinToString("")
        return "${randomPart}.onion"
    }
    
    private fun handleClientConnection(socket: Socket) {
        serviceScope.launch {
            try {
                val input = socket.getInputStream().bufferedReader()
                val output = socket.getOutputStream().bufferedWriter()
                
                // Simple protocol: read messages line by line
                var line: String?
                while (socket.isConnected && input.readLine().also { line = it } != null) {
                    line?.let { message ->
                        // Parse message and notify listener
                        val parts = message.split("|", limit = 2)
                        if (parts.size == 2) {
                            val fromAddress = parts[0]
                            val messageContent = parts[1]
                            connectionListener?.onMessageReceived(fromAddress, messageContent)
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client connection", e)
            } finally {
                socket.close()
            }
        }
    }
    
    fun sendMessage(toAddress: String, message: String): Boolean {
        if (!isConnected) return false
        
        serviceScope.launch {
            try {
                // Connect to recipient's hidden service
                val socket = Socket()
                // In real implementation, this would use Tor proxy
                // socket.connect(InetSocketAddress(toAddress, HIDDEN_SERVICE_PORT))
                
                val output = socket.getOutputStream().bufferedWriter()
                output.write("${onionAddress}|$message\n")
                output.flush()
                
                socket.close()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
        
        return true
    }
    
    fun getOnionAddress(): String? = onionAddress
    fun getPublicKey(): String? = keyPair?.let { 
        com.tormessenger.crypto.CryptoManager().publicKeyToString(it.public)
    }
    fun isConnected(): Boolean = isConnected
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, TorMessengerApplication.TOR_SERVICE_CHANNEL_ID)
            .setContentTitle("Tor Messenger")
            .setContentText("Connected to Tor network")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTorConnection()
        serviceScope.cancel()
    }
}
