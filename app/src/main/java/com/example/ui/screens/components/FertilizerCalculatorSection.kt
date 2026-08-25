package com.example.ui.screens.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Farm
import com.example.data.model.FertilizerDosageRecommendation
import com.example.data.util.FertilizerCalculator
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen

@Composable
fun FertilizerCalculatorSection(
    farm: Farm,
    modifier: Modifier = Modifier,
    onAddFertilizerTask: ((title: String, desc: String, category: String) -> Unit)? = null
) {
    var taskAddedConfirmation by remember { mutableStateOf(false) }

    // Automatically use data given when adding the farm
    val recommendation: FertilizerDosageRecommendation = remember(farm.primaryCropOrAnimal, farm.totalArea, farm.unit) {
        val crop = farm.primaryCropOrAnimal.ifBlank { "Wheat" }
        val area = if (farm.totalArea > 0) farm.totalArea else 5.0
        val unit = if (farm.unit.isNotBlank()) farm.unit else "Acres"
        FertilizerCalculator.calculate(
            cropName = crop,
            fieldArea = area,
            areaUnit = unit
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fertilizer_calculator_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header Banner (All text placed below heading)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SecondaryContainerGreen,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fertilizer Dosage Calculator",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Precision NPK ratios & commercial bag requirements tailored for ${farm.name}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Applied for ${farm.totalArea} ${farm.unit} of ${farm.primaryCropOrAnimal} (${farm.soilType})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )
                }
            }
        }

        // Recommended NPK Ratio Card (Text placed down/below heading)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Recommended NPK Ratio",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recommendation.recommendedNpkRatio,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = SecondaryContainerGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${recommendation.fieldArea} ${recommendation.areaUnit} • ${recommendation.cropName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Pure Nutrients Cards (Nitrogen N, Phosphorus P, Potassium K)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nitrogen (N)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Nitrogen (N)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            Text(
                                text = "${String.format("%.1f", recommendation.pureNitrogenKg)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D4ED8)
                            )
                            Text("Pure Nutrient", fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Phosphorus (P)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Phosphorus (P)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text(
                                text = "${String.format("%.1f", recommendation.purePhosphorusKg)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309)
                            )
                            Text("Pure Nutrient", fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Potassium (K)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Potassium (K)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            Text(
                                text = "${String.format("%.1f", recommendation.purePotassiumKg)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreen
                            )
                            Text("Pure Nutrient", fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }
        }

        // Commercial Fertilizer Bags Requirement Card (Text placed down/below heading)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Commercial Fertilizer Bag Requirements",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Est. Total Cost: ₹${recommendation.estimatedCostInr}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen
                    )
                }

                // Fertilizer Item Rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // DAP 50kg Bags
                    FertilizerItemRow(
                        title = "DAP (Di-Ammonium Phosphate 18:46:0)",
                        quantity = "${recommendation.dapBags50Kg} Bags (50 kg)",
                        subtitle = "Supplies entire Phosphorus requirement + starter Nitrogen",
                        badgeColor = Color(0xFFF59E0B),
                        icon = Icons.Default.Science
                    )

                    // Urea 45kg Bags
                    FertilizerItemRow(
                        title = "Neem Coated Urea (46% N)",
                        quantity = "${recommendation.ureaBags45Kg} Bags (45 kg)",
                        subtitle = "Or replace 1 bag with 1 bottle of IFFCO Nano Urea (${recommendation.nanoUreaBottles} bottles)",
                        badgeColor = Color(0xFF3B82F6),
                        icon = Icons.Default.Eco
                    )

                    // MOP 50kg Bags
                    FertilizerItemRow(
                        title = "MOP / Potash (Muriate of Potash 60% K2O)",
                        quantity = "${recommendation.mopBags50Kg} Bags (50 kg)",
                        subtitle = "Strengthens stalks against lodging & boosts grain plumpness",
                        badgeColor = PrimaryGreen,
                        icon = Icons.Default.Spa
                    )

                    // Zinc Sulphate
                    FertilizerItemRow(
                        title = "Zinc Sulphate (21% / 33%)",
                        quantity = "${recommendation.zincSulphateKg} kg",
                        subtitle = "Essential micronutrient for enzyme activation and chlorophyll",
                        badgeColor = Color(0xFF8B5CF6),
                        icon = Icons.Default.Biotech
                    )

                    // Organic Compost
                    FertilizerItemRow(
                        title = "Organic Compost / Farmyard Manure (FYM)",
                        quantity = "${recommendation.organicCompostRecommendedTonnes} Tonnes",
                        subtitle = "Restores soil organic carbon and beneficial soil microbes",
                        badgeColor = Color(0xFF10B981),
                        icon = Icons.Default.Grass
                    )
                }
            }
        }

        // Split Application Schedule Timeline (Text placed down/below heading)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Split Application Timing Schedule",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Synchronized crop growth stage dosages for optimum absorption",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stage 1: Basal
                ScheduleStageItem(
                    stageNum = "1",
                    stageName = "Basal Application (At Sowing / Puddling)",
                    details = recommendation.basalDoseSummary
                )

                // Stage 2: Top Dressing 1
                ScheduleStageItem(
                    stageNum = "2",
                    stageName = "1st Top Dressing (Tillering / CRI Stage)",
                    details = recommendation.topDressing1Summary
                )

                // Stage 3: Top Dressing 2
                ScheduleStageItem(
                    stageNum = "3",
                    stageName = "2nd Top Dressing / Foliar Spray",
                    details = recommendation.topDressing2Summary
                )

                // Agronomist Note
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SecondaryContainerGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = recommendation.applicationAdvice,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Action Button: Add to Farm Tasks / Schedule Reminder
        if (onAddFertilizerTask != null) {
            Button(
                onClick = {
                    onAddFertilizerTask(
                        "Apply Basal NPK for ${recommendation.cropName}",
                        "Apply ${recommendation.dapBags50Kg} bags DAP, ${recommendation.mopBags50Kg} bags MOP on ${recommendation.fieldArea} ${recommendation.areaUnit}. Basal ratio: ${recommendation.basalDoseSummary}",
                        "Fertilizer"
                    )
                    taskAddedConfirmation = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_add_fertilizer_schedule"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (taskAddedConfirmation) "✓ Added to Farm Tasks Schedule" else "Schedule Fertilizer Tasks for ${farm.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FertilizerItemRow(
    title: String,
    quantity: String,
    subtitle: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(quantity, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = badgeColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun ScheduleStageItem(
    stageNum: String,
    stageName: String,
    details: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(stageNum, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stageName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(1.dp))
            Text(details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
        }
    }
}
