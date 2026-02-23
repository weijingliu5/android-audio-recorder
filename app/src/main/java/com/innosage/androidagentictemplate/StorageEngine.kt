package com.innosage.androidagentictemplate

import java.io.File
import java.util.*

/**
 * Manages audio file storage, chunking, and cleanup.
 */
class StorageEngine(private val baseDir: File) {

    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    /**
     * Generates a new file path for a 10-minute recording chunk.
     */
    fun getNextChunkFile(): File {
        val timestamp = System.currentTimeMillis()
        return File(baseDir, "rec_$timestamp.pcm")
    }

    /**
     * Deletes files older than the specified threshold.
     * @param maxAgeMillis Maximum age of files in milliseconds.
     */
    fun cleanup(maxAgeMillis: Long): Int {
        val now = System.currentTimeMillis()
        val files = baseDir.listFiles { file ->
            file.isFile && file.name.startsWith("rec_") && (now - file.lastModified() > maxAgeMillis)
        }
        var deletedCount = 0
        files?.forEach {
            if (it.delete()) deletedCount++
        }
        return deletedCount
    }

    /**
     * Returns all recording files sorted by timestamp.
     */
    fun getRecordingFiles(): List<File> {
        return baseDir.listFiles { file ->
            file.isFile && file.name.startsWith("rec_")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
