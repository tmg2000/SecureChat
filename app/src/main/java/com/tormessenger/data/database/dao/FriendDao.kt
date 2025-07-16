package com.tormessenger.data.database.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.tormessenger.data.database.entity.Friend

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY displayName ASC")
    fun getAllFriends(): LiveData<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE onionAddress = :onionAddress")
    suspend fun getFriend(onionAddress: String): Friend?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)
    
    @Update
    suspend fun updateFriend(friend: Friend)
    
    @Delete
    suspend fun deleteFriend(friend: Friend)
    
    @Query("UPDATE friends SET isOnline = :isOnline, lastSeen = :lastSeen WHERE onionAddress = :onionAddress")
    suspend fun updateOnlineStatus(onionAddress: String, isOnline: Boolean, lastSeen: Long)
}
