package com.example.arimage

import android.content.Context
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.util.Size
import com.google.ar.sceneform.SceneView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

private const val folder = "/artists"

private const val videoBaseName = "ar"

class VideoRecorder(
    private val sceneView: SceneView,
    context: Context
) {
    // recordingVideoFlag is true when the media recorder is capturing video.
    var isRecording = false

    private val TAG = VideoRecorder::class.java.simpleName
    private var mediaRecorder: MediaRecorder? = null
    private var videoSize: Size? = null
    private val videoDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
            .toString() + folder
    )
    var videoPath: File? = null


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

        return kotlin.runCatching {
            mediaRecorder?.start()

            // Set up Surface for the MediaRecorder
            val encoderSurface = mediaRecorder?.surface ?: return Result.failure(Throwable())
            val size = videoSize ?: return  Result.failure(Throwable())

            sceneView.startMirroringToSurface(
                encoderSurface, 0, 0, size.width, size.height
            )
            isRecording = true
        }
    }


    private fun prepareVideoRecorder(): Result<Unit> {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }

        if (buildFilename().not()) {
            return Result.failure(Throwable())
        }
        val file = videoPath ?: return Result.failure(Throwable())
        val profile = getCamcorderProfile() ?: return Result.failure(Throwable())
        videoSize = Size(profile.videoFrameHeight, profile.videoFrameWidth)

        return kotlin.runCatching {
            mediaRecorder?.run {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                //setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                setOrientationHint(90)
                setProfile(profile)

                setOutputFile(file.absolutePath)
//                setVideoEncodingBitRate(profile.videoBitRate)
//                setVideoFrameRate(profile.videoFrameRate)
                setVideoSize(profile.videoFrameHeight, profile.videoFrameWidth)
//                setVideoEncoder(profile.videoCodec)
                prepare()
            }
        }
    }

    private fun buildFilename(): Boolean {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        videoPath = File(
            videoDirectory, "$videoBaseName$timeStamp.mp4"
        )
        if (videoDirectory.exists().not()) {
            if (!videoDirectory.mkdirs()) {
                Log.d(TAG, "failed to create directory")
                return false
            }
        }
        Log.d(TAG, videoPath?.absolutePath ?: "video path is null")
        return true
    }

    private fun stopRecordingVideo(): Result<Unit> {
        // UI
        isRecording = false

        return kotlin.runCatching {
            val encoderSurface = mediaRecorder?.surface ?: return Result.failure(Throwable())
            sceneView.stopMirroringToSurface(encoderSurface)

            // Stop recording
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder = null
        }

    }

    private fun getCamcorderProfile(): CamcorderProfile? {
        return QUALITY_LEVELS.find {
            CamcorderProfile.hasProfile(it)
        }?.let {
            CamcorderProfile.get(it)
        }
    }
}