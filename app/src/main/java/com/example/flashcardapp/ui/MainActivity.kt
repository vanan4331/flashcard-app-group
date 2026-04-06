package com.example.flashcardapp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Khởi tạo Kênh thông báo (Bắt buộc cho Android 8.0 trở lên)
        createNotificationChannel()

        // 2. Kết nối nút "Tra từ"
        findViewById<Button>(R.id.btnGoToSearch).setOnClickListener {
            val intent = Intent(this, VocabularyActivity::class.java)
            startActivity(intent)
        }

        // 3. Kết nối nút "Ôn tập"
        findViewById<Button>(R.id.btnGoToStudy).setOnClickListener {
            val intent = Intent(this, StudyActivity::class.java)
            startActivity(intent)
        }

        // 4. Kết nối nút "Nhắc nhở" (Tính năng mới thêm)
        findViewById<Button>(R.id.btnGoToReminder).setOnClickListener {
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Flashcard Reminder"
            val descriptionText = "Thông báo nhắc nhở học từ vựng hàng ngày"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("study_id", name, importance).apply {
                description = descriptionText
            }
            // Đăng ký kênh với hệ thống
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}