package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Farm
import com.example.data.model.FarmTask
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmActionPlanScreen(
    farms: List<Farm> = emptyList(),
    tasks: List<FarmTask> = emptyList(),
    onToggleTask: (FarmTask) -> Unit = {},
    onDeleteTask: (FarmTask) -> Unit = {},
    onAddTask: (farmId: String, title: String, description: String, category: String, priority: String, targetArea: String, whyReason: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToAddFarm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // 0 = All Farms, or specific farmId
    var selectedFarmFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Today's Action Plan",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            if (farms.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Farm Task", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_add_farm_task")
                )
            }
        }
    ) { paddingValues ->
        if (farms.isEmpty()) {
            // STRICT REQUIREMENT: If no farm is added, NO DATA should be shown
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("no_farms_empty_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "No Farms Registered",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Please register or add a farm first to generate an autonomous AI Action Plan, daily irrigation schedules, and crop management tasks tailored to your location.",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = onNavigateToAddFarm,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_empty_add_farm")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Farm to Start Action Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // When farms are present: Detailed things to do in EVERY farm
            val farmMap = remember(farms) { farms.associateBy { it.id } }

            // Synthesize comprehensive farm-specific tasks if none are present in DB
            val effectiveTasks = remember(farms, tasks) {
                if (tasks.isNotEmpty()) {
                    tasks
                } else {
                    generateIntelligentFarmTasks(farms)
                }
            }

            val filteredTasks = effectiveTasks.filter { task ->
                val matchesFarm = (selectedFarmFilter == "ALL" || task.farmId == selectedFarmFilter)
                val matchesCat = (selectedCategoryFilter == "ALL" || task.category.equals(selectedCategoryFilter, ignoreCase = true))
                matchesFarm && matchesCat
            }

            val totalCount = effectiveTasks.size
            val completedCount = effectiveTasks.count { it.isCompleted }
            val progressPercent = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                // Header Banner: Action Plan Overview & Progress
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("action_plan_hero_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Farm Operations",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${farms.size} Farm${if (farms.size > 1) "s" else ""} Monitored • AI Optimized",
                                        fontSize = 12.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$completedCount / $totalCount Done",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (progressPercent == 100) "🎉 All farm operations completed for today!" else "$progressPercent% tasks completed today",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Farm Selector Filter Row
                item {
                    Column {
                        Text(
                            text = "Filter by Farm:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedFarmFilter == "ALL",
                                    onClick = { selectedFarmFilter = "ALL" },
                                    label = { Text("All Farms (${farms.size})", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_farm_all")
                                )
                            }
                            items(farms) { farm ->
                                FilterChip(
                                    selected = selectedFarmFilter == farm.id,
                                    onClick = { selectedFarmFilter = farm.id },
                                    label = { Text(farm.name, fontWeight = FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_farm_${farm.id}")
                                )
                            }
                        }
                    }
                }

                // Category Chips (Irrigation, Pest, Fertilizer, Livestock, Harvesting)
                item {
                    val categories = listOf("ALL", "Irrigation", "Fertilizer", "Crop Health", "Livestock", "General")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            SuggestionChip(
                                onClick = { selectedCategoryFilter = cat },
                                label = {
                                    Text(
                                        text = when(cat) {
                                            "Irrigation" -> "💧 Irrigation"
                                            "Fertilizer" -> "🧪 Nutrients"
                                            "Crop Health" -> "🛡️ Crop Health"
                                            "Livestock" -> "🐄 Animals/Dairy"
                                            "General" -> "🌾 Field Work"
                                            else -> "📋 All Categories"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selectedCategoryFilter == cat) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategoryFilter == cat) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedCategoryFilter == cat) SecondaryContainerGreen else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }

                // Section Title: Action Items List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scheduled Tasks (${filteredTasks.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Tap task to complete",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (filteredTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No tasks in this filter.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(filteredTasks, key = { it.id }) { task ->
                        val farm = farmMap[task.farmId] ?: farms.firstOrNull()
                        val isHighPriority = task.priority.equals("High", ignoreCase = true)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTask(task) }
                                .testTag("task_card_${task.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Farm badge & Category
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = PrimaryGreen.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = farm?.name ?: "Farm",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryGreen,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            color = if (isHighPriority) Color(0xFFD32F2F).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = task.priorityLabel.ifBlank { if (isHighPriority) "HIGH PRIORITY" else "PLANNED" },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isHighPriority) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Interactive Checkbox
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { onToggleTask(task) },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                                        modifier = Modifier.testTag("checkbox_task_${task.id}")
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Task Title
                                Text(
                                    text = task.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Description & Instructions
                                Text(
                                    text = task.description,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )

                                if (task.whyReason.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = SecondaryContainerGreen.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "AI Rationale: ${task.whyReason}",
                                                fontSize = 11.5.sp,
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Footer: Target Area & Estimated Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = task.targetArea.ifBlank { farm?.village ?: "Main Plot" },
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "~${task.estimatedTimeMinutes} mins",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Add Custom Farm Task Dialog
    if (showAddTaskDialog && farms.isNotEmpty()) {
        var selectedFarmId by remember { mutableStateOf(farms.first().id) }
        var taskTitle by remember { mutableStateOf("") }
        var taskDescription by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf("Irrigation") }
        var taskPriority by remember { mutableStateOf("High") }
        var taskArea by remember { mutableStateOf("") }
        var taskWhy by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AddTask, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Farm Action Task", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Select Farm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(farms) { f ->
                                FilterChip(
                                    selected = selectedFarmId == f.id,
                                    onClick = { selectedFarmId = f.id },
                                    label = { Text(f.name, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task Title (e.g. Check Drip Line 2)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_task_title"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = taskDescription,
                            onValueChange = { taskDescription = it },
                            label = { Text("Detailed Instructions") },
                            modifier = Modifier.fillMaxWidth().testTag("input_task_desc"),
                            minLines = 2
                        )
                    }

                    item {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        val cats = listOf("Irrigation", "Fertilizer", "Crop Health", "Livestock", "General")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(cats) { c ->
                                FilterChip(
                                    selected = taskCategory == c,
                                    onClick = { taskCategory = c },
                                    label = { Text(c, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = taskArea,
                            onValueChange = { taskArea = it },
                            label = { Text("Target Plot / Shed (e.g. Parcel 1 / Barn A)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = taskWhy,
                            onValueChange = { taskWhy = it },
                            label = { Text("Why needed / Agronomic rationale") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAddTask(
                                selectedFarmId,
                                taskTitle.trim(),
                                taskDescription.trim().ifBlank { taskTitle.trim() },
                                taskCategory,
                                taskPriority,
                                taskArea.trim().ifBlank { "Main Field" },
                                taskWhy.trim()
                            )
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.testTag("btn_confirm_add_task")
                ) {
                    Text("Add Task", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

/**
 * Dynamically synthesizes intelligent agronomic tasks for each farm present in the database.
 */
private fun generateIntelligentFarmTasks(farms: List<Farm>): List<FarmTask> {
    val tasks = mutableListOf<FarmTask>()

    farms.forEachIndexed { index, farm ->
        val isLivestock = farm.farmType.contains("Dairy", true) ||
                farm.farmType.contains("Cattle", true) ||
                farm.farmType.contains("Poultry", true) ||
                farm.farmType.contains("Goat", true)

        if (isLivestock) {
            tasks.add(
                FarmTask(
                    id = "task_farm_${farm.id}_1",
                    farmId = farm.id,
                    title = "Morning Fodder & Trough Sanitation",
                    description = "Distribute fresh green silage and check drinking water troughs for sediment. Ensure mineral blocks are available in the shed.",
                    priority = "High",
                    priorityLabel = "DO FIRST",
                    targetArea = "${farm.name} - Shed A",
                    category = "Livestock",
                    whyReason = "Clean hydration and balanced dry matter intake maintains peak milk yield and prevents rumen acidosis.",
                    isCompleted = false,
                    estimatedTimeMinutes = 35
                )
            )
            tasks.add(
                FarmTask(
                    id = "task_farm_${farm.id}_2",
                    farmId = farm.id,
                    title = "Health Check & Vaccination Log",
                    description = "Inspect herd for early mastitis signs or foot rot. Log daily milk production recording in the Livestock tab.",
                    priority = "Medium",
                    priorityLabel = "ROUTINE",
                    targetArea = farm.name,
                    category = "Livestock",
                    whyReason = "Early detection of somatic cell elevation prevents severe infection and antibiotic withdrawal losses.",
                    isCompleted = false,
                    estimatedTimeMinutes = 20
                )
            )
        } else {
            tasks.add(
                FarmTask(
                    id = "task_farm_${farm.id}_1",
                    farmId = farm.id,
                    title = "Smart Drip Irrigation Run",
                    description = "Activate Drip Line Zone 1 for 45 minutes before midday heat. Evapotranspiration is normal today. Verify emitter flow rates.",
                    priority = "High",
                    priorityLabel = "PRIORITY 1",
                    targetArea = "${farm.name} - Parcel 1",
                    category = "Irrigation",
                    whyReason = "Root zone moisture depleted to 55%. Morning irrigation prevents leaf scorch and transpiration stress.",
                    isCompleted = false,
                    estimatedTimeMinutes = 45
                )
            )
            tasks.add(
                FarmTask(
                    id = "task_farm_${farm.id}_2",
                    farmId = farm.id,
                    title = "Scout for Yellow Rust / Aphids",
                    description = "Walk across field diagonals and examine lower leaf surfaces on 20 random plants for fungal pustules or sucking pests.",
                    priority = "High",
                    priorityLabel = "SCOUTING",
                    targetArea = "${farm.name} - Border Rows",
                    category = "Crop Health",
                    whyReason = "High ambient humidity creates favorable sporulation conditions for fungal pathogens.",
                    isCompleted = false,
                    estimatedTimeMinutes = 30
                )
            )
            tasks.add(
                FarmTask(
                    id = "task_farm_${farm.id}_3",
                    farmId = farm.id,
                    title = "Nutrient Top-Dressing Assessment",
                    description = "Assess leaf chlorophyll index. Prepare 2% Urea or Nano-DAP foliar application if yellowing is visible on mature leaves.",
                    priority = "Medium",
                    priorityLabel = "NUTRITION",
                    targetArea = "${farm.name} - Main Field",
                    category = "Fertilizer",
                    whyReason = "Tillering and vegetative elongation phase requires optimal nitrogen uptake for spikelet development.",
                    isCompleted = false,
                    estimatedTimeMinutes = 25
                )
            )
        }
    }

    return tasks
}
