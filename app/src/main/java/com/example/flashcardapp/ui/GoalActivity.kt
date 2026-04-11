package com.example.flashcardapp.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R
import java.text.SimpleDateFormat
import java.util.*

class GoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        // Ánh xạ view
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnHome = findViewById<Button>(R.id.btnHome)
        val edtGoal = findViewById<EditText>(R.id.edtGoal)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val tvGoalToday = findViewById<TextView>(R.id.tvGoalToday)
        //val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val tvPercent = findViewById<TextView>(R.id.tvPercent)
        val tvDone = findViewById<TextView>(R.id.tvDone)

        val sharedPref = getSharedPreferences("GoalData", MODE_PRIVATE)

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val savedDate = sharedPref.getString("date", "")

        // 👉 Lấy danh sách từ đã học (không trùng)
        val learnedSet = if (today == savedDate) {
            sharedPref.getStringSet("learned_words", setOf()) ?: setOf()
        } else {
            setOf()
        }

        val learnedCount = learnedSet.size

        // 👉 Lấy mục tiêu
        val goalText = sharedPref.getString("goal_text", "")
        val goalNumber = goalText?.toIntOrNull() ?: 0

        // 👉 Tính %
        val percent = if (goalNumber > 0) {
            (learnedCount * 100) / goalNumber
        } else 0

        // 👉 Hiển thị
        tvGoalToday.text = "Mục tiêu hôm nay: $goalNumber từ"
        tvPercent.text = "$percent%"
        tvDone.text = "Đã học $learnedCount/$goalNumber từ"
        //seekBar.progress = percent

        // 🔙 Back
        btnBack.setOnClickListener {
            finish()
        }

        // 🏠 Home
        btnHome.setOnClickListener {
            finish()
        }

        // ❌ KHÔNG cho kéo tay nữa (chỉ hiển thị)
       // seekBar.isEnabled = false
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.progress = percent
        // 💾 Lưu mục tiêu
        btnSave.setOnClickListener {
            val goalInput = edtGoal.text.toString().trim()
            val goalNumberInput = goalInput.toIntOrNull()

            if (goalNumberInput == null || goalNumberInput <= 0) {
                Toast.makeText(this, "Nhập số hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPref.edit()
                .putString("goal_text", goalNumberInput.toString())
                .apply()

            tvGoalToday.text = "Mục tiêu hôm nay: $goalNumberInput từ"

            Toast.makeText(this, "Đã lưu mục tiêu!", Toast.LENGTH_SHORT).show()
        }
    }
}