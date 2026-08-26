package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var showHelplineDialog by remember { mutableStateOf(false) }

    if (showHelplineDialog) {
        FarmerEmergencyHelplinesDialog(
            onDismiss = { showHelplineDialog = false },
            onReportDamageClick = { 
                showHelplineDialog = false
                onNavigateSubScreen(SubScreen.GOVT_SCHEMES)
            }
        )
    }
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
                titleLine1 = "Farm Accounting",
                titleLine2 = "",
                icon = Icons.Default.AccountBalanceWallet,
                testTag = "hub_finance_item",
                onClick = { onNavigateSubScreen(SubScreen.FINANCE_DASHBOARD) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Livestock & Crop",
                titleLine2 = "Overview Registry",
                icon = Icons.Default.Pets,
                testTag = "hub_livestock_item",
                onClick = { onNavigateSubScreen(SubScreen.LIVESTOCK_LIST) }
            )
        }

item {
            HubNavigationCard(
                titleLine1 = "Crop Insurance &",
                titleLine2 = "Govt Payouts",
                icon = Icons.Default.Shield,
                testTag = "hub_insurance_item",
                onClick = { onNavigateSubScreen(SubScreen.GOVT_SCHEMES) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Government Schemes &",
                titleLine2 = "Subsidies",
                icon = Icons.Default.AccountBalance,
                testTag = "hub_schemes_item",
                onClick = { onNavigateSubScreen(SubScreen.GOVT_SCHEMES) }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "Farmer Emergency &",
                titleLine2 = "Helplines",
                icon = Icons.Default.PhoneInTalk,
                testTag = "hub_helpline_item",
                onClick = { showHelplineDialog = true }
            )
        }

        item {
            HubNavigationCard(
                titleLine1 = "App Settings &",
                titleLine2 = "Farmer Profile",
                icon = Icons.Default.Settings,
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
    icon: ImageVector,
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
                            if (titleLine2.isNotEmpty()) {
                                Text(
                                    text = titleLine2,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

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
