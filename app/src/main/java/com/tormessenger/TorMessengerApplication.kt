package com.tormessenger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.tormessenger.data.database.AppDatabase

class TorMessengerApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "tor_messenger_channel"
        const val TOR_SERVICE_CHANNEL_ID = "tor_service_channel"
        const val MESSAGE_SERVICE_CHANNEL_ID = "message_service_channel"
    }
    
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tor_messenger_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            val mainChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Tor Messenger",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Tor Messenger"
            }
            
            val torServiceChannel = NotificationChannel(
                TOR_SERVICE_CHANNEL_ID,
                "Tor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tor connection service"
            }
            
            val messageServiceChannel = NotificationChannel(
                MESSAGE_SERVICE_CHANNEL_ID,
                "Message Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Message handling service"
            }
            
            notificationManager.createNotificationChannel(mainChannel)
            notificationManager.createNotificationChannel(torServiceChannel)
            notificationManager.createNotificationChannel(messageServiceChannel)
        }
    }
}
