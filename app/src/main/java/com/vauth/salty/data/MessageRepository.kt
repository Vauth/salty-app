package com.vauth.salty.data

import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    
    val allMessages: Flow<List<MessageEntry>> = messageDao.getAllMessages()
    val messageCount: Flow<Int> = messageDao.getMessageCount()

    fun searchMessages(query: String): Flow<List<MessageEntry>> {
        return messageDao.searchMessages(query)
    }

    fun getMessagesByType(type: MessageType): Flow<List<MessageEntry>> {
        return messageDao.getMessagesByType(type)
    }

    suspend fun getMessageById(id: Long): MessageEntry? {
        return messageDao.getMessageById(id)
    }

    suspend fun insertMessage(message: MessageEntry): Long {
        return messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntry) {
        messageDao.updateMessage(message.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMessage(message: MessageEntry) {
        messageDao.deleteMessage(message)
    }

    suspend fun deleteMessageById(id: Long) {
        messageDao.deleteMessageById(id)
    }
}
