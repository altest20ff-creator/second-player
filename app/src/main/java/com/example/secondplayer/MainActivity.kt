package com.example.secondplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.secondplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlay.setOnClickListener {
            if (player?.isPlaying == true) {
                player?.pause()
                updateButton(false)
            } else {
                startPlayback()
            }
        }
    }

    private fun startPlayback() {
        val rawUrl = binding.urlInput.text?.toString()?.trim().orEmpty()
        if (rawUrl.isBlank()) {
            binding.urlInputLayout.error = "أدخل رابط البث أولاً"
            return
        }

        val uri = runCatching { Uri.parse(rawUrl) }.getOrNull()
        val scheme = uri?.scheme?.lowercase()
        if (uri == null || (scheme != "https" && scheme != "http")) {
            binding.urlInputLayout.error = "الرابط يجب أن يبدأ بـ http:// أو https://"
            return
        }

        binding.urlInputLayout.error = null
        releasePlayer()

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> binding.statusText.text = "جارٍ التحميل..."
                        Player.STATE_READY -> {
                            binding.statusText.text = "يعمل الآن"
                            updateButton(true)
                        }
                        Player.STATE_ENDED -> {
                            binding.statusText.text = "انتهى البث"
                            updateButton(false)
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    binding.statusText.text = "تعذر تشغيل البث"
                    updateButton(false)
                    Toast.makeText(
                        this@MainActivity,
                        "تحقق من الرابط أو صيغة البث",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun updateButton(isPlaying: Boolean) {
        binding.btnPlay.text = if (isPlaying) "إيقاف مؤقت" else "تشغيل الموسيقى"
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
        updateButton(false)
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
