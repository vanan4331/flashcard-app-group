package com.example.flashcardapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedule_table ORDER BY time DESC")
    suspend fun getAll(): List<ScheduleEntity>
}