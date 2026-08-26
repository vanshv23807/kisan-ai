package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KisanTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import com.example.R

data class BreachDetails(val cowInfo: String, val camName: String)

@Composable
fun VirtualFencingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var isMonitoring by remember { mutableStateOf(false) }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    
    // Store fences for all 3 cameras — each camera gets its own independent fence
    val fences = remember { mutableStateMapOf<Int, Pair<Offset, Offset>>() }
    var currentFenceStart by remember { mutableStateOf<Offset?>(null) }
    
    var breachPopup by remember { mutableStateOf<BreachDetails?>(null) }
    
    var selectedVideoId by remember { mutableStateOf(R.raw.clip_01) }

    // Mock cow database for hackathon demo
    val mockCows = listOf("Bella (ID: TAG-492)", "Bessie (ID: TAG-102)", "Daisy (ID: TAG-833)", "Luna (ID: TAG-771)")

    val allFencesDrawn = fences.size == 3

    // Initialize TFLite Object Detector
    val detector = remember {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .build()
        try {
            ObjectDetector.createFromFileAndOptions(context, "mobilenet_ssd.tflite", options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Load first frame when camera changes so user can see where to draw the fence
    LaunchedEffect(selectedVideoId) {
        currentFenceStart = null
        detections = emptyList()
        
        // Always load a still preview frame for this camera
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                val uri = Uri.parse("android.resource://${context.packageName}/$selectedVideoId")
                retriever.setDataSource(context, uri)
                val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                
                // Run an initial detection on the preview frame so it's visible immediately!
                var initialDetections: List<Detection> = emptyList()
                if (frame != null && detector != null) {
                    val smallBmp = Bitmap.createScaledBitmap(frame, 300, 300, false)
                    val tensorImage = TensorImage.fromBitmap(smallBmp)
                    val results = detector.detect(tensorImage)
                    
                    val scaleBackX = frame.width / 300f
                    val scaleBackY = frame.height / 300f
                    initialDetections = results?.map { det ->
                        val origBox = det.boundingBox
                        origBox.left = origBox.left * scaleBackX
                        origBox.top = origBox.top * scaleBackY
                        origBox.right = origBox.right * scaleBackX
                        origBox.bottom = origBox.bottom * scaleBackY
                        det
                    } ?: emptyList()
                    smallBmp.recycle()
                }

                withContext(Dispatchers.Main) {
                    currentFrame = frame
                    detections = initialDetections
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }
        }
    }

    // Playback and Inference Loop — ONLY runs when isMonitoring AND current camera has a fence
    val hasFenceOnCurrentCam = fences[selectedVideoId] != null
    LaunchedEffect(isMonitoring, selectedVideoId, hasFenceOnCurrentCam) {
        if (isMonitoring && detector != null && hasFenceOnCurrentCam) {
            withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    val uri = Uri.parse("android.resource://${context.packageName}/$selectedVideoId")
                    retriever.setDataSource(context, uri)
                    
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = (durationStr?.toLongOrNull() ?: 10000L).coerceAtLeast(1000L)
                    
                    var timeUs = 0L
                    val stepUs = 300_000L
                    var frameCount = 0
                    var cachedDetections: List<Detection> = emptyList()
                    
                    while (isMonitoring) {
                        val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                        if (frame != null) {
                            if (frameCount % 3 == 0) {
                                val smallBmp = Bitmap.createScaledBitmap(frame, 300, 300, false)
                                val tensorImage = TensorImage.fromBitmap(smallBmp)
                                val results = detector.detect(tensorImage)
                                
                                val scaleBackX = frame.width / 300f
                                val scaleBackY = frame.height / 300f
                                cachedDetections = results?.map { det ->
                                    val origBox = det.boundingBox
                                    origBox.left = origBox.left * scaleBackX
                                    origBox.top = origBox.top * scaleBackY
                                    origBox.right = origBox.right * scaleBackX
                                    origBox.bottom = origBox.bottom * scaleBackY
                                    det
                                } ?: emptyList()
                                smallBmp.recycle()
                            }
                            
                            withContext(Dispatchers.Main) {
                                currentFrame = frame
                                detections = cachedDetections
                            }
                        } else {
                            timeUs = 0L
                            continue
                        }
                        
                        frameCount++
                        timeUs += stepUs
                        if (timeUs >= durationMs * 1000) {
                            timeUs = 0L
                        }
                        delay(100)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    retriever.release()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            KisanTopAppBar(title = "Virtual Fencing AI", showBackButton = true, onBackClick = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (fences[selectedVideoId] == null) "Tap 2 points on the video to draw a fence line." 
                       else if (!isMonitoring) "Fence set! Press Start Monitoring to begin."
                       else "System armed. AI is monitoring this camera.",
                fontSize = 14.sp,
                color = if (isMonitoring) Color(0xFF2E7D32) else Color.DarkGray,
                fontWeight = if (isMonitoring) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Camera selector tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val cams = listOf(R.raw.clip_01 to "Cam 1", R.raw.clip_02 to "Cam 2", R.raw.clip_03 to "Cam 3")
                cams.forEach { (id, label) ->
                    val hasFence = fences[id] != null
                    val isSelected = selectedVideoId == id
                    OutlinedButton(
                        onClick = { selectedVideoId = id },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.Transparent
                        )
                    ) { 
                        Text(
                            text = if (hasFence) "$label ✅" else "$label ○",
                            color = if (hasFence) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        ) 
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Video / Still Frame Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .pointerInput(isMonitoring) {
                        detectTapGestures { offset ->
                            // Only allow drawing when NOT monitoring
                            if (!isMonitoring && fences[selectedVideoId] == null) {
                                if (currentFenceStart == null) {
                                    currentFenceStart = offset
                                } else {
                                    fences[selectedVideoId] = Pair(currentFenceStart!!, offset)
                                    currentFenceStart = null
                                }
                            }
                        }
                    }
            ) {
                // Video Frame
                currentFrame?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Video Frame",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Overlay Canvas for fence + detections
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = if (currentFrame != null) size.width / currentFrame!!.width else 1f
                    val scaleY = if (currentFrame != null) size.height / currentFrame!!.height else 1f

                    val currentFence = fences[selectedVideoId]

                    // Draw in-progress fence point (first tap)
                    if (currentFenceStart != null) {
                        drawCircle(Color.Red, radius = 12f, center = currentFenceStart!!)
                    }
                    
                    // Draw completed fence line
                    if (currentFence != null) {
                        drawCircle(Color.Red, radius = 12f, center = currentFence.first)
                        drawCircle(Color.Red, radius = 12f, center = currentFence.second)
                        drawLine(
                            color = Color.Red,
                            start = currentFence.first,
                            end = currentFence.second,
                            strokeWidth = 6f
                        )
                    }

                    // Draw Detections (always visible) & Check Crossings (only when monitoring)
                    var breachDetected = false
                    for (det in detections) {
                            val box = det.boundingBox
                            val left = box.left * scaleX
                            val top = box.top * scaleY
                            val right = box.right * scaleX
                            val bottom = box.bottom * scaleY
                            
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                style = Stroke(width = 4f)
                            )
                            
                            val feetX = (left + right) / 2f
                            val feetY = bottom
                            drawCircle(Color.Yellow, radius = 8f, center = Offset(feetX, feetY))

                            if (currentFence != null) {
                                val dist = pointLineDistance(Offset(feetX, feetY), currentFence.first, currentFence.second)
                                if (dist < 40f) {
                                    breachDetected = true
                                    drawRect(
                                        color = Color.Red,
                                        topLeft = Offset(left, top),
                                        size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                        style = Stroke(width = 8f)
                                    )
                                }
                            }
                        }

                    if (isMonitoring && breachDetected) {
                        if (breachPopup == null) {
                            val camName = when(selectedVideoId) {
                                R.raw.clip_01 -> "Cam 1"
                                R.raw.clip_02 -> "Cam 2"
                                else -> "Cam 3"
                            }
                            breachPopup = BreachDetails(mockCows.random(), camName)
                        }
                    } else if (isMonitoring && !breachDetected) {
                        breachPopup = null
                    }
                }

                // Status chip overlay on top-left
                if (!isMonitoring && fences[selectedVideoId] == null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Text(
                            text = if (currentFenceStart == null) "Tap point 1" else "Tap point 2",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else if (!isMonitoring && fences[selectedVideoId] != null) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Text(
                            text = "✅ Fence set",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else if (isMonitoring) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Text(
                            text = "● LIVE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // Clear fence for current camera
                OutlinedButton(
                    onClick = { 
                        fences.remove(selectedVideoId)
                        currentFenceStart = null
                        breachPopup = null
                        if (isMonitoring) {
                            isMonitoring = false
                        }
                    },
                    enabled = fences[selectedVideoId] != null && !isMonitoring,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Fence")
                }
                
                // Start / Stop monitoring for the current camera
                Button(
                    onClick = { isMonitoring = !isMonitoring },
                    enabled = fences[selectedVideoId] != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoring) Color.Red else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isMonitoring) "⬛ Stop" else "▶ Start Monitoring")
                }
            }
            
            if (fences[selectedVideoId] == null && !isMonitoring) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap 2 points on the video above to create a fence line.", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Banner for Breach Alerts
            AnimatedVisibility(
                visible = breachPopup != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "🚨 BREACH DETECTED", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "📍 ${breachPopup?.camName}", color = Color.DarkGray, fontSize = 14.sp)
                            Text(text = "🐄 ${breachPopup?.cowInfo}", color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// Distance from point p to line segment a-b
private fun pointLineDistance(p: Offset, a: Offset, b: Offset): Float {
    val normalLength = kotlin.math.hypot(b.x - a.x, b.y - a.y)
    if (normalLength == 0f) return kotlin.math.hypot(p.x - a.x, p.y - a.y)
    val distance = kotlin.math.abs((p.x - a.x) * (b.y - a.y) - (p.y - a.y) * (b.x - a.x)) / normalLength
    
    val dotProduct = (p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)
    if (dotProduct < 0) return kotlin.math.hypot(p.x - a.x, p.y - a.y)
    if (dotProduct > normalLength * normalLength) return kotlin.math.hypot(p.x - b.x, p.y - b.y)
    return distance
}
