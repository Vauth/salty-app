package com.example.salty.data

import android.content.Context
import androidx.room.*

@Database(entities = [MessageEntry::class], version = 2, exportSchema = false)
@TypeConverters(MessageConverters::class)
abstract class SaltyDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: SaltyDatabase? = null

        fun getDatabase(context: Context): SaltyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaltyDatabase::class.java,
                    "salty_database"
                )
                    .fallbackToDestructiveMigration() // Since we're changing the schema completely
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class MessageConverters {
    @TypeConverter
    fun fromMessageType(type: MessageType): String {
        return type.name
    }

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try {
            MessageType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageType.ENCODED
        }
    }
}
