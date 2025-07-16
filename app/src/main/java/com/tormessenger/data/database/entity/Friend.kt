package com.tormessenger.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey
    val onionAddress: String,
    val displayName: String,
    val publicKey: String,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val addedAt: Long = System.currentTimeMillis()
)
