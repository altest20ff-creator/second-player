package com.example.secondplayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MediaRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNowPlaying: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var etStreamUrl: EditText
    private lateinit var btnPlayStream: Button

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = MediaRepository(this)
        player = ExoPlayer.Builder(this).build()

        recyclerView = findViewById(R.id.recyclerView)
        tvNowPlaying = findViewById(R.id.tvNowPlaying)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        etStreamUrl = findViewById(R.id.etStreamUrl)
        btnPlayStream = findViewById(R.id.btnPlayStream)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        btnPlayStream.setOnClickListener {
            val url = etStreamUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                playAudioUri(Uri.parse(url), "بث مباشر: $url")
            } else {
                Toast.makeText(this, "يرجى أدخال رابط صحيح", Toast.LENGTH_SHORT).show()
            }
        }

        checkPermissionAndLoad()
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
        val audioList = repository.fetchLocalAudioFiles()
        if (audioList.isEmpty()) {
            Toast.makeText(this, "لم يتم العثور على ملفات صوتية", Toast.LENGTH_SHORT).show()
        } else {
            val adapter = AudioAdapter(audioList) { selectedAudio ->
                playAudioUri(selectedAudio.uri, selectedAudio.title)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun playAudioUri(uri: Uri, title: String) {
        player?.let {
            it.stop()
            val mediaItem = MediaItem.fromUri(uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
            tvNowPlaying.text = "جاري التشغيل: $title"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAudioFiles()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
