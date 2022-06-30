package com.trufflear.trufflear

import android.content.res.Resources
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trufflear.trufflear.data.ArImageRepository
import com.trufflear.trufflear.data.WeddingImageStorage
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.trufflear.trufflear.models.CardTransformation
import com.trufflear.trufflear.modules.DefaultDispatcher
import com.trufflear.trufflear.viewmodels.ToastViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(
    private val arImageRepository: ArImageRepository,
    private val weddingImageDatabase: WeddingImageStorage,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val resources: Resources
): ViewModel() {
    private val TAG = ArViewModel::class.java.simpleName

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _arFragmentConfig = MutableStateFlow(ArFragmentConfig())
    val arFragmentConfig: StateFlow<ArFragmentConfig> = _arFragmentConfig

    private val _toastViewModel = MutableStateFlow(ToastViewModelWrapper())
    val toastViewModel: StateFlow<ToastViewModelWrapper> = _toastViewModel

    private val imageToTransformationMap = mutableMapOf<Int, CardTransformation>()
    fun setupArtistImageDatabase(
        session: Session,
        config: Config
    ) {
        viewModelScope.launch(defaultDispatcher) {

            val result = arImageRepository.getCardTransformations()
            result.onSuccess { map ->
                imageToTransformationMap.clear()
                imageToTransformationMap.putAll(map)

                val localConfig = weddingImageDatabase.getConfigWithImageDatabase(
                    config,
                    session,
                    imageToTransformationMap.values.map { it.cardImage }
                )
                _arFragmentConfig.value = ArFragmentConfig(localConfig)
                _isInitialLoading.value = false
            }

            result.onFailure {
                _toastViewModel.value = ToastViewModelWrapper(
                    ToastViewModel(
                        text = resources.getString(R.string.generic_fatal_error_snackbar_message),
                        duration = Toast.LENGTH_LONG
                    )
                )
            }
        }
    }

    fun getCardTransformationForImage(imageId: Int): CardTransformation? = imageToTransformationMap[imageId]

}

data class ArFragmentConfig(
    val config: Config? = null
)

data class ToastViewModelWrapper(
    val toastViewModel: ToastViewModel? = null
)