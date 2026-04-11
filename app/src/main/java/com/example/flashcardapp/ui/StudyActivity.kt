package com.example.flashcardapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R
import com.example.flashcardapp.data.AppDatabase
import com.example.flashcardapp.data.Flashcard
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

class StudyActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var studyList = mutableListOf<Flashcard>()
    private var currentIndex = 0

    private lateinit var frontCard: View
    private lateinit var backCard: View
    private lateinit var txtVietnamese: TextView
    private lateinit var txtEnglish: TextView
    private lateinit var cardContainer: View
    private var isFront = true

    private lateinit var layoutContent: LinearLayout
    private lateinit var txtStatus: TextView
    private lateinit var txtCount: TextView
    private lateinit var btnEasy: Button
    private lateinit var btnHard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        db = AppDatabase.getDatabase(this)
        initViews()
        loadFlashcards()

        fun pressAnim(view: View) {
            view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()
        }

        btnEasy.setOnClickListener {
            pressAnim(btnEasy)
            saveLearnedWord(studyList[currentIndex].word) // 🔥 FIX CHÍNH
            processSM2(5)
        }

        btnHard.setOnClickListener {
            pressAnim(btnHard)
            saveLearnedWord(studyList[currentIndex].word) // 🔥 FIX CHÍNH
            processSM2(2)
        }

        val scale = resources.displayMetrics.density
        cardContainer.cameraDistance = 8000 * scale

        cardContainer.setOnClickListener {
            if (isFront) {
                frontCard.animate().rotationY(90f).setDuration(200)
                    .withEndAction {
                        frontCard.visibility = View.GONE
                        backCard.visibility = View.VISIBLE
                        backCard.rotationY = -90f
                        backCard.animate().rotationY(0f).setDuration(200).start()
                    }.start()
            } else {
                backCard.animate().rotationY(90f).setDuration(200)
                    .withEndAction {
                        backCard.visibility = View.GONE
                        frontCard.visibility = View.VISIBLE
                        frontCard.rotationY = -90f
                        frontCard.animate().rotationY(0f).setDuration(200).start()
                    }.start()
            }
            isFront = !isFront
        }

        findViewById<Button>(R.id.btnGoHomeStudy)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // 🔥 HÀM QUAN TRỌNG NHẤT (KHÔNG TRÙNG + RESET NGÀY)
    private fun saveLearnedWord(word: String) {
        val sharedPref = getSharedPreferences("GoalData", MODE_PRIVATE)

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val savedDate = sharedPref.getString("date", "")

        val set = if (today == savedDate) {
            sharedPref.getStringSet("learned_words", mutableSetOf())?.toMutableSet()
                ?: mutableSetOf()
        } else {
            mutableSetOf() // 🔥 ngày mới → reset
        }

        set.add(word) // 🔥 không trùng

        sharedPref.edit()
            .putString("date", today)
            .putStringSet("learned_words", set)
            .apply()
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

            txtVietnamese.text = card.word
            txtEnglish.text = card.definition

            frontCard.visibility = View.VISIBLE
            backCard.visibility = View.GONE

            frontCard.rotationY = 0f
            backCard.rotationY = 0f

            isFront = true

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
        layoutContent.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                layoutContent.visibility = View.GONE
                txtStatus.visibility = View.VISIBLE
                txtStatus.alpha = 0f
                txtStatus.animate().alpha(1f).setDuration(500).start()
            }.start()
    }

    private fun initViews() {
        cardContainer = findViewById(R.id.cardContainer)
        frontCard = findViewById(R.id.frontCard)
        backCard = findViewById(R.id.backCard)
        txtVietnamese = findViewById(R.id.txtVietnamese)
        txtEnglish = findViewById(R.id.txtEnglish)

        layoutContent = findViewById(R.id.layoutContent)
        txtStatus = findViewById(R.id.txtStatus)
        txtCount = findViewById(R.id.tvCount)

        btnEasy = findViewById(R.id.btnEasy)
        btnHard = findViewById(R.id.btnHard)
    }
}