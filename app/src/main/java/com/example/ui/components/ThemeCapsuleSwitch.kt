package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningYellow

@Composable
fun ThemeCapsuleSwitch(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offset by animateDpAsState(
        targetValue = if (isDark) 36.dp else 4.dp,
        animationSpec = tween(durationMillis = 250),
        label = "capsule_offset"
    )

    Box(
        modifier = modifier
            .testTag("theme_capsule_switch")
            .width(76.dp)
            .height(36.dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0xFF1E2A23) else Color(0xFFECF3EE))
            .clickable { onToggle() }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background labels/icons
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Light mode",
                tint = if (isDark) Color.Gray else WarningYellow,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Dark mode",
                tint = if (isDark) PrimaryGreen else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        // Sliding knob
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isDark) PrimaryGreen else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = if (isDark) Color.White else WarningYellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
