package com.example.flashcardapp.model

data class DictionaryEntry(
    val word: String,
    val phonetic: String?,
    val meanings: List<Meaning>,
    val sourceUrls: List<String>
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val definition: String,
    val example: String?
)