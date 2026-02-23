package com.innosage.androidagentictemplate

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VADProcessorTest {

    private lateinit var vadProcessor: VADProcessor
    private val threshold = 800.0
    private val hangoverMs = 1000L

    @Before
    fun setup() {
        vadProcessor = VADProcessor(threshold, hangoverMs)
    }

    @Test
    fun `isVoiced returns false for zeroed-out array (silence)`() {
        val silence = ByteArray(1024) { 0 }
        assertFalse("Silence should not be voiced", vadProcessor.isVoiced(silence))
    }

    @Test
    fun `isVoiced returns true for high-amplitude array (speech)`() {
        // Create a 1000Hz sine wave with high amplitude
        val speech = ByteArray(1024)
        for (i in 0 until 512) {
            val value = (3000 * kotlin.math.sin(2.0 * kotlin.math.PI * i / 16.0)).toInt()
            speech[i * 2] = (value and 0xFF).toByte()
            speech[i * 2 + 1] = (value shr 8 and 0xFF).toByte()
        }
        assertTrue("High amplitude signal should be voiced", vadProcessor.isVoiced(speech))
    }

    @Test
    fun `isVoiced handles borderline noise based on threshold`() {
        // Below threshold (RMS ~ 400)
        val lowNoise = ShortArray(1024) { 400 }
        assertFalse("Low noise (below threshold) should not be voiced", vadProcessor.isVoiced(lowNoise, lowNoise.size))

        // Above threshold (RMS ~ 1200)
        val highNoise = ShortArray(1024) { 1200 }
        assertTrue("High noise (above threshold) should be voiced", vadProcessor.isVoiced(highNoise, highNoise.size))
    }

    @Test
    fun `shouldRecord implements hangover logic correctly`() {
        val startTime = 1000L
        
        // 1. Initial state (silence)
        assertFalse("Should not record initially with silence", vadProcessor.shouldRecord(false, startTime))
        
        // 2. Speech detected
        assertTrue("Should record when speech is detected", vadProcessor.shouldRecord(true, startTime + 100))
        
        // 3. Speech stops, but within hangover period (500ms later)
        assertTrue("Should continue recording during hangover period", vadProcessor.shouldRecord(false, startTime + 600))
        
        // 4. Still within hangover period (999ms after last speech)
        assertTrue("Should continue recording just before hangover ends", vadProcessor.shouldRecord(false, startTime + 100 + hangoverMs - 1))
        
        // 5. Beyond hangover period (1001ms after last speech)
        assertFalse("Should stop recording after hangover period", vadProcessor.shouldRecord(false, startTime + 100 + hangoverMs + 1))
    }

    @Test
    fun `reset clears hangover state`() {
        val now = 1000L
        vadProcessor.shouldRecord(true, now)
        assertTrue("Should record during hangover", vadProcessor.shouldRecord(false, now + 500))
        
        vadProcessor.reset()
        assertFalse("Should not record after reset even if within old hangover period", vadProcessor.shouldRecord(false, now + 500))
    }
}
