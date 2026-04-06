package com.example.flashcardapp.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R
import com.example.flashcardapp.data.AppDatabase
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class VocabularyActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var currentWord: String = ""
    private var currentDefinition: String = ""
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabulary)

        db = AppDatabase.getDatabase(this)
        tts = TextToSpeech(this, this)

        val edtSearch = findViewById<EditText>(R.id.edtSearchWord)
        val btnSearch = findViewById<Button>(R.id.btnSearchWord)
        val tvResultWord = findViewById<TextView>(R.id.tvResultWord)
        val tvResultMeaning = findViewById<TextView>(R.id.tvResultMeaning)
        val btnAdd = findViewById<Button>(R.id.btnAddToStudy)

        findViewById<ImageButton>(R.id.btnBackHomeVocabulary).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnGoHomeVocabulary)?.setOnClickListener { finish() }

        btnSearch.setOnClickListener {
            val word = edtSearch.text.toString().trim()
            if (word.isNotEmpty()) {
                searchWord(word, tvResultWord, tvResultMeaning, btnAdd)
            }
        }

        tvResultWord.setOnClickListener {
            if (currentWord.isNotEmpty()) speakOut(currentWord)
        }

        // ĐÃ SỬA LỖI: Truyền đầy đủ tham số mà Flashcard yêu cầu
        btnAdd.setOnClickListener {
            if (currentWord.isNotEmpty()) {
                val newCard = Flashcard(
                    word = currentWord,
                    definition = currentDefinition,
                    phonetic = "", // Thêm tham số còn thiếu theo lỗi báo
                    partOfSpeech = "noun", // Thêm tham số còn thiếu theo lỗi báo
                    nextReview = System.currentTimeMillis()
                )

                CoroutineScope(Dispatchers.IO).launch {
                    db.flashcardDao().insert(newCard)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@VocabularyActivity, "Đã lưu từ '$currentWord'!", Toast.LENGTH_SHORT).show()
                        btnAdd.isEnabled = false
                    }
                }
            }
        }
    }

    private fun searchWord(word: String, tvWord: TextView, tvMeaning: TextView, btnAdd: Button) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.translateWord(word = word)

                val translated = response.split("\"").getOrNull(1) ?: "Không có nghĩa"

                withContext(Dispatchers.Main) {
                    currentWord = word
                    currentDefinition = translated

                    tvWord.text = "Từ: $word (Loa 🔊)"
                    tvMeaning.text = "Nghĩa: $translated"

                    btnAdd.isEnabled = true
                    speakOut(currentWord)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvWord.text = "Không tìm thấy!"
                    tvMeaning.text = e.message
                    btnAdd.isEnabled = false
                }
            }
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale.US
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}