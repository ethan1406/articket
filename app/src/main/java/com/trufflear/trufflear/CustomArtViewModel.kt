package com.trufflear.trufflear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trufflear.trufflear.data.ArImageRepository
import com.trufflear.trufflear.data.ArtistImageDatabase
import com.trufflear.trufflear.models.ArtistLinkModel
import com.google.ar.core.Config
import com.google.ar.core.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomArtViewModel @Inject constructor(
    private val arImageRepository: ArImageRepository,
    private val artistImageDatabase: ArtistImageDatabase
): ViewModel() {
    private val TAG = CustomArtViewModel::class.java.simpleName

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _arFragmentConfig = MutableStateFlow(ArFragmentConfig())
    val arFragmentConfig: StateFlow<ArFragmentConfig> = _arFragmentConfig

    private val _artistLinks = MutableStateFlow(emptyList<ArtistLinkModel>())
    val artistLinks = _artistLinks.asStateFlow()

    init {
        loadArtistLinks()
    }

    fun setupArtistImageDatabase(
        session: Session,
        config: Config
    ) {
        viewModelScope.launch {
            val config = artistImageDatabase.getConfigWithImageDatabase(
                config,
                session,
                arImageRepository.loadArImageBitmapModels()
            )
            _arFragmentConfig.value = ArFragmentConfig(config)
            _isInitialLoading.value = false
        }
    }

    private fun loadArtistLinks() {
        _artistLinks.value = (arImageRepository.getWeddingLinks().map {
            ArtistLinkModel(
                image = it.image,
                text = it.text,
                webLink = it.webLink
            )
        })
    }

    fun getVideoForImage(imageName: String): Int? = artistImageDatabase.getVideoForImage(imageName)
}

data class ArFragmentConfig(
    val config: Config? = null
)