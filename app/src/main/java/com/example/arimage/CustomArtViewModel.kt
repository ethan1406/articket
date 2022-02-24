package com.example.arimage

import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arimage.data.ArImageRepository
import com.example.arimage.viewmodels.ArtistLinkViewModel
import com.example.arimage.viewmodels.RecordViewState
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.SceneView
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.ReplaySubject
import java.io.File
import javax.inject.Inject

private const val IMAGE_NAME_LENGTH = 6

@HiltViewModel
class CustomArtViewModel @Inject constructor(private val arImageRepository: ArImageRepository): ViewModel() {
    private val TAG = CustomArtViewModel::class.java.simpleName

    val isInitialLoading = MutableLiveData(true)
    val artistLinks = MutableLiveData(listOf<ArtistLinkViewModel>())
    val openWebIntent = MutableLiveData<Pair<CustomTabsIntent, String>>()
    val recordViewState = MutableLiveData<RecordViewState>()
    val requestAudioPermission = MutableLiveData<Unit>()
    val toastMessage = ReplaySubject<Unit>

    private var videoRecorder: VideoRecorder? = null
    private var augmentedImageDatabase: AugmentedImageDatabase? = null
    private val imageAndVideoMap = mutableMapOf<String, Int>()

    init {
        loadArtistLinks()
        toastMessage.postValue(Unit)
    }

    fun setupAugmentedImagesDB(config: Config, session: Session?): Single<Config> =
        Single.create<Config> { emitter ->
            if (augmentedImageDatabase == null) {
                arImageRepository.loadArImageBitmapModels().forEach { model ->
                    augmentedImageDatabase = AugmentedImageDatabase(session)
                    val imageName = getRandomString(IMAGE_NAME_LENGTH)
                    model.imageWidthMeters?.let {
                        augmentedImageDatabase?.addImage(imageName, model.image, model.imageWidthMeters)
                    } ?: run {
                        augmentedImageDatabase?.addImage(imageName, model.image)
                    }
                    imageAndVideoMap.putIfAbsent(imageName, model.videoRes)
                }
            }
            config.augmentedImageDatabase = augmentedImageDatabase
            emitter.onSuccess(config)
        }
            .doOnSuccess {
                isInitialLoading.postValue(false)
            }


    private fun loadArtistLinks() {
        artistLinks.postValue(arImageRepository.getArtistLinks().map {
            ArtistLinkViewModel(
                image = it.image,
                text = it.text,
                webLink = it.webLink,
                onClick = this::artistLinkOnClick
            )
        })
    }

    fun initializeRecorder(
        sceneView: SceneView,
        fileDirectory: File,
        recordClicks: Observable<Boolean>
    ) {
        videoRecorder = VideoRecorder(sceneView, fileDirectory)

        recordClicks.subscribe(
            { hasAudioPermission ->
                when (hasAudioPermission) {
                    false -> requestAudioPermission.postValue(Unit)
                    true -> startRecording()
                }
            },
            { Log.d(TAG, "error with record clicks") }
        )
    }

    fun startRecording() {
        val recording = videoRecorder?.onToggleRecord()

        recording?.onSuccess { isRecording ->
            val state = when (isRecording) {
                true -> RecordViewState.StartRecording
                false -> RecordViewState.StopRecording(videoRecorder?.recordingFile)
            }
            recordViewState.postValue(state)
        }
    }

    private fun artistLinkOnClick(link: String) =
        openWebIntent.postValue(
            Pair(
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build(),
                link
            )
        )

    fun getVideoForImage(imageName: String): Int? = imageAndVideoMap[imageName]


    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}