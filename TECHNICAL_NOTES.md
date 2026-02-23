# Technical Notes: Phase 3 Multilingual Transcription

## Model Selection
- **Model:** `whisper-small.q8_0.bin` (Quantized GGUF/GGML).
- **Size:** ~244 MB.
- **RAM Usage:** ~450-500 MB peak.
- **Performance:** Excellent for English and Mandarin. Acceptable for Cantonese (Standard Written Chinese output).

## Language Logic (Code-Switching Support)
The system must handle English, Mandarin, and Cantonese seamlessly.

1. **Auto-Detection (Default):**
   - Whisper.cpp `whisper_full_params` set to `language = "auto"`.
   - The engine performs language identification (LangID) on the first 30s of audio (or the whole utterance if shorter).
   - *Pros:* Hands-free.
   - *Cons:* Higher latency for the first segment; potential for wrong detection on very short clips (<2s).

2. **Manual Override:**
   - User can lock the language to "English" or "Chinese" (Mandarin/Cantonese).
   - Locking to "Chinese" (`zh`) is recommended for code-switching environments. Whisper's `zh` model handles embedded English tokens better than the `en` model handles Chinese.

3. **Cantonese Specifics:**
   - While Whisper supports `yue` (Cantonese) as a language code in some versions, the standard `zh` model with Traditional Chinese script output is the most stable for general use.
   - We will implement a "Script" toggle: Simplified vs. Traditional Chinese.

## JNI Implementation Details
- Use `libwhisper.so` compiled with OpenCL or NNAPI support for Android.
- Buffer management: Since Phase 2 provides PCM chunks via VAD, we should use a `TranscriptionQueue` to process utterances in the background without blocking the `AudioRecordService`.
