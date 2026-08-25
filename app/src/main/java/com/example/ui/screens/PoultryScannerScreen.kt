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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
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
                            text = "Record 5 seconds of coop audio to detect respiratory disease via HuggingFace ML.",
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
                            // Manual stop triggered
                            isRecording = false
                        } else {
                            coroutineScope.launch {
                                isRecording = true
                                resultLabel = null
                                // 1. Start Recording
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
                                val result = uploadAudioForAnalysis(audioFile)
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
                    isRecording -> "Recording... (Keep phone near flock)"
                    isAnalyzing -> "Sending to ML Server..."
                    else -> "Tap to Listen to Flock"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isRecording) AlertRed else Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Results UI
            AnimatedVisibility(visible = resultLabel != null) {
                val isHealthy = resultLabel == "Healthy"
                val bgColor = if (isHealthy) SecondaryContainerGreen else AlertRedContainer
                val iconColor = if (isHealthy) PrimaryGreen else AlertRed
                
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
                            imageVector = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analysis: ${resultLabel?.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = iconColor
                        )
                        Text(
                            text = "Confidence: ${(resultConfidence * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!isHealthy) {
                            Text(
                                "Respiratory distress / coughing detected in the audio sample. Isolate affected birds and consult a vet.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                "Flock vocalizations sound normal. No widespread respiratory issues detected.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Audio Recording Logic ──
private var mediaRecorder: MediaRecorder? = null

private fun startRecording(context: Context): File {
    val outputFile = File(context.cacheDir, "poultry_audio.mp4")
    mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(outputFile.absolutePath)
        try {
            prepare()
            start()
        } catch (e: IOException) {
            Log.e("AudioRecord", "prepare() failed")
        }
    }
    return outputFile
}

private fun stopRecording() {
    mediaRecorder?.apply {
        stop()
        release()
    }
    mediaRecorder = null
}

// ── API Call to Local Server ──
private suspend fun uploadAudioForAnalysis(file: File): Pair<String, Float> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    
    // Using Localtunnel to completely bypass Android localhost/Wi-Fi/cleartext issues
    // The Python server is securely exposed via this public HTTPS URL.
    val url = "https://kisan-ai-audio.loca.lt/predict"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "audio",
            file.name,
            file.asRequestBody("audio/mp4".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url(url)
        .header("Bypass-Tunnel-Reminder", "true")
        .post(requestBody)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            val responseData = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                val errorMsg = try { JSONObject(responseData).getString("error") } catch (e: Exception) { "HTTP ${response.code}" }
                return@withContext Pair("ERR: $errorMsg".take(50), 0.0f)
            }

            val jsonObject = JSONObject(responseData)
            
            val label = jsonObject.getString("label")
            val confidence = jsonObject.getDouble("confidence").toFloat()
            
            Pair(label, confidence)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("ERR: ${e.message?.take(30)}", 0.0f)
    }
}
