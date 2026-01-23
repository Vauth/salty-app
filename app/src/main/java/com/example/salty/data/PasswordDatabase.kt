package com.example.salty.data

import android.content.Context
import androidx.room.*

@Database(entities = [PasswordEntry::class], version = 1, exportSchema = false)
@TypeConverters(PasswordConverters::class)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: PasswordDatabase? = null

        fun getDatabase(context: Context): PasswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java,
                    "salty_passwords_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PasswordConverters {
    @TypeConverter
    fun fromCategory(category: PasswordCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): PasswordCategory {
        return try {
            PasswordCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PasswordCategory.OTHER
        }
    }
}
