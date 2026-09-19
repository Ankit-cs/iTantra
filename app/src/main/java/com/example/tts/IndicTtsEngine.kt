package com.example.tts

import android.content.Context
import android.util.Log
import com.example.audio.AlertAudioManager
import com.example.model.SupportedLanguage
import com.example.model.ModelDownloadManager
import com.example.model.ModelPack
import com.example.model.ModelRegistry
import com.k2fsa.sherpa.onnx.OfflineTts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer

class IndicTtsEngine(
    private val context: Context,
    private val alertAudioManager: AlertAudioManager,
    private val scope: CoroutineScope,
    private val modelDownloadManager: ModelDownloadManager
) : TtsEngine {
    private val TAG = "IndicTtsEngine"
    private var tts: OfflineTts? = null
    private var loadedLanguageCode: String? = null
    private var job: Job? = null

    private val _modelInfo = MutableStateFlow(TtsModelInfo())
    override val modelInfo: StateFlow<TtsModelInfo> = _modelInfo
    
    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking
    
    private val _currentlyPlayingText = MutableStateFlow<String?>(null)
    override val currentlyPlayingText: StateFlow<String?> = _currentlyPlayingText

    fun loadModels(language: SupportedLanguage) {
        if (loadedLanguageCode == language.code && tts != null) {
            return
        }

        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            try {
                val start = System.nanoTime()

                val ttsPack = when (language.code) {
                    "hi" -> ModelPack.TTS_HINDI
                    "ml" -> ModelPack.TTS_MALAYALAM
                    "bn" -> ModelPack.TTS_BENGALI
                    "gu" -> ModelPack.TTS_GUJARATI
                    "en" -> ModelPack.TTS_ENGLISH
                    else -> {
                        Log.w(TAG, "No TTS voice pack available for '${language.code}'")
                        _modelInfo.value = _modelInfo.value.copy(name = "Unsupported Language", isReady = false)
                        return@launch
                    }
                }

                val voiceDirName = ModelRegistry.getInfo(ttsPack)?.extractDirName ?: return@launch
                val espeakDirName = ModelRegistry.getInfo(ModelPack.ESPEAK_NG_DATA)?.extractDirName ?: return@launch
                
                val voiceDir = File(context.filesDir, "models/$voiceDirName")
                val espeakDataDir = File(context.filesDir, "models/$espeakDirName")
                
                if (!voiceDir.isDirectory || !espeakDataDir.isDirectory) {
                    Log.w(TAG, "TTS models for '${language.code}' not downloaded yet.")
                    _modelInfo.value = _modelInfo.value.copy(name = "Not Downloaded", isReady = false)
                    return@launch
                }

                val onnxFile = voiceDir.listFiles { f -> f.extension == "onnx" }?.firstOrNull() ?: return@launch
                val tokensFile = File(voiceDir, "tokens.txt")

                val config = com.k2fsa.sherpa.onnx.OfflineTtsConfig(
                    model = com.k2fsa.sherpa.onnx.OfflineTtsModelConfig(
                        vits = com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig(
                            model = onnxFile.absolutePath,
                            tokens = tokensFile.absolutePath,
                            dataDir = espeakDataDir.absolutePath
                        ),
                        numThreads = 2,
                        debug = false,
                        provider = "cpu"
                    )
                )

                val newTts = OfflineTts(assetManager = null, config = config)
                
                tts?.release()
                tts = newTts
                loadedLanguageCode = language.code

                val ttsInfo = ModelRegistry.getInfo(ttsPack)
                _modelInfo.value = TtsModelInfo(
                    name = ttsPack.displayName,
                    runtime = "sherpa-onnx (VITS)",
                    modelSizeMb = (ttsInfo?.sizeBytes ?: 0L) / 1_000_000f,
                    sampleRateHz = newTts.sampleRate(),
                    isReady = true,
                )
                Log.i(TAG, "Loaded TTS for '${language.code}' in ${(System.nanoTime() - start)/1000000}ms")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load TTS models for '${language.code}'", e)
                _modelInfo.value = _modelInfo.value.copy(name = "Failed to load model: ${e.message}", isReady = false)
            }
        }
    }

    private fun cleanText(text: String): String {
        var t = Normalizer.normalize(text, Normalizer.Form.NFC)
        t = t.lowercase()
        t = t.replace(";", ",").replace("-", " ").replace(":", ",")
        t = t.replace(Regex("[<>()\\[\\]\"]+"), "")
        t = t.replace(Regex("\\s+"), " ").trim()
        return t
    }

    private suspend fun generate(text: String, isAlert: Boolean): FloatArray? = withContext(Dispatchers.Default) {
        val currentTts = tts
        if (currentTts == null) {
            Log.w(TAG, "TTS not loaded for generate()")
            return@withContext null
        }

        try {
            val startMs = System.currentTimeMillis()
            val speed = if (isAlert) 1.25f else 1.0f
            val audio = currentTts.generate(text = cleanText(text), sid = 0, speed = speed)
            val synthesisMs = System.currentTimeMillis() - startMs

            Log.d(TAG, "sherpa-onnx TTS synthesized ${audio.samples.size} samples @ ${audio.sampleRate}Hz in ${synthesisMs}ms")
            
            resampleTo22050(audio.samples, audio.sampleRate)
        } catch (e: Exception) {
            Log.e(TAG, "TTS synthesis failed", e)
            null
        }
    }

    override fun speak(text: String, language: SupportedLanguage, isAlert: Boolean, onDone: () -> Unit) {
        if (text.isBlank()) {
            onDone()
            return
        }
        _isSpeaking.value = true
        _currentlyPlayingText.value = text

        scope.launch {
            loadModels(language)
            val audio = generate(text, isAlert)
            if (audio != null) {
                alertAudioManager.playTts(audio, onDone)
            } else {
                onDone()
            }
            _isSpeaking.value = false
            _currentlyPlayingText.value = null
        }
    }

    override fun preload(language: SupportedLanguage) {
        loadModels(language)
    }

    override fun stop() {
        alertAudioManager.stopTts()
        _isSpeaking.value = false
        _currentlyPlayingText.value = null
    }

    override fun shutdown() {
        stop()
        release()
    }

    private fun resampleTo22050(waveform: FloatArray, sourceSampleRate: Int): FloatArray {
        val targetRate = 22050
        if (sourceSampleRate == targetRate) return waveform
        val ratio = targetRate.toDouble() / sourceSampleRate
        val outputLength = (waveform.size * ratio).toInt()
        val resampled = FloatArray(outputLength)

        for (i in resampled.indices) {
            val srcIdx = i / ratio
            val floor = srcIdx.toInt()
            val frac = (srcIdx - floor).toFloat()

            resampled[i] = if (floor + 1 < waveform.size) {
                waveform[floor] * (1f - frac) + waveform[floor + 1] * frac
            } else {
                waveform.getOrElse(floor) { 0f }
            }
        }
        return resampled
    }

    fun release() {
        tts?.release()
        tts = null
    }
}
