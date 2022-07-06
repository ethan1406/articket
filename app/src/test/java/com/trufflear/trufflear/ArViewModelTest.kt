package com.trufflear.trufflear

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.ar.core.Config
import com.trufflear.trufflear.data.ArImageRepository
import com.trufflear.trufflear.data.WeddingImageStorage
import com.trufflear.trufflear.models.AttachmentLinkModel
import com.trufflear.trufflear.models.CardImage
import com.trufflear.trufflear.models.CardTransformation
import com.trufflear.trufflear.models.CardVideo
import com.trufflear.trufflear.models.Position
import com.trufflear.trufflear.models.Size
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.Exception

@RunWith(MockitoJUnitRunner::class)
class ArViewModelTest {

    @ExperimentalCoroutinesApi
    @Mock
    private lateinit var testDispatcher: TestDispatcher

    @Mock
    private lateinit var weddingImageStorage: WeddingImageStorage

    @Mock
    private lateinit var connectivityManager: ConnectivityManager

    @Mock
    private lateinit var arImageRepository: ArImageRepository

    @Mock
    private lateinit var resources: Resources

    @InjectMocks
    private lateinit var arViewModel: ArViewModel

    @Before
    fun setup() {
        val networkCapabilitiesMock = mock<NetworkCapabilities> {
            on { hasTransport(any()) } doReturn true
        }
        whenever(connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork))
            .thenReturn(networkCapabilitiesMock)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `setupArtistImageDatabase should emit config and stop loading when repository returns success`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val paramConfig = mock<Config>()
        val returnConfig = mock<Config>()

        whenever(weddingImageStorage.getConfigWithImageDatabase(eq(paramConfig), any(), any())).thenReturn(returnConfig)
        whenever(arImageRepository.getCardTransformations()).thenReturn(Result.success(emptyMap()))

        val loadingResult = mutableListOf<Boolean>()
        val configResult = mutableListOf<ArFragmentConfig>()
        val loadingJob = launch {
            arViewModel.isInitialLoading.toList(loadingResult)
        }

        val configJob = launch {
            arViewModel.arFragmentConfig.toList(configResult)
        }

        // ACT
        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, dispatcher, mock(), connectivityManager)
        arViewModel.setupArtistImageDatabase(mock(), paramConfig)

        advanceUntilIdle()

        // ASSERT
        assertThat(loadingResult.first()).isEqualTo(true)
        assertThat(loadingResult.last()).isEqualTo(false)

        assertThat(configResult.first().config).isNull()
        assertThat(configResult.last().config).isEqualTo(returnConfig)

        loadingJob.cancel()
        configJob.cancel()

        verify(weddingImageStorage, times(1)).getConfigWithImageDatabase(eq(paramConfig), any(), any())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `setupArtistImageDatabase should emit error toast and stop loading when repository returns error`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val paramConfig = mock<Config>()
        val resources = mock<Resources> {
            on { getString(any()) } doReturn ""
        }

        whenever(arImageRepository.getCardTransformations()).thenReturn(Result.failure(Exception()))

        val loadingResult = mutableListOf<Boolean>()
        val loadingJob = launch {
            arViewModel.isInitialLoading.toList(loadingResult)
        }

        val toastResult = mutableListOf<ToastViewModelWrapper>()
        val toastJob = launch {
            arViewModel.toastViewModel.toList(toastResult)
        }


        // ACT
        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, dispatcher, resources, connectivityManager)
        arViewModel.setupArtistImageDatabase(mock(), paramConfig)

        advanceUntilIdle()

        // ASSERT
        assertThat(loadingResult.first()).isEqualTo(true)
        assertThat(loadingResult.last()).isEqualTo(false)

        assertThat(toastResult.first().toastViewModel).isNull()
        assertThat(toastResult.last().toastViewModel).isNotNull

        loadingJob.cancel()
        toastJob.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `setupArtistImageDatabase should emit error toast when there is no internet connection`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val paramConfig = mock<Config>()
        val resources = mock<Resources> {
            on { getString(any()) } doReturn ""
        }

        val configResult = mutableListOf<ArFragmentConfig>()
        val configJob = launch {
            arViewModel.arFragmentConfig.toList(configResult)
        }

        val toastResult = mutableListOf<ToastViewModelWrapper>()
        val toastJob = launch {
            arViewModel.toastViewModel.toList(toastResult)
        }

        val networkCapabilitiesMock = mock<NetworkCapabilities> {
            on { hasTransport(any()) } doReturn false
        }
        whenever(connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork))
            .thenReturn(networkCapabilitiesMock)

        // ACT
        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, dispatcher, resources, connectivityManager)
        arViewModel.setupArtistImageDatabase(mock(), paramConfig)

        advanceUntilIdle()

        // ASSERT
        assertThat(configResult.size).isEqualTo(1)

        assertThat(toastResult.first().toastViewModel).isNotNull
        assertThat(toastResult.size).isEqualTo(1)

        toastJob.cancel()
        configJob.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getVideoForImage should return video`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val paramConfig = mock<Config>()
        val returnConfig = mock<Config>()

        whenever(weddingImageStorage.getConfigWithImageDatabase(eq(paramConfig), any(), any())).thenReturn(returnConfig)
        whenever(arImageRepository.getCardTransformations()).thenReturn(Result.success(getCardToTransformationMap()))

        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, dispatcher, mock(), connectivityManager)
        arViewModel.setupArtistImageDatabase(mock(), paramConfig)

        advanceUntilIdle()

        // ACT
        val transformation1 = arViewModel.getCardTransformationForImage(1)
        val transformation2 = arViewModel.getCardTransformationForImage(3)

        // ASSERT
        assertThat(transformation1?.transformationId).isEqualTo(2)
        assertThat(transformation2?.transformationId).isEqualTo(9)

        assertThat(transformation1?.cardImage?.imageId).isEqualTo(1)
        assertThat(transformation2?.cardLinks?.size).isEqualTo(1)
        assertThat(transformation2?.cardVideo?.videoUrl).isEqualTo("video url")
    }
}

private fun getCardToTransformationMap(): Map<Int, CardTransformation> =
    mapOf(
        1 to getCardTransformation(2, 1),
        3 to getCardTransformation(9, 3)
    )

private fun getCardTransformation(transformationId: Int, imageId: Int): CardTransformation =
    CardTransformation(
        transformationId = transformationId,
        cardVideo = CardVideo(
            videoUrl = "video url",
            widthScaleToImageWidth = 3f,
            position = Position(
                xScaleToImageWidth = 7f,
                y = 2f,
                zScaleToImageHeight = 9f
            )
        ),
        cardImage = CardImage(
            imageId =  imageId,
            imageUrl = "image url",
            imageName = "image name",
            physicalSize = Size(2f, 4f)
        ),
        cardLinks = listOf(
            AttachmentLinkModel(
                imageUrl = "attachment url",
                text = "test text",
                webLink = "test link",
                colorCode = "#colorCode"
            ),
        ),
        attachmentView = com.trufflear.trufflear.models.AttachmentView(
            minScale = 2f,
            maxScale = 3f,
            position = Position(
                xScaleToImageWidth = 7f,
                y = 2f,
                zScaleToImageHeight = 9f
            )
        )
    )