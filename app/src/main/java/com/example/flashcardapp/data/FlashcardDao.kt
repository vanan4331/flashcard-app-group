package com.example.flashcardapp.data

import androidx.room.*

@Dao
interface FlashcardDao {

    // ✅ Lấy tất cả flashcard
    @Query("SELECT * FROM Flashcards")
    suspend fun getAll(): List<Flashcard>

    // ✅ Lấy thẻ cần ôn
    @Query("SELECT * FROM Flashcards WHERE nextReview <= :currentTime")
    suspend fun getCardsToReview(currentTime: Long): List<Flashcard>

    // ✅ Thêm flashcard
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: Flashcard)

    // ✅ Cập nhật flashcard
    @Update
    suspend fun update(flashcard: Flashcard)
}