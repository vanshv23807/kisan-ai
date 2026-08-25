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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.t
import com.example.data.model.FarmDraftItem
import com.example.ui.components.MapLocationPickerDialog
import com.example.ui.components.StatePickerDialog
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

data class FarmTypeInfo(
    val title: String,
    val icon: ImageVector,
    val category: String,
    val defaultSoilOrHousing: String,
    val defaultPrimaryAsset: String,
    val unitDefault: String = "Acres"
)

val ALL_FARM_TYPES = listOf(
    FarmTypeInfo("Agriculture / Crop Farm", Icons.Default.Agriculture, "Crops", "Loamy Alluvial", "Wheat & Mustard"),
    FarmTypeInfo("Horticulture / Fruit Farm", Icons.Default.Park, "Orchard", "Well-Drained Loam", "Mango & Apple"),
    FarmTypeInfo("Vegetable Farm", Icons.Default.Eco, "Vegetables", "Rich Organic Loam", "Tomato, Onion & Chili"),
    FarmTypeInfo("Cattle / Dairy Farm", Icons.Default.Pets, "Livestock", "Ventilated Free Stall", "Gir Cow & Murrah Buffalo"),
    FarmTypeInfo("Poultry Farm", Icons.Default.Egg, "Poultry", "Deep Litter Shed", "Broiler (500 Birds)"),
    FarmTypeInfo("Goat Farm", Icons.Default.Pets, "Small Ruminant", "Semi-Intensive Pen", "Beetal & Sirohi Goats"),
    FarmTypeInfo("Sheep Farm", Icons.Default.Pets, "Small Ruminant", "Pasture & Covered Shed", "Marwari & Nellore Sheep"),
    FarmTypeInfo("Pig Farm", Icons.Default.Pets, "Piggery", "Slotted Floor Barn", "Large White Yorkshire"),
    FarmTypeInfo("Fish / Aquaculture Farm", Icons.Default.Water, "Aquaculture", "Earthen Pond (6ft)", "Rohu, Catla & Tilapia"),
    FarmTypeInfo("Beekeeping / Honey Farm", Icons.Default.Spa, "Apiculture", "Langstroth Wooden Hives", "Apis Mellifera (20 Hives)"),
    FarmTypeInfo("Mushroom Farm", Icons.Default.Grass, "Indoor", "Climate Controlled Room", "Button & Oyster Mushroom"),
    FarmTypeInfo("Nursery / Plant Farm", Icons.Default.Yard, "Nursery", "Greenhouse & Shade Net", "Saplings & Fruit Trees"),
    FarmTypeInfo("Organic / Natural Farm", Icons.Default.Verified, "Organic", "Compost Enriched Soil", "Desi Wheat & Pulses"),
    FarmTypeInfo("Mixed Farm", Icons.Default.Hub, "Integrated", "Integrated Farm System", "Crops + Dairy + Poultry"),
    FarmTypeInfo("Other Farm", Icons.Default.HomeWork, "General", "Standard Agricultural", "Custom Farm Assets")
)

val BIGGEST_PROBLEMS_OPTIONS = listOf(
    "Pest Attacks & Insect Damage",
    "Sudden Mandi Price Drop",
    "Water Scarcity & Drought",
    "High Fertilizer & Seed Costs",
    "Labor Shortage during Harvest",
    "Crop Disease & Fungal Blight"
)

