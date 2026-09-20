package com.example.secondplayer

import android.app.*
import android.content.*
import android.content.ContentUris
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.media3.common.*
import androidx.media3.session.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var controller: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private lateinit var root: LinearLayout
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var status: TextView
    private lateinit var seek: SeekBar
    private lateinit var query: EditText
    private lateinit var url: EditText
    private lateinit var list: LinearLayout
    private lateinit var play: MaterialButton
    private val tracks = mutableListOf<MediaItem>()
    private val visible = mutableListOf<MediaItem>()
    private val favorites = linkedSetOf<String>()
    private val playlists = linkedMapOf<String, MutableList<MediaItem>>()
    private val prefs by lazy { getSharedPreferences("library", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            addTrack(MediaItem.Builder().setUri(it).setMediaMetadata(MediaMetadata.Builder()
                .setTitle(it.lastPathSegment ?: "موسيقى محلية").setArtist("ملف من الجهاز").build()).build())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favorites.addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet())
        buildUi()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 91)
        } else if (Build.VERSION.SDK_INT < 33 && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 91)
        }
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture!!.addListener({
            runCatching { controller = controllerFuture!!.get(); connectPlayer() }
        }, ContextCompat.getMainExecutor(this))
        discoverMusic()
        handler.post(updateProgress)
    }
    private fun buildUi() {
        root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,24,22,18); setBackgroundColor(0xFF0D1529.toInt()) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL }
        scroll.addView(content)
        setContentView(scroll)
        fun text(s:String,size:Float,color:Int=0xFFF4F5FA.toInt(),bold:Boolean=false)=TextView(this).apply { text=s; textSize=size; setTextColor(color); if(bold)setTypeface(null,1); gravity=Gravity.CENTER }
        content.addView(text("SECOND PLAYER",20f,0xFF8B5CF6.toInt(),true))
        val card=MaterialCardView(this).apply { radius=36f; cardBackgroundColor=0xFF1D2940.toInt(); strokeWidth=0; layoutParams=LinearLayout.LayoutParams(-1,220).apply{setMargins(0,24,0,18)} }
        card.addView(text("♫",92f,0xFF8B5CF6.toInt(),true)); content.addView(card)
        title=text("اختر أغنية لتبدأ",24f,bold=true); content.addView(title)
        artist=text("Second Player • مشغل الموسيقى",16f,0xFF9BA9C2.toInt()); content.addView(artist)
        status=text("جاهز للتشغيل",14f,0xFF9BA9C2.toInt()); content.addView(status)
        seek=SeekBar(this); content.addView(seek,LinearLayout.LayoutParams(-1,-2))
        seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean) {}
            override fun onStartTrackingTouch(s:SeekBar?) {}
            override fun onStopTrackingTouch(s:SeekBar?) { controller?.seekTo(seek.progress.toLong()) }
        })
        val controls=LinearLayout(this).apply{gravity=Gravity.CENTER}
        fun button(label:String, action:()->Unit):MaterialButton {
            val b=MaterialButton(this).apply { text=label; cornerRadius=48; setBackgroundColor(0xFF8B5CF6.toInt()); setTextColor(-1) }
            b.setOnClickListener{action()}; controls.addView(b,LinearLayout.LayoutParams(0,54,1f).apply{setMargins(3,4,3,4)}); return b
        }
        button("السابق"){controller?.seekToPreviousMediaItem()}
        play=button("▶"){ if(controller?.isPlaying==true) controller?.pause() else controller?.play() }
        button("التالي"){controller?.seekToNextMediaItem()}
        content.addView(controls)
        val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER}
        fun action(label:String, fn:()->Unit) { val b=Button(this).apply{text=label;setTextColor(-1);setBackgroundColor(0xFF25324A.toInt())}; b.setOnClickListener{fn()}; actions.addView(b,LinearLayout.LayoutParams(0,52,1f).apply{setMargins(2,8,2,8)}) }
        action("＋ ملفات"){ picker.launch(arrayOf("audio/*")) }
        action("❤️ مفضلة"){ showFavorites() }
        action("⏱ نوم"){ sleepDialog() }
        content.addView(actions)
        query=EditText(this).apply{hint="ابحث في مكتبتك";setHintTextColor(0xFF9BA9C2.toInt());setTextColor(-1);setSingleLine(true)}
        content.addView(query,LinearLayout.LayoutParams(-1,-2))
        query.addTextChangedListener(object:android.text.TextWatcher{
            override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){}
            override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){ render(tracks.filter { metadata(it).contains(s.toString(),true) }) }
            override fun afterTextChanged(s:android.text.Editable?){}
        })
        val online=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        url=EditText(this).apply{hint="رابط صوتي HTTP/HTTPS";setHintTextColor(0xFF9BA9C2.toInt());setTextColor(-1);setSingleLine(true)}
        online.addView(url,LinearLayout.LayoutParams(0,-2,1f))
        val stream=Button(this).apply{text="بث";setTextColor(-1);setBackgroundColor(0xFF8B5CF6.toInt())}
        stream.setOnClickListener {
            val raw=url.text.toString().trim(); val u=runCatching{Uri.parse(raw)}.getOrNull()
            if(u==null || u.scheme !in listOf("http","https")) { status.text="أدخل رابط HTTP أو HTTPS صحيحًا"; return@setOnClickListener }
            val item=MediaItem.Builder().setUri(u).setMediaMetadata(MediaMetadata.Builder().setTitle(u.lastPathSegment ?: "بث مباشر").setArtist("بث عبر الإنترنت").build()).build()
            addTrack(item); playItem(item)
        }
        online.addView(stream); content.addView(online)
        val section=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        section.addView(text("مكتبة الموسيقى",20f,bold=true),LinearLayout.LayoutParams(0,-2,1f))
        val playlist=Button(this).apply{text="＋ قائمة تشغيل";setTextColor(-1);setBackgroundColor(0xFF25324A.toInt())}
        playlist.setOnClickListener{createPlaylistDialog()}; section.addView(playlist); content.addView(section)
        list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; content.addView(list)
    }
    private fun discoverMusic() {
        val collection=if(Build.VERSION.SDK_INT>=29) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        runCatching {
            contentResolver.query(collection,arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST),
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",null,"${MediaStore.Audio.Media.TITLE} ASC")?.use { c ->
                val id=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID); val t=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE); val a=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                while(c.moveToNext()) {
                    val uri=ContentUris.withAppendedId(collection,c.getLong(id))
                    tracks.add(MediaItem.Builder().setUri(uri).setMediaMetadata(MediaMetadata.Builder().setTitle(c.getString(t) ?: "بدون عنوان").setArtist(c.getString(a) ?: "فنان غير معروف").build()).build())
                }
            }
        }
        render(tracks)
    }
    private fun addTrack(item:MediaItem){ if(tracks.none{it.localConfiguration?.uri==item.localConfiguration?.uri}) tracks.add(0,item); render(tracks) }
    private fun metadata(item:MediaItem)=listOfNotNull(item.mediaMetadata.title,item.mediaMetadata.artist).joinToString(" ")
    private fun render(items:List<MediaItem>) {
        if(!::list.isInitialized)return
        list.removeAllViews(); visible.clear(); visible.addAll(items)
        if(items.isEmpty()){ list.addView(TextView(this).apply{text="لا توجد نتائج. أضف ملفات موسيقى من جهازك.";setTextColor(0xFF9BA9C2.toInt());setPadding(8,18,8,18)});return }
        items.forEach { item ->
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(10,10,10,10);setBackgroundColor(0xFF1D2940.toInt())}
            val info=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
            info.addView(TextView(this).apply{text=item.mediaMetadata.title?.toString() ?: "موسيقى";textSize=16f;setTextColor(-1)})
            info.addView(TextView(this).apply{text=item.mediaMetadata.artist?.toString() ?: "ملف صوتي";setTextColor(0xFF9BA9C2.toInt())})
            row.addView(info,LinearLayout.LayoutParams(0,-2,1f))
            val fav=Button(this).apply{text=if(favorites.contains(item.mediaId.ifEmpty{item.localConfiguration?.uri.toString()}))"♥" else "♡";setTextColor(0xFFCB4B75.toInt())}
            fav.setOnClickListener { val id=item.localConfiguration?.uri.toString(); if(!favorites.add(id))favorites.remove(id);prefs.edit().putStringSet("favorites",favorites).apply(); render(visible.toList()) }
            row.addView(fav)
            row.setOnClickListener{playItem(item)}
            list.addView(row,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,4,0,4)})
        }
    }
    private fun showFavorites()=render(tracks.filter{favorites.contains(it.localConfiguration?.uri.toString())})
    private fun playItem(item:MediaItem) {
        val c=controller ?: run { status.text="جارٍ تجهيز المشغل..."; return }
        val idx=tracks.indexOf(item).coerceAtLeast(0)
        c.setMediaItems(tracks,idx,0); c.prepare(); c.play()
        status.text="جارٍ التشغيل"
    }
    private fun connectPlayer() {
        controller?.addListener(object:Player.Listener{
            override fun onMediaItemTransition(item:MediaItem?,reason:Int){ title.text=item?.mediaMetadata?.title?.toString() ?: "لا توجد أغنية"; artist.text=item?.mediaMetadata?.artist?.toString() ?: "Second Player"; }
            override fun onIsPlayingChanged(isPlaying:Boolean){play.text=if(isPlaying)"Ⅱ" else "▶";status.text=if(isPlaying)"يعمل الآن" else "متوقف مؤقتًا"}
            override fun onPlaybackStateChanged(state:Int){ if(state==Player.STATE_BUFFERING)status.text="جارٍ التحميل…"; if(state==Player.STATE_ENDED)status.text="انتهت الأغنية" }
        })
    }
    private val updateProgress=object:Runnable{
        override fun run(){controller?.let{ if(it.duration>0 && it.duration!=C.TIME_UNSET){seek.max=it.duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt();seek.progress=it.currentPosition.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()} };handler.postDelayed(this,500)}
    }
    private fun sleepDialog() {
        val choices=arrayOf("10 دقائق","20 دقيقة","30 دقيقة","60 دقيقة","عند انتهاء الأغنية","إلغاء المؤقت")
        AlertDialog.Builder(this).setTitle("مؤقت النوم").setItems(choices){_,which->
            if(which==5){handler.removeCallbacks(sleepStop);status.text="تم إلغاء مؤقت النوم"}
            else if(which==4){controller?.addListener(object:Player.Listener{override fun onPlaybackStateChanged(state:Int){if(state==Player.STATE_ENDED)controller?.pause()}});status.text="سيتوقف عند نهاية الأغنية"}
            else {handler.removeCallbacks(sleepStop);handler.postDelayed(sleepStop,TimeUnit.MINUTES.toMillis(listOf(10,20,30,60)[which].toLong()));status.text="تم ضبط مؤقت النوم"}
        }.show()
    }
    private val sleepStop=Runnable{controller?.pause();status.text="انتهى مؤقت النوم"}
    private fun createPlaylistDialog(){
        val input=EditText(this).apply{hint="اسم قائمة التشغيل"}
        AlertDialog.Builder(this).setTitle("إنشاء قائمة تشغيل").setView(input).setPositiveButton("إنشاء"){_,_->val name=input.text.toString().trim();if(name.isNotEmpty()){playlists.putIfAbsent(name, mutableListOf());status.text="تم إنشاء القائمة: $name"}}.setNegativeButton("إلغاء",null).show()
    }
    override fun onDestroy(){handler.removeCallbacks(updateProgress);controllerFuture?.let{MediaController.releaseFuture(it)};controller=null;super.onDestroy()}
}
