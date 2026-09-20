package com.example.secondplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondplayer.adapter.AudioAdapter
import com.example.secondplayer.model.AudioItem
import com.example.secondplayer.repository.MediaRepository
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MediaRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTrackCount: TextView
    private lateinit var tvNowPlayingTitle: TextView
    private lateinit var tvNowPlayingArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button
    private lateinit var btnPrevious: Button

    private var player: ExoPlayer? = null
    private var audioList: List<AudioItem> = emptyList()
    private var currentTrackIndex: Int = -1

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            player?.let {
                if (it.isPlaying) {
                    val currentPos = it.currentPosition
                    val duration = it.duration.coerceAtLeast(1)
                    seekBar.max = duration.toInt()
                    seekBar.progress = currentPos.toInt()

                    tvCurrentTime.text = formatTime(currentPos)
                    tvTotalDuration.text = formatTime(duration)

                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = MediaRepository(this)
        player = ExoPlayer.Builder(this).build()

        initViews()
        setupListeners()
        checkPermissionAndLoad()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvTrackCount = findViewById(R.id.tvTrackCount)
        tvNowPlayingTitle = findViewById(R.id.tvNowPlayingTitle)
        tvNowPlayingArtist = findViewById(R.id.tvNowPlayingArtist)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        seekBar = findViewById(R.id.seekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    btnPlayPause.text = "▶"
                } else {
                    it.play()
                    btnPlayPause.text = "⏸"
                    handler.post(updateProgressRunnable)
                }
            }
        }

        btnNext.setOnClickListener {
            if (audioList.isNotEmpty() && currentTrackIndex < audioList.size - 1) {
                playTrack(currentTrackIndex + 1)
            }
        }

        btnPrevious.setOnClickListener {
            if (audioList.isNotEmpty() && currentTrackIndex > 0) {
                playTrack(currentTrackIndex - 1)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                    tvCurrentTime.text = formatTime(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        } else {
            loadAudioFiles()
        }
    }

    private fun loadAudioFiles() {
        audioList = repository.fetchLocalAudioFiles()
        tvTrackCount.text = audioList.size.toString()

        if (audioList.isNotEmpty()) {
            val adapter = AudioAdapter(audioList) { selectedAudio ->
                val index = audioList.indexOf(selectedAudio)
                playTrack(index)
            }
            recyclerView.adapter = adapter
        } else {
            Toast.makeText(this, "لم يتم العثور على ملفات صوتية", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playTrack(index: Int) {
        if (index in audioList.indices) {
            currentTrackIndex = index
            val item = audioList[index]

            tvNowPlayingTitle.text = item.title
            tvNowPlayingArtist.text = item.artist

            player?.let {
                it.stop()
                val mediaItem = MediaItem.fromUri(item.uri)
                it.setMediaItem(mediaItem)
                it.prepare()
                it.play()
                btnPlayPause.text = "⏸"
                handler.post(updateProgressRunnable)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        player?.release()
        player = null
    }
}
