package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.PCConnectionManager
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackpadScreen(connectionManager: PCConnectionManager) {
    var sensitivity by remember { mutableFloatStateOf(1.2f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tracker Panel UI Intro
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Mouse, contentDescription = null, tint = Color(0xFF00E676))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Precision Trackpad",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Trackpad with Scroll Strips Side-by-Side
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Touch Area Box
            Box(
                modifier = Modifier
                    .weight(0.82f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141419))
                    .border(1.dp, Color(0xFF2B2B36), RoundedCornerShape(16.dp))
                    .testTag("trackpad_touch_area")
                    .pointerInput(sensitivity) {
                        detectTapGestures(
                            onTap = {
                                connectionManager.sendCommand("C:L")
                            },
                            onDoubleTap = {
                                connectionManager.sendCommand("C:D")
                            },
                            onLongPress = {
                                connectionManager.sendCommand("C:R")
                            }
                        )
                    }
                    .pointerInput(sensitivity) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x * sensitivity
                            val dy = dragAmount.y * sensitivity
                            connectionManager.sendCommand("M:$dx:$dy")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Carbon Mesh Touch Grids Visual Effect
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "TOUCHPAD",
                        color = Color(0xFF32323D),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Slide to move. Tap to left-click. Hold to right-click.",
                        color = Color(0xFF555562),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Dedicated Vertical Scroll Strip Box
            Box(
                modifier = Modifier
                    .weight(0.18f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16161D))
                    .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(16.dp))
                    .testTag("trackpad_scroll_strip")
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Smooth scroll events downscaled
                            val scrollDeltaY = -(dragAmount.y / 15f)
                            if (scrollDeltaY.roundToInt() != 0) {
                                connectionManager.sendCommand("S:${scrollDeltaY.roundToInt()}")
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Scroll wheel icon",
                        tint = Color(0xFF00E676).copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "S\nC\nR\nO\nL\nL",
                        color = Color(0xFF333342),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hardware Clicking Buttons Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { connectionManager.sendCommand("C:L") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color(0xFF333345), RoundedCornerShape(12.dp))
                    .testTag("trackpad_left_click_button")
            ) {
                Text("LEFT CLICK", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { connectionManager.sendCommand("C:R") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color(0xFF333345), RoundedCornerShape(12.dp))
                    .testTag("trackpad_right_click_button")
            ) {
                Text("RIGHT CLICK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sensitivity Speed Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
            border = BorderStroke(1.dp, Color(0xFF2E2E38)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pointer Speed Sensitivity", color = Color.LightGray, fontSize = 13.sp)
                    Text("${"%.1f".format(sensitivity)}x", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.5f..3.0f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color(0xFF2E2E38),
                        thumbColor = Color(0xFF00E676)
                    ),
                    modifier = Modifier.testTag("sensitivity_slider")
                )
            }
        }
    }
}
