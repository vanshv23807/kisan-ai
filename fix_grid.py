with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# 1. We remove the 3 old horizontal AI cards entirely.
start_idx = content.find('        // Mastitis AI & Udder Scanner Block')
end_idx = content.find('        // CowCatcherAI & CCTV Monitor Block')
end_idx = content.find('        }', end_idx + 100)
end_idx = content.find('        }', end_idx + 1)
# Make sure we got it right
print("Start idx:", start_idx)
print("End idx:", end_idx)

# Delete them
content = content[:start_idx] + content[end_idx + 10:]

# 2. Insert new Row 3 and Row 4 at the end of the Grid.
grid_end = content.find('                    // Payouts')
# find the closing braces of the grid item
grid_end = content.find('            }\n        }', grid_end)

new_rows = """
                // Row 3
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Mastitis AI
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clickable { onNavigateSubScreen(SubScreen.MASTITIS_SCANNER) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFCDD2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFFC62828))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Mastitis AI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = "Scan udders",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    // Poultry Audio
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clickable { onNavigateSubScreen(SubScreen.POULTRY_SCANNER) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFF9C4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFF57F17))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Poultry Audio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = "Listen sounds",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                // Row 4
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // CowCatcher AI
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clickable { onNavigateSubScreen(SubScreen.CCTV_MONITOR) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE1BEE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF6A1B9A))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("CowCatcher", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = "Live CCTV",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    // Empty spacer card so the left card maintains its square aspect ratio gracefully
                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                }"""

content = content[:grid_end] + new_rows + '\n' + content[grid_end:]

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

