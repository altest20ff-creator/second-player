package com.example.secondplayer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvTrackCount: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTrackCount = findViewById(R.id.tvTrackCount)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)

        tvTrackCount.text = "0 / 0"
        tvCurrentTime.text = "00:00"
        tvTotalDuration.text = "00:00"

        btnNext.setOnClickListener {
            // كود التشغيل التالي
        }

        btnPrevious.setOnClickListener {
            // كود التشغيل السابق
        }
    }
}
