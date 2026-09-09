package com.artknower.app.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var currentEncoding: String = "WEBM_OPUS"

    fun startRecording(): Boolean {
        try {
            stopRecording() // Clean up any active session

            val (file, encoding) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File(context.cacheDir, "artisan_voice_${System.currentTimeMillis()}.webm") to "WEBM_OPUS"
            } else {
                File(context.cacheDir, "artisan_voice_${System.currentTimeMillis()}.amr") to "AMR_WB"
            }

            currentOutputFile = file
            currentEncoding = encoding

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setOutputFormat(MediaRecorder.OutputFormat.WEBM)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(16000)
                    setAudioEncodingBitRate(32000)
                } else {
                    setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
                    setAudioSamplingRate(16000)
                }
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            release()
            return false
        }
    }

    data class RecordedAudio(
        val base64Data: String,
        val encoding: String,
        val sampleRateHertz: Int = 16000,
        val byteCount: Int
    )

    fun stopAndGetAudio(): RecordedAudio? {
        val recorder = mediaRecorder ?: return null
        val file = currentOutputFile
        try {
            recorder.stop()
        } catch (_: Exception) {
            // Can happen if stopped immediately after start
        } finally {
            release()
        }

        if (file == null || !file.exists() || file.length() == 0L) {
            return null
        }

        return try {
            val bytes = file.readBytes()
            file.delete()
            if (bytes.isEmpty()) return null
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            RecordedAudio(
                base64Data = base64,
                encoding = currentEncoding,
                sampleRateHertz = 16000,
                byteCount = bytes.size
            )
        } catch (e: Exception) {
            null
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}
        release()
    }

    fun release() {
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
    }
}
