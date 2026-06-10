package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.PCConnectionManager

@Composable
fun KeyboardScreen(connectionManager: PCConnectionManager) {
    var typedText by remember { mutableStateOf("") }
    
    // Modifier locks
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }
    var metaActive by remember { mutableStateOf(false) }

    fun sendWithModifiers(key: String) {
        val activeMods = mutableListOf<String>()
        if (ctrlActive) activeMods.add("ctrl")
        if (altActive) activeMods.add("alt")
        if (shiftActive) activeMods.add("shift")
        if (metaActive) activeMods.add("win")

        if (activeMods.isNotEmpty()) {
            val modsJoined = activeMods.joinToString(",")
            connectionManager.sendCommand("K_MOD:$modsJoined:$key")
            // Reset modifier locks after hotkey executes
            ctrlActive = false
            altActive = false
            shiftActive = false
            metaActive = false
        } else {
            connectionManager.sendCommand("K_RAW:$key")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Keyboard screen label
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color(0xFF00E676))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Emulated Keyboard",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Real-time Text Input Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            border = BorderStroke(1.dp, Color(0xFF2A2A38)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Real-time Text Stream",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = typedText,
                    onValueChange = { newValue ->
                        // Detect typed letter dynamically or differences
                        if (newValue.length > typedText.length) {
                            val newChar = newValue.last().toString()
                            connectionManager.sendCommand("K:$newChar")
                        } else if (newValue.length < typedText.length) {
                            connectionManager.sendCommand("K_RAW:backspace")
                        }
                        typedText = newValue
                    },
                    placeholder = { Text("Start typing on PC...", color = Color(0xFF4C4C5B)) },
                    singleLine = false,
                    maxLines = 3,
                    trailingIcon = {
                        if (typedText.isNotEmpty()) {
                            IconButton(onClick = { typedText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF24242F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keyboard_text_editor")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            connectionManager.sendCommand("K_RAW:enter")
                        }
                    ) {
                        Text("Send Enter Code ↵", color = Color(0xFF00E676), fontSize = 13.sp)
                    }

                    TextButton(
                        onClick = {
                            if (typedText.isNotEmpty()) {
                                connectionManager.sendCommand("K:$typedText")
                                typedText = ""
                            }
                        },
                        enabled = typedText.isNotEmpty()
                    ) {
                        Text("Transmit Block ➔", color = if (typedText.isNotEmpty()) Color(0xFF00E676) else Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Modifier Hardware Locks Row
        Text(
            "System Modifiers",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModifierKeyButton(label = "Ctrl", isActive = ctrlActive, modifier = Modifier.weight(1f)) { ctrlActive = !ctrlActive }
            ModifierKeyButton(label = "Alt", isActive = altActive, modifier = Modifier.weight(1f)) { altActive = !altActive }
            ModifierKeyButton(label = "Shift", isActive = shiftActive, modifier = Modifier.weight(1f)) { shiftActive = !shiftActive }
            ModifierKeyButton(label = "Win / ⌘", isActive = metaActive, modifier = Modifier.weight(1f)) { metaActive = !metaActive }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PC Action Keys Grids
        Text(
            "Quick Layout Actions",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val keyActionsList = remember {
            listOf(
                KeyActionData("Esc", "escape"),
                KeyActionData("Tab", "tab"),
                KeyActionData("Space", "space"),
                KeyActionData("Enter", "enter"),
                KeyActionData("Backspace", "backspace"),
                KeyActionData("Delete", "delete"),
                KeyActionData("F5 (Slides)", "f5"),
                KeyActionData("Home", "home"),
                KeyActionData("End", "end")
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(keyActionsList) { act ->
                PCKeyButton(label = act.label) {
                    sendWithModifiers(act.keyName)
                }
            }
        }

        // Arrow Key DPad Controller
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            border = BorderStroke(1.dp, Color(0xFF2A2A38)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Up
                ArrrowControlBtn(Icons.Default.KeyboardArrowUp, "up") { sendWithModifiers("up") }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left
                    ArrrowControlBtn(Icons.Default.KeyboardArrowLeft, "left") { sendWithModifiers("left") }
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF22222B), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    
                    // Right
                    ArrrowControlBtn(Icons.Default.KeyboardArrowRight, "right") { sendWithModifiers("right") }
                }

                // Down
                ArrrowControlBtn(Icons.Default.KeyboardArrowDown, "down") { sendWithModifiers("down") }
            }
        }
    }
}

data class KeyActionData(val label: String, val keyName: String)

@Composable
fun ModifierKeyButton(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Color(0xFF00E676) else Color(0xFF1F1F24))
            .border(1.dp, if (isActive) Color(0xFF00E676) else Color(0xFF2E2E38), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PCKeyButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E24))
            .border(1.dp, Color(0xFF2C2C38), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ArrrowControlBtn(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E24))
            .border(1.dp, Color(0xFF2C2C38), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = desc, tint = Color(0xFF00E676))
    }
}
