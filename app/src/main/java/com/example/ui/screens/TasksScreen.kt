package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.FarmTask
import com.example.ui.components.KisanTopAppBar
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    notifications: List<AppNotification> = emptyList(),
    tasks: List<FarmTask> = emptyList(),
    onMarkNotificationRead: (String) -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {},
    onDeleteNotification: (String) -> Unit = {},
    onToggleTask: (FarmTask) -> Unit = {},
    onDeleteTask: (FarmTask) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val unreadNotifications = notifications.filter { !it.isRead }
    var selectedTab by remember { mutableStateOf(0) } // 0: Weather Alerts & Notifications, 1: AI Farm Tasks

    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = "Notifications & Alerts",
                showBackButton = true,
                onBackClick = onBack,
                unreadNotificationCount = unreadNotifications.size
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // Tab Switcher Row: Alerts & Weather vs Farm Tasks
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryGreen
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Weather Alerts", fontWeight = FontWeight.Bold)
                                if (unreadNotifications.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFD32F2F), shape = CircleShape) {
                                        Text(
                                            text = unreadNotifications.size.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Farm Tasks (${tasks.count { !it.isCompleted }})", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // WEATHER ALERTS & NOTIFICATIONS FEED
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unreadNotifications.size} Unread Alerts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (unreadNotifications.isNotEmpty()) {
                            TextButton(
                                onClick = onMarkAllNotificationsRead,
                                modifier = Modifier.testTag("mark_all_read_button")
                            ) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark All as Read", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (notifications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(48.dp))
                                Text("All Caught Up!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("No severe weather warnings or pending farm notifications.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(notifications, key = { it.id }) { notif ->
                        val isCritical = notif.severity == "CRITICAL"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notification_card_${notif.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    // Title & Red dot indicator at the top
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFD32F2F))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = notif.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Message text directly below title with tight spacing
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Sub-info: Farm name and time
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Farm: ${notif.farmName}",
                                            fontSize = 11.sp,
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = notif.timeFormatted,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Right side column aligned to top: Checkbox (Tick box) above Delete button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Checkbox(
                                        checked = notif.isRead,
                                        onCheckedChange = { onMarkNotificationRead(notif.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                                        modifier = Modifier.testTag("mark_read_checkbox_${notif.id}")
                                    )

                                    IconButton(
                                        onClick = { onDeleteNotification(notif.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete notification",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // FARM TASKS TAB
                items(tasks, key = { it.id }) { task ->
                    val isPriority1 = task.priorityLabel.contains("1") || task.priorityLabel.contains("NOW")
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("task_item_${task.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (task.isCompleted) MaterialTheme.colorScheme.outline
                            else if (isPriority1) PrimaryGreen
                            else WarningYellowDark
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                                    .clickable { onToggleTask(task) }
                            ) {
                                Text(
                                    text = "${task.priorityLabel} • ${task.targetArea}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPriority1) PrimaryGreen else WarningYellowDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleTask(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                                )
                                IconButton(
                                    onClick = { onDeleteTask(task) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFD32F2F))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

