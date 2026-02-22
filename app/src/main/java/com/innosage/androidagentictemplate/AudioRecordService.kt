package com.innosage.androidagentictemplate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHANNEL_ID = "AudioRecordServiceChannel"
private const val NOTIFICATION_ID = 1
private const val LOG_TAG = "AudioRecordService"
private const val CHUNK_DURATION_MS = 10 * 60 * 1000L // 10 minutes

class AudioRecordService : Service() {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isRecording = false

    private val chunkRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                stopRecording()
                startRecording()
                handler.postDelayed(this, CHUNK_DURATION_MS)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        if (!isRecording) {
            startRecording()
            isRecording = true
            handler.postDelayed(chunkRunnable, CHUNK_DURATION_MS)
        }

        return START_STICKY
    }

    private fun startRecording() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "REC_$timeStamp.3gp"
        val storageDir = getExternalFilesDir(null)
        currentFile = File(storageDir, fileName)

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(currentFile?.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed: ${e.message}")
            }

            start()
        }
        Log.d(LOG_TAG, "Started recording: ${currentFile?.absolutePath}")
        
        // Rolling Clean: delete files older than 24h
        cleanOldFiles()
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "stop() failed: ${e.message}")
            }
            release()
        }
        recorder = null
        Log.d(LOG_TAG, "Stopped recording: ${currentFile?.absolutePath}")
    }

    private fun cleanOldFiles() {
        val storageDir = getExternalFilesDir(null)
        val files = storageDir?.listFiles { file -> file.name.startsWith("REC_") }
        val now = System.currentTimeMillis()
        val limit = 24 * 60 * 60 * 1000L // 24 hours

        files?.forEach { file ->
            if (now - file.lastModified() > limit) {
                if (file.delete()) {
                    Log.d(LOG_TAG, "Deleted old file: ${file.name}")
                }
            }
        }
    }

    override fun onDestroy() {
        isRecording = false
        handler.removeCallbacks(chunkRunnable)
        stopRecording()
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
            .setContentText("Recording in progress...")
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
