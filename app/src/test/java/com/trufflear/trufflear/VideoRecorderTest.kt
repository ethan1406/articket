package com.trufflear.trufflear

import android.media.MediaRecorder
import android.util.Size
import com.google.ar.sceneform.SceneView
import com.trufflear.trufflear.file.FileCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class VideoRecorderTest {

    @Mock
    private lateinit var file: File

    @Mock
    private lateinit var sceneView: SceneView

    @Mock
    private lateinit var fileCreator: FileCreator

    @Mock
    private lateinit var recorderConfigurer: RecorderConfigurer

    @Mock
    private lateinit var mediaRecorder: MediaRecorder

    private lateinit var videoRecorder: VideoRecorder

    @Before
    fun setup() {
        whenever(file.exists()).thenReturn(true)
        whenever(fileCreator.createFile(any(), any())).thenReturn(file)

        val size: Size = mock {
            on { width } doReturn 5
            on { height } doReturn 5
        }

        whenever(recorderConfigurer.hasProfile(any())).thenReturn(mock())
        whenever(recorderConfigurer.getSize(any(), any())).thenReturn(size)
        whenever(mediaRecorder.surface).thenReturn(mock())


        videoRecorder = VideoRecorder(sceneView, file, fileCreator, recorderConfigurer, mediaRecorder)
    }

    @Test
    fun `onToggleRecord should start recording`() {
        // ARRANGE

        // ACT
        val result = videoRecorder.onToggleRecord()

        // ASSERT
        assertThat(result).isEqualTo(Result.success(true))

        verify(recorderConfigurer, times(1)).configureMediaRecorder(any(), any(), any())
        verify(mediaRecorder, times(1)).start()
    }

    @Test
    fun `onToggleRecord should stop recording when it is called the second time`() {
        // ARRANGE

        // ACT
        videoRecorder.onToggleRecord()
        val result = videoRecorder.onToggleRecord()

        // ASSERT
        assertThat(result).isEqualTo(Result.success(false))

        verify(mediaRecorder, times(1)).stop()
        verify(mediaRecorder, times(1)).reset()
    }
}