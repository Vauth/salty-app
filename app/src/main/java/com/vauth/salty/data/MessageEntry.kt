package com.vauth.salty.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalMessage: String,
    val encodedMessage: String,
    val salt: String,
    val notes: String = "",
    val messageType: MessageType = MessageType.ENCODED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MessageType(val displayName: String) {
    ENCODED("Encoded"),
    DECODED("Decoded"),
    BOTH("Both")
}
