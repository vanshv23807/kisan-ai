package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.AudioFormat
import android.media.AudioRecord

import org.pytorch.LiteModuleLoader
import org.json.JSONObject
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoultryScannerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var resultLabel by remember { mutableStateOf<String?>(null) }
    var resultConfidence by remember { mutableFloatStateOf(0f) }
    var hasPermission by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    val maxRecordingSeconds = 45

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Poultry Audio AI",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = InfoBlueContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(InfoBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Cough & Distress Detector",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue
                        )
                        Text(
                            text = "Record up to 45 seconds of coop audio. Tap to stop early. AI analyses the full session for respiratory disease.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Record Button UI
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) AlertRed.copy(alpha = 0.1f) else PrimaryGreen.copy(alpha = 0.1f))
                    .clickable(enabled = !isAnalyzing && hasPermission) {
                        if (isRecording) {
                            // User pressed stop manually before 45s
                            isRecording = false
                        } else {
                            coroutineScope.launch {
                                isRecording = true
                                recordingSeconds = 0
                                resultLabel = null

                                // Launch a live-timer coroutine in parallel
                                val timerJob = launch {
                                    while (isRecording && recordingSeconds < maxRecordingSeconds) {
                                        delay(1000)
                                        recordingSeconds++
                                        if (recordingSeconds >= maxRecordingSeconds) {
                                            isRecording = false
                                        }
                                    }
                                }

                                // Record audio (blocks until isRecording=false or max duration)
                                val audioData = recordAudioWithDuration(context, maxRecordingSeconds) { isRecording }

                                timerJob.cancel()
                                isRecording = false
                                isAnalyzing = true

                                // Analyze by sliding 1.5s windows over the full recording
                                val result = analyzeAudioLocally(context, audioData)
                                resultLabel = result.first
                                resultConfidence = result.second
                                isAnalyzing = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isRecording -> AlertRed
                                isAnalyzing -> WarningYellow
                                else -> PrimaryGreen
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(60.dp))
                    } else {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    !hasPermission -> "Microphone permission required"
                    isRecording -> "Recording... ${recordingSeconds}s / ${maxRecordingSeconds}s  (Tap to stop early)"
                    isAnalyzing -> "Analyzing audio with AI..."
                    else -> "Tap to Listen to Flock"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isRecording) AlertRed else Color.Gray,
                textAlign = TextAlign.Center
            )

            // Progress bar while recording
            if (isRecording) {
                LinearProgressIndicator(
                    progress = { recordingSeconds.toFloat() / maxRecordingSeconds.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = AlertRed,
                    trackColor = AlertRed.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results UI
            AnimatedVisibility(visible = resultLabel != null) {
                val isNoAudio = resultLabel?.startsWith("NO_AUDIO") == true
                val isHealthy = resultLabel == "Healthy"
                val isError = resultLabel?.startsWith("ERR") == true
                
                val debugRms = if (isNoAudio) resultLabel?.substringAfter("NO_AUDIO_") else null

                val bgColor = when {
                    isNoAudio || isError -> Color(0xFFFFF3E0) // orange container
                    isHealthy -> SecondaryContainerGreen
                    else -> AlertRedContainer
                }
                val iconColor = when {
                    isNoAudio || isError -> Color(0xFFE65100) // deep orange
                    isHealthy -> PrimaryGreen
                    else -> AlertRed
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when {
                                isNoAudio || isError -> Icons.Default.Info
                                isHealthy -> Icons.Default.CheckCircle
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when {
                                isNoAudio -> "No Audio Detected"
                                isError -> "Analysis Error"
                                else -> "Analysis: ${resultLabel?.uppercase()}"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = iconColor
                        )
                        if (!isNoAudio && !isError) {
                            Text(
                                text = "Confidence: ${(resultConfidence * 100).toInt()}%",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        } else if (isNoAudio && debugRms != null) {
                            Text(
                                text = "Debug Audio Level (RMS): $debugRms",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = when {
                                isNoAudio -> "No chicken vocalizations detected. Please hold your phone closer to the flock and ensure the birds are vocalizing."
                                isError -> "Something went wrong. Please try again."
                                !isHealthy -> "Respiratory distress / coughing detected in the audio sample. Isolate affected birds and consult a vet."
                                else -> "Flock vocalizations sound normal. No widespread respiratory issues detected."
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Audio Recording Logic ──
private const val SAMPLE_RATE = 22050
private const val WINDOW_SAMPLES = 33075 // 1.5 seconds per window

/**
 * Records audio for up to [maxSeconds] seconds, or until [isStillRecording] returns false.
 * Returns the full raw PCM float array of however much was captured.
 */
private suspend fun recordAudioWithDuration(
    context: Context,
    maxSeconds: Int,
    isStillRecording: () -> Boolean
): FloatArray = withContext(Dispatchers.IO) {
    val maxSamples = SAMPLE_RATE * maxSeconds
    val minBufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val chunkSize = maxOf(minBufferSize, SAMPLE_RATE / 4) // read in 250ms chunks
    val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        chunkSize * 2
    )

    val allSamples = ArrayList<Float>(maxSamples)
    val shortChunk = ShortArray(chunkSize)

    try {
        audioRecord.startRecording()
        while (isStillRecording() && allSamples.size < maxSamples) {
            val read = audioRecord.read(shortChunk, 0, chunkSize)
            if (read <= 0) break
            for (i in 0 until read) {
                allSamples.add(shortChunk[i].toFloat() / 32768.0f)
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    } finally {
        audioRecord.stop()
        audioRecord.release()
    }

    allSamples.toFloatArray()
}

// ── On-Device ML Inference (Sliding Window) ──
private suspend fun analyzeAudioLocally(context: Context, audioData: FloatArray): Pair<String, Float> = withContext(Dispatchers.IO) {
    try {
        // Check overall energy first
        var sumSquares = 0.0
        for (sample in audioData) sumSquares += (sample * sample).toDouble()
        val rms = kotlin.math.sqrt(sumSquares / audioData.size.coerceAtLeast(1)).toFloat()

        if (rms < 0.02f || audioData.size < WINDOW_SAMPLES) {
            val formattedRms = String.format("%.4f", rms)
            return@withContext Pair("NO_AUDIO_$formattedRms", 0f)
        }

        // Load model once
        val modelPath = assetFilePath(context, "chicken_model_lite.ptl")
        val module = LiteModuleLoader.load(modelPath)

        // Slide 1.5s windows with 50% overlap over the full recording
        val stepSize = WINDOW_SAMPLES / 2
        val classCounts = IntArray(3) // Healthy, Noise, Unhealthy
        val classConfidences = FloatArray(3)
        var windowsAnalyzed = 0

        var offset = 0
        while (offset + WINDOW_SAMPLES <= audioData.size) {
            val window = audioData.copyOfRange(offset, offset + WINDOW_SAMPLES)
            offset += stepSize

            // Skip silent windows
            var wSumSq = 0.0
            for (s in window) wSumSq += (s * s).toDouble()
            val wRms = kotlin.math.sqrt(wSumSq / WINDOW_SAMPLES).toFloat()
            if (wRms < 0.02f) continue

            // Run inference on this window
            val tensor = org.pytorch.Tensor.fromBlob(window, longArrayOf(1, WINDOW_SAMPLES.toLong()))
            val outputTensor = module.forward(org.pytorch.IValue.from(tensor)).toTensor()
            val scores = outputTensor.dataAsFloatArray

            // Softmax
            var maxScore = scores[0]
            var maxIdx = 0
            for (i in 1 until 3) {
                if (scores[i] > maxScore) { maxScore = scores[i]; maxIdx = i }
            }
            var sumExp = 0f
            for (i in 0 until 3) sumExp += kotlin.math.exp((scores[i] - maxScore).toDouble()).toFloat()
            val conf = kotlin.math.exp((scores[maxIdx] - maxScore).toDouble()).toFloat() / sumExp

            classCounts[maxIdx]++
            classConfidences[maxIdx] += conf
            windowsAnalyzed++
        }

        if (windowsAnalyzed == 0) {
            val formattedRms = String.format("%.4f", rms)
            return@withContext Pair("NO_AUDIO_$formattedRms", 0f)
        }

        // Majority vote — if ANY window says Unhealthy, prioritise it
        val finalIdx = when {
            classCounts[2] > 0 -> 2 // Unhealthy wins if detected at all
            classCounts[0] >= classCounts[1] -> 0 // Healthy
            else -> 1 // Noise
        }
        val avgConf = if (classCounts[finalIdx] > 0)
            classConfidences[finalIdx] / classCounts[finalIdx]
        else 0f

        val classes = arrayOf("Healthy", "Noise", "Unhealthy")
        Pair(classes[finalIdx], avgConf)

    } catch (e: Exception) {
        e.printStackTrace()
        Pair("ERR: ${e.message?.take(30)}", 0f)
    }
}
