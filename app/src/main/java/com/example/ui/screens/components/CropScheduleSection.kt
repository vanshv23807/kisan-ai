package com.example.ui.screens.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropTask
import com.example.data.model.Farm
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

@Composable
fun CropScheduleSection(
    farm: Farm,
    cropTasks: List<CropTask>,
    onToggleCropTask: (CropTask) -> Unit,
    onAddCropTask: ((CropTask) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All Schedules, 1: Irrigation Reminders, 2: Planting Schedules
    val seasons = listOf("All", "Rabi", "Kharif", "Zaid")
    var selectedSeason by remember { mutableStateOf("All") }

    val farmCropTasks = cropTasks.filter { it.farmId == farm.id || it.farmId == "farm_1" }
    val filteredTasks = farmCropTasks.filter { task ->
        val matchesSeason = (selectedSeason == "All") || task.season.equals(selectedSeason, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            1 -> task.isIrrigationReminder
            2 -> task.isPlantingSchedule
            else -> true
        }
        matchesSeason && matchesTab
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("crop_schedule_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header (Text placed down/below heading)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SecondaryContainerGreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Seasonal Planting & Irrigation Schedule",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Milestones for ${farm.primaryCropOrAnimal} • ${farmCropTasks.count { !it.isCompleted }} tasks pending",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sub-filter tabs (All | Irrigation Reminders | Planting Milestones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("All (${farmCropTasks.size})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("💧 Irrigation (${farmCropTasks.count { it.isIrrigationReminder }})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                label = { Text("🌱 Planting (${farmCropTasks.count { it.isPlantingSchedule }})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = Color.White
                )
            )
        }

        // Season Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(seasons) { season ->
                val isSel = selectedSeason == season
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedSeason = season }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (season == "All") "All Seasons" else "$season Season",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Task Cards List
        if (filteredTasks.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                    Text("No Seasonal Tasks in this filter", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("All scheduled milestones and irrigation reminders are up to date.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredTasks.forEach { task ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else PrimaryGreen.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCropTask(task) }
                            .testTag("crop_task_${task.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleCropTask(task) },
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen, checkmarkColor = Color.White),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                // Task Title (Heading on top)
                                Text(
                                    text = task.taskTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )

                                // Tag / Badge placed below heading
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = if (task.isIrrigationReminder) Color(0xFFE0F2FE) else SecondaryContainerGreen,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (task.isIrrigationReminder) "💧 IRRIGATION REMINDER" else "🌱 ${task.season.uppercase()} SCHEDULE",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isIrrigationReminder) Color(0xFF0369A1) else PrimaryGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stage: ${task.stage} • ${task.recommendedDateDisplay}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryGreen
                                )

                                if (task.taskDescription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = task.taskDescription,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (task.fertilizerRecommendation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "NPK / Nutrition: ${task.fertilizerRecommendation}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFD97706),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (task.isIrrigationReminder && task.waterRequirementMm > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Water: ${task.waterRequirementMm} mm (${task.irrigationDurationMinutes} mins run)",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
