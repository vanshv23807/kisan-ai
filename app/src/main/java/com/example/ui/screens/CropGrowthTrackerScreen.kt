package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropRecord
import com.example.data.model.Farm
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

data class GrowthStageItem(
    val stageNumber: Int,
    val stageName: String,
    val dayRange: String,
    val description: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val requiredAction: String,
    val keyWatchout: String
)

data class FieldObservationLog(
    val id: String,
    val dateDisplay: String,
    val stageName: String,
    val note: String,
    val healthTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropGrowthTrackerScreen(
    crops: List<CropRecord>,
    farms: List<Farm>,
    onAddCropClick: (name: String, variety: String, stage: String, isNewCrop: Boolean, daysElapsed: Int) -> Unit,
    onBack: () -> Unit
) {
    val hasFarm = farms.isNotEmpty()

    // Default crop data when farms exist
    var localCrops by remember(crops, hasFarm) {
        mutableStateOf(
            if (!hasFarm) emptyList()
            else if (crops.isNotEmpty()) crops
            else listOf(
                CropRecord(
                    id = "c_101",
                    farmId = farms.first().id,
                    parcelId = "p_1",
                    cropName = "Wheat",
                    variety = "HD-3086 (Pusa Gautami)",
                    growthStage = "Flowering & Booting",
                    healthScore = 92,
                    sowingDate = "2024-11-10",
                    expectedHarvest = "2025-03-25",
                    expectedYieldPerAcre = 22.5,
                    notes = "Vibrant green canopy, 2nd irrigation completed.",
                    isNewCrop = false,
                    daysElapsed = 48
                ),
                CropRecord(
                    id = "c_102",
                    farmId = farms.first().id,
                    parcelId = "p_2",
                    cropName = "Mustard",
                    variety = "Pusa Bold",
                    growthStage = "Grain Filling",
                    healthScore = 88,
                    sowingDate = "2024-10-15",
                    expectedHarvest = "2025-02-28",
                    expectedYieldPerAcre = 8.0,
                    notes = "Pod formation healthy. Aphid inspection clear.",
                    isNewCrop = false,
                    daysElapsed = 72
                ),
                CropRecord(
                    id = "c_103",
                    farmId = farms.first().id,
                    parcelId = "p_3",
                    cropName = "Gram / Chickpea (New Sowing)",
                    variety = "JG 14",
                    growthStage = "Sowing & Germination",
                    healthScore = 98,
                    sowingDate = "Today (Day 0)",
                    expectedHarvest = "In 115 Days",
                    expectedYieldPerAcre = 10.5,
                    notes = "Freshly sown seedbed. Basal DAP & Trichoderma treatment applied.",
                    isNewCrop = true,
                    daysElapsed = 0
                )
            )
        )
    }

    var selectedCropId by remember { mutableStateOf(localCrops.firstOrNull()?.id ?: "") }
    val selectedCrop = localCrops.find { it.id == selectedCropId } ?: localCrops.firstOrNull()

    // Override toggle for quick live comparison (New vs Old crop view)
    var isNewCropModeOverride by remember(selectedCrop?.id) {
        mutableStateOf<Boolean?>(null)
    }
    val effectiveIsNewCrop = isNewCropModeOverride ?: (selectedCrop?.isNewCrop ?: false)

    var showAddCropDialog by remember { mutableStateOf(false) }
    var showLogNoteDialog by remember { mutableStateOf(false) }

    // Logs state
    var observationLogs by remember {
        mutableStateOf(
            listOf(
                FieldObservationLog(
                    id = "log_1",
                    dateDisplay = "Today, 08:30 AM",
                    stageName = if (effectiveIsNewCrop) "Sowing & Germination (Day 0)" else "Flowering & Booting (Day 48)",
                    note = if (effectiveIsNewCrop)
                        "Completed seed treatment with Trichoderma (5g/kg). Basal DAP 50kg/acre drilled at 4cm depth."
                    else
                        "Applied 25kg Urea/acre after 2nd irrigation. Crop height ~45 cm, canopy healthy.",
                    healthTag = if (effectiveIsNewCrop) "Fresh Sowing" else "Healthy Growth"
                ),
                FieldObservationLog(
                    id = "log_2",
                    dateDisplay = "5 days ago",
                    stageName = if (effectiveIsNewCrop) "Pre-Sowing Land Prep" else "Vegetative Stage",
                    note = if (effectiveIsNewCrop)
                        "Deep plowing and rotavator leveling finished. Optimum soil moisture achieved."
                    else
                        "Crown Root Initiation (CRI) irrigation completed. Zero pest activity.",
                    healthTag = "Optimal Moisture"
                )
            )
        )
    }

    // Dynamic Growth Stages based on whether it is a NEW crop (starts from 0) or OLD standing crop
    val growthStages: List<GrowthStageItem> = remember(selectedCrop?.cropName, effectiveIsNewCrop) {
        if (effectiveIsNewCrop) {
            // NEW CROP: Starts from zero (Day 0), all stages upcoming, stage 1 is currently active at 0%
            listOf(
                GrowthStageItem(
                    stageNumber = 1,
                    stageName = "Sowing & Germination (Day 0)",
                    dayRange = "Day 0 - 15 (Current: Day 0)",
                    description = "Freshly sown seeds sprout and first root structures emerge. Growth progress starts from 0%.",
                    isCompleted = false,
                    isCurrent = true,
                    requiredAction = "Maintain uniform soil moisture & 3-5 cm sowing depth. Protect from birds.",
                    keyWatchout = "Watch for soil crusting, cutworms & seed rotting before emergence"
                ),
                GrowthStageItem(
                    stageNumber = 2,
                    stageName = "Crown Root Initiation & Tillering",
                    dayRange = "Day 16 - 45",
                    description = "Rapid leaf growth, crown root establishment, tillers branching out.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "1st Critical Irrigation (at 21 days) & 1st Nitrogen top-dressing",
                    keyWatchout = "Scout for weed competition & early yellowing"
                ),
                GrowthStageItem(
                    stageNumber = 3,
                    stageName = "Stem Elongation & Booting",
                    dayRange = "Day 46 - 75",
                    description = "Ears form inside leaf sheath. Peak water & nutrient uptake window.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "2nd Irrigation & prophylactic bio-fungicide spray if humid",
                    keyWatchout = "Monitor for rust fungus, aphids & soil dry spells"
                ),
                GrowthStageItem(
                    stageNumber = 4,
                    stageName = "Flowering & Grain Filling",
                    dayRange = "Day 76 - 105",
                    description = "Milky to soft dough stage as grains fill with starch & proteins.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "Final light irrigation during milky stage; avoid waterlogging",
                    keyWatchout = "Shield against high wind lodging & unseasonal heat shock"
                ),
                GrowthStageItem(
                    stageNumber = 5,
                    stageName = "Harvest & Maturation",
                    dayRange = "Day 106 - 125",
                    description = "Grains turn golden yellow, moisture drops below 12-14% ready for combine.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "Check grain hardness test & schedule harvester / thresher machinery",
                    keyWatchout = "Ensure clean, moisture-free storage bags before bagging"
                )
            )
        } else {
            // OLD / STANDING CROP: In progress with completed past stages
            listOf(
                GrowthStageItem(
                    stageNumber = 1,
                    stageName = "Sowing & Germination",
                    dayRange = "Day 0 - 15",
                    description = "Seeds successfully sprouted with >95% uniform germination.",
                    isCompleted = true,
                    isCurrent = false,
                    requiredAction = "Completed: Sowing & basal fertilization accomplished",
                    keyWatchout = "Completed: Germination achieved safely"
                ),
                GrowthStageItem(
                    stageNumber = 2,
                    stageName = "Vegetative & Tillering",
                    dayRange = "Day 16 - 45",
                    description = "Vigorous tiller branching and deep crown root network established.",
                    isCompleted = true,
                    isCurrent = false,
                    requiredAction = "Completed: 1st CRI irrigation & urea top dressing finished",
                    keyWatchout = "Completed: Zero weed pressure recorded"
                ),
                GrowthStageItem(
                    stageNumber = 3,
                    stageName = "Flowering & Booting",
                    dayRange = "Day 46 - 75 (Active Now)",
                    description = "Ears/flowers emerging inside sheath. Peak water & nutrient demand.",
                    isCompleted = false,
                    isCurrent = true,
                    requiredAction = "2nd Irrigation & Zinc/Potash foliar spray to boost earhead size",
                    keyWatchout = "Monitor for rust fungus, aphids & high daytime temperatures"
                ),
                GrowthStageItem(
                    stageNumber = 4,
                    stageName = "Grain Filling & Pod Formation",
                    dayRange = "Day 76 - 105",
                    description = "Milky to dough stage as grains pack nutrients and starch.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "Final light irrigation; avoid excessive nitrogen fertilizer",
                    keyWatchout = "Shield against lodging from strong winds"
                ),
                GrowthStageItem(
                    stageNumber = 5,
                    stageName = "Harvest & Maturation",
                    dayRange = "Day 106 - 130",
                    description = "Grains turn golden yellow, moisture drops below 14%.",
                    isCompleted = false,
                    isCurrent = false,
                    requiredAction = "Check grain hardness & schedule thresher/combine machine",
                    keyWatchout = "Ensure dry warehouse & clean tarpaulins before harvest"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Crop Growth Tracker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (effectiveIsNewCrop) "🌱 New Crop Mode: Starts from Zero (Day 0)" else "🌾 Standing Crop: Active Stage Progress",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasFarm) {
                        IconButton(
                            onClick = { showAddCropDialog = true },
                            modifier = Modifier.testTag("growth_tracker_add_crop_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Crop", tint = PrimaryGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (!hasFarm) {
            // Empty state when NO farm is added
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "No Farm Added Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "No farm data found. Add your farm first from the Farms tab to start tracking crop growth stages, vegetative progress, and agronomy alerts.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Go Back to Add Farm", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else if (localCrops.isEmpty()) {
            // Empty state when farm exists but no crops yet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SecondaryContainerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "No Crops Added",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "No active crops found for ${farms.first().name}. Add your sown crops to view growth progress, timeline milestones, and irrigation reminders.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = { showAddCropDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Crop Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
            ) {
                // 1. Crop Selector Horizontal Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(localCrops) { crop ->
                            val isSelected = crop.id == selectedCropId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCropId = crop.id
                                    isNewCropModeOverride = null
                                },
                                label = {
                                    Text(
                                        text = "${crop.cropName} ${if (crop.isNewCrop) "🌱 (New)" else "🌾 (Standing)"}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (crop.isNewCrop) Icons.Default.Spa else Icons.Default.Grass,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainerGreen,
                                    selectedLabelColor = PrimaryGreen
                                )
                            )
                        }
                    }
                }

                // 2. Crop Type Selector Switch (New Crop - Start From Zero vs Old / Standing Crop)
                if (selectedCrop != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Select Growth Tracking Mode:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Option A: New Crop (Start From 0)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isNewCropModeOverride = true }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Spa,
                                                contentDescription = null,
                                                tint = if (effectiveIsNewCrop) Color.White else PrimaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "New Crop",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (effectiveIsNewCrop) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Start from 0% (Day 0)",
                                                    fontSize = 9.5.sp,
                                                    color = if (effectiveIsNewCrop) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Option B: Old Crop (Standing / Mid-Cycle)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (!effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isNewCropModeOverride = false }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Agriculture,
                                                contentDescription = null,
                                                tint = if (!effectiveIsNewCrop) Color.White else PrimaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Old / Standing Crop",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (!effectiveIsNewCrop) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "In-Progress Details",
                                                    fontSize = 9.5.sp,
                                                    color = if (!effectiveIsNewCrop) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Active Crop Progress Visual Card (0% for New Crop, in-progress for Old Crop)
                    item {
                        val daysElapsed = if (effectiveIsNewCrop) 0 else (if (selectedCrop.daysElapsed > 0) selectedCrop.daysElapsed else 48)
                        val totalDays = 120
                        val progressPercent = if (effectiveIsNewCrop) 0f else (daysElapsed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("active_crop_progress_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryContainerGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (effectiveIsNewCrop) Icons.Default.Spa else Icons.Default.Park,
                                                contentDescription = null,
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${selectedCrop.cropName} • ${selectedCrop.variety}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = if (effectiveIsNewCrop)
                                                    "🌱 New Sowing • Day 0 of $totalDays (Start from Zero)"
                                                else
                                                    "🌾 Standing Crop • Sown on ${selectedCrop.sowingDate} • Day $daysElapsed of $totalDays",
                                                fontSize = 12.sp,
                                                color = if (effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (effectiveIsNewCrop) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (effectiveIsNewCrop) PrimaryGreen else SecondaryContainerGreen,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (effectiveIsNewCrop) "0% (Day 0)" else "${selectedCrop.healthScore}% Health",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (effectiveIsNewCrop) Color.White else PrimaryGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Growth Progress Bar Visual (0% for New Crop)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (effectiveIsNewCrop) "Current Stage: Sowing & Germination (Day 0)" else "Current Stage: ${selectedCrop.growthStage}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = PrimaryGreen
                                    )
                                    Text(
                                        text = if (effectiveIsNewCrop) "0% Complete (Starting)" else "${(progressPercent * 100).toInt()}% Complete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (effectiveIsNewCrop) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                    )

                                    LinearProgressIndicator(
                                        progress = { progressPercent },
                                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                        color = PrimaryGreen,
                                        trackColor = SecondaryContainerGreen
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                // Projection Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (effectiveIsNewCrop) "Est. Harvest Window" else "Est. Harvest Date",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (effectiveIsNewCrop) "In 120 Days (Full Cycle)" else selectedCrop.expectedHarvest,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Projected Yield", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${selectedCrop.expectedYieldPerAcre} Qtl / Acre", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                                    }
                                }
                            }
                        }
                    }

                    // 4. SPECIFIC DETAILS ACCORDING TO CROP STATUS (NEW CROP VS OLD CROP)
                    item {
                        if (effectiveIsNewCrop) {
                            // NEW CROP DETAILS CARD
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen.copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("New Crop Sowing Specifications & Action Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        NewCropDetailRow(
                                            title = "1. Seed Treatment & Rate",
                                            detail = "Treat with Trichoderma viride @ 5g/kg or Carbendazim @ 2g/kg before sowing. Recommended seed rate: 40-45 kg/acre."
                                        )
                                        NewCropDetailRow(
                                            title = "2. Sowing Depth & Spacing",
                                            detail = "Sow seeds at 3 to 5 cm depth into moist soil zone. Maintain row spacing of 20-22.5 cm using seed drill."
                                        )
                                        NewCropDetailRow(
                                            title = "3. Basal Fertilizer Placement",
                                            detail = "Drill 50kg DAP + 25kg MOP + 10kg Zinc Sulphate at time of sowing. Do not place concentrated urea in direct seed contact."
                                        )
                                        NewCropDetailRow(
                                            title = "4. First Irrigation Milestone",
                                            detail = "First critical irrigation due at Crown Root Initiation (CRI) exactly 21 days from today. Keep field free of standing stagnant water."
                                        )
                                    }
                                }
                            }
                        } else {
                            // OLD / STANDING CROP DETAILS CARD
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Analytics, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Standing Crop Status & In-Progress Agronomy Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OldCropDetailRow(
                                            title = "Days in Field & Age",
                                            value = "${if (selectedCrop.daysElapsed > 0) selectedCrop.daysElapsed else 48} Days active (Mid-Season Stage)"
                                        )
                                        OldCropDetailRow(
                                            title = "Canopy Health Index",
                                            value = "${selectedCrop.healthScore}% - Robust vegetative biomass & tillering"
                                        )
                                        OldCropDetailRow(
                                            title = "Next Top-Dressing",
                                            value = "Apply 2nd split of Neem Coated Urea (25kg/acre) after current irrigation"
                                        )
                                        OldCropDetailRow(
                                            title = "Pest & Disease Advisory",
                                            value = "Yellow rust and aphid scouting active. Weather condition suitable."
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Stage Timeline Stepper
                    item {
                        Text(
                            text = if (effectiveIsNewCrop) "Stage Timeline (Starting From Day 0)" else "Stage Progression & Completed Milestones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    itemsIndexed(growthStages) { index, stage ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (stage.isCurrent) SecondaryContainerGreen.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (stage.isCurrent) 1.5.dp else 1.dp,
                                color = if (stage.isCurrent) PrimaryGreen else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        stage.isCompleted -> PrimaryGreen
                                                        stage.isCurrent -> PrimaryGreen
                                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (stage.isCompleted) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            } else {
                                                Text(
                                                    "${stage.stageNumber}",
                                                    color = if (stage.isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        Column {
                                            Text(stage.stageName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                            Text(stage.dayRange, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        color = when {
                                            stage.isCompleted -> PrimaryGreen.copy(alpha = 0.15f)
                                            stage.isCurrent -> PrimaryGreen
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                stage.isCompleted -> "Completed"
                                                stage.isCurrent -> if (effectiveIsNewCrop) "Starting (Day 0)" else "Active Now"
                                                else -> "Upcoming"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = when {
                                                stage.isCompleted -> PrimaryGreen
                                                stage.isCurrent -> Color.White
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Text(stage.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(15.dp))
                                            Text("Action Needed: ${stage.requiredAction}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(15.dp))
                                            Text("Watchout: ${stage.keyWatchout}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Observation Logs & Field Journal
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Field Observations & Growth Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { showLogNoteDialog = true }) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Note", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(observationLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(log.stageName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                                    Text(log.dateDisplay, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Text(log.note, fontSize = 12.5.sp)

                                Surface(
                                    color = SecondaryContainerGreen,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = log.healthTag,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Add New Crop (Supports choosing "New Crop: Start From Zero" vs "Old / Standing Crop")
    if (showAddCropDialog) {
        var nameInput by remember { mutableStateOf("") }
        var varietyInput by remember { mutableStateOf("") }
        var isNewCropSelected by remember { mutableStateOf(true) }
        var stageInput by remember { mutableStateOf("Sowing & Germination") }
        var daysElapsedInput by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddCropDialog = false },
            title = { Text("Track Crop Growth", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Crop Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNewCropSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isNewCropSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isNewCropSelected = true
                                    stageInput = "Sowing & Germination"
                                    daysElapsedInput = "0"
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌱 New Crop", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isNewCropSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                                Text("Start from 0%", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isNewCropSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (!isNewCropSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isNewCropSelected = false
                                    stageInput = "Vegetative & Tillering"
                                    daysElapsedInput = "45"
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌾 Old / Standing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isNewCropSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                                Text("In-Progress", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Crop Name (e.g. Wheat, Cotton, Mustard)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = varietyInput,
                        onValueChange = { varietyInput = it },
                        label = { Text("Crop Variety (e.g. HD-3086, Hybrid)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isNewCropSelected) {
                        OutlinedTextField(
                            value = stageInput,
                            onValueChange = { stageInput = it },
                            label = { Text("Current Stage (e.g. Vegetative, Flowering)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = daysElapsedInput,
                            onValueChange = { daysElapsedInput = it },
                            label = { Text("Days Since Sowing (e.g. 45)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            val days = if (isNewCropSelected) 0 else (daysElapsedInput.toIntOrNull() ?: 45)
                            val newCrop = CropRecord(
                                id = "c_${System.currentTimeMillis()}",
                                farmId = farms.firstOrNull()?.id ?: "farm_1",
                                parcelId = "p_new",
                                cropName = nameInput.trim(),
                                variety = varietyInput.ifBlank { "Standard Variety" },
                                growthStage = if (isNewCropSelected) "Sowing & Germination" else stageInput.ifBlank { "Vegetative" },
                                healthScore = if (isNewCropSelected) 98 else 90,
                                sowingDate = if (isNewCropSelected) "Today (Day 0)" else "45 days ago",
                                expectedHarvest = if (isNewCropSelected) "In 120 Days" else "In 75 Days",
                                expectedYieldPerAcre = 20.0,
                                isNewCrop = isNewCropSelected,
                                daysElapsed = days
                            )
                            localCrops = localCrops + newCrop
                            selectedCropId = newCrop.id
                            isNewCropModeOverride = isNewCropSelected
                            onAddCropClick(nameInput, varietyInput, stageInput, isNewCropSelected, days)
                            showAddCropDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Start Tracking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCropDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 2: Add Observation Note
    if (showLogNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        var selectedTag by remember { mutableStateOf(if (effectiveIsNewCrop) "Seedling Emergence" else "Optimal Growth") }

        AlertDialog(
            onDismissRequest = { showLogNoteDialog = false },
            title = { Text("Log Field Observation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Observation details (e.g. height, emergence, irrigation, fertilizer)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text("Health / Status Tag:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    val tags = if (effectiveIsNewCrop)
                        listOf("Seedling Emergence", "Moisture Optimal", "Basal Applied")
                    else
                        listOf("Optimal Growth", "Need Irrigation", "Pest Inspected")

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            val newLog = FieldObservationLog(
                                id = "log_${System.currentTimeMillis()}",
                                dateDisplay = "Just now",
                                stageName = if (effectiveIsNewCrop) "Sowing & Germination (Day 0)" else (selectedCrop?.growthStage ?: "Vegetative"),
                                note = noteText.trim(),
                                healthTag = selectedTag
                            )
                            observationLogs = listOf(newLog) + observationLogs
                            showLogNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NewCropDetailRow(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = PrimaryGreen)
        Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 14.sp)
    }
}

@Composable
private fun OldCropDetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
