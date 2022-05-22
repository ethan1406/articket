package com.trufflear.trufflear

import com.google.ar.core.Config
import com.trufflear.trufflear.data.ArImageRepository
import com.trufflear.trufflear.data.WeddingImageStorage
import com.trufflear.trufflear.models.WeddingLinkModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class ArViewModelTest {

    @ExperimentalCoroutinesApi
    @Mock
    private lateinit var testDispatcher: TestDispatcher

    @Mock
    private lateinit var weddingImageStorage: WeddingImageStorage

    @Mock
    private lateinit var arImageRepository: ArImageRepository

    @InjectMocks
    private lateinit var arViewModel: ArViewModel

    @Test
    fun `getVideoForImage should return video`() {
        // ARRANGE
        val imageName = "testImage"
        val videoInt = 5

        whenever(weddingImageStorage.getVideoForImage(imageName)).thenReturn(videoInt)

        // ACT
        val testVideoInt = arViewModel.getVideoForImage(imageName)

        // ASSERT
        assertThat(testVideoInt).isEqualTo(videoInt)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `init should emit wedding links`() {
        // ARRANGE
        val imageName = "testImage"
        val imageRes = 5
        val webLink = "trufflear.com"

        whenever(arImageRepository.getWeddingLinks()).thenReturn(
            listOf(
                WeddingLinkModel(
                    image = imageRes,
                    text = imageName,
                    webLink = webLink
                )
            )
        )

        // ACT
        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, testDispatcher)
        val artistLinks = arViewModel.artistLinks.value

        // ASSERT
        assertThat(artistLinks.size).isEqualTo(1)
        assertThat(artistLinks.first().image).isEqualTo(imageRes)
        assertThat(artistLinks.first().text).isEqualTo(imageName)
        assertThat(artistLinks.first().webLink).isEqualTo(webLink)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun `setupArtistImageDatabase should emit config and stop loading`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val paramConfig = mock<Config>()
        val returnConfig = mock<Config>()

        whenever(weddingImageStorage.getConfigWithImageDatabase(eq(paramConfig), any(), any())).thenReturn(returnConfig)

        val loadingResult = mutableListOf<Boolean>()
        val configResult = mutableListOf<ArFragmentConfig>()
        val loadingJob = launch {
            arViewModel.isInitialLoading.toList(loadingResult)
        }

        val configJob = launch {
            arViewModel.arFragmentConfig.toList(configResult)
        }

        // ACT
        arViewModel = ArViewModel(arImageRepository, weddingImageStorage, dispatcher)
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
}