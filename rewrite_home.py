import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove AI Crop Scanner
content = re.sub(r'[ \t]*// AI Crop Photo Growth & Disease Scanner Block.*?// Smart Bluetooth Cattle Tracker & Pasture Fencing Alert Block', '\n        // Smart Bluetooth Cattle Tracker & Pasture Fencing Alert Block', content, flags=re.DOTALL)

# 2. Remove Crop Insurance
content = re.sub(r'[ \t]*// Crop Insurance & Govt Payout Support Block.*?// Crop Growth Tracker Card', '\n        // Crop Growth Tracker Card', content, flags=re.DOTALL)

# 3. Remove Govt Schemes
content = re.sub(r'[ \t]*// Government Schemes & Subsidies Block.*?// Bank Accounts & Credit Card', '\n        // Bank Accounts & Credit Card', content, flags=re.DOTALL)

# 4. Remove Helplines
# The helplines item starts with "// Farmer Emergency & Govt Helpline Card"
# and ends right before the closing of the LazyColumn and HomeScreen
content = re.sub(r'[ \t]*// Farmer Emergency & Govt Helpline Card.*?    }\n}', '    }\n}', content, flags=re.DOTALL)


# 5. Insert the 3 new AI cards right before "// Bank Accounts & Credit Card"
new_ai_cards = """
        // CowCatcherAI & CCTV Monitor Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateSubScreen(SubScreen.CCTV_MONITOR) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SecondaryContainerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CowCatcherAI & CCTV Monitor", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Live ML animal tracking", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen)
                }
            }
        }

        // Mastitis AI & Udder Scanner Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateSubScreen(SubScreen.MASTITIS_SCANNER) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SecondaryContainerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Biotech, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mastitis AI & Udder Scanner", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Detect udder infections early", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen)
                }
            }
        }

        // Poultry Audio AI & Cough Detectors Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateSubScreen(SubScreen.POULTRY_SCANNER) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SecondaryContainerGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Poultry Audio AI & Cough Detectors", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Audio ML for bird health", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen)
                }
            }
        }

        // Bank Accounts & Credit Card
"""

content = content.replace('// Bank Accounts & Credit Card', new_ai_cards)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
