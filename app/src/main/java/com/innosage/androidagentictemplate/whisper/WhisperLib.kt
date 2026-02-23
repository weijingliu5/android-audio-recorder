package com.innosage.androidagentictemplate.whisper

import android.util.Log

class WhisperLib {
    companion object {
        private const val TAG = "WhisperLib"
        
        init {
            try {
                // Check if we are in a test environment or if the library exists
                // For now, we wrap in try-catch to allow the app to launch even if .so is missing
                System.loadLibrary("whisper")
                Log.i(TAG, "Loaded whisper native library")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native whisper library not found. Transcription will be disabled.", e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load whisper native library", e)
            }
        }
    }

    /**
     * Initializes the Whisper context with the provided model path.
     * @param modelPath Absolute path to the .bin model file.
     * @return A pointer (long) to the whisper_context, or 0 if failed.
     */
    external fun initContext(modelPath: String): Long

    /**
     * Frees the Whisper context.
     * @param contextPtr The pointer returned by initContext.
     */
    external fun freeContext(contextPtr: Long)

    /**
     * Transcribes audio data.
     * @param contextPtr The pointer to the whisper_context.
     * @param audioData Float array of PCM audio (16kHz, mono).
     * @return 0 on success, non-zero on error.
     */
    external fun fullTranscribe(contextPtr: Long, audioData: FloatArray): Int

    /**
     * Returns the number of text segments from the last transcription.
     */
    external fun getTextSegmentCount(contextPtr: Long): Int

    /**
     * Returns a specific text segment.
     */
    external fun getTextSegment(contextPtr: Long, index: Int): String

    /**
     * Returns system information (AVX, NEON, etc).
     */
    external fun getSystemInfo(): String
}
