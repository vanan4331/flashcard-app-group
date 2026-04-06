package com.example.flashcardapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.ScheduleEntity
import com.example.flashcardapp.data.ScheduleDao
@Database(
    entities = [Flashcard::class, ScheduleEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashcard_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}