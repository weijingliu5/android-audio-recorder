package com.innosage.androidagentictemplate

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StorageEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCleanupDeletesOldFiles() {
        val storageEngine = StorageEngine(tempFolder.root)
        
        // Create 3 mock files
        val oldFile = File(tempFolder.root, "rec_old.3gp").apply { 
            createNewFile()
            setLastModified(System.currentTimeMillis() - 1000 * 60 * 60 * 49) // 49 hours ago
        }
        val newFile = File(tempFolder.root, "rec_new.3gp").apply { 
            createNewFile()
            setLastModified(System.currentTimeMillis()) 
        }
        
        // Threshold: 48 hours (48 * 60 * 60 * 1000 ms)
        val maxAge = 1000L * 60 * 60 * 48
        
        val deletedCount = storageEngine.cleanup(maxAge)
        
        assertEquals(1, deletedCount)
        assertFalse(oldFile.exists())
        assertTrue(newFile.exists())
    }

    @Test
    fun testGetNextChunkFileGeneratesCorrectPattern() {
        val storageEngine = StorageEngine(tempFolder.root)
        val file = storageEngine.getNextChunkFile()
        
        assertTrue(file.name.startsWith("rec_"))
        assertTrue(file.name.endsWith(".3gp"))
        assertEquals(tempFolder.root, file.parentFile)
    }

    @Test
    fun testGetRecordingFilesReturnsSortedFiles() {
        val storageEngine = StorageEngine(tempFolder.root)
        
        val file1 = File(tempFolder.root, "rec_1.3gp").apply { createNewFile(); setLastModified(1000) }
        val file2 = File(tempFolder.root, "rec_2.3gp").apply { createNewFile(); setLastModified(2000) }
        
        val files = storageEngine.getRecordingFiles()
        
        assertEquals(2, files.size)
        assertEquals("rec_2.3gp", files[0].name) // Descending order
    }
}
