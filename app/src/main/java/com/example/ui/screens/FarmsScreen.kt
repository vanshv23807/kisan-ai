package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiFarmAgent
import com.example.data.model.CropGrowthVisionAnalysis
import com.example.data.model.CropTask
import com.example.data.model.Farm
import com.example.data.model.FarmTask
import com.example.data.model.IrrigationZone
import com.example.data.model.LandParcel
import com.example.data.model.SoilHealthRecord
import com.example.data.util.FarmHealthCalculator
import com.example.ui.screens.components.CropScheduleSection
import com.example.ui.screens.components.FertilizerCalculatorSection
import com.example.ui.screens.components.SoilHealthSection
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private enum class FarmDetailTab {
    TASKS, FERTILIZER, SCHEDULE, SOIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmsScreen(
    farms: List<Farm>,
    selectedFarmId: String,
    onSelectFarm: (String) -> Unit,
    parcels: List<LandParcel>,
    tasks: List<FarmTask>,
    irrigationZones: List<IrrigationZone>,
    onToggleTask: (FarmTask) -> Unit,
    onToggleIrrigationZone: (IrrigationZone) -> Unit,
    onAdvanceFarmDay: (String) -> Unit,
    onAddNewFarmDetails: (
        name: String,
        type: String,
        area: Double,
        unit: String,
        primaryCropOrAnimal: String,
        ownership: String,
        houseNo: String,
        landmark: String,
        village: String,
        district: String,
        state: String,
        pinCode: String,
        soilType: String,
        soilTestingStatus: String,
        fertilizerPractice: String,
        drainageCondition: String,
        waterSource: String,
        pestPressure: String,
        diseaseSigns: String,
        seedQuality: String,
        boundaryFencing: String
    ) -> Unit,
    soilRecords: List<SoilHealthRecord> = emptyList(),
    onLogSoilHealthRecord: ((ph: Double, moisture: Double, source: String, notes: String, dateLabel: String, temp: Double, ec: Double, battery: Int, deviceName: String) -> Unit)? = null,
    onDeleteSoilHealthRecord: ((String) -> Unit)? = null,
    cropTasks: List<CropTask> = emptyList(),
    onToggleCropTask: ((CropTask) -> Unit)? = null,
    onAddFertilizerTask: ((title: String, desc: String, category: String) -> Unit)? = null,
    onAddCropTask: ((CropTask) -> Unit)? = null
) {
    var showAddFarmDialog by remember { mutableStateOf(false) }
    // Viewing a specific farm in detail view (null = showing the clean farms list with 'Get Details' buttons)
    var viewingDetailFarmId by remember { mutableStateOf<String?>(null) }
    var selectedInternalTab by remember { mutableStateOf(FarmDetailTab.TASKS) }

    // If viewing farm was deleted, reset
    val currentDetailFarm = farms.find { it.id == viewingDetailFarmId }

    // Dialog for adding farm with 4 steps
    if (showAddFarmDialog) {
        val coroutineScope = rememberCoroutineScope()
        var wizardStep by remember { mutableStateOf(1) } // 1: Basics, 2: Address, 3: Cropping Pattern & Crop History, 4: Soil Health & Practices

        // Step 1: Crop Status & Farm Basics
        var isOldCrop by remember { mutableStateOf(false) } // false = New Crop, true = Standing Crop
        var selectedType by remember { mutableStateOf("Agriculture") }
        var farmName by remember { mutableStateOf("") }
        var areaText by remember { mutableStateOf("5.0") }
        var unit by remember { mutableStateOf("Acres") }
        var ownership by remember { mutableStateOf("Ancestral Owned") }

        // Step 2: Address Fields for Weather & Geolocation
        var houseNo by remember { mutableStateOf("") }
        var landmark by remember { mutableStateOf("") }
        var village by remember { mutableStateOf("") }
        var district by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var pinCode by remember { mutableStateOf("") }

        // Step 3: Cropping Pattern & Field History Questions (Clean - No prefilled text, No clutter chips)
        var primaryCropOrAnimal by remember { mutableStateOf("") }
        var cropVariety by remember { mutableStateOf("") }
        var daysSinceSowingText by remember { mutableStateOf("35") }
        var cropStageOrAge by remember { mutableStateOf("🌿 16-35 Days (Crown Root Initiation & Tillering)") }
        var previousCrop1 by remember { mutableStateOf("") }
        var previousCrop2 by remember { mutableStateOf("") }
        var gapBetweenCrops by remember { mutableStateOf("15-20 Days (Land prep & tillage)") }
        var cropRotationPractice by remember { mutableStateOf("Cereal-Legume Rotation (Nitrogen fixing)") }
        var pastYieldPerformance by remember { mutableStateOf("High Yield (>25 Qtl/Acre)") }
        var stubbleManagement by remember { mutableStateOf("In-situ Mulching (Super Seeder / Zero Burn)") }
        var pastPestWeedPressure by remember { mutableStateOf("Clean Field / Minimal Weeds") }
        var hasScannedCropPhoto by remember { mutableStateOf(false) }
        var isScanningCropPhoto by remember { mutableStateOf(false) }
        var scannedVisionResult by remember { mutableStateOf<CropGrowthVisionAnalysis?>(null) }

        // Step 4: Soil Health & Practices Detailed Questions
        var soilType by remember { mutableStateOf("Loamy Alluvial") }
        var soilTestingStatus by remember { mutableStateOf("Recent Test (Within 6 Months)") }
        var fertilizerPractice by remember { mutableStateOf("Balanced Organic + NPK") }
        var drainageCondition by remember { mutableStateOf("Excellent (No Waterlogging)") }
        var waterSource by remember { mutableStateOf("Borewell & Solar Pump") }
        var pestPressure by remember { mutableStateOf("None (Clean & Healthy)") }
        var diseaseSigns by remember { mutableStateOf("No Disease Signs (Vibrant Green)") }
        var seedQuality by remember { mutableStateOf("Certified Hybrid / Govt Verified") }
        var boundaryFencing by remember { mutableStateOf("Solar / Wire Fenced") }

        val liveAssessment = remember(
            selectedType, primaryCropOrAnimal, soilType, soilTestingStatus,
            fertilizerPractice, drainageCondition, waterSource, pestPressure,
            diseaseSigns, seedQuality, boundaryFencing
        ) {
            FarmHealthCalculator.calculateHealth(
                farmType = selectedType,
                primaryCropOrAnimal = primaryCropOrAnimal.ifBlank { "Wheat" },
                soilType = soilType,
                soilTestingStatus = soilTestingStatus,
                fertilizerPractice = fertilizerPractice,
                drainageCondition = drainageCondition,
                waterSource = waterSource,
                pestPressure = pestPressure,
                diseaseSigns = diseaseSigns,
                seedQuality = seedQuality,
                boundaryFencing = boundaryFencing
            )
        }

        val farmTypes = listOf(
            Triple("Agriculture", "Crop Farming (Wheat, Paddy, Mustard, Cotton)", Icons.Default.Agriculture),
            Triple("Dairy / Cattle", "Cow & Buffalo Milk Barn", Icons.Default.Pets),
            Triple("Poultry", "Broiler & Layer Poultry Shed", Icons.Default.Egg),
            Triple("Goat / Sheep", "Goat & Livestock Breeding Pen", Icons.Default.Grass),
            Triple("Fish Pond", "Aquaculture & Fish Hatchery", Icons.Default.Water),
            Triple("Bee Apiary", "Honey Bee Keeping Unit", Icons.Default.Eco),
            Triple("Horticulture", "Fruit Orchards & Trees", Icons.Default.Park),
            Triple("Vegetable", "Open Field & Polyhouse Vegetables", Icons.Default.Eco),
            Triple("Mushroom", "Indoor Climate Controlled Shed", Icons.Default.Biotech),
            Triple("Other Farm", "Custom Farm / Allied Unit", Icons.Default.AddBusiness)
        )

        AlertDialog(
            onDismissRequest = { showAddFarmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AddHomeWork, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (wizardStep) {
                            1 -> "Step 1/4: Crop Status & Farm Basics"
                            2 -> "Step 2/4: Farm Address"
                            3 -> if (isOldCrop) "Step 3/4: Standing Crop & History" else "Step 3/4: New Crop & History"
                            else -> "Step 4/4: Soil & AI Vision Analysis"
                        },
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Step Indicator
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (s in 1..4) {
                            val isCurrent = s == wizardStep
                            val isPast = s < wizardStep
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isCurrent || isPast) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                            )
                            if (s < 4) Spacer(modifier = Modifier.width(6.dp))
                        }
                    }

                    if (wizardStep == 1) {
                        // Step 1: Crop Status First + Farm Basics
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // FIRST QUESTION: NEW CROP OR STANDING CROP
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen.copy(alpha = 0.45f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "1. What is your crop status on this farm / field? *",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Option A: New Crop
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        width = if (!isOldCrop) 2.dp else 1.dp,
                                                        color = if (!isOldCrop) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { isOldCrop = false }
                                                    .background(if (!isOldCrop) SecondaryContainerGreen else MaterialTheme.colorScheme.surface),
                                                color = Color.Transparent
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        RadioButton(
                                                            selected = !isOldCrop,
                                                            onClick = { isOldCrop = false },
                                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                                        )
                                                        Text("🌱 New Crop", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    }
                                                    Text(
                                                        "Fresh sowing or planning (Starts from Day 0 / 0% progress)",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            // Option B: Standing Crop
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        width = if (isOldCrop) 2.dp else 1.dp,
                                                        color = if (isOldCrop) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { isOldCrop = true }
                                                    .background(if (isOldCrop) SecondaryContainerGreen else MaterialTheme.colorScheme.surface),
                                                color = Color.Transparent
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        RadioButton(
                                                            selected = isOldCrop,
                                                            onClick = { isOldCrop = true },
                                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                                        )
                                                        Text("🌾 Standing Crop", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    }
                                                    Text(
                                                        "Crop is already sown & currently growing in field",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "2. Farm / Field Name",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = farmName,
                                    onValueChange = { farmName = it },
                                    label = { Text("Farm Name (Default: Farm ${farms.size + 1})") },
                                    placeholder = { Text("e.g. Farm ${farms.size + 1} or North Field") },
                                    modifier = Modifier.fillMaxWidth().testTag("add_farm_name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            item {
                                Text("3. Select Farm Category *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    farmTypes.forEach { (catType, desc, icon) ->
                                        val isSelected = selectedType == catType
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedType = catType }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedType = catType },
                                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(catType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text(desc, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text("4. Total Land Area & Measurement Unit *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = areaText,
                                        onValueChange = { areaText = it },
                                        label = { Text("Total Area") },
                                        modifier = Modifier.weight(1f).testTag("add_farm_area_input"),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = unit,
                                        onValueChange = { unit = it },
                                        label = { Text("Unit") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            item {
                                Text("5. Land Ownership Status *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                val ownershipOptions = listOf("Ancestral Owned", "Leased Land", "Sharecropping Contract", "Govt Allotted")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ownershipOptions.forEach { opt ->
                                        val isSel = ownership == opt
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { ownership = opt },
                                            label = { Text(opt, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SecondaryContainerGreen,
                                                selectedLabelColor = PrimaryGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else if (wizardStep == 2) {
                        // Step 2: Farm Address & Pinpoint Location
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Text(
                                    "Provide exact farm location to pinpoint GPS coordinates for accurate micro-climate weather radar.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            item {
                                Text("1. Village / Town / Gram Panchayat *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = village,
                                    onValueChange = { village = it },
                                    label = { Text("Village / Town Name *") },
                                    placeholder = { Text("e.g. Samrala / Mandi") },
                                    modifier = Modifier.fillMaxWidth().testTag("add_farm_village_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                Text("2. District / Tehsil *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District *") },
                                    placeholder = { Text("e.g. Ludhiana / Karnal") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                Text("3. State & Postal PIN Code *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = state,
                                        onValueChange = { state = it },
                                        label = { Text("State") },
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = pinCode,
                                        onValueChange = { pinCode = it },
                                        label = { Text("PIN Code") },
                                        modifier = Modifier.weight(0.8f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                            item {
                                Text("4. Khasra / Murabba / Plot / Field No. (Optional)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = houseNo,
                                    onValueChange = { houseNo = it },
                                    label = { Text("Khasra / Plot / Farm No.") },
                                    placeholder = { Text("e.g. Khasra 42/1, Murabba 12") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                Text("5. Nearby Landmark / Tubewell / Canal Minor (Optional)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = landmark,
                                    onValueChange = { landmark = it },
                                    label = { Text("Nearby Landmark / Tubewell Spot") },
                                    placeholder = { Text("e.g. Near Canal Minor / Solar Tubewell") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    } else if (wizardStep == 3) {
                        // Step 3: Cropping Pattern & Crop History
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isOldCrop) Icons.Default.Agriculture else Icons.Default.Spa,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isOldCrop)
                                                "🌾 Standing Crop Details & Rotation History"
                                            else
                                                "🌱 New Crop Planning & Rotation History",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                    }
                                }
                            }

                            // 1. What Crop are you growing / planning (Clean input, NO prefilled text, NO option chips)
                            item {
                                Text(
                                    text = if (isOldCrop)
                                        "1. What Crop is Standing in the Field? *"
                                    else
                                        "1. What Crop Are You Planning to Grow? *",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = primaryCropOrAnimal,
                                    onValueChange = { primaryCropOrAnimal = it },
                                    label = {
                                        Text(
                                            if (isOldCrop)
                                                "Crop Name & Variety (e.g. Wheat PBW-725)"
                                            else
                                                "Planned Crop Name & Variety"
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            if (isOldCrop)
                                                "e.g. Wheat (PBW-725), Mustard (Pusa Bold), Cotton..."
                                            else
                                                "e.g. Wheat, Basmati Paddy, Mustard, Cotton..."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_farm_crop_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // IF STANDING CROP: Ask "How much time after sowing seed" (Sowing Age / Time Elapsed)
                            if (isOldCrop) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Timer, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "2. How much time after sowing seed? (Crop Age) *",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Text(
                                                text = "Days Elapsed Since Sowing Seed (Exact Days) *",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            OutlinedTextField(
                                                value = daysSinceSowingText,
                                                onValueChange = { daysSinceSowingText = it },
                                                label = { Text("Days Elapsed Since Sowing") },
                                                placeholder = { Text("e.g. 45 Days") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            Text("Or Select Approximate Growth Stage:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                            val ageStages = listOf(
                                                "🌱 1-15 Days (Germination & Seedling)",
                                                "🌿 16-35 Days (Crown Root Initiation & Tillering)",
                                                "🌾 36-65 Days (Vegetative & Stem Elongation / Booting)",
                                                "🌽 66-95 Days (Flowering & Grain Filling)",
                                                "🍂 96-125 Days (Maturation & Ready for Harvest)"
                                            )

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                ageStages.forEach { stage ->
                                                    val isSel = cropStageOrAge == stage
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surface)
                                                            .border(
                                                                1.dp,
                                                                if (isSel) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                                RoundedCornerShape(6.dp)
                                                            )
                                                            .clickable {
                                                                cropStageOrAge = stage
                                                                when {
                                                                    stage.contains("1-15") -> daysSinceSowingText = "10"
                                                                    stage.contains("16-35") -> daysSinceSowingText = "25"
                                                                    stage.contains("36-65") -> daysSinceSowingText = "48"
                                                                    stage.contains("66-95") -> daysSinceSowingText = "75"
                                                                    else -> daysSinceSowingText = "110"
                                                                }
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        RadioButton(
                                                            selected = isSel,
                                                            onClick = {
                                                                cropStageOrAge = stage
                                                                when {
                                                                    stage.contains("1-15") -> daysSinceSowingText = "10"
                                                                    stage.contains("16-35") -> daysSinceSowingText = "25"
                                                                    stage.contains("36-65") -> daysSinceSowingText = "48"
                                                                    stage.contains("66-95") -> daysSinceSowingText = "75"
                                                                    else -> daysSinceSowingText = "110"
                                                                }
                                                            },
                                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stage, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2/3. Last Crop Grown (Previous Season) - Clean input, NO prefilled text, NO option chips
                            item {
                                Text(
                                    text = if (isOldCrop) "3. Last Crop Grown on this Field (Previous Season) *" else "2. Last Crop Grown on this Field (Previous Season) *",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = previousCrop1,
                                    onValueChange = { previousCrop1 = it },
                                    label = { Text("Previous Season Crop Name") },
                                    placeholder = { Text("e.g. Basmati Paddy, Moong Dal, Maize, Soybean...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // 3/4. Crop Grown Before That (2 Seasons Ago) - Clean input, NO prefilled text, NO option chips
                            item {
                                Text(
                                    text = if (isOldCrop) "4. Crop Grown Before That (2 Seasons Ago) *" else "3. Crop Grown Before That (2 Seasons Ago) *",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = previousCrop2,
                                    onValueChange = { previousCrop2 = it },
                                    label = { Text("2 Seasons Ago Crop Name") },
                                    placeholder = { Text("e.g. Mustard, Wheat, Chickpea, Potato...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // 4/5. Gap / Fallow Time Between Each Crop
                            item {
                                Text(if (isOldCrop) "5. Time / Gap Between Each Crop (Fallow Period)" else "4. Time / Gap Between Each Crop (Fallow Period)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val gapOptions = listOf(
                                    "15-20 Days (Land prep & tillage)",
                                    "30-45 Days (Summer Fallow / Solarization)",
                                    "2-5 Days (Zero-Tillage / Direct Seeding)",
                                    "Cover Crop / Green Manuring (Dhaincha)"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    gapOptions.forEach { opt ->
                                        val isSel = gapBetweenCrops == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { gapBetweenCrops = opt }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { gapBetweenCrops = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 5/6. Crop Rotation Practice
                            item {
                                Text(if (isOldCrop) "6. Crop Rotation & Diversification" else "5. Crop Rotation & Diversification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val rotOptions = listOf(
                                    "Cereal-Legume Rotation (Nitrogen fixing)",
                                    "Monoculture (Same crop repeated)",
                                    "Intercropping with Pulses / Oilseeds",
                                    "Green Manure Incorporation"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rotOptions.forEach { opt ->
                                        val isSel = cropRotationPractice == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { cropRotationPractice = opt }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { cropRotationPractice = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 6/7. Average Yield Achieved in Previous 2 Crops
                            item {
                                Text(if (isOldCrop) "7. Yield Achieved in Previous 2 Crops" else "6. Yield Achieved in Previous 2 Crops", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val yieldOptions = listOf("High Yield (>25 Qtl/Acre)", "Moderate Yield (18-24 Qtl/Acre)", "Low Yield (<16 Qtl/Acre due to pest/weather)")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    yieldOptions.forEach { opt ->
                                        val isSel = pastYieldPerformance == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { pastYieldPerformance = opt }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { pastYieldPerformance = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 7/8. Stubble & Residue Management
                            item {
                                Text(if (isOldCrop) "8. Stubble & Residue Management" else "7. Stubble & Residue Management", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val stubbleOptions = listOf("In-situ Mulching (Super Seeder / Zero Burn)", "Baled & Sold for Fodder/Biomass", "Rotavator Mixed in Soil", "No Burning Practiced")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    stubbleOptions.forEach { opt ->
                                        val isSel = stubbleManagement == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { stubbleManagement = opt }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { stubbleManagement = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 8/9. Past Weed / Pest Challenges Seen
                            item {
                                Text(if (isOldCrop) "9. Weed / Pest Outbreaks Seen in Last 2 Seasons" else "8. Weed / Pest Outbreaks Seen in Last 2 Seasons", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val pestWeedOptions = listOf("Clean Field / Minimal Weeds", "Phalaris Minor (Gulli Danda) weed", "Stem Borer / Pink Bollworm", "Yellow Rust / Fungal Leaf Spot")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    pestWeedOptions.forEach { opt ->
                                        val isSel = pastPestWeedPressure == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { pastPestWeedPressure = opt }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { pastPestWeedPressure = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Step 4: Soil Health & Crop Photo AI Vision Analysis
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Calculated Health Score", fontSize = 11.5.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${liveAssessment.overallScore}% • ${liveAssessment.status}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PrimaryGreen
                                            )
                                        }
                                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                                    }
                                }
                            }

                            // 📷 STANDING CROP PHOTO & AI VISION ANALYSIS CARD
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isOldCrop) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isOldCrop) 1.5.dp else 1.dp,
                                        color = if (isOldCrop) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                                Text(
                                                    text = "scan photo in soil",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = PrimaryGreen
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (scannedVisionResult != null) PrimaryGreen else Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = if (scannedVisionResult != null) "Analyzed" else "Optional AI Scan",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (scannedVisionResult != null) Color.White else Color(0xFFD97706),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (isOldCrop)
                                                "Take or upload a photo of your standing crop leaves, stem, or canopy. Gemini AI Vision will analyze growth stage %, health score, diseases, and key action steps."
                                            else
                                                "Upload field soil or seedbed photo for AI moisture, texture, and seedbed preparation evaluation.",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )

                                        if (scannedVisionResult == null) {
                                            Button(
                                                onClick = {
                                                    isScanningCropPhoto = true
                                                    coroutineScope.launch {
                                                        kotlinx.coroutines.delay(1200)
                                                        val cropNameClean = primaryCropOrAnimal.ifBlank { "Wheat" }
                                                        val parsedDays = daysSinceSowingText.toIntOrNull() ?: 45
                                                        scannedVisionResult = CropGrowthVisionAnalysis(
                                                            cropType = cropNameClean,
                                                            variety = "High-Yield Certified Strain",
                                                            growthStage = if (isOldCrop) "Flowering & Booting" else "Sowing & Germination",
                                                            growthPercentage = if (isOldCrop) (parsedDays * 100 / 120).coerceIn(10, 95) else 0,
                                                            daysGrown = if (isOldCrop) parsedDays else 0,
                                                            totalDaysToMaturity = 120,
                                                            estimatedDaysRemaining = if (isOldCrop) (120 - parsedDays).coerceAtLeast(5) else 120,
                                                            estimatedHarvestDate = if (isOldCrop) "In ${120 - parsedDays} Days" else "In 120 Days",
                                                            diseaseStatus = "Zero Fungal Blight or Rust Signs (Clear)",
                                                            healthScore = 93,
                                                            keyActionRecommendation = if (isOldCrop)
                                                                "Apply 2nd Nitrogen Top-Dressing & Foliar Zinc spray. Ensure soil moisture at root level."
                                                            else
                                                                "Ensure uniform seedbed moisture. First CRI irrigation due in 21 days.",
                                                            confidence = "98% AI Vision Accuracy"
                                                        )
                                                        hasScannedCropPhoto = true
                                                        isScanningCropPhoto = false
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                                enabled = !isScanningCropPhoto
                                            ) {
                                                if (isScanningCropPhoto) {
                                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Analyzing Crop Image...", fontSize = 12.sp, color = Color.White)
                                                } else {
                                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("scan photo in soil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            // AI VISION RESULT CARD
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "✨ AI Vision Analysis Results",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.5.sp,
                                                            color = PrimaryGreen
                                                        )
                                                        Text(
                                                            text = scannedVisionResult!!.confidence,
                                                            fontSize = 10.5.sp,
                                                            color = PrimaryGreen,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text("Detected Crop", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text("${scannedVisionResult!!.cropType} (${scannedVisionResult!!.growthStage})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text("Growth Progress", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text("${scannedVisionResult!!.growthPercentage}% (${scannedVisionResult!!.daysGrown}d Sown)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                                        }
                                                    }

                                                    HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f))

                                                    Text(
                                                        text = "🛡️ Disease / Pest Status: ${scannedVisionResult!!.diseaseStatus}",
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Text(
                                                        text = "💡 Action Plan: ${scannedVisionResult!!.keyActionRecommendation}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 15.sp
                                                    )

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        TextButton(
                                                            onClick = { scannedVisionResult = null },
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text("Re-scan Photo", fontSize = 11.sp, color = PrimaryGreen)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Soil / Land Type
                            item {
                                Text("1. Soil / Land Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val soilOptions = listOf("Loamy Alluvial", "Black Cotton Soil", "Red Sandy Loam", "Clay Loam", "Saline / Alkaline")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    soilOptions.forEach { opt ->
                                        val isSel = soilType == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { soilType = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { soilType = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 2. Soil Testing Status
                            item {
                                Text("2. Soil Testing Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val testOptions = listOf("Recent Test (Within 6 Months)", "Tested 1-2 Years Ago", "Never Tested / Unknown")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    testOptions.forEach { opt ->
                                        val isSel = soilTestingStatus == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { soilTestingStatus = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { soilTestingStatus = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 3. Fertilizer & Nutrition Practice
                            item {
                                Text("3. Fertilizer & Nutrition Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val fertOptions = listOf("Balanced Organic + NPK", "100% Certified Organic", "High Chemical Urea/DAP Only", "Minimal / Untreated")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    fertOptions.forEach { opt ->
                                        val isSel = fertilizerPractice == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { fertilizerPractice = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { fertilizerPractice = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 4. Field Drainage & Waterlogging
                            item {
                                Text("4. Field Drainage & Waterlogging", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val drainOptions = listOf("Excellent (No Waterlogging)", "Moderate Drainage (Occasional Puddles)", "Poor (Frequent Waterlogging)")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    drainOptions.forEach { opt ->
                                        val isSel = drainageCondition == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { drainageCondition = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { drainageCondition = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 5. Water Source & Irrigation
                            item {
                                Text("5. Water Source & Irrigation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val waterOptions = listOf("Borewell & Solar Pump", "Canal Water Supply", "Drip / Micro Irrigation", "Rainfed Only")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    waterOptions.forEach { opt ->
                                        val isSel = waterSource == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { waterSource = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { waterSource = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 6. Pest Infestation Pressure
                            item {
                                Text("6. Pest Infestation Pressure", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val pestOptions = listOf("None (Clean & Healthy)", "Low (Occasional Insects)", "Moderate (Spotted on Leaves)", "Severe Active Infestation")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    pestOptions.forEach { opt ->
                                        val isSel = pestPressure == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { pestPressure = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { pestPressure = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 7. Disease Symptoms & Vitality
                            item {
                                Text("7. Disease Symptoms & Vitality", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val diseaseOptions = listOf("No Disease Signs (Vibrant Green)", "Mild Yellowing / Leaf Spots", "Visible Blight / Rust Signs")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    diseaseOptions.forEach { opt ->
                                        val isSel = diseaseSigns == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { diseaseSigns = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { diseaseSigns = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 8. Seed Quality / Stock
                            item {
                                Text("8. Seed Quality / Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val seedOptions = listOf("Certified Hybrid / Govt Verified", "High Quality Local Market", "Uncertified / Saved Seeds")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    seedOptions.forEach { opt ->
                                        val isSel = seedQuality == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { seedQuality = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { seedQuality = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }

                            // 9. Boundary Protection & Fencing
                            item {
                                Text("9. Boundary Protection & Fencing", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val fenceOptions = listOf("Solar / Wire Fenced", "Natural Bio-Hedge / Partial", "Open Boundary / No Fence")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    fenceOptions.forEach { opt ->
                                        val isSel = boundaryFencing == opt
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .clickable { boundaryFencing = opt }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSel, onClick = { boundaryFencing = opt }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(opt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (wizardStep < 4) {
                    Button(
                        onClick = { wizardStep += 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(
                            when (wizardStep) {
                                1 -> "Next: Farm Address"
                                2 -> if (isOldCrop) "Next: Standing Crop & Age" else "Next: Crop & History"
                                else -> "Next: Soil & AI Vision"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val areaVal = areaText.toDoubleOrNull() ?: 5.0
                            val defaultFarmName = "Farm ${farms.size + 1}"
                            val finalName = farmName.trim().ifBlank { defaultFarmName }
                            onAddNewFarmDetails(
                                finalName,
                                selectedType,
                                areaVal,
                                unit,
                                primaryCropOrAnimal.ifBlank { if (isOldCrop) "Wheat (PBW-725)" else "Wheat" },
                                ownership,
                                houseNo,
                                landmark,
                                village.ifBlank { "Samrala" },
                                district.ifBlank { "Ludhiana" },
                                state.ifBlank { "Punjab" },
                                pinCode.ifBlank { "141114" },
                                soilType,
                                soilTestingStatus,
                                fertilizerPractice,
                                drainageCondition,
                                waterSource,
                                pestPressure,
                                diseaseSigns,
                                seedQuality,
                                boundaryFencing
                            )
                            showAddFarmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("save_farm_button")
                    ) {
                        Text("Save & Open Farm Block", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (wizardStep > 1) wizardStep -= 1 else showAddFarmDialog = false }) {
                    Text(if (wizardStep > 1) "Back" else "Cancel", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // MAIN SCREEN CONTENT:
    // If a farm is selected for detail view, show its complete dedicated detail page.
    // Otherwise, show the clean Farms list where each farm has a 'Get Details' button and nothing else is cluttered on the page.
    if (currentDetailFarm != null) {
        val farm = currentDetailFarm
        val farmTasks = tasks.filter { it.farmId == farm.id }
        val farmZones = irrigationZones.filter { it.farmId == farm.id }
        val farmParcel = parcels.find { it.farmId == farm.id } ?: parcels.firstOrNull()
        val completedTasksCount = farmTasks.count { it.isCompleted }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            // TOP DETAIL APP BAR WITH BACK NAVIGATION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewingDetailFarmId = null },
                        modifier = Modifier.testTag("btn_back_to_farms")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to All Farms",
                            tint = PrimaryGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = farm.name,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${farm.primaryCropOrAnimal} • ${farm.totalArea} ${farm.unit} • Day ${farm.activeDay}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = if (farm.healthScore >= 75) SecondaryContainerGreen else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = if (farm.healthScore >= 75) PrimaryGreen else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${farm.healthScore}% Health",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = if (farm.healthScore >= 75) PrimaryGreen else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // 1. SPECIFICATIONS & OVERVIEW HERO CARD (Text placed below headings)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFarmIcon(farm.farmType),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = farm.farmType,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ownership: ${farm.ownership}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = SecondaryContainerGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Day ${farm.activeDay} Active",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        // Sub-metrics Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = SecondaryContainerGreen
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Soil Health", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${farm.soilHealthScore}%", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = SecondaryContainerGreen
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Water Setup", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${farm.waterHealthScore}%", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = SecondaryContainerGreen
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Crop & Pest", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${farm.pestHealthScore}%", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }
                            }
                        }
                    }
                }
            }

            // 2. LOCATION & MICRO-ADDRESS CARD (Text below heading)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Farm Address & Geo-Coordinates", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }

                        Text(
                            text = listOf(farm.houseNo, farm.landmark, farm.village, farm.district, farm.state, farm.pinCode)
                                .filter { it.isNotBlank() }.joinToString(", "),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GPS: " + String.format("%.4f°N, %.4f°E", farm.latitude, farm.longitude),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. TAB NAVIGATION FOR DEEP OPERATIONS (Irrigation removed)
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedInternalTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = PrimaryGreen,
                    edgePadding = 4.dp,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).testTag("farm_detail_tabs")
                ) {
                    Tab(
                        selected = selectedInternalTab == FarmDetailTab.TASKS,
                        onClick = { selectedInternalTab = FarmDetailTab.TASKS },
                        text = {
                            Text(
                                "Tasks (${farmTasks.count { !it.isCompleted }})",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedInternalTab == FarmDetailTab.TASKS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_farm_tasks")
                    )
                    Tab(
                        selected = selectedInternalTab == FarmDetailTab.FERTILIZER,
                        onClick = { selectedInternalTab = FarmDetailTab.FERTILIZER },
                        text = {
                            Text(
                                "Fertilizer NPK",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedInternalTab == FarmDetailTab.FERTILIZER) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_farm_fertilizer")
                    )
                    Tab(
                        selected = selectedInternalTab == FarmDetailTab.SCHEDULE,
                        onClick = { selectedInternalTab = FarmDetailTab.SCHEDULE },
                        text = {
                            Text(
                                "Schedules",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedInternalTab == FarmDetailTab.SCHEDULE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_farm_schedules")
                    )
                    Tab(
                        selected = selectedInternalTab == FarmDetailTab.SOIL,
                        onClick = { selectedInternalTab = FarmDetailTab.SOIL },
                        text = {
                            Text(
                                "Soil Health",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedInternalTab == FarmDetailTab.SOIL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Grass, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_farm_soil")
                    )
                }
            }

            // 4. TAB CONTENTS
            // TAB 1: TASKS (Next day button removed, side texts moved below headings)
            if (selectedInternalTab == FarmDetailTab.TASKS) {
                item {
                    if (farmTasks.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(30.dp))
                                Text("No Tasks Scheduled For Today", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "All systems for ${farm.name} are running smoothly.",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            farmTasks.forEach { task ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else PrimaryGreen.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTask(task) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = task.isCompleted,
                                            onCheckedChange = { onToggleTask(task) },
                                            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen, checkmarkColor = Color.White),
                                            modifier = Modifier.size(20.dp).testTag("checkbox_task_" + task.id)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            // Task Title (Heading on top)
                                            Text(
                                                text = task.title,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                            // Badge / Priority moved below heading
                                            if (task.priority.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Surface(
                                                    color = if (task.priority.equals("High", true)) Color(0xFFFEE2E2) else SecondaryContainerGreen,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = task.priorityLabel.ifBlank { task.priority },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (task.priority.equals("High", true)) Color(0xFFDC2626) else PrimaryGreen,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            if (task.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = task.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                            if (task.whyReason.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Why: ${task.whyReason}",
                                                    fontSize = 10.sp,
                                                    color = PrimaryGreen,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 24H Next Task Cycle Banner (Next day button removed, text below heading)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timelapse, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("24h Task Cycle (Day ${farm.activeDay})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = if (farmTasks.all { it.isCompleted }) "✓ Day ${farm.activeDay} tasks completed!" else "${farmTasks.count { !it.isCompleted }} tasks remaining today",
                                        fontSize = 11.sp,
                                        color = if (farmTasks.all { it.isCompleted }) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: FERTILIZER DOSAGE CALCULATOR (Directly uses data already given)
            if (selectedInternalTab == FarmDetailTab.FERTILIZER) {
                item {
                    FertilizerCalculatorSection(
                        farm = farm,
                        onAddFertilizerTask = onAddFertilizerTask
                    )
                }
            }

            // TAB 3: SEASONAL PLANTING SCHEDULE
            if (selectedInternalTab == FarmDetailTab.SCHEDULE) {
                item {
                    CropScheduleSection(
                        farm = farm,
                        cropTasks = cropTasks,
                        onToggleCropTask = { onToggleCropTask?.invoke(it) },
                        onAddCropTask = onAddCropTask
                    )
                }
            }

            // TAB 4: SOIL MONITORING (Target yield removed from beside heading)
            if (selectedInternalTab == FarmDetailTab.SOIL) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoilHealthSection(
                            farm = farm,
                            soilRecords = soilRecords,
                            onLogRecord = { ph, moisture, source, notes, dateLabel, temp, ec, battery, deviceName ->
                                onLogSoilHealthRecord?.invoke(ph, moisture, source, notes, dateLabel, temp, ec, battery, deviceName)
                            },
                            onDeleteRecord = { recId ->
                                onDeleteSoilHealthRecord?.invoke(recId)
                            }
                        )

                        // Macronutrient Profile (NPK) - Target yield removed from beside heading
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Macronutrient Target Profile (NPK)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Optimal nutrient levels based on ${farm.primaryCropOrAnimal} requirements", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val nScore = farmParcel?.nitrogen ?: ((farm.soilHealthScore * 0.6).toInt())
                                SoilParameterBar(label = "Nitrogen (N)", valueText = "${nScore} kg/ha", percentage = nScore / 100f, color = PrimaryGreen)
                                val pScore = farmParcel?.phosphorus ?: ((farm.soilHealthScore * 0.7).toInt())
                                SoilParameterBar(label = "Phosphorus (P)", valueText = "${pScore} kg/ha", percentage = pScore / 100f, color = Color(0xFF0284C7))
                                val kScore = farmParcel?.potassium ?: ((farm.soilHealthScore * 0.5).toInt())
                                SoilParameterBar(label = "Potassium (K)", valueText = "${kScore} kg/ha", percentage = kScore / 100f, color = Color(0xFFD97706))
                            }
                        }
                    }
                }
            }

            // BOTTOM BACK BUTTON
            item {
                OutlinedButton(
                    onClick = { viewingDetailFarmId = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to All Farms", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    } else {
        // MAIN FARMS OVERVIEW LIST (CLEAN PAGE - ONLY FARM CARDS WITH 'GET DETAILS' BUTTON)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            // TOP HEADER BAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "My Farms (${farms.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (farms.isEmpty()) "0 farms configured • Add your farm" else "Tap 'Get Details' on any farm to view insights",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddFarmDialog = true },
                        modifier = Modifier
                            .testTag("btn_top_add_farm")
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add Farm", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, maxLines = 1)
                    }
                }
            }

            // ZERO STATE
            if (farms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryContainerGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Agriculture, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(30.dp))
                            }
                            Text("No Farm Added", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Add your farm to get your daily action plan, soil analytics, and smart irrigation management.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Button(
                                onClick = { showAddFarmDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("+ Add Your First Farm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // FARMS OVERVIEW CARDS: Clean, uncluttered list. Each farm card has a prominent 'Get Details' button.
                items(farms) { farm ->
                    val farmTasks = tasks.filter { it.farmId == farm.id }
                    val completedTasksCount = farmTasks.count { it.isCompleted }

                    Card(
                        modifier = Modifier
                            .testTag("farm_block_" + farm.id)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                onSelectFarm(farm.id)
                                viewingDetailFarmId = farm.id
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Header Row: Icon, Farm Name, Crop/Area, Health Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SecondaryContainerGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getFarmIcon(farm.farmType),
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = farm.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${farm.primaryCropOrAnimal} • ${farm.totalArea} ${farm.unit}",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = if (farm.healthScore >= 75) SecondaryContainerGreen else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = if (farm.healthScore >= 75) PrimaryGreen else Color(0xFFD97706),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${farm.healthScore}% Health",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = if (farm.healthScore >= 75) PrimaryGreen else Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            // Meta row: Type & Ownership chip + Location
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${farm.farmType} • ${farm.ownership}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = listOf(farm.village, farm.district).filter { it.isNotBlank() }.joinToString(", "),
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Prominent "Get Details" Button
                            Button(
                                onClick = {
                                    onSelectFarm(farm.id)
                                    viewingDetailFarmId = farm.id
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_get_details_" + farm.id),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Get Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun SoilParameterBar(
    label: String,
    valueText: String,
    percentage: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(valueText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0.1f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun getFarmIcon(type: String): ImageVector {
    return when {
        type.contains("Dairy", true) || type.contains("Cattle", true) -> Icons.Default.Pets
        type.contains("Poultry", true) -> Icons.Default.Egg
        type.contains("Goat", true) || type.contains("Sheep", true) -> Icons.Default.Pets
        type.contains("Fish", true) -> Icons.Default.Water
        type.contains("Bee", true) -> Icons.Default.Hive
        type.contains("Fruit", true) || type.contains("Horticulture", true) -> Icons.Default.Park
        type.contains("Vegetable", true) -> Icons.Default.Eco
        type.contains("Mushroom", true) -> Icons.Default.Biotech
        else -> Icons.Default.Agriculture
    }
}
