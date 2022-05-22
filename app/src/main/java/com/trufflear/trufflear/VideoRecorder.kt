package com.trufflear.trufflear

import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import com.bugsnag.android.Bugsnag
import com.google.ar.sceneform.SceneView
import com.trufflear.trufflear.file.FileCreator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Video Recorder class handles recording the contents of a SceneView. It uses MediaRecorder to
 * encode the video. The quality settings can be set explicitly or simply use the CamcorderProfile
 * class to select a predefined set of parameters.
 */

private val QUALITY_LEVELS = listOf(
    CamcorderProfile.QUALITY_1080P,
    CamcorderProfile.QUALITY_HIGH,
    CamcorderProfile.QUALITY_2160P,
    CamcorderProfile.QUALITY_720P,
    CamcorderProfile.QUALITY_480P
)

private const val VIDEO_BASE_NAME = "artist"
const val MAX_DURATION_MS = 15000

class VideoRecorder(
    private val sceneView: SceneView,
    fileDirectory: File,
    private val fileCreator: FileCreator,
    private val recorderConfigurer: RecorderConfigurer,
    private val mediaRecorder: MediaRecorder
) {
    // recordingVideoFlag is true when the media recorder is capturing video.
    var isRecording = false

    private val TAG = VideoRecorder::class.java.simpleName
    private var videoSize: Size? = null
    private val videoDirectory = fileCreator.createFile(
        fileDirectory, FileProviderConstants.FILE_NAME
    )
    var recordingFile: File? = null

    /**
     * Toggles the state of video recording.
     *
     * @return true if recording is now active.
     */
    fun onToggleRecord(): Result<Boolean> {
        return if (isRecording) {
            stopRecordingVideo().map {
                isRecording
            }
        } else {
            startRecordingVideo().map {
                isRecording
            }
        }
    }

    private fun startRecordingVideo(): Result<Unit> {
        prepareVideoRecorder()

        return runCatching {
            mediaRecorder.start()

            // Set up Surface for the MediaRecorder
            val encoderSurface = mediaRecorder.surface ?: return Result.failure(Throwable())
            val size = videoSize ?: return  Result.failure(Throwable())

            sceneView.startMirroringToSurface(
                encoderSurface, 0, 0, size.width, size.height
            )
            isRecording = true
        }
    }


    private fun prepareVideoRecorder(): Result<Unit> {
        recordingFile = buildFile() ?: return Result.failure(Throwable())
        val profile = getCamcorderProfile() ?: return Result.failure(Throwable())
        videoSize = recorderConfigurer.getSize(profile.videoFrameHeight, profile.videoFrameWidth)

        return runCatching {
            recorderConfigurer.configureMediaRecorder(mediaRecorder, profile, recordingFile)
        }
    }

    private fun buildFile(): File? {
        if (videoDirectory.exists().not()) {
            if (!videoDirectory.mkdirs()) {
                Bugsnag.notify(Exception())
                Log.d(TAG, "failed to create directory")
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val recordingFile = fileCreator.createFile(
            videoDirectory, "$VIDEO_BASE_NAME$timeStamp.mp4"
        )
        return recordingFile
    }

    private fun stopRecordingVideo(): Result<Unit> {
        isRecording = false

        return runCatching {
            val encoderSurface = mediaRecorder.surface ?: return Result.failure(Throwable())
            sceneView.stopMirroringToSurface(encoderSurface)

            // Stop recording
            mediaRecorder.stop()
            mediaRecorder.reset()
        }.onFailure {
            mediaRecorder.reset()
        }
    }

    private fun getCamcorderProfile(): CamcorderProfile? = recorderConfigurer.hasProfile(QUALITY_LEVELS)
}

class RecorderConfigurer @Inject constructor() {

    fun hasProfile(qualityLevels: List<Int>): CamcorderProfile? =
        qualityLevels.find {
            CamcorderProfile.hasProfile(it)
        }?.let {
            CamcorderProfile.get(it)
        }

    fun configureMediaRecorder(mediaRecorder: MediaRecorder?, profile: CamcorderProfile, recordingFile: File?) {
        mediaRecorder?.run {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setProfile(profile)
            setOutputFile(recordingFile)
            setVideoSize(profile.videoFrameHeight, profile.videoFrameWidth)
            setMaxDuration(MAX_DURATION_MS)
            prepare()
        }
    }

    fun getSize(height: Int, width: Int): Size = Size(height, width)
}