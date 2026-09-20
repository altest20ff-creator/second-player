package com.example.secondplayer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.secondplayer.model.AudioItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var ivDiscAlbumArt: ImageView
    private lateinit var tvFullTitle: TextView
    private lateinit var tvFullArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var fullSeekBar: SeekBar
    private lateinit var fabPlayPause: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnThemePicker: ImageButton
    private lateinit var btnFavorite: ImageButton

    private var rotateAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initViews()
        setupDiscAnimation()
        applyAccentTheme()

        val track = intent.getSerializableExtra("TRACK") as? AudioItem
        track?.let {
            tvFullTitle.text = it.title
            tvFullArtist.text = it.artist

            val minutes = TimeUnit.MILLISECONDS.toMinutes(it.duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(it.duration) % 60
            tvTotalTime.text = String.format("%02d:%02d", minutes, seconds)

            Glide.with(this)
                .load(it.albumUri)
                .placeholder(android.R.drawable.ic_media_play)
                .error(android.R.drawable.ic_media_play)
                .into(ivDiscAlbumArt)
        }

        btnBack.setOnClickListener { finish() }

        btnThemePicker.setOnClickListener {
            showThemeDialog()
        }
    }

    private fun initViews() {
        ivDiscAlbumArt = findViewById(R.id.ivDiscAlbumArt)
        tvFullTitle = findViewById(R.id.tvFullTitle)
        tvFullArtist = findViewById(R.id.tvFullArtist)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        fullSeekBar = findViewById(R.id.fullSeekBar)
        fabPlayPause = findViewById(R.id.fabPlayPause)
        btnBack = findViewById(R.id.btnBack)
        btnThemePicker = findViewById(R.id.btnThemePicker)
        btnFavorite = findViewById(R.id.btnFavorite)
    }

    private fun setupDiscAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(ivDiscAlbumArt, "rotation", 0f, 360f).apply {
            duration = 15000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun applyAccentTheme() {
        val accentColor = ThemeManager.getAccentColor(this)
        fabPlayPause.backgroundTintList = ColorStateList.valueOf(accentColor)
        fullSeekBar.progressTintList = ColorStateList.valueOf(accentColor)
        fullSeekBar.thumbTintList = ColorStateList.valueOf(accentColor)
        findViewById<TextView>(R.id.tvLyricsPrompt).setTextColor(accentColor)
    }

    private fun showThemeDialog() {
        val colors = arrayOf("الذهبي الأصلي", "الأرجواني الفاخر", "الأخضر النيون", "الأزرق السماوي", "الأحمر العاطفي")
        AlertDialog.Builder(this)
            .setTitle("اختر ثيم اللون للمشغل")
            .setItems(colors) { _, which ->
                val selectedHex = ThemeManager.THEME_COLORS[which]
                ThemeManager.setAccentColor(this, selectedHex)
                applyAccentTheme()
            }
            .show()
    }
}
