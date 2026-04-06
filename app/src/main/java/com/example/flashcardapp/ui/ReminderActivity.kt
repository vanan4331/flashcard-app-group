package com.example.flashcardapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.flashcardapp.R
import com.example.flashcardapp.data.AppDatabase
import com.example.flashcardapp.data.ScheduleEntity
import com.example.flashcardapp.worker.ReminderWorker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderActivity : AppCompatActivity() {

    private var selectedCal = Calendar.getInstance()
    private lateinit var tvHistory: TextView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        db = AppDatabase.getDatabase(this)

        tvHistory = findViewById(R.id.tvHistory)
        val tvInfoDate = findViewById<TextView>(R.id.tvSelectedDate)
        val tvInfoTime = findViewById<TextView>(R.id.tvSelectedTime)

        // 🔥 Load lịch lần đầu
        loadSchedule()

        // Nút quay về
        findViewById<ImageButton>(R.id.btnBackHomeReminder).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnGoHomeReminder).setOnClickListener {
            finish()
        }

        // Chọn ngày
        findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                selectedCal.set(y, m, d)
                tvInfoDate.text = "Ngày: $d/${m + 1}/$y"
            }, 2026, 3, 5).show()
        }

        // Chọn giờ
        findViewById<Button>(R.id.btnPickTime).setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                selectedCal.set(Calendar.HOUR_OF_DAY, h)
                selectedCal.set(Calendar.MINUTE, m)
                tvInfoTime.text = "Giờ: $h:$m"
            }, 12, 0, true).show()
        }

        // 🔥 Lưu nhắc nhở
        findViewById<Button>(R.id.btnSaveReminder).setOnClickListener {

            val delay = selectedCal.timeInMillis - System.currentTimeMillis()

            if (delay > 0) {

                // 1. WorkManager
                val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(this).enqueue(request)

                // 2. Lưu snapshot từ vựng
                CoroutineScope(Dispatchers.IO).launch {

                    val wordsList = db.flashcardDao()
                        .getCardsToReview(System.currentTimeMillis())

                    val wordsText = if (wordsList.isEmpty()) {
                        "Không có từ"
                    } else {
                        wordsList.joinToString(", ") { it.word }
                    }

                    db.scheduleDao().insert(
                        ScheduleEntity(
                            time = selectedCal.timeInMillis,
                            words = wordsText
                        )
                    )

                    // 🔥 Load lại lịch sau khi lưu
                    withContext(Dispatchers.Main) {
                        loadSchedule()
                    }
                }

                Toast.makeText(this, "Đã hẹn giờ học!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Vui lòng chọn thời gian ở tương lai!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 HÀM LOAD LỊCH
    private fun loadSchedule() {
        CoroutineScope(Dispatchers.IO).launch {

            val list = db.scheduleDao().getAll()

            val text = if (list.isEmpty()) {
                "Chưa có lịch học"
            } else {
                list.joinToString("\n\n") {
                    val time = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        .format(Date(it.time))

                    "🕒 $time\n📚 Từ: ${it.words}"
                }
            }

            withContext(Dispatchers.Main) {
                tvHistory.text = text
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSchedule()
    }
}