# InnoSage 24/7 Recorder Execution Plan

This document outlines the sequential phases to transform the `android-audio-recorder` into a robust, 24/7 autonomous context-capture engine.

---

## 🏗️ Phase 1: Robust Background Infrastructure (Core)
*Goal: Ensure the recorder can survive Android's lifecycle management and record indefinitely.*

1.  **Foreground Service Implementation:** 
    *   Migrate recording logic from `MainActivity` to an `AudioRecordService`.
    *   Implement a persistent notification (required by Android for 24/7 background MIC access).
    *   Handle `START_STICKY` to ensure the service restarts if killed by the OS.
2.  **Storage Engine (Rolling Buffer):**
    *   Implement a 1-hour chunking strategy (saving audio in 10-minute files).
    *   Add a "Disk Cleaner" that auto-deletes files older than 24-48 hours.
3.  **Wakelock & Battery Optimization:**
    *   Manage `PowerManager.WakeLock` and `WifiLock` for consistent performance when the screen is off.

## 🧠 Phase 2: Local Intelligence (VAD & Trim)
*Goal: Reduce storage/compute waste by ignoring silence and noise.*

1.  **On-Device VAD (Voice Activity Detection):**
    *   **Completed**: Integrated a lightweight Energy-based VAD (RMS thresholding) into `AudioRecordService`.
    *   **Completed**: Migrated from `MediaRecorder` to `AudioRecord` for raw PCM capture and processing.
    *   **Completed**: Logic implemented to skip writing PCM data to disk during periods of silence (<800 RMS) with a 1s hangover period to prevent clipping speech.
2.  **Smart Trim:**
    *   **Completed**: Leading/trailing silence is effectively trimmed by the real-time VAD during recording.

## 📝 Phase 3: Downstream Context Processing (The "Brain")
*Goal: Convert raw audio into actionable data.*

1.  **Local Transcription Gate:**
    *   **Choice:** **Whisper.cpp (via JNI)** for its superior optimization and quantization support.
    *   **Model:** `whisper-small.q8_0.bin` (~244MB, 8-bit quantized).
        *   *Why:* While `base` is faster, `small` is the minimum requirement for acceptable accuracy in Mandarin and Cantonese.
        *   *Multilingual:* Supports English, Mandarin, and Cantonese (via `zh` language code).
    *   **Mechanism:** UTTERANCE-BASED trigger.
        *   VAD-active segments are passed to the engine.
        *   **Language Logic:** Implement "Smart Auto-Detection" with a manual override. For code-switching (English/Chinese), the model will be initialized in multilingual mode.
    *   **Database:** Store results in a local SQLite/Room DB (`transcripts` table: id, timestamp, text, audio_ref, language_detected).
2.  **Metadata Layer:**
    *   Extract environment metadata (ambient noise level, location, timestamp) to tag recordings.
3.  **Integration with Alpha:**
    *   Expose a secure API for the Alpha agent to "query" recent context (e.g., "What was discussed in my 2 PM meeting?").

## 🛡️ Phase 4: Privacy & Hardening (Final Polish)
*Goal: User trust and extreme reliability.*

1.  **Encryption at Rest:**
    *   Encrypt audio chunks on the device before they are processed or uploaded.
2.  **Privacy UI:**
    *   Add a "Mute" toggle and "Delete last 15 minutes" quick-action to the notification.
3.  **Final Gate Integration:**
    *   Update `gate.sh` with stress tests for 24h+ continuous execution in the emulator.

---
**Current Status:** 🟡 Phase 3 (In Progress: Local Transcription Gate)
**Next Step:** Phase 3.1: Database Integration (Room/SQLite)
