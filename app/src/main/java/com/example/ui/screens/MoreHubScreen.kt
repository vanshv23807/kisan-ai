package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.t
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerGreen
import com.example.ui.viewmodel.SubScreen

@Composable
fun MoreHubScreen(
    onNavigateSubScreen: (SubScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = t("nav_more"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Autonomous tools, diagnostics, and farm records",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Farm Finance &",
                titleLine2 = "Accounting Logs",
                subtitle = "Track expenses, profits & financial health",
                icon = Icons.Default.AccountBalanceWallet,
                badge = "Finance",
                testTag = "hub_finance_item",
                onClick = { onNavigateSubScreen(SubScreen.FINANCE_DASHBOARD) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Livestock & Crop",
                titleLine2 = "Overview Registry",
                subtitle = "View crops, cattle, poultry & farm assets in real time",
                icon = Icons.Default.Pets,
                badge = "Overview",
                testTag = "hub_livestock_item",
                onClick = { onNavigateSubScreen(SubScreen.LIVESTOCK_LIST) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Crop Growth &",
                titleLine2 = "Stage Progress Tracker",
                subtitle = "Track crops from sowing to harvest with stage progression",
                icon = Icons.Default.Timeline,
                badge = "Growth",
                testTag = "hub_crop_tracker_item",
                onClick = { onNavigateSubScreen(SubScreen.CROP_GROWTH_TRACKER) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "AI Crop Doctor &",
                titleLine2 = "Leaf Camera Scan",
                subtitle = "Instant leaf scan & AI pathogen diagnosis",
                icon = Icons.Default.CameraAlt,
                badge = "AI Scan",
                testTag = "hub_crop_doctor_item",
                onClick = { onNavigateSubScreen(SubScreen.CROP_DOCTOR) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Today's AI Plan &",
                titleLine2 = "Farm Notifications",
                subtitle = "Prioritized farm tasks & action triggers",
                icon = Icons.Default.TaskAlt,
                badge = "Alerts",
                testTag = "hub_tasks_item",
                onClick = { onNavigateSubScreen(SubScreen.TASKS_LIST) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "App Settings &",
                titleLine2 = "Farmer Profile",
                subtitle = "5 Languages, Light/Dark mode, Account",
                icon = Icons.Default.Settings,
                badge = null,
                testTag = "hub_settings_item",
                onClick = { onNavigateSubScreen(SubScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun HubNavigationCard(
    titleLine1: String,
    titleLine2: String,
    subtitle: String,
    icon: ImageVector,
    badge: String?,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Left Green Background Block (52dp fixed size)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SecondaryContainerGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Two-line title in green background look
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryContainerGreen.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column {
                            Text(
                                text = titleLine1,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = titleLine2,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
