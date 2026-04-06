package com.example.flashcardapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R
import com.example.flashcardapp.data.AppDatabase
import com.example.flashcardapp.data.Flashcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import android.content.Intent
import com.example.flashcardapp.ui.MainActivity
class StudyActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var studyList = mutableListOf<Flashcard>()
    private var currentIndex = 0

    private lateinit var layoutContent: LinearLayout
    private lateinit var txtWord: TextView
    private lateinit var txtMeaning: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtCount: TextView
    private lateinit var btnShow: Button
    private lateinit var btnEasy: Button
    private lateinit var btnHard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        db = AppDatabase.getDatabase(this)
        initViews()
        loadFlashcards()

        // Xử lý quay về trang chủ: finish() sẽ đóng activity hiện tại
        // và quay lại màn hình gọi nó (thường là MainActivity)
        findViewById<Button>(R.id.btnGoHomeStudy)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnShow.setOnClickListener {
            txtMeaning.visibility = View.VISIBLE
            btnShow.visibility = View.GONE
        }

        btnEasy.setOnClickListener { processSM2(5) }
        btnHard.setOnClickListener { processSM2(2) }
    }

    private fun initViews() {
        layoutContent = findViewById(R.id.layoutContent)
        txtWord = findViewById(R.id.txtWordStudy)
        txtMeaning = findViewById(R.id.txtMeaningStudy)
        txtStatus = findViewById(R.id.txtStatus)
        txtCount = findViewById(R.id.tvCount)
        btnShow = findViewById(R.id.btnShowMeaning)
        btnEasy = findViewById(R.id.btnEasy)
        btnHard = findViewById(R.id.btnHard)
    }

    private fun loadFlashcards() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = db.flashcardDao().getCardsToReview(System.currentTimeMillis())
            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    showFinishedState()
                } else {
                    studyList.clear()
                    studyList.addAll(list)
                    showCurrentCard()
                }
            }
        }
    }

    private fun showCurrentCard() {
        if (currentIndex < studyList.size) {
            val card = studyList[currentIndex]
            txtWord.text = card.word
            txtMeaning.text = card.definition
            txtMeaning.visibility = View.INVISIBLE
            btnShow.visibility = View.VISIBLE
            txtCount.text = "Thẻ ${currentIndex + 1}/${studyList.size}"
        } else {
            showFinishedState()
        }
    }

    private fun processSM2(quality: Int) {
        if (studyList.isEmpty() || currentIndex >= studyList.size) return
        val card = studyList[currentIndex]

        if (quality >= 3) {
            card.interval = when (card.repetition) {
                0 -> 1
                1 -> 6
                else -> (card.interval * card.easeFactor).roundToInt()
            }
            card.repetition++
        } else {
            card.repetition = 0
            card.interval = 1
        }

        card.easeFactor += (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        if (card.easeFactor < 1.3) card.easeFactor = 1.3
        card.nextReview = System.currentTimeMillis() + (card.interval * 86400000L)

        CoroutineScope(Dispatchers.IO).launch {
            db.flashcardDao().update(card)
            withContext(Dispatchers.Main) {
                currentIndex++
                showCurrentCard()
            }
        }
    }

    private fun showFinishedState() {
        layoutContent.visibility = View.GONE
        txtStatus.visibility = View.VISIBLE
        txtStatus.text = "Tốt, bạn đã hoàn thành ôn tập!"
        txtCount.text = "Hoàn thành!"

        txtStatus.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }, 1500)
    }
}