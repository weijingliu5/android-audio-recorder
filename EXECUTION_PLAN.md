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
    *   Integrate a lightweight VAD (e.g., Silero or WebRTC VAD) to pause file writing during silence.
    *   Benchmark battery impact vs. raw recording.
2.  **Smart Trim:**
    *   Post-process raw chunks to strip leading/trailing silence before they are queued for transcription.

## 📝 Phase 3: Downstream Context Processing (The "Brain")
*Goal: Convert raw audio into actionable data.*

1.  **Local Transcription Gate:**
    *   Evaluate on-device Whisper (Whisper.cpp / MediaPipe) for privacy-first transcription.
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
**Current Status:** 🟢 Phase 2 (Completed: On-Device VAD & PCM Capture)
**Next Step:** Phase 3: Downstream Context Processing (Transcription & Brain)
