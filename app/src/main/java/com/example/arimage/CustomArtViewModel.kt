package com.example.arimage

import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arimage.data.ArImageRepository
import com.example.arimage.viewmodels.ArtistLinkViewModel
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

private const val IMAGE_NAME_LENGTH = 4

@HiltViewModel
class CustomArtViewModel @Inject constructor(private val arImageRepository: ArImageRepository): ViewModel() {
    private val TAG = CustomArtViewModel::class.java.simpleName

    val isInitialLoading = MutableLiveData(true)
    val artistLinks = MutableLiveData(listOf<ArtistLinkViewModel>())
    val openWebIntent = MutableLiveData<Pair<CustomTabsIntent, String>>()

    private var augmentedImageDatabase: AugmentedImageDatabase? = null
    private val imageAndVideoMap = mutableMapOf<String, Int>()

    init {
        loadArtistLinks()
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
                config.augmentedImageDatabase = augmentedImageDatabase
            }
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