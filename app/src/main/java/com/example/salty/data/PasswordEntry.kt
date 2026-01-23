package com.example.salty.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val website: String = "",
    val notes: String = "",
    val category: PasswordCategory = PasswordCategory.OTHER,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PasswordCategory(val displayName: String, val icon: String) {
    SOCIAL("Social Media", "share"),
    EMAIL("Email", "email"),
    BANKING("Banking", "account_balance"),
    SHOPPING("Shopping", "shopping_cart"),
    WORK("Work", "work"),
    ENTERTAINMENT("Entertainment", "movie"),
    GAMING("Gaming", "sports_esports"),
    OTHER("Other", "more_horiz")
}

enum class PasswordStrength(val label: String, val score: Int) {
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    GOOD("Good", 3),
    STRONG("Strong", 4),
    VERY_STRONG("Very Strong", 5)
}
