package com.aichat.platform

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

class AndroidAudioRecorderManager : AudioRecorderManager {
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var outputFile: File? = null
    private var startTime: Long = 0L

    override fun startRecording(onError: (String) -> Unit) {
        val context = AndroidAppContext.context
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val requestPerm = AndroidActivityHelper.requestAudioPermission
            if (requestPerm != null) {
                requestPerm { granted ->
                    if (granted) {
                        doStartRecording(onError)
                    } else {
                        onError("麦克风权限被拒绝")
                    }
                }
            } else {
                onError("无法请求权限")
            }
            return
        }
        doStartRecording(onError)
    }

    private fun doStartRecording(onError: (String) -> Unit) {
        try {
            val context = AndroidAppContext.context
            val dir = File(context.filesDir, "voice_messages")
            dir.mkdirs()
            outputFile = File(dir, "voice_${System.currentTimeMillis()}.m4a")
            startTime = System.currentTimeMillis()

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }
            recording = true
        } catch (e: Exception) {
            recording = false
            onError("录音失败: ${e.message}")
        }
    }

    override fun stopRecording(cancel: Boolean): RecordResult? {
        if (!recording) return null

        val duration = System.currentTimeMillis() - startTime
        try {
            recorder?.stop()
        } catch (e: Exception) {
            releaseRecorder()
            outputFile?.delete()
            recording = false
            return null
        }
        releaseRecorder()
        recording = false

        val file = outputFile
        outputFile = null

        if (cancel || file == null || !file.exists()) {
            file?.delete()
            return null
        }

        // Too short, ignore
        if (duration < 800) {
            file.delete()
            return null
        }

        // Fix ftyp brand for compatibility
        try {
            java.io.RandomAccessFile(file, "rw").use { raf ->
                if (raf.length() >= 12) {
                    raf.seek(8)
                    val brand = ByteArray(4)
                    raf.readFully(brand)
                    if (brand[0] == 'm'.code.toByte() &&
                        brand[1] == 'p'.code.toByte() &&
                        brand[2] == '4'.code.toByte() &&
                        brand[3] == '2'.code.toByte()) {
                        raf.seek(8)
                        raf.write(byteArrayOf('M'.code.toByte(), '4'.code.toByte(), 'A'.code.toByte(), ' '.code.toByte()))
                    }
                }
            }
        } catch (_: Exception) {}

        return RecordResult(
            filePath = file.absolutePath,
            durationMs = duration,
        )
    }

    private fun releaseRecorder() {
        recorder?.apply {
            try { reset() } catch (_: Exception) {}
            try { release() } catch (_: Exception) {}
        }
        recorder = null
    }

    override fun isRecording(): Boolean = recording

    override fun release() {
        if (recording) {
            try { recorder?.stop() } catch (_: Exception) {}
        }
        releaseRecorder()
        recording = false
    }
}

@Composable
actual fun rememberAudioRecorder(): AudioRecorderManager {
    return remember { AndroidAudioRecorderManager() }
}
