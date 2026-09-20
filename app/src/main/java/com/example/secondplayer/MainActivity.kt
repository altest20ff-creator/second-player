package com.example.secondplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondplayer.adapter.AudioAdapter
import com.example.secondplayer.adapter.FolderAdapter
import com.example.secondplayer.model.AudioItem
import com.example.secondplayer.model.FolderItem
import com.example.secondplayer.repository.MediaRepository

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MediaRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCurrentCategoryTitle: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvBottomTitle: TextView
    private lateinit var tvBottomArtist: TextView
    private lateinit var ivBottomArt: ImageView
    private lateinit var btnBottomPlayPause: ImageButton
    private lateinit var bottomPlayerCard: RelativeLayout

    private lateinit var btnCatTracks: Button
    private lateinit var btnCatFolders: Button
    private lateinit var btnCatFavs: Button

    private var player: ExoPlayer? = null
    private var allTracks: List<AudioItem> = emptyList()
    private var foldersList: List<FolderItem> = emptyList()
    private var currentPlayingTrack: AudioItem? = null

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
        tvCurrentCategoryTitle = findViewById(R.id.tvCurrentCategoryTitle)
        tvCount = findViewById(R.id.tvCount)
        tvBottomTitle = findViewById(R.id.tvBottomTitle)
        tvBottomArtist = findViewById(R.id.tvBottomArtist)
        ivBottomArt = findViewById(R.id.ivBottomArt)
        btnBottomPlayPause = findViewById(R.id.btnBottomPlayPause)
        bottomPlayerCard = findViewById(R.id.bottomPlayerCard)

        btnCatTracks = findViewById(R.id.btnCatTracks)
        btnCatFolders = findViewById(R.id.btnCatFolders)
        btnCatFavs = findViewById(R.id.btnCatFavs)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        // عند الضغط على الشريط السفلي يفتح الشاشة الكاملة للمشغل التفاعلي
        bottomPlayerCard.setOnClickListener {
            currentPlayingTrack?.let { track ->
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("TRACK", track)
                startActivity(intent)
            }
        }

        btnCatTracks.setOnClickListener {
            highlightTab(btnCatTracks)
            showTracksList(allTracks, "جميع المسارات الصوتية")
        }

        btnCatFolders.setOnClickListener {
            highlightTab(btnCatFolders)
            showFoldersList()
        }

        btnCatFavs.setOnClickListener {
            highlightTab(btnCatFavs)
            val favs = allTracks.filter { it.isFavorite }
            showTracksList(favs, "المسارات المفضلة")
        }
    }

    private fun highlightTab(selected: Button) {
        val accent = ThemeManager.getAccentColor(this)
        listOf(btnCatTracks, btnCatFolders, btnCatFavs).forEach {
            if (it == selected) {
                it.setTextColor(accent)
            } else {
                it.setTextColor(Color.WHITE)
            }
        }
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        } else {
            loadData()
        }
    }

    private fun loadData() {
        allTracks = repository.fetchAllAudio()
        foldersList = allTracks.groupBy { it.folderName }.map { (folder, tracks) ->
            FolderItem(folder, tracks.size, tracks)
        }
        showTracksList(allTracks, "جميع المسارات الصوتية")
    }

    private fun showTracksList(tracks: List<AudioItem>, title: String) {
        tvCurrentCategoryTitle.text = title
        tvCount.text = "${tracks.size} مسار"

        recyclerView.adapter = AudioAdapter(tracks) { item ->
            playTrack(item)
        }
    }

    private fun showFoldersList() {
        tvCurrentCategoryTitle.text = "المجلدات"
        tvCount.text = "${foldersList.size} مجلد"

        recyclerView.adapter = FolderAdapter(foldersList) { folder ->
            showTracksList(folder.tracks, "مجلد: ${folder.folderName}")
        }
    }

    private fun playTrack(item: AudioItem) {
        currentPlayingTrack = item
        tvBottomTitle.text = item.title
        tvBottomArtist.text = item.artist

        Glide.with(this)
            .load(item.albumUri)
            .placeholder(android.R.drawable.ic_media_play)
            .into(ivBottomArt)

        player?.let {
            it.stop()
            it.setMediaItem(MediaItem.fromUri(item.uri))
            it.prepare()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
