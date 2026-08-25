package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

@Composable
fun AddAnimalScreen(
    onSaveAnimal: (name: String, type: String, breed: String, age: Int, tag: String, hasBleTracker: Boolean, bleDeviceName: String) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedType by remember { mutableStateOf("Cow") }
    var animalName by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("Gir Breed") }
    var ageText by remember { mutableStateOf("3") }
    var tagId by remember { mutableStateOf("C-05") }
    var hasBleTracker by remember { mutableStateOf(true) }
    var bleDeviceName by remember { mutableStateOf("KisanTrack-BLE-05") }

    val animalTypes = listOf("Cow", "Buffalo", "Goat", "Sheep", "Poultry", "Other")

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Add Animal",
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
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Step $step of 3",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SecondaryContainerGreen)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (step == 1) {
                    Text(
                        text = "What type of animal?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select the category to setup correct vaccine schedules and yield models.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(animalTypes) { type ->
                            val isSelected = type == selectedType
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clickable { selectedType = type },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SecondaryContainerGreen else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else if (step == 2) {
                    Text(
                        text = "Animal Details",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = animalName,
                        onValueChange = { animalName = it },
                        label = { Text("Animal Name (e.g. Kaveri)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = tagId,
                        onValueChange = { tagId = it },
                        label = { Text("Ear Tag ID (e.g. C-05)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed (e.g. Sahiwal / Gir / Murrah)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age (in Years)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Step 3: Smart Cattle Bluetooth / GPS Tracker Setup
                    Text(
                        text = "Smart Cattle Bluetooth Tracker",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attach a smart Bluetooth collar tag to track live grazing position and get alerts if the animal wanders beyond the farm boundary fence.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (hasBleTracker) SecondaryContainerGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (hasBleTracker) PrimaryGreen else MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = if (hasBleTracker) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bluetooth Tracker Attached?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Switch(
                                    checked = hasBleTracker,
                                    onCheckedChange = { hasBleTracker = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = SecondaryContainerGreen)
                                )
                            }

                            if (hasBleTracker) {
                                Text(
                                    text = "✓ Geofence boundary active (150m radius). You will get instant alerts if this animal leaves the pasture boundary.",
                                    fontSize = 12.sp,
                                    color = PrimaryGreen
                                )

                                OutlinedTextField(
                                    value = bleDeviceName,
                                    onValueChange = { bleDeviceName = it },
                                    label = { Text("Bluetooth Collar Device Name / ID") },
                                    placeholder = { Text("e.g. KisanTrack-BLE-05") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            } else {
                                Text(
                                    text = "⚠ No device attached: Location tracking and fencing boundary alerts will display 'No Data Available'.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step += 1
                        } else {
                            onSaveAnimal(
                                animalName.ifBlank { "New $selectedType" },
                                selectedType,
                                breed,
                                ageText.toIntOrNull() ?: 3,
                                tagId,
                                hasBleTracker,
                                bleDeviceName.ifBlank { "KisanTrack-BLE" }
                            )
                        }
                    },
                    modifier = Modifier
                        .testTag("add_animal_continue_button")
                        .weight(if (step > 1) 2f else 1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(
                        text = if (step < 3) "Continue" else "Save Animal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
