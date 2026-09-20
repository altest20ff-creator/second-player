package com.example.secondplayer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    private lateinit var ivVinylArt: ImageView
    private lateinit var tvPlayerTitle: TextView
    private lateinit var tvPlayerArtist: TextView
    private lateinit var tvCurTime: TextView
    private lateinit var tvTotTime: TextView
    private lateinit var playerSeekBar: SeekBar
    private lateinit var fabPlayerPlay: FloatingActionButton
    private lateinit var btnPrevTrack: ImageButton
    private lateinit var btnNextTrack: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnThemePicker: ImageButton

    private var rotateAnimator: ObjectAnimator? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            val player = AudioPlayerManager.getPlayer(this@PlayerActivity)
            if (player.isPlaying) {
                val curPos = player.currentPosition
                val duration = player.duration.coerceAtLeast(1)
                playerSeekBar.max = duration.toInt()
                playerSeekBar.progress = curPos.toInt()

                tvCurTime.text = formatTime(curPos)
                tvTotTime.text = formatTime(duration)

                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initViews()
        setupVinylAnimation()
        applyTheme()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        ivVinylArt = findViewById(R.id.ivVinylArt)
        tvPlayerTitle = findViewById(R.id.tvPlayerTitle)
        tvPlayerArtist = findViewById(R.id.tvPlayerArtist)
        tvCurTime = findViewById(R.id.tvCurTime)
        tvTotTime = findViewById(R.id.tvTotTime)
        playerSeekBar = findViewById(R.id.playerSeekBar)
        fabPlayerPlay = findViewById(R.id.fabPlayerPlay)
        btnPrevTrack = findViewById(R.id.btnPrevTrack)
        btnNextTrack = findViewById(R.id.btnNextTrack)
        btnBack = findViewById(R.id.btnBack)
        btnThemePicker = findViewById(R.id.btnThemePicker)
    }

    private fun setupVinylAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(ivVinylArt, "rotation", 0f, 360f).apply {
            duration = 16000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        fabPlayerPlay.setOnClickListener {
            AudioPlayerManager.togglePlayPause(this)
            updateUI()
        }

        btnNextTrack.setOnClickListener {
            AudioPlayerManager.playNext(this)
            updateUI()
        }

        btnPrevTrack.setOnClickListener {
            AudioPlayerManager.playPrevious(this)
            updateUI()
        }

        btnThemePicker.setOnClickListener {
            val colors = arrayOf("الذهبي الساطع", "الأرجواني الفاخر", "الأخضر النيون", "الأزرق السماوي", "الوردي المتوهج")
            AlertDialog.Builder(this)
                .setTitle("اختر لون الثيم")
                .setItems(colors) { _, which ->
                    ThemeManager.setAccentColor(this, ThemeManager.THEME_COLORS[which])
                    applyTheme()
                }
                .show()
        }

        playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    AudioPlayerManager.getPlayer(this@PlayerActivity).seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun updateUI() {
        val track = AudioPlayerManager.getCurrentTrack() ?: return
        tvPlayerTitle.text = track.title
        tvPlayerArtist.text = track.artist

        Glide.with(this)
            .load(track.albumArtUriStr)
            .placeholder(android.R.drawable.ic_media_play)
            .into(ivVinylArt)

        if (AudioPlayerManager.isPlaying()) {
            fabPlayerPlay.setImageResource(android.R.drawable.ic_media_pause)
            if (rotateAnimator?.isStarted != true) rotateAnimator?.start()
            handler.post(updateProgressRunnable)
        } else {
            fabPlayerPlay.setImageResource(android.R.drawable.ic_media_play)
            rotateAnimator?.pause()
        }
    }

    private fun applyTheme() {
        val accent = ThemeManager.getAccentColor(this)
        fabPlayerPlay.backgroundTintList = ColorStateList.valueOf(accent)
        playerSeekBar.progressTintList = ColorStateList.valueOf(accent)
        playerSeekBar.thumbTintList = ColorStateList.valueOf(accent)
    }

    private fun formatTime(millis: Long): String {
        val min = TimeUnit.MILLISECONDS.toMinutes(millis)
        val sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        rotateAnimator?.cancel()
    }
}
