package com.example.secondplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondplayer.adapter.AudioTrackAdapter
import com.example.secondplayer.adapter.FolderAdapter
import com.example.secondplayer.model.AudioTrack
import com.example.secondplayer.model.FolderModel
import com.example.secondplayer.repository.MediaRepository

class MainActivity : AppCompatActivity() {

    private lateinit var repository: MediaRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvSectionName: TextView
    private lateinit var tvTrackCount: TextView
    private lateinit var tvBottomTitle: TextView
    private lateinit var tvBottomArtist: TextView
    private lateinit var ivBottomArt: ImageView
    private lateinit var btnBottomPlay: ImageButton
    private lateinit var bottomBar: RelativeLayout

    private lateinit var btnCategoryTracks: Button
    private lateinit var btnCategoryFolders: Button
    private lateinit var btnCategoryFavorites: Button

    private var allTracks: List<AudioTrack> = emptyList()
    private var folderList: List<FolderModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = MediaRepository(this)

        initViews()
        setupListeners()
        checkPermissionAndLoad()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvSectionName = findViewById(R.id.tvSectionName)
        tvTrackCount = findViewById(R.id.tvTrackCount)
        tvBottomTitle = findViewById(R.id.tvBottomTitle)
        tvBottomArtist = findViewById(R.id.tvBottomArtist)
        ivBottomArt = findViewById(R.id.ivBottomArt)
        btnBottomPlay = findViewById(R.id.btnBottomPlay)
        bottomBar = findViewById(R.id.bottomBar)

        btnCategoryTracks = findViewById(R.id.btnCategoryTracks)
        btnCategoryFolders = findViewById(R.id.btnCategoryFolders)
        btnCategoryFavorites = findViewById(R.id.btnCategoryFavorites)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        bottomBar.setOnClickListener {
            if (AudioPlayerManager.getCurrentTrack() != null) {
                startActivity(Intent(this, PlayerActivity::class.java))
            }
        }

        btnBottomPlay.setOnClickListener {
            AudioPlayerManager.togglePlayPause(this)
            updateBottomBarUI()
        }

        btnCategoryTracks.setOnClickListener {
            showTracks(allTracks, "جميع المسارات الصوتية")
        }

        btnCategoryFolders.setOnClickListener {
            showFolders()
        }

        btnCategoryFavorites.setOnClickListener {
            val favs = allTracks.filter { it.isFavorite }
            showTracks(favs, "المسارات المفضلة")
        }
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 200)
        } else {
            loadData()
        }
    }

    private fun loadData() {
        allTracks = repository.loadAllTracks()
        folderList = allTracks.groupBy { it.folderName }.map { (folder, list) ->
            FolderModel(folder, list.size, list)
        }
        showTracks(allTracks, "جميع المسارات الصوتية")
    }

    private fun showTracks(list: List<AudioTrack>, title: String) {
        tvSectionName.text = title
        tvTrackCount.text = "${list.size} مسار"

        recyclerView.adapter = AudioTrackAdapter(list) { index ->
            AudioPlayerManager.playTrackAt(this, list, index)
            updateBottomBarUI()
        }
    }

    private fun showFolders() {
        tvSectionName.text = "المجلدات"
        tvTrackCount.text = "${folderList.size} مجلد"

        recyclerView.adapter = FolderAdapter(folderList) { folder ->
            showTracks(folder.tracks, "مجلد: ${folder.folderName}")
        }
    }

    private fun updateBottomBarUI() {
        val track = AudioPlayerManager.getCurrentTrack() ?: return
        tvBottomTitle.text = track.title
        tvBottomArtist.text = track.artist

        Glide.with(this)
            .load(track.albumArtUriStr)
            .placeholder(android.R.drawable.ic_media_play)
            .into(ivBottomArt)

        if (AudioPlayerManager.isPlaying()) {
            btnBottomPlay.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            btnBottomPlay.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    override fun onResume() {
        super.onResume()
        updateBottomBarUI()
    }
}
