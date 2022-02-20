package com.example.arimage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.views.ArtistLinkAdapter
import com.example.arimage.views.ArtistLinkViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment.OnSessionConfigurationListener
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.VideoNode
import java.io.IOException

// TODO create a view model that encapsulates data, video recorder and mediaplayer. Try to create a dedicated fragment class
class MainActivity : AppCompatActivity(), OnSessionConfigurationListener, FragmentOnAttachListener {
    private var isAdded = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var arFragment: ArFragment
    private lateinit var artistLinkAdapter: ArtistLinkAdapter

    private val videoView by lazy { findViewById<VideoView>(R.id.video_view) }
    private val arFragmentView by lazy { findViewById<FragmentContainerView>(R.id.arFragment) }
    private val shareButton by lazy { findViewById<ImageButton>(R.id.share_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeRecyclerViewAdapter()
        supportFragmentManager.addFragmentOnAttachListener(this)

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }


    }

    private fun initializeRecyclerViewAdapter() {
        artistLinkAdapter = ArtistLinkAdapter(LayoutInflater.from(this))
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    private fun setupAugmentedImagesDB(config: Config, session: Session?): Boolean {
        val bitmap: Bitmap = loadAugmentedImage() ?: return false
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("ticket", bitmap, 0.20f)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImage(): Bitmap? {
        try {
            assets.open("ticket.jpg").use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e("arcoreimage", "io exception", e)
        }
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        if (augmentedImage.trackingState == TrackingState.TRACKING
            && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            if (augmentedImage.name == "ticket" && isAdded.not()) {
                createArtistArView(arFragment, augmentedImage.createAnchor(augmentedImage.centerPose), augmentedImage)
                isAdded = true
            }
        }
    }

    private fun createArtistArView(arFragment: ArFragment, anchor: Anchor, image: AugmentedImage) {
        val anchorNode = AnchorNode(anchor)
        mediaPlayer = initializeMediaPlayer()

        mediaPlayer?.let { mediaPlayer ->
            val videoNode = VideoNode(
                this,
                mediaPlayer.apply { start() },
                null,
                null
            )
            videoNode.setOnTapListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if(mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                }
            }
            videoNode.parent = anchorNode
            videoNode.localRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)
            videoNode.localScale = getScale(image, mediaPlayer.videoHeight, mediaPlayer.videoWidth)
            videoNode.localPosition = Vector3(0f, 0.007f, image.extentZ/2)
        }
        arFragment.arSceneView.scene.addChild(anchorNode)

        ViewRenderable.builder()
            .setView(this, R.layout.artist_links)
            .build()
            .thenAccept {
                val recyclerView = it.view.findViewById<RecyclerView>(R.id.artist_links)
                recyclerView.apply {
                    adapter = artistLinkAdapter
                }

                it.isShadowCaster = false
                it.isShadowReceiver = false
                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = it
                node.parent = anchorNode
                node.localRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)
                node.localPosition = Vector3(0f, 0f, image.extentZ)
                //node.localScale = Vector3(0.05f,  0.01f, 1f)
                node.scaleController.minScale = 0.1f
                node.scaleController.maxScale = 0.2f
            }
            .exceptionally {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                null
            }

        artistLinkAdapter.submitList(
            listOf(
                ArtistLinkViewModel(
                    image = R.drawable.team_wang,
                    text = "Wang Merch",
                    onClick = { openWebView(it) },
                    webLink = "https://teamwangdesign.com/"
                ),
                ArtistLinkViewModel(
                    image = R.drawable.bird,
                    text = "Website",
                    onClick = {},
                    webLink = ""
                )
            )
        )
    }

    private fun openWebView(url: String) {
        val customTabsIntent = createCustomTabIntent()
        try {
            customTabsIntent.launchUrl(this, url.toUri())
        } catch (e: Exception) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getScale(image: AugmentedImage, videoHeight: Int, videoWidth: Int): Vector3 {
        val imageHeight = 0.143f
        val imageWidth = 0.20f

        val newVideoHeight = image.extentZ * 2
        val newVideoWidth = image.extentZ  * (videoWidth.toFloat()/videoHeight.toFloat())

        return Vector3(newVideoWidth, newVideoHeight, 1f)
    }

    private fun initializeMediaPlayer(): MediaPlayer =
        MediaPlayer.create(this, R.raw.jackson).also {
            it.isLooping = true
        }

    override fun onSessionConfiguration(session: Session, config: Config) {
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED

        if (setupAugmentedImagesDB(config, session)) Log.d("arcoreimg_db", "success") else Log.e(
            "arcoreimg_db",
            "faliure setting up db"
        )

        arFragment.instructionsController = null
        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate)
        initializeRecorder()
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment

            arFragment.setOnSessionConfigurationListener(this)
        }
    }

    private fun createCustomTabIntent(): CustomTabsIntent =
        CustomTabsIntent.Builder()
            .setExitAnimations(this, 0, 0)
            .setShowTitle(true)
            .build()

    private fun toggleRecording(videoRecorder: VideoRecorder, recordButton: FloatingActionButton) {
        val recording = videoRecorder.onToggleRecord()

        recording.onSuccess { isRecording ->
            if (isRecording) {
                recordButton.setImageResource(R.drawable.round_stop)
            } else {
                mediaPlayer?.pause()
                arFragmentView.isVisible = false
                videoView.isVisible = true
                shareButton.isVisible = true
                val uri = Uri.fromFile(videoRecorder.recordingFile)
                startVideo(uri)

                shareButton.setOnClickListener {
                    videoRecorder.recordingFile?.let { file ->
                        val contentUri = getUriForFile(this, FileProviderConstants.AUTHORITY, file)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                            type = "video/mp4"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    } ?: run {
                        Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                    }

                }

                recordButton.setImageResource(R.drawable.round_videocam)
//                videoRecorder.videoPath?.absolutePath?.let {
//                    Toast.makeText(this, "Video saved: $it", Toast.LENGTH_SHORT).show()
//
//                    // Send  notification of updated content.
//                    val values = ContentValues()
//                    values.put(MediaStore.Video.Media.TITLE, "Jackson Wang")
//                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//
//                    values.put(MediaStore.Video.Media.DATA, it)
//                    contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
//                }

            }
        }

        recording.onFailure {
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startVideo(uri: Uri) {
        videoView.isVisible = true
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
        videoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.isLooping = true }
    }

    private fun initializeRecorder() {
        val videoRecorder = VideoRecorder(
            arFragment.arSceneView,
            contentResolver,
            this
        )
        val recordButton = findViewById<FloatingActionButton>(R.id.record)
        recordButton.setOnClickListener {
            toggleRecording(videoRecorder, recordButton)
        }
    }
}