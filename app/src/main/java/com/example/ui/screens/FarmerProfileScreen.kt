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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.KisanTopAppBar
import com.example.ui.components.StatePickerDialog
import com.example.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    userProfile: UserProfile?,
    onUpdateProfile: (UserProfile) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val profile = userProfile ?: UserProfile()

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Farmer Profile",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Profile Main Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("farmer_profile_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(1).uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = profile.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = profile.phone,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PM-KISAN ID: ${profile.farmerId}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Edit Profile Button inside Header
                        FilledTonalButton(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White,
                                contentColor = PrimaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_edit_profile_header")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Details", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                    }
                }
            }

            // Quick Info Grid Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Agricultural Identity & Registration",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Agricultural Identity",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Aadhaar Number", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(profile.aadhaarNumber, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Farming Type", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(profile.farmingType, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Age / Gender", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${profile.age} Yrs • ${profile.gender}", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Language Preference", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    when (profile.selectedLanguage) {
                                        "hi" -> "Hindi (हिंदी)"
                                        "pa" -> "Punjabi (ਪੰਜਾਬੀ)"
                                        else -> "English"
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }

            // House Address Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = PrimaryGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "House & Farm Address",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Address",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "${profile.houseNo}, ${profile.landmark}\nVillage ${profile.village}, District ${profile.district}, ${profile.state} - ${profile.pinCode}",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "GPS: ${profile.latitude}, ${profile.longitude}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Profile Actions: Edit Details & Settings
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Edit Details Primary Button
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_edit_farmer_details"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Settings Button
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_profile_settings"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Settings & Preferences", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Edit Profile Details Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(profile.name) }
        var editPhone by remember { mutableStateOf(profile.phone) }
        var editFarmerId by remember { mutableStateOf(profile.farmerId) }
        var editAadhaar by remember { mutableStateOf(profile.aadhaarNumber) }
        var editAge by remember { mutableStateOf(profile.age.toString()) }
        var editGender by remember { mutableStateOf(profile.gender) }
        var editFarmingType by remember { mutableStateOf(profile.farmingType) }
        var editHouseNo by remember { mutableStateOf(profile.houseNo) }
        var editLandmark by remember { mutableStateOf(profile.landmark) }
        var editVillage by remember { mutableStateOf(profile.village) }
        var editDistrict by remember { mutableStateOf(profile.district) }
        var editState by remember { mutableStateOf(profile.state) }
        var editPinCode by remember { mutableStateOf(profile.pinCode) }

        var showStatePicker by remember { mutableStateOf(false) }

        val farmingTypesList = listOf(
            "Agriculture / Crop Farm",
            "Cattle / Dairy Farm",
            "Poultry Farm",
            "Horticulture / Fruit Farm",
            "Vegetable Farm",
            "Organic / Natural Farm",
            "Fish / Aquaculture Farm",
            "Goat & Sheep Farm",
            "Mixed Integrated Farm"
        )

        if (showStatePicker) {
            StatePickerDialog(
                currentState = editState,
                onDismiss = { showStatePicker = false },
                onStateSelected = { selected ->
                    editState = selected
                    showStatePicker = false
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Personal & Identity", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    item {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Mobile / Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { editAge = it },
                                label = { Text("Age") },
                                modifier = Modifier.weight(1f).testTag("edit_profile_age_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Gender", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Male", "Female").forEach { g ->
                                        FilterChip(
                                            selected = editGender.equals(g, ignoreCase = true),
                                            onClick = { editGender = g },
                                            label = { Text(g, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryGreen,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editAadhaar,
                            onValueChange = { editAadhaar = it },
                            label = { Text("Aadhaar Number") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_aadhaar_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editFarmerId,
                            onValueChange = { editFarmerId = it },
                            label = { Text("Farmer ID / PM-KISAN ID") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_farmer_id_input"),
                            singleLine = true
                        )
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Farming Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(farmingTypesList) { fType ->
                                FilterChip(
                                    selected = editFarmingType == fType,
                                    onClick = { editFarmingType = fType },
                                    label = { Text(fType, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Address & Location", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    item {
                        OutlinedTextField(
                            value = editHouseNo,
                            onValueChange = { editHouseNo = it },
                            label = { Text("House / Ward No.") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editLandmark,
                            onValueChange = { editLandmark = it },
                            label = { Text("Landmark") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editVillage,
                            onValueChange = { editVillage = it },
                            label = { Text("Village / Town") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editDistrict,
                            onValueChange = { editDistrict = it },
                            label = { Text("District") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedCard(
                            onClick = { showStatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("State", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(editState, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryGreen)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editPinCode,
                            onValueChange = { editPinCode = it },
                            label = { Text("PIN Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = profile.copy(
                            name = editName.trim().ifBlank { profile.name },
                            phone = editPhone.trim().ifBlank { profile.phone },
                            farmerId = editFarmerId.trim().ifBlank { profile.farmerId },
                            aadhaarNumber = editAadhaar.trim().ifBlank { profile.aadhaarNumber },
                            age = editAge.toIntOrNull() ?: profile.age,
                            gender = editGender,
                            farmingType = editFarmingType,
                            houseNo = editHouseNo.trim(),
                            landmark = editLandmark.trim(),
                            village = editVillage.trim(),
                            district = editDistrict.trim(),
                            state = editState.trim(),
                            pinCode = editPinCode.trim()
                        )
                        onUpdateProfile(updated)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.testTag("btn_save_profile_edits")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
