package com.vauth.salty

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vauth.salty.data.*
import com.vauth.salty.utils.DecodingException
import com.vauth.salty.utils.EncodingException
import com.vauth.salty.utils.HashUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SaltyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MessageRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedType = MutableStateFlow<MessageType?>(null)
    val selectedType: StateFlow<MessageType?> = _selectedType.asStateFlow()
    
    private val _messageToEdit = MutableStateFlow<MessageEntry?>(null)
    val messageToEdit: StateFlow<MessageEntry?> = _messageToEdit.asStateFlow()
    
    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult.asStateFlow()
    
    val messages: StateFlow<List<MessageEntry>>
    val messageCount: StateFlow<Int>

    init {
        val database = SaltyDatabase.getDatabase(application)
        repository = MessageRepository(database.messageDao())
        
        messages = combine(
            _searchQuery,
            _selectedType,
            repository.allMessages
        ) { query, type, allMessages ->
            var result = allMessages
            
            if (query.isNotBlank()) {
                result = result.filter { message ->
                    message.title.contains(query, ignoreCase = true) ||
                    message.originalMessage.contains(query, ignoreCase = true) ||
                    message.salt.contains(query, ignoreCase = true)
                }
            }
            
            if (type != null) {
                result = result.filter { it.messageType == type }
            }
            
            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        messageCount = repository.messageCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectType(type: MessageType?) {
        _selectedType.value = type
    }
    
    fun setMessageToEdit(message: MessageEntry?) {
        _messageToEdit.value = message
    }
    
    fun clearOperationResult() {
        _operationResult.value = null
    }
    
    /**
     * Encodes a message with the provided salt
     */
    fun encodeMessage(
        title: String,
        message: String,
        salt: String,
        notes: String = "",
        saveToHistory: Boolean = true
    ): OperationResult {
        return try {
            val encoded = HashUtils.encode(message, salt)
            
            if (saveToHistory) {
                viewModelScope.launch {
                    val entry = MessageEntry(
                        title = title,
                        originalMessage = message,
                        encodedMessage = encoded,
                        salt = salt,
                        notes = notes,
                        messageType = MessageType.ENCODED
                    )
                    repository.insertMessage(entry)
                }
            }
            
            OperationResult.Success(encoded)
        } catch (e: EncodingException) {
            OperationResult.Error("Encoding failed: ${e.message}")
        } catch (e: Exception) {
            OperationResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    /**
     * Decodes an encoded message with the provided salt
     */
    fun decodeMessage(
        title: String,
        encodedMessage: String,
        salt: String,
        notes: String = "",
        saveToHistory: Boolean = true
    ): OperationResult {
        return try {
            val decoded = HashUtils.decode(encodedMessage, salt)
            
            if (saveToHistory) {
                viewModelScope.launch {
                    val entry = MessageEntry(
                        title = title,
                        originalMessage = decoded,
                        encodedMessage = encodedMessage,
                        salt = salt,
                        notes = notes,
                        messageType = MessageType.DECODED
                    )
                    repository.insertMessage(entry)
                }
            }
            
            OperationResult.Success(decoded)
        } catch (e: DecodingException) {
            OperationResult.Error("Decoding failed: Wrong key or corrupted message")
        } catch (e: Exception) {
            OperationResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    fun deleteMessage(message: MessageEntry) {
        viewModelScope.launch {
            repository.deleteMessage(message)
        }
    }
    
    fun deleteMessageById(id: Long) {
        viewModelScope.launch {
            repository.deleteMessageById(id)
        }
    }
}

sealed class OperationResult {
    data class Success(val result: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
