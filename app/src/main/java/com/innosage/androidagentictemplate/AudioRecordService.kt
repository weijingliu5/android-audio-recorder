package com.innosage.androidagentictemplate

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

private const val CHANNEL_ID = "AudioRecordServiceChannel"
private const val NOTIFICATION_ID = 1
private const val LOG_TAG = "AudioRecordService"
private const val CHUNK_DURATION_MS = 10 * 60 * 1000L // 10 minutes
private const val SAMPLE_RATE = 16000
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
private const val VAD_THRESHOLD = 800.0 // Adjusted for typical room noise
private const val HANGOVER_MS = 1000L // Keep recording for 1s after voice stops

class AudioRecordService : Service() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var storageEngine: StorageEngine
    private lateinit var transcriptionEngine: TranscriptionEngine
    private val vadProcessor = VADProcessor(VAD_THRESHOLD, HANGOVER_MS)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        storageEngine = StorageEngine(getExternalFilesDir(null)!!)
        
        transcriptionEngine = TranscriptionEngine(this)
        // Assume model is in files directory. In real app, we'd check/download.
        val modelFile = File(getExternalFilesDir(null), "whisper-small.q8_0.bin")
        transcriptionEngine.initialize(modelFile.absolutePath)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        if (!isRecording.get()) {
            startRecording()
        }

        return START_STICKY
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "Missing RECORD_AUDIO permission")
            stopSelf()
            return
        }
        
        isRecording.set(true)
        recordingThread = Thread {
            recordLoop()
        }.apply { start() }
        Log.d(LOG_TAG, "Started VAD-enabled recording thread")
    }

    @SuppressLint("MissingPermission")
    private fun recordLoop() {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = Math.max(minBufferSize, 2048)
        val audioBuffer = ShortArray(bufferSize)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            vadProcessor.reset()

            var currentFile: File = storageEngine.getNextChunkFile()
            var outputStream = FileOutputStream(currentFile)
            var lastChunkStartTime = System.currentTimeMillis()

            Log.d(LOG_TAG, "Recording to: ${currentFile.absolutePath}")

            while (isRecording.get()) {
                val readResult = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                if (readResult > 0) {
                    val voiced = vadProcessor.isVoiced(audioBuffer, readResult)
                    val now = System.currentTimeMillis()

                    // Write to disk if voiced or within hangover period
                    if (vadProcessor.shouldRecord(voiced, now)) {
                        val byteBuffer = java.nio.ByteBuffer.allocate(readResult * 2)
                        byteBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until readResult) {
                            byteBuffer.putShort(audioBuffer[i])
                        }
                        outputStream.write(byteBuffer.array())
                    }

                    // Check for chunk rotation
                    if (now - lastChunkStartTime > CHUNK_DURATION_MS) {
                        outputStream.close()
                        
                        // Hand off completed chunk for transcription
                        val completedChunk = currentFile
                        transcriptionEngine.transcribeChunk(completedChunk) { text ->
                            // TODO: Store in Database (Phase 3.1)
                            Log.i(LOG_TAG, "CHUNK TRANSCRIPT: $text")
                        }

                        // Clean old files before starting new chunk
                        storageEngine.cleanup(24 * 60 * 60 * 1000L)
                        
                        currentFile = storageEngine.getNextChunkFile()
                        outputStream = FileOutputStream(currentFile)
                        lastChunkStartTime = now
                        Log.d(LOG_TAG, "Rotated to new chunk: ${currentFile.absolutePath}")
                    }
                }
            }

            outputStream.close()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error in recordLoop: ${e.message}")
        } finally {
            try {
                audioRecord?.stop()
            } catch (e: Exception) {}
            audioRecord?.release()
            audioRecord = null
        }
    }

    private fun stopRecording() {
        isRecording.set(false)
        recordingThread?.join(1000)
        recordingThread = null
        Log.d(LOG_TAG, "Stopped recording thread")
    }

    override fun onDestroy() {
        stopRecording()
        transcriptionEngine.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, AudioRecordService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("InnoSage Recorder")
            .setContentText("VAD Recording active...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Service", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Audio Record Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
