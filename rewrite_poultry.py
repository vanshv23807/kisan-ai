import re

with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'r') as f:
    content = f.read()

# 1. Update imports
imports_target = "import okhttp3.*"
imports_new = """import android.media.AudioFormat
import android.media.AudioRecord
import com.litongjava.jlibrosa.JLibrosa
import org.pytorch.LiteModuleLoader"""

content = content.replace(imports_target, imports_new)
content = content.replace("import okhttp3.MediaType.Companion.toMediaTypeOrNull\n", "")
content = content.replace("import okhttp3.RequestBody.Companion.asRequestBody\n", "")

# 2. Update record button logic
record_logic_target = """                                // 1. Start Recording
                                val audioFile = startRecording(context)
                                
                                // 2. Wait up to 45 seconds, checking if user stopped it manually
                                var timeElapsed = 0
                                while (isRecording && timeElapsed < 45000) {
                                    delay(100)
                                    timeElapsed += 100
                                }
                                
                                // 3. Stop Recording
                                isRecording = false
                                stopRecording()
                                isAnalyzing = true
                                
                                // 4. Send to Local Flask Server
                                val result = uploadAudioForAnalysis(audioFile)"""

record_logic_new = """                                // 1. Start Recording (1.5 seconds)
                                // We don't need a loop, it records a fixed chunk synchronously in the background
                                val audioData = recordAudioLocally(context)
                                
                                isRecording = false
                                isAnalyzing = true
                                
                                // 2. Analyze via JLibrosa & PyTorch Mobile
                                val result = analyzeAudioLocally(context, audioData)"""
content = content.replace(record_logic_target, record_logic_new)

# 3. Replace the entire bottom section (from // ── Audio Recording Logic ── to end of file)
bottom_idx = content.find("// ── Audio Recording Logic ──")
bottom_new = """// ── Audio Recording Logic ──
private const val SAMPLE_RATE = 22050
private const val REQUIRED_SAMPLES = 33075 // 1.5 seconds

private suspend fun recordAudioLocally(context: Context): FloatArray = withContext(Dispatchers.IO) {
    try {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, REQUIRED_SAMPLES * 2)
        
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        
        val audioData = FloatArray(REQUIRED_SAMPLES)
        val shortBuffer = ShortArray(REQUIRED_SAMPLES)
        
        try {
            audioRecord.startRecording()
            var readCount = 0
            while (readCount < REQUIRED_SAMPLES) {
                val result = audioRecord.read(shortBuffer, readCount, REQUIRED_SAMPLES - readCount)
                if (result < 0) break
                readCount += result
            }
            
            // Convert Short PCM to Float (-1.0 to 1.0)
            for (i in 0 until REQUIRED_SAMPLES) {
                audioData[i] = shortBuffer[i].toFloat() / 32768.0f
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
        
        audioData
    } catch (e: SecurityException) {
        e.printStackTrace()
        FloatArray(REQUIRED_SAMPLES)
    }
}

// ── On-Device ML Inference ──
private suspend fun analyzeAudioLocally(context: Context, audioData: FloatArray): Pair<String, Float> = withContext(Dispatchers.IO) {
    try {
        // 1. Generate Mel Spectrogram using JLibrosa
        val jLibrosa = JLibrosa()
        val n_fft = 2648
        val n_mels = 128
        val hop_length = 256
        val melSpec = jLibrosa.generateMelSpectroGram(audioData, SAMPLE_RATE, n_fft, n_mels, hop_length)
        
        // 2. Power to dB (Librosa style)
        var maxVal = 1e-10f
        for (row in melSpec) {
            for (v in row) {
                if (v > maxVal) maxVal = v
            }
        }
        
        // Expected shape for PyTorch model is (1, 1, 128, 130)
        val rows = 128
        val cols = 130
        val flatData = FloatArray(rows * cols)
        
        for (i in 0 until minOf(melSpec.size, rows)) {
            val rowLength = minOf(melSpec[i].size, cols)
            for (j in 0 until rowLength) {
                val db = 10.0f * kotlin.math.log10((melSpec[i][j] + 1e-10f) / maxVal)
                flatData[i * cols + j] = db
            }
        }
        
        // 3. Load model (using assetFilePath from MastitisScannerScreen.kt)
        val modelPath = assetFilePath(context, "chicken_model_lite.ptl")
        val module = LiteModuleLoader.load(modelPath)
        
        // 4. Create Tensor & Run Inference
        val tensor = org.pytorch.Tensor.fromBlob(flatData, longArrayOf(1, 1, 128, 130))
        val outputTensor = module.forward(org.pytorch.IValue.from(tensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray
        
        // 5. Softmax to get probabilities
        var maxScore = -Float.MAX_VALUE
        var maxIdx = -1
        for (i in 0 until 3) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxIdx = i
            }
        }
        
        var sumExp = 0f
        for (i in 0 until 3) {
            sumExp += kotlin.math.exp((scores[i] - maxScore).toDouble()).toFloat()
        }
        val confidence = kotlin.math.exp((scores[maxIdx] - maxScore).toDouble()).toFloat() / sumExp
        
        val classes = arrayOf("Healthy", "Noise", "Unhealthy")
        val prediction = classes[maxIdx]
        
        Pair(prediction, confidence)
        
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("ERR: ${e.message?.take(30)}", 0f)
    }
}
"""

content = content[:bottom_idx] + bottom_new

with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'w') as f:
    f.write(content)

