package com.example.salty

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salty.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.SecureRandom

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: PasswordRepository
    private val secureRandom = SecureRandom()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<PasswordCategory?>(null)
    val selectedCategory: StateFlow<PasswordCategory?> = _selectedCategory.asStateFlow()
    
    private val _passwordToEdit = MutableStateFlow<PasswordEntry?>(null)
    val passwordToEdit: StateFlow<PasswordEntry?> = _passwordToEdit.asStateFlow()
    
    val passwords: StateFlow<List<PasswordEntry>>
    val passwordCount: StateFlow<Int>

    init {
        val database = PasswordDatabase.getDatabase(application)
        repository = PasswordRepository(database.passwordDao())
        
        passwords = combine(
            _searchQuery,
            _selectedCategory,
            repository.allPasswords
        ) { query, category, allPasswords ->
            var result = allPasswords
            
            if (query.isNotBlank()) {
                result = result.filter { password ->
                    password.title.contains(query, ignoreCase = true) ||
                    password.username.contains(query, ignoreCase = true) ||
                    password.website.contains(query, ignoreCase = true)
                }
            }
            
            if (category != null) {
                result = result.filter { it.category == category }
            }
            
            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        passwordCount = repository.passwordCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCategory(category: PasswordCategory?) {
        _selectedCategory.value = category
    }
    
    fun setPasswordToEdit(password: PasswordEntry?) {
        _passwordToEdit.value = password
    }
    
    fun addPassword(
        title: String,
        username: String,
        password: String,
        website: String = "",
        notes: String = "",
        category: PasswordCategory = PasswordCategory.OTHER
    ) {
        viewModelScope.launch {
            val entry = PasswordEntry(
                title = title,
                username = username,
                password = password,
                website = website,
                notes = notes,
                category = category
            )
            repository.insertPassword(entry)
        }
    }
    
    fun updatePassword(
        id: Long,
        title: String,
        username: String,
        password: String,
        website: String = "",
        notes: String = "",
        category: PasswordCategory = PasswordCategory.OTHER
    ) {
        viewModelScope.launch {
            val entry = PasswordEntry(
                id = id,
                title = title,
                username = username,
                password = password,
                website = website,
                notes = notes,
                category = category
            )
            repository.updatePassword(entry)
            _passwordToEdit.value = null
        }
    }
    
    fun deletePassword(password: PasswordEntry) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }
    
    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.WEAK
        
        var score = 0
        
        // Length checks
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        
        // Character variety checks
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.FAIR
            score <= 5 -> PasswordStrength.GOOD
            score <= 6 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
    
    companion object {
        const val SPECIAL_CHARS = "!@#\$%^&*()_+-=[]{}|;:,.<>?"
    }
    
    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = true
    ): String {
        val chars = buildString {
            if (includeLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (includeUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (includeNumbers) append("0123456789")
            if (includeSpecialChars) append(SPECIAL_CHARS)
        }
        
        if (chars.isEmpty()) return ""
        
        return (1..length)
            .map { chars[secureRandom.nextInt(chars.length)] }
            .joinToString("")
    }
}
