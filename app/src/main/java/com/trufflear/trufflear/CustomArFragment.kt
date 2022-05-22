package com.trufflear.trufflear

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bugsnag.android.Bugsnag
import com.trufflear.trufflear.models.ArtistLinkModel
import com.trufflear.trufflear.viewmodels.ArtistLinkViewModel
import com.trufflear.trufflear.views.ArtistLinkAdapter
import com.trufflear.trufflear.views.OnRecordListener
import com.trufflear.trufflear.views.indicators.CircularProgressIndicator
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.VideoNode
import com.google.firebase.analytics.FirebaseAnalytics
import com.trufflear.trufflear.views.RecordButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class CustomArFragment: Fragment(),
    FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {

    private val TAG = CustomArFragment::class.java.simpleName

    private val viewModel by viewModels<ArViewModel>()

    private var isModelAdded = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var arFragment: ArFragment
    @Inject lateinit var artistLinkAdapter: ArtistLinkAdapter

    private lateinit var progressIndicator: CircularProgressIndicator

    // Recording
    private lateinit var recordButton: RecordButton
    private lateinit var tutorialTextPresenter: TutorialTextPresenter

    private var videoRecorder: VideoRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arFragment = ArFragment()
        childFragmentManager.addFragmentOnAttachListener(this)
        childFragmentManager.beginTransaction().apply {
            add(R.id.arFragment, ArFragment())
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAnalytics.getInstance(requireContext()).logEvent("home_screen_viewed", null)
        deleteRecursive(File(requireActivity().filesDir, FileProviderConstants.FILE_NAME))

        tutorialTextPresenter.showDefaultTutorialMessages()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_ar_fragment, container, false)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        recordButton = view.findViewById(R.id.record)
        tutorialTextPresenter = TutorialTextPresenter(
            tutorialTextView = view.findViewById(R.id.tutorial_text),
            countDownTimer = DefaultCountDownTimer(),
            resources = resources
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.arFragmentConfig.collect {
                        it.config?.let { config ->
                            arFragment.setSessionConfig(config, true)
                        }
                    }
                }
                launch {
                    viewModel.isInitialLoading.collect {
                        progressIndicator.visibility = if (it) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                    }
                }
                launch {
                    viewModel.artistLinks.collect { list ->
                        artistLinkAdapter.submitList(list.map { it.toViewModel() })
                    }
                }
            }
        }
    }

    override fun onPause() {
        mediaPlayer?.pause()
        if (videoRecorder?.isRecording == true) {
            videoRecorder?.onToggleRecord()
        }
        super.onPause()
    }

    private fun ArtistLinkModel.toViewModel(): ArtistLinkViewModel =
        ArtistLinkViewModel(
            image = image,
            text = text,
            webLink = webLink,
            onClick = ::openWebView
        )


    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment
            arFragment.setOnSessionConfigurationListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session, config: Config) {
        arFragment.instructionsController = null
        arFragment.arSceneView.planeRenderer.isVisible = false
        viewModel.setupArtistImageDatabase(session, config)
        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate)
        initializeRecorder()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        if (augmentedImage.trackingState == TrackingState.TRACKING
            && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            if (isModelAdded.not()) {
                viewModel.getVideoForImage(augmentedImage.name)?.let {
                    showImageDetectedMessage()
                    createArtistArView(arFragment, augmentedImage, it)
                    isModelAdded = true
                }
            }
        }
    }

    private fun showImageDetectedMessage() {
        tutorialTextPresenter.showMessage(resources.getString(R.string.image_detected_message))
    }

    private fun initializeRecorder() {
        activity?.let {
            videoRecorder = VideoRecorder(
                arFragment.arSceneView,
                it.filesDir
            )
            setupRecordButton()
        }
    }

    private fun setupRecordButton() {
        recordButton.maxMilisecond = MAX_DURATION_MS
        recordButton.setRecordListener(object : OnRecordListener {
            override fun onRecord() {
                if (videoRecorder?.isRecording == false) {
                    handleMicrophonePermission()
                }
            }

            override fun onRecordCancel() {
                if (videoRecorder?.isRecording == true) {
                    FirebaseAnalytics.getInstance(requireContext()).logEvent("stop_recording", null)
                    toggleRecording()
                }
            }

            override fun onRecordFinish() {
                if (videoRecorder?.isRecording == true) {
                    FirebaseAnalytics.getInstance(requireContext()).logEvent("stop_recording", null)
                    toggleRecording()
                }
            }
        })
    }

    private fun handleMicrophonePermission() {
        val permission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            audioPermissionResultLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            FirebaseAnalytics.getInstance(requireContext()).logEvent("start_recording",
                Bundle().apply {
                    putString("microphone_permission_status", "granted")
                }
            )
            toggleRecording()
        }
    }

    private fun toggleRecording() {
        val recording = videoRecorder?.onToggleRecord()

        recording?.onSuccess { isRecording ->
            if (isRecording.not()) {
                mediaPlayer?.pause()
                videoRecorder?.recordingFile?.let {
                    navigateToPreviewAndEdit(it.absolutePath)
                }
            }
        }

        recording?.onFailure {
            Toast.makeText(activity, context?.getString(R.string.record_button_generic_error_message), Toast.LENGTH_SHORT).show()
            Bugsnag.notify(it)
        }
    }

    private fun resetModel() {
        isModelAdded = false
    }

    private fun navigateToPreviewAndEdit(filePath: String) {
        resetModel()
        val action = CustomArFragmentDirections.actionArFragmentToPreviewFragment(filePath)
        findNavController().navigate(action)
    }

    private fun createArtistArView(
        arFragment: ArFragment,
        augmentedImage: AugmentedImage,
        videoRes: Int
    ) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
        mediaPlayer = initializeMediaPlayer(videoRes)

        // TODO handle media player loading
        mediaPlayer?.let { mediaPlayer ->
            val videoNode = VideoNode(
                activity,
                mediaPlayer.apply { start() },
                null,
                null
            )

            videoNode.setOnTapListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if(mediaPlayer.isPlaying) {
                        FirebaseAnalytics.getInstance(requireContext()).logEvent("video_view_tapped",
                            Bundle().apply {
                                putString("to_status", "pause")
                            }
                        )
                        mediaPlayer.pause()
                    } else {
                        FirebaseAnalytics.getInstance(requireContext()).logEvent("video_view_tapped",
                            Bundle().apply {
                                putString("to_status", "play")
                            }
                        )
                        mediaPlayer.start()
                    }
                }
            }
            videoNode.parent = anchorNode
            videoNode.localRotation = flattenViewOnImage()
            videoNode.localScale = getScale(augmentedImage, mediaPlayer.videoHeight, mediaPlayer.videoWidth)
            videoNode.localPosition = Vector3(0f, NodeConfig.videoZPostion, augmentedImage.extentZ/2)

            FirebaseAnalytics.getInstance(requireContext()).logEvent("video_viewed",
                Bundle().apply {
                    putString("type", "local")
                }
            )
        }
        arFragment.arSceneView.scene.addChild(anchorNode)

        ViewRenderable.builder()
            .setView(activity, R.layout.artist_links)
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
                node.localRotation = flattenViewOnImage()
                node.localPosition = Vector3(0f, 0f, augmentedImage.extentZ * 0.9f)
                node.rotationController.isEnabled = false
                node.translationController.isEnabled = false
                node.scaleController.minScale = NodeConfig.viewMinScale
                node.scaleController.maxScale = NodeConfig.viewMaxScale

                FirebaseAnalytics.getInstance(requireContext()).logEvent("attachment_links_viewed",
                    Bundle().apply {
                        putString("type", "local")
                        putInt("count", artistLinkAdapter.itemCount)
                    }
                )
            }
            .exceptionally {
                Toast.makeText(activity, resources.getString(R.string.generic_error_snackbar_message), Toast.LENGTH_SHORT).show()
                Bugsnag.notify(it)
                null
            }
    }

    private fun openWebView(url: String) {
        FirebaseAnalytics.getInstance(requireContext()).logEvent("attachment_link_button_tapped",
            Bundle().apply {
                putString("url", url)
            }
        )
        if (videoRecorder?.isRecording == false) {
            try {
                activity?.let {
                    CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                        .launchUrl(it, url.toUri())
                }
            } catch (e: Exception) {
                Toast.makeText(activity, resources.getString(R.string.generic_error_snackbar_message), Toast.LENGTH_SHORT).show()
                Bugsnag.notify(e)
            }
        } else {
            Toast.makeText(activity, resources.getString(R.string.link_button_tap_while_recording_snackbar_error_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getScale(image: AugmentedImage, videoHeight: Int, videoWidth: Int): Vector3 {
        val newVideoHeight = image.extentZ * 2
        val newVideoWidth = image.extentZ  * (videoWidth.toFloat()/videoHeight.toFloat())

        return Vector3(newVideoWidth, newVideoHeight, 1f)
    }

    private fun initializeMediaPlayer(videoRes: Int): MediaPlayer =
        MediaPlayer.create(activity, videoRes).also {
            it.isLooping = true
        }

    private fun flattenViewOnImage(): Quaternion = Quaternion(Vector3(1f, 0f, 0f), NodeConfig.flattenNodeOnImageRotation)

    private val audioPermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted.not()) {
                FirebaseAnalytics.getInstance(requireContext()).logEvent("start_recording",
                    Bundle().apply {
                        putString("microphone_permission_status", "denied")
                    }
                )
                Toast.makeText(activity, resources.getString(R.string.microphone_permission_required_snackbar_error_message), Toast.LENGTH_LONG).show()
            }
        }

    // delete recordings from previous sessions
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.toList()?.forEach { child ->
                Log.d(TAG, "deleting file with path ${child.absolutePath}")
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }
}