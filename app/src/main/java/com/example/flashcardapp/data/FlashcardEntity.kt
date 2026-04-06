package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val word: String,
    val definition: String,
    val phonetic: String?,
    val partOfSpeech: String?,
    // SM-2 variables
    var interval: Int = 1,
    var repetition: Int = 0,
    var easeFactor: Double = 2.5,
    var nextReview: Long = System.currentTimeMillis()
)