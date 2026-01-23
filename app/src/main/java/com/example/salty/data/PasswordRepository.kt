package com.example.salty.data

import kotlinx.coroutines.flow.Flow

class PasswordRepository(private val passwordDao: PasswordDao) {
    
    val allPasswords: Flow<List<PasswordEntry>> = passwordDao.getAllPasswords()
    val passwordCount: Flow<Int> = passwordDao.getPasswordCount()

    fun searchPasswords(query: String): Flow<List<PasswordEntry>> {
        return passwordDao.searchPasswords(query)
    }

    fun getPasswordsByCategory(category: PasswordCategory): Flow<List<PasswordEntry>> {
        return passwordDao.getPasswordsByCategory(category)
    }

    suspend fun getPasswordById(id: Long): PasswordEntry? {
        return passwordDao.getPasswordById(id)
    }

    suspend fun insertPassword(password: PasswordEntry): Long {
        return passwordDao.insertPassword(password)
    }

    suspend fun updatePassword(password: PasswordEntry) {
        passwordDao.updatePassword(password.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePassword(password: PasswordEntry) {
        passwordDao.deletePassword(password)
    }

    suspend fun deletePasswordById(id: Long) {
        passwordDao.deletePasswordById(id)
    }
}
