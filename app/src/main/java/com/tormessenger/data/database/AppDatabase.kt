package com.tormessenger.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tormessenger.data.database.dao.FriendDao
import com.tormessenger.data.database.dao.MessageDao
import com.tormessenger.data.database.entity.Friend
import com.tormessenger.data.database.entity.Message

@Database(
    entities = [Friend::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun messageDao(): MessageDao
}
