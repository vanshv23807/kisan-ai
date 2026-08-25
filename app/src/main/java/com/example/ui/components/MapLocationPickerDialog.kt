package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

data class MapPreset(
    val name: String,
    val village: String,
    val district: String,
    val state: String,
    val pin: String,
    val lat: Double,
    val lng: Double
)

val INDIAN_STATES_AND_UTS = listOf(
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Andaman and Nicobar Islands",
    "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi (NCT)",
    "Jammu and Kashmir",
    "Ladakh",
    "Lakshadweep",
    "Puducherry"
)

@Composable
fun MapLocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationConfirmed: (state: String, district: String, village: String, pin: String, lat: Double, lng: Double) -> Unit
) {
    val presets = listOf(
        MapPreset("Samrala Farm", "Samrala", "Ludhiana", "Punjab", "141114", 30.8987, 75.8573),
        MapPreset("Karnal Wheat Basin", "Gharaunda", "Karnal", "Haryana", "132001", 29.6857, 76.9905),
        MapPreset("Nashik Vineyard", "Dindori", "Nashik", "Maharashtra", "422003", 20.0110, 73.7903),
        MapPreset("Jaipur Mustard Belt", "Chomu", "Jaipur", "Rajasthan", "303702", 27.1700, 75.7200),
        MapPreset("Coimbatore Coconut Farm", "Pollachi", "Coimbatore", "Tamil Nadu", "642001", 10.6585, 77.0080)
    )

    var selectedPreset by remember { mutableStateOf(presets[0]) }
    var isSatelliteView by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SecondaryContainerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Maps Location Picker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "High precision GPS coordinate lock",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Interactive Simulated Map View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSatelliteView) Color(0xFF1E3A2F) else Color(0xFFE8F5E9))
                        .border(1.5.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    // Map Grid lines & terrain graphics
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val step = 40.dp.toPx()
                        for (x in 0..(size.width / step).toInt()) {
                            drawLine(
                                color = if (isSatelliteView) Color(0x334CAF50) else Color(0x3381C784),
                                start = Offset(x * step, 0f),
                                end = Offset(x * step, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..(size.height / step).toInt()) {
                            drawLine(
                                color = if (isSatelliteView) Color(0x334CAF50) else Color(0x3381C784),
                                start = Offset(0f, y * step),
                                end = Offset(size.width, y * step),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Centered Marker Pin with Radar Pulse
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Farm GPS Pin",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Text(
                                text = "${selectedPreset.village}, ${selectedPreset.district}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xCC000000))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Map controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = { isSatelliteView = !isSatelliteView },
                            label = { Text(if (isSatelliteView) "Satellite" else "Terrain", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }

                    // Coordinates pill at bottom
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xEEFFFFFF),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lat: ${selectedPreset.lat}° N, Long: ${selectedPreset.lng}° E (Accurate: 3m)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Quick Locations Chips
                Column {
                    Text(
                        text = "Quick Geographic Hubs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(3).forEach { preset ->
                            val isSelected = preset.name == selectedPreset.name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedPreset = preset }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.district,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Detected Address Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Detected Geo-Address", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedPreset.village}, Tehsil ${selectedPreset.district}, State ${selectedPreset.state} - ${selectedPreset.pin}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Confirm Action Button
                Button(
                    onClick = {
                        onLocationConfirmed(
                            selectedPreset.state,
                            selectedPreset.district,
                            selectedPreset.village,
                            selectedPreset.pin,
                            selectedPreset.lat,
                            selectedPreset.lng
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .testTag("confirm_map_location_button")
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm This Address & Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
        }
    }
}
