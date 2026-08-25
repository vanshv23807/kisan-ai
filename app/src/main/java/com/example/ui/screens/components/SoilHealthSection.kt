package com.example.ui.screens.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Farm
import com.example.data.model.SoilHealthRecord
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen
import java.util.*

@Composable
fun SoilHealthSection(
    farm: Farm,
    soilRecords: List<SoilHealthRecord>,
    onLogRecord: ((
        ph: Double,
        moisture: Double,
        source: String,
        notes: String,
        dateLabel: String,
        temp: Double,
        ec: Double,
        battery: Int,
        deviceName: String
    ) -> Unit)? = null,
    onDeleteRecord: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val farmSoilRecords = remember(soilRecords, farm.id) {
        soilRecords.filter { it.farmId == farm.id }
    }

    val latestRecord = farmSoilRecords.lastOrNull()
    val currentPh = latestRecord?.phLevel ?: 6.8
    val currentMoisture = latestRecord?.moisturePercent ?: 58.0
    val currentEc = latestRecord?.electricalConductivityEc ?: 1.15
    val currentTemp = latestRecord?.soilTemperatureCelsius ?: 24.0
    val currentCarbon = latestRecord?.organicCarbonPercent ?: 0.72

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("soil_health_monitoring_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. SECTION HEADER (Clean layout with subtitle down/below heading, side block removed)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryContainerGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soil Health Monitoring",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Telemetry & Nutrient Index for ${farm.name} (${farm.soilType})",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Primary Metrics Row (pH & Moisture)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // pH Metric Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentPh in 6.0..7.5) PrimaryGreen.copy(alpha = 0.5f) else Color(0xFFEAB308).copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Soil pH Level", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f pH", currentPh),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentPh in 6.0..7.5) PrimaryGreen else Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    currentPh < 6.0 -> "Acidic (Lime advised)"
                                    currentPh <= 7.5 -> "Optimal (6.0 - 7.5)"
                                    else -> "Alkaline (Gypsum advised)"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentPh in 6.0..7.5) PrimaryGreen else Color(0xFFD97706)
                            )
                        }
                    }

                    // Soil Moisture Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentMoisture in 35.0..70.0) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFFEAB308).copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Volumetric Moisture", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", currentMoisture),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    currentMoisture < 35.0 -> "Low (Irrigate Soon)"
                                    currentMoisture <= 70.0 -> "Adequate / Field Capacity"
                                    else -> "High / Saturated"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentMoisture in 35.0..70.0) Color(0xFF0284C7) else Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Secondary Telemetry (EC, Temp, Organic Carbon)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text("Soil Temp", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(String.format(Locale.getDefault(), "%.1f°C", currentTemp), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text("EC Conductivity", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(String.format(Locale.getDefault(), "%.2f mS/cm", currentEc), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text("Organic Carbon", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text("${currentCarbon}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                }
            }
        }

        // 2. SOIL DIAGNOSTICS & ADVISORY SUMMARY (Text below headings)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Soil Diagnostics & Health Status",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Testing status: ${farm.soilTestingStatus}",
                        fontSize = 11.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Soil texture is ${farm.soilType}. Maintain organic matter levels by incorporating vermicompost or FYM before the next planting cycle.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
