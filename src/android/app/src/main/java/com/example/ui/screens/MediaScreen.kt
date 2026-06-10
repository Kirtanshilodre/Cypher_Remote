package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.PCConnectionManager

@Composable
fun MediaScreen(connectionManager: PCConnectionManager) {
    var volumeLevel by remember { mutableFloatStateOf(50f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Media Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00E676))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Media Playback Controller",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Giant Circular Play/Pause Cockpit Disk
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF233529), Color(0xFF141419))
                    ),
                    CircleShape
                )
                .border(2.dp, Color(0xFF00E676).copy(alpha = 0.5f), CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F27))
                    .border(1.dp, Color(0xFF333342), CircleShape)
                    .clickable { connectionManager.sendCommand("MEDIA:playpause") }
                    .testTag("media_play_pause_dial"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Action",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "PLAY / PAUSE",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Skips Left & Right Controls Map
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaCircleButton(Icons.Default.SkipPrevious, "PREV") {
                connectionManager.sendCommand("MEDIA:prevtrack")
            }

            MediaCircleButton(Icons.Default.VolumeMute, "MUTE", buttonSize = 64.dp, iconTint = Color(0xFFCF6679)) {
                connectionManager.sendCommand("MEDIA:volumemute")
            }

            MediaCircleButton(Icons.Default.SkipNext, "NEXT") {
                connectionManager.sendCommand("MEDIA:nexttrack")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Physical Volume Rails Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            border = BorderStroke(1.dp, Color(0xFF2A2A38)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "PC System Volume Adjusters",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { connectionManager.sendCommand("MEDIA:volumedown") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, Color(0xFF323242), RoundedCornerShape(8.dp))
                            .testTag("media_volume_down_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeDown, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Volume (-)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = { connectionManager.sendCommand("MEDIA:volumeup") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, Color(0xFF323242), RoundedCornerShape(8.dp))
                            .testTag("media_volume_up_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Volume (+)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCircleButton(
    icon: ImageVector,
    label: String,
    buttonSize: androidx.compose.ui.unit.Dp = 50.dp,
    iconTint: Color = Color(0xFF00E676),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(Color(0xFF1E1E24))
                .border(1.dp, Color(0xFF2C2C39), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(buttonSize * 0.45f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