val FINANCIAL_LOSS_OPTIONS = listOf(
    "Overuse of Chemical Fertilizers",
    "Post-Harvest Storage Spoilage",
    "Unpredictable Weather & Unseasonal Rain",
    "High Fuel & Machinery Rental Costs",
    "Poor Quality / Non-Certified Seeds",
    "Delayed Disease Diagnosis"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(
    currentStep: Int,
    // Step 1: Farmer Identity
    name: String,
    onNameChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    farmerId: String,
    onFarmerIdChange: (String) -> Unit,
    aadhaar: String,
    onAadhaarChange: (String) -> Unit,
    isOtpSent: Boolean,
    onSendOtp: () -> Unit,
    otpCode: String,
    onOtpCodeChange: (String) -> Unit,
    isOtpVerified: Boolean,
    onVerifyOtp: (String) -> Unit,
    otpHelperMessage: String?,
    age: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    farmingType: String,
    onFarmingTypeChange: (String) -> Unit,
    // Step 2: Address & Location
    houseNo: String,
    onHouseNoChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    state: String,
    onStateChange: (String) -> Unit,
    district: String,
    onDistrictChange: (String) -> Unit,
    village: String,
    onVillageChange: (String) -> Unit,
    pinCode: String,
    onPinCodeChange: (String) -> Unit,
    latitude: Double,
    longitude: Double,
    onLocationPicked: (state: String, district: String, village: String, pin: String, lat: Double, lng: Double) -> Unit,
    // Step 3: Multi-Farm System
    farmsList: List<FarmDraftItem>,
    onAddFarmDraft: (type: String) -> Unit,
    onRemoveFarmDraft: (id: String) -> Unit,
    onUpdateFarmDraft: (FarmDraftItem) -> Unit,
    // Step 4: Challenges & AI Setup (Selecting only)
    biggestProblem: String,
    onBiggestProblemChange: (String) -> Unit,
    financialLossCause: String,
    onFinancialLossCauseChange: (String) -> Unit,
    aiPriorities: Set<String>,
    onToggleAiPriority: (String) -> Unit,
    // Wizard Navigation
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onFinish: () -> Unit
) {
    var showStatePicker by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var showAddFarmTypeDialog by remember { mutableStateOf(false) }

    if (showStatePicker) {
        StatePickerDialog(
            currentState = state,
            onDismiss = { showStatePicker = false },
            onStateSelected = { selected ->
                onStateChange(selected)
            }
        )
    }

    if (showMapPicker) {
        MapLocationPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { pickedState, pickedDist, pickedVill, pickedPin, lat, lng ->
                onLocationPicked(pickedState, pickedDist, pickedVill, pickedPin, lat, lng)
            }
        )
    }

    if (showAddFarmTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddFarmTypeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Farm Type to Add", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ALL_FARM_TYPES) { farmTypeInfo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddFarmDraft(farmTypeInfo.title.split("/")[0].trim())
                                    showAddFarmTypeDialog = false
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryContainerGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = farmTypeInfo.icon,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = farmTypeInfo.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        softWrap = true
                                    )
                                    Text(
                                        text = "${farmTypeInfo.category} • ${farmTypeInfo.defaultSoilOrHousing}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = true
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddFarmTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KisanAI Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> "Step 1 of 4: Farmer Profile & OTP"
                                2 -> "Step 2 of 4: Address & Location"
                                3 -> "Step 3 of 4: Challenges & AI Setup"
                                else -> "Step 4 of 4: AI Profile Activation"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onPrevStep,
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("back")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = onPrevStep,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .testTag("onboarding_prev_button")
                                .height(50.dp)
                        ) {
                            Text(t("back"), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < 4) onNextStep()
                            else onFinish()
                        },
                        modifier = Modifier
                            .testTag(if (currentStep == 4) "onboarding_finish_button" else "onboarding_next_button")
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentStep == 4) "Launch Kisan AI" else t("next"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (currentStep == 4) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 4f },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryGreen,
                trackColor = PrimaryGreen.copy(alpha = 0.2f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        item {
                            Step1FarmerProfile(
                                name = name,
                                onNameChange = onNameChange,
                                mobile = mobile,
                                onMobileChange = onMobileChange,
                                farmerId = farmerId,
                                onFarmerIdChange = onFarmerIdChange,
                                aadhaar = aadhaar,
                                onAadhaarChange = onAadhaarChange,
                                isOtpSent = isOtpSent,
                                onSendOtp = onSendOtp,
                                otpCode = otpCode,
                                onOtpCodeChange = onOtpCodeChange,
                                isOtpVerified = isOtpVerified,
                                onVerifyOtp = onVerifyOtp,
                                otpHelperMessage = otpHelperMessage,
                                age = age,
                                onAgeChange = onAgeChange,
                                gender = gender,
                                onGenderChange = onGenderChange,
                                farmingType = farmingType,
                                onFarmingTypeChange = onFarmingTypeChange
                            )
                        }
                    }
                    2 -> {
                        item {
                            Step2AddressAndLocation(
                                houseNo = houseNo,
                                onHouseNoChange = onHouseNoChange,
                                landmark = landmark,
                                onLandmarkChange = onLandmarkChange,
                                state = state,
                                onOpenStatePicker = { showStatePicker = true },
                                district = district,
                                onDistrictChange = onDistrictChange,
                                village = village,
                                onVillageChange = onVillageChange,
                                pinCode = pinCode,
                                onPinCodeChange = onPinCodeChange,
                                onOpenMapPicker = { showMapPicker = true }
                            )
                        }
                    }
                    3 -> {
                        item {
                            Step4ChallengesAndAiSelection(
                                biggestProblem = biggestProblem,
                                onSelectBiggestProblem = onBiggestProblemChange,
                                financialLossCause = financialLossCause,
                                onSelectFinancialLossCause = onFinancialLossCauseChange,
                                aiPriorities = aiPriorities,
                                onToggleAiPriority = onToggleAiPriority
                            )
                        }
                    }
                    4 -> {
                        item {
                            Step5DigitalProfileActivation(
                                name = name,
                                mobile = mobile,
                                village = village,
                                state = state,
                                farmsList = farmsList
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step1FarmerProfile(
    name: String,
    onNameChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    farmerId: String,
    onFarmerIdChange: (String) -> Unit,
    aadhaar: String,
    onAadhaarChange: (String) -> Unit,
    isOtpSent: Boolean,
    onSendOtp: () -> Unit,
    otpCode: String,
    onOtpCodeChange: (String) -> Unit,
    isOtpVerified: Boolean,
    onVerifyOtp: (String) -> Unit,
    otpHelperMessage: String?,
    age: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    farmingType: String,
    onFarmingTypeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Farmer Profile & Verification",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Please enter your identity and contact details to link your AI Farm Manager.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Farmer Full Name") },
            placeholder = { Text("e.g. Ramesh Kumar") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_name")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Mobile Number & OTP Verification
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mobile Number & OTP", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (isOtpVerified) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecondaryContainerGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verified", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = onMobileChange,
                        label = { Text("Mobile Number") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen)
                        },
                        modifier = Modifier
                            .testTag("input_farmer_mobile")
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSendOtp,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("btn_send_otp")
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOtpSent) MaterialTheme.colorScheme.surfaceVariant else PrimaryGreen,
                            contentColor = if (isOtpSent) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    ) {
                        Text(if (isOtpSent) "Resend" else "Send OTP", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // OTP Entry
                if (isOtpSent && !isOtpVerified) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = onOtpCodeChange,
                            label = { Text("Enter 4-Digit OTP") },
                            placeholder = { Text("5892") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PrimaryGreen)
                            },
                            modifier = Modifier
                                .testTag("input_otp_code")
                                .weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onVerifyOtp(otpCode) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .testTag("btn_verify_otp")
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Verify", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (otpHelperMessage != null) {
                    Text(
                        text = otpHelperMessage,
                        fontSize = 11.5.sp,
                        color = if (isOtpVerified) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Farmer ID Number
        OutlinedTextField(
            value = farmerId,
            onValueChange = onFarmerIdChange,
            label = { Text("Farmer ID / Registration No.") },
            placeholder = { Text("e.g. PMK-9872-4102 / KCC-4481") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_id")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Aadhaar Card Number
        OutlinedTextField(
            value = aadhaar,
            onValueChange = onAadhaarChange,
            label = { Text("Aadhaar Card Number") },
            placeholder = { Text("12-digit Aadhaar Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_aadhaar")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Age & Gender Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = onAgeChange,
                label = { Text("Age (Yrs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .testTag("input_farmer_age")
                    .weight(0.4f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Column(modifier = Modifier.weight(0.6f)) {
                Text(
                    text = "Gender",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { g ->
                        val isSelected = gender.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onGenderChange(g) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Full-time or Part-time Farmer
        Column {
            Text(
                text = "Farming Engagement",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Full-Time", "Part-Time").forEach { type ->
                    val isSelected = farmingType == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFarmingTypeChange(type) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onFarmingTypeChange(type) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                            )
                            Text(
                                text = "$type Farmer",
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step2AddressAndLocation(
    houseNo: String,
    onHouseNoChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    state: String,
    onOpenStatePicker: () -> Unit,
    district: String,
    onDistrictChange: (String) -> Unit,
    village: String,
    onVillageChange: (String) -> Unit,
    pinCode: String,
    onPinCodeChange: (String) -> Unit,
    onOpenMapPicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Address & Location",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Set your residential address and farm region for micro-climate forecasting and local Mandi prices.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // GPS / Google Maps CTA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMapPicker() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Use Current Location",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Text(
                            text = "Auto-fill address with Google Maps",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenMapPicker,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Map View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // House No. / Building No.
        OutlinedTextField(
            value = houseNo,
            onValueChange = onHouseNoChange,
            label = { Text("House No. / Building No.") },
            placeholder = { Text("e.g. House #42, Ward 3 / Khasra No. 118") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_house_no")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Landmark (if any)
        OutlinedTextField(
            value = landmark,
            onValueChange = onLandmarkChange,
            label = { Text("Landmark (if any)") },
            placeholder = { Text("e.g. Near Old Panchayat Ghar / Primary School") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_landmark")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // State Selector with Live Search
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenStatePicker() },
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("State / Union Territory", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = state.ifBlank { "Tap to select state (e.g. Punjab)" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryGreen)
            }
        }

        // District & Village / Town
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = district,
                onValueChange = onDistrictChange,
                label = { Text("District") },
                placeholder = { Text("e.g. Ludhiana") },
                modifier = Modifier
                    .testTag("input_farmer_district")
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = village,
                onValueChange = onVillageChange,
                label = { Text("Village / Town") },
                placeholder = { Text("e.g. Samrala") },
                modifier = Modifier
                    .testTag("input_farmer_village")
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // PIN Code
        OutlinedTextField(
            value = pinCode,
            onValueChange = onPinCodeChange,
            label = { Text("PIN Code") },
            placeholder = { Text("141114") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = PrimaryGreen)
            },
            modifier = Modifier
                .testTag("input_farmer_pincode")
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
    }
}

@Composable
fun Step3MultiFarmManagement(
    farmsList: List<FarmDraftItem>,
    onOpenAddFarmDialog: () -> Unit,
    onRemoveFarm: (String) -> Unit,
    onUpdateFarm: (FarmDraftItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "What type of farm do you manage?",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "You can add multiple farms under one account.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // If no farms added yet, show Add Farm primary box
        if (farmsList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAddFarmDialog() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryContainerGreen),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AddHomeWork, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = "Add Farm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "Click here to select and configure your farm type (Agriculture, Cattle, Poultry, Fish, etc.)",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // List of Configured Farm Cards
            farmsList.forEachIndexed { index, farmDraft ->
                FarmDraftCard(
                    index = index + 1,
                    farm = farmDraft,
                    canDelete = true,
                    onDelete = { onRemoveFarm(farmDraft.id) },
                    onUpdate = onUpdateFarm
                )
            }

            // Add Another Farm Button below existing cards
            Button(
                onClick = onOpenAddFarmDialog,
                modifier = Modifier
                    .testTag("btn_add_another_farm")
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Add Another Farm", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun FarmDraftCard(
    index: Int,
    farm: FarmDraftItem,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onUpdate: (FarmDraftItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainerGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$index", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${farm.type} Unit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                    }
                }
            }

            // Farm Name
            OutlinedTextField(
                value = farm.name,
                onValueChange = { onUpdate(farm.copy(name = it)) },
                label = { Text("Farm / Unit Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Area & Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = farm.area,
                    onValueChange = { onUpdate(farm.copy(area = it)) },
                    label = { Text("Total Area") },
                    placeholder = { Text("10.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.36f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Column(modifier = Modifier.weight(0.64f)) {
                    Text("Area Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Acres", "Bigha", "Hectares").forEach { unit ->
                            val isSel = farm.unit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onUpdate(farm.copy(unit = unit)) }
                                    .padding(vertical = 12.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Land Ownership (Owned, Leased, Rented, Shared)
            Column {
                Text("Land Ownership", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Owned", "Leased", "Rented", "Shared").forEach { own ->
                        val isSel = farm.ownership == own
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(if (isSel) 1.dp else 0.dp, if (isSel) PrimaryGreen else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { onUpdate(farm.copy(ownership = own)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(own, fontSize = 11.sp, color = if (isSel) PrimaryGreen else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Water Availability (3 options: High/Ample, Moderate/Seasonal, Scarce/Low)
            Column {
                Text("Water Availability", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Ample / High", "Moderate / Seasonal", "Scarce / Low").forEach { water ->
                        val isSel = farm.waterAvailability == water
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(if (isSel) 1.dp else 0.dp, if (isSel) PrimaryGreen else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { onUpdate(farm.copy(waterAvailability = water)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(water.split("/")[0].trim(), fontSize = 11.sp, color = if (isSel) PrimaryGreen else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Dynamic Questions based on Farm Type
            if (farm.type.contains("Poultry", ignoreCase = true)) {
                OutlinedTextField(
                    value = farm.primaryCropOrLivestock,
                    onValueChange = { onUpdate(farm.copy(primaryCropOrLivestock = it)) },
                    label = { Text("Bird Breed & Housing Type") },
                    placeholder = { Text("e.g. Broiler (500 birds) / Layer (300 birds)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else if (farm.type.contains("Cattle", ignoreCase = true) || farm.type.contains("Dairy", ignoreCase = true)) {
                OutlinedTextField(
                    value = farm.primaryCropOrLivestock,
                    onValueChange = { onUpdate(farm.copy(primaryCropOrLivestock = it)) },
                    label = { Text("Cattle Breed & Daily Milk Yield") },
                    placeholder = { Text("e.g. 6 Gir Cows, 2 Murrah Buffalo (38 L/day)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else if (farm.type.contains("Fish", ignoreCase = true) || farm.type.contains("Aquaculture", ignoreCase = true)) {
                OutlinedTextField(
                    value = farm.primaryCropOrLivestock,
                    onValueChange = { onUpdate(farm.copy(primaryCropOrLivestock = it)) },
                    label = { Text("Pond Count & Fish Species") },
                    placeholder = { Text("e.g. 2 Earthen Ponds, Rohu & Catla") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else if (farm.type.contains("Beekeeping", ignoreCase = true)) {
                OutlinedTextField(
                    value = farm.primaryCropOrLivestock,
                    onValueChange = { onUpdate(farm.copy(primaryCropOrLivestock = it)) },
                    label = { Text("Hive Count & Bee Species") },
                    placeholder = { Text("e.g. 25 Wooden Hives, Apis Mellifera") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else {
                // Crop / Agriculture / Horticulture / Organic / Vegetable
                OutlinedTextField(
                    value = farm.primaryCropOrLivestock,
                    onValueChange = { onUpdate(farm.copy(primaryCropOrLivestock = it)) },
                    label = { Text("Current Crops & Varieties") },
                    placeholder = { Text("e.g. Wheat (PBW 725), Mustard (Pusa Bold)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun Step4ChallengesAndAiSelection(
    biggestProblem: String,
    onSelectBiggestProblem: (String) -> Unit,
    financialLossCause: String,
    onSelectFinancialLossCause: (String) -> Unit,
    aiPriorities: Set<String>,
    onToggleAiPriority: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Farmer's Challenges & AI Setup",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Select your key challenges so the AI agent can prioritize preventative alerts for your farm.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Question 1: Biggest Farming Problem (Selection cards)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "1. What is your biggest farming problem?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            BIGGEST_PROBLEMS_OPTIONS.forEach { option ->
                val isSelected = biggestProblem == option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectBiggestProblem(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectBiggestProblem(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Question 2: Main Cause of Financial Loss (Selection cards)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "2. What causes the most financial loss?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FINANCIAL_LOSS_OPTIONS.forEach { option ->
                val isSelected = financialLossCause == option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectFinancialLossCause(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectFinancialLossCause(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Question 3: What AI Should Monitor (Checkbox cards)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "3. What would you like AI to monitor automatically?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val prioritiesList = listOf(
                "Dynamic Weather & Rain Skip",
                "Pest & Disease Diagnosis",
                "Mandi Price Trends",
                "Irrigation Timing",
                "Livestock Vaccination Dates",
                "Feed Stock Refill Alerts"
            )

            prioritiesList.forEach { priority ->
                val isChecked = aiPriorities.contains(priority)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleAiPriority(priority) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isChecked) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isChecked) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleAiPriority(priority) },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(priority, fontSize = 13.sp, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun Step5DigitalProfileActivation(
    name: String,
    mobile: String,
    village: String,
    state: String,
    farmsList: List<FarmDraftItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SecondaryContainerGreen)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Farm Digital Profile Ready!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = "Autonomous AI Farm Agent initialized for $name",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Active Multi-Farm Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (farmsList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("0 Farms Configured", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("You can add your farms anytime from the Farms tab after launch.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            farmsList.forEach { farm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(farm.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${farm.type} • ${farm.area} ${farm.unit} • ${farm.primaryCropOrLivestock}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Autonomous AI Monitoring Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                Text("• Hyper-local weather & evapotranspiration rain-skip active.", fontSize = 11.5.sp)
                Text("• Real-time Mandi price aggregator active for your district ($village, $state).", fontSize = 11.5.sp)
                Text("• Multi-farm dashboard with quick switching enabled.", fontSize = 11.5.sp)
            }
        }
    }
}
