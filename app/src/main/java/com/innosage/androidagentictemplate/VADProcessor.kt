package com.innosage.androidagentictemplate

import kotlin.math.sqrt

/**
 * Voice Activity Detection Processor.
 * Handles RMS-based speech detection and hangover logic.
 */
class VADProcessor(
    private val threshold: Double = 800.0,
    private val hangoverMs: Long = 1000L
) {
    private var lastVoiceTime = 0L

    /**
     * Determines if the given PCM 16-bit buffer contains speech based on RMS threshold.
     */
    fun isVoiced(buffer: ShortArray, size: Int): Boolean {
        if (size <= 0) return false
        var sum = 0.0
        for (i in 0 until size) {
            sum += buffer[i].toDouble() * buffer[i]
        }
        val rms = sqrt(sum / size)
        return rms > threshold
    }

    /**
     * Determines if the given ByteArray (PCM 16-bit Little Endian) contains speech.
     */
    fun isVoiced(byteBuffer: ByteArray): Boolean {
        val shortBuffer = ShortArray(byteBuffer.size / 2)
        for (i in shortBuffer.indices) {
            val low = byteBuffer[i * 2].toInt() and 0xFF
            val high = byteBuffer[i * 2 + 1].toInt()
            shortBuffer[i] = ((high shl 8) or low).toShort()
        }
        return isVoiced(shortBuffer, shortBuffer.size)
    }

    /**
     * Determines if the current frame should be recorded based on current voice status
     * and the hangover period.
     */
    fun shouldRecord(voiced: Boolean, nowMs: Long): Boolean {
        if (voiced) {
            lastVoiceTime = nowMs
            return true
        }
        return (nowMs - lastVoiceTime) < hangoverMs
    }

    /**
     * Resets the internal state (e.g. last voice time).
     */
    fun reset() {
        lastVoiceTime = 0L
    }
}
