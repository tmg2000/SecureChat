package com.tormessenger.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val friendOnionAddress: String,
    val content: String,
    val encryptedContent: String,
    val timestamp: Date,
    val isOutgoing: Boolean,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val retryCount: Int = 0,
    val maxRetries: Int = 5
)
