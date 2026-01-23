package com.example.salty.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY updatedAt DESC")
    fun getAllMessages(): Flow<List<MessageEntry>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntry?

    @Query("SELECT * FROM messages WHERE title LIKE '%' || :query || '%' OR originalMessage LIKE '%' || :query || '%'")
    fun searchMessages(query: String): Flow<List<MessageEntry>>

    @Query("SELECT * FROM messages WHERE messageType = :type ORDER BY updatedAt DESC")
    fun getMessagesByType(type: MessageType): Flow<List<MessageEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntry): Long

    @Update
    suspend fun updateMessage(message: MessageEntry)

    @Delete
    suspend fun deleteMessage(message: MessageEntry)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("SELECT COUNT(*) FROM messages")
    fun getMessageCount(): Flow<Int>
}
