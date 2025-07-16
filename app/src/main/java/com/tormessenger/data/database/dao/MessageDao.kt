package com.tormessenger.data.database.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.tormessenger.data.database.entity.Message

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE friendOnionAddress = :friendOnionAddress ORDER BY timestamp ASC")
    fun getMessagesForFriend(friendOnionAddress: String): LiveData<List<Message>>
    
    @Query("SELECT * FROM messages WHERE isOutgoing = 1 AND isDelivered = 0 AND retryCount < maxRetries")
    suspend fun getPendingMessages(): List<Message>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId")
    suspend fun markAsDelivered(messageId: String)
    
    @Query("UPDATE messages SET isRead = 1 WHERE friendOnionAddress = :friendOnionAddress AND isOutgoing = 0")
    suspend fun markMessagesAsRead(friendOnionAddress: String)
    
    @Query("UPDATE messages SET retryCount = retryCount + 1 WHERE id = :messageId")
    suspend fun incrementRetryCount(messageId: String)
}
