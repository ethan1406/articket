package com.trufflear.trufflear

import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trufflear.trufflear.data.ArImageRepository
import com.trufflear.trufflear.data.WeddingImageStorage
import com.trufflear.trufflear.models.WeddingLinkModel
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.trufflear.trufflear.modules.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(
    private val arImageRepository: ArImageRepository,
    private val weddingImageDatabase: WeddingImageStorage,
    @MainDispatcher private val dispatcher: CoroutineDispatcher
): ViewModel() {
    private val TAG = ArViewModel::class.java.simpleName

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _arFragmentConfig = MutableStateFlow(ArFragmentConfig())
    val arFragmentConfig: StateFlow<ArFragmentConfig> = _arFragmentConfig

    private val _artistLinks = MutableStateFlow(emptyList<WeddingLinkModel>())
    val artistLinks = _artistLinks.asStateFlow()

    init {
        loadWeddingLinks()
    }

    fun setupArtistImageDatabase(
        session: Session,
        config: Config
    ) {
        viewModelScope.launch(dispatcher) {
            val config = weddingImageDatabase.getConfigWithImageDatabase(
                config,
                session,
                arImageRepository.loadArImageBitmapModels()
            )
            _arFragmentConfig.value = ArFragmentConfig(config)
            _isInitialLoading.value = false
        }
    }

    private fun loadWeddingLinks() {
        _artistLinks.value = arImageRepository.getWeddingLinks()
    }

    @RawRes fun getVideoForImage(imageName: String): Int? = weddingImageDatabase.getVideoForImage(imageName)
}

data class ArFragmentConfig(
    val config: Config? = null
)