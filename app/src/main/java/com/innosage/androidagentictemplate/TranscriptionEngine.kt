package com.innosage.androidagentictemplate

import android.content.Context
import android.util.Log
import com.innosage.androidagentictemplate.whisper.WhisperLib
import java.io.File
import java.util.concurrent.Executors

/**
 * Handles background transcription of PCM chunks using Whisper.cpp.
 */
class TranscriptionEngine(private val context: Context) {
    private val whisper = WhisperLib()
    private var contextPtr: Long = 0
    private val executor = Executors.newSingleThreadExecutor()
    private val TAG = "TranscriptionEngine"

    fun initialize(modelPath: String) {
        executor.execute {
            if (!File(modelPath).exists()) {
                Log.e(TAG, "Model file not found: $modelPath")
                return@execute
            }
            contextPtr = whisper.initContext(modelPath)
            if (contextPtr != 0L) {
                Log.i(TAG, "Whisper initialized with model: $modelPath")
            } else {
                Log.e(TAG, "Failed to initialize Whisper context")
            }
        }
    }

    fun transcribeChunk(pcmFile: File, onResult: (String) -> Unit) {
        if (contextPtr == 0L) {
            Log.w(TAG, "Engine not initialized. Skipping transcription for ${pcmFile.name}")
            return
        }

        executor.execute {
            try {
                val pcmData = pcmFile.readBytes()
                // Convert 16-bit PCM (Little Endian) to FloatArray (-1.0 to 1.0)
                val floatData = FloatArray(pcmData.size / 2)
                for (i in floatData.indices) {
                    val low = pcmData[i * 2].toInt() and 0xFF
                    val high = pcmData[i * 2 + 1].toInt()
                    val sample = ((high shl 8) or low).toShort()
                    floatData[i] = sample.toFloat() / 32768.0f
                }

                val status = whisper.fullTranscribe(contextPtr, floatData)
                if (status == 0) {
                    val segmentCount = whisper.getTextSegmentCount(contextPtr)
                    val sb = StringBuilder()
                    for (i in 0 until segmentCount) {
                        sb.append(whisper.getTextSegment(contextPtr, i))
                    }
                    val result = sb.toString().trim()
                    Log.d(TAG, "Transcription result: $result")
                    onResult(result)
                } else {
                    Log.e(TAG, "Transcription failed with status: $status")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error transcribing chunk: ${e.message}")
            }
        }
    }

    fun release() {
        executor.execute {
            if (contextPtr != 0L) {
                whisper.freeContext(contextPtr)
                contextPtr = 0
                Log.i(TAG, "Whisper context released")
            }
        }
        executor.shutdown()
    }
}
