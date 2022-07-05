package com.trufflear.trufflear.data

import com.trufflear.trufflear.AttachmentView
import com.trufflear.trufflear.AugmentedImage
import com.trufflear.trufflear.AugmentedVideo
import com.trufflear.trufflear.GetCardTransformationDataResponse
import com.trufflear.trufflear.LinkButton
import com.trufflear.trufflear.attachmentView
import com.trufflear.trufflear.augmentedImage
import com.trufflear.trufflear.augmentedTransformation
import com.trufflear.trufflear.augmentedVideo
import com.trufflear.trufflear.data.api.CardTransformationApi
import com.trufflear.trufflear.getCardTransformationDataResponse
import com.trufflear.trufflear.linkButton
import com.trufflear.trufflear.position
import com.trufflear.trufflear.size
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.lang.Exception

@RunWith(MockitoJUnitRunner::class)
class ArImageRepositoryTest {

    @Mock
    private lateinit var api: CardTransformationApi


    @ExperimentalCoroutinesApi
    @Test
    fun `get card transformations should return image to transformation map when api returns success`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = ArImageRepository(api, dispatcher)

        whenever(api.getCardTransformationData()).thenReturn(Result.success(getTransformations()))

        // ACT
        val response = repository.getCardTransformations()

        advanceUntilIdle()

        // ASSERT
        assertThat(response.isSuccess).isTrue

        response.onSuccess { map ->
            assertThat(map.entries.size).isEqualTo(3)
            assertThat(map[0]).isNull()
            assertThat(map[1]?.transformationId).isEqualTo(1)
            assertThat(map[2]?.transformationId).isEqualTo(1)
            assertThat(map[3]?.transformationId).isEqualTo(2)

            assertThat(map[1]?.attachmentView?.minScale).isEqualTo(5f)
            assertThat(map[1]?.attachmentView?.position?.zScaleToImageHeight).isEqualTo(9f)

            assertThat(map[1]?.cardLinks?.size).isEqualTo(2)
            assertThat(map[2]?.cardLinks?.size).isEqualTo(2)
            assertThat(map[3]?.cardLinks?.size).isEqualTo(1)

            assertThat(map[1]?.cardVideo?.videoUrl).isEqualTo("test video url")

            assertThat(map[3]?.cardImage?.imageId).isEqualTo(3)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get card transformations should return error when api returns error`() = runTest {
        // ARRANGE
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = ArImageRepository(api, dispatcher)

        val errorMessage = "Test message"
        val exception = Exception(errorMessage)
        whenever(api.getCardTransformationData()).thenReturn(Result.failure(exception))

        // ACT
        val response = repository.getCardTransformations()

        advanceUntilIdle()

        // ASSERT
        assertThat(response.isFailure).isTrue

        response.onFailure { throwable ->
            assertThat(throwable.message).isEqualTo(errorMessage)
        }
    }
}

private fun getTransformations(): GetCardTransformationDataResponse =
    getCardTransformationDataResponse {
        augmentedTransformations.addAll(
            listOf(
                augmentedTransformation {
                    transformationId = 1
                    augmentedVideo = getAugmentedVideo()
                    attachmentView = getAttachmentViewConfig(2)
                    augmentedImages.addAll(
                        listOf(
                            getAugmentedImage(1),
                            getAugmentedImage(2)
                        )
                    )
                },
                augmentedTransformation {
                    transformationId = 2
                    augmentedVideo = getAugmentedVideo()
                    attachmentView = getAttachmentViewConfig(1)
                    augmentedImages.addAll(
                        listOf(
                            getAugmentedImage(3)
                        )
                    )
                }
            )
        )
    }

private fun getAttachmentViewConfig(numLinks: Int): AttachmentView =
    attachmentView {
        minScale = 5f
        maxScale = 3f
        position = position {
            xScaleToImageWidth = 31f
            y = 2f
            zScaleToImageHeight = 9f
        }
        linkButtons.addAll(
            (0 until numLinks).map {
                getLinkButton()
            }
        )
    }

private fun getAugmentedImage(id: Int = 3): AugmentedImage =
    augmentedImage {
        imageId = id
        imageUrl = "image url"
        imageName = "image name"
        physicalImageSize = size {
            width = 14f
            height = 12f
        }
    }

private fun getAugmentedVideo(): AugmentedVideo =
    augmentedVideo {
        videoUrl = "test video url"
        videoWidthScaleToImageWidth = 7f
        position = position {
            xScaleToImageWidth = 1f
            y = 1.4f
            zScaleToImageHeight = 2.1f
        }
    }

private fun getLinkButton(id: Int = 3): LinkButton =
    linkButton {
        linkButtonId = id
        imageUrl = "image url"
        text = "Test"
        colorCode = "#hexcode"
        webUrl = "test url"
    }