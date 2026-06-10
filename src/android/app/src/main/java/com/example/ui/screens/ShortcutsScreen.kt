package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.PCConnectionManager
import com.example.shortcuts.ShortcutItem
import com.example.shortcuts.ShortcutManager
import com.example.shortcuts.ShortcutProfile
import java.util.UUID

@Composable
fun ShortcutsScreen(
    connectionManager: PCConnectionManager,
    shortcutManager: ShortcutManager
) {
    // Dynamic lists of profiles
    var profiles by remember { mutableStateOf(shortcutManager.loadProfiles()) }
    var selectedProfileId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    
    val selectedProfile = profiles.find { it.id == selectedProfileId }

    // Dialog state controllers
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    // New Shortcut Inputs
    var shortcutLabel by remember { mutableStateOf("") }
    var shortcutKey by remember { mutableStateOf("") }
    var ctrlSelected by remember { mutableStateOf(false) }
    var altSelected by remember { mutableStateOf(false) }
    var shiftSelected by remember { mutableStateOf(false) }
    var metaSelected by remember { mutableStateOf(false) }

    fun triggerShortcut(item: ShortcutItem) {
        val activeMods = mutableListOf<String>()
        if (item.ctrl) activeMods.add("ctrl")
        if (item.alt) activeMods.add("alt")
        if (item.shift) activeMods.add("shift")
        if (item.meta) activeMods.add("win")
        val modsStr = activeMods.joinToString(",")
        connectionManager.sendCommand("K_MOD:$modsStr:${item.key}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SettingsApplications, contentDescription = null, tint = Color(0xFF00E676))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Custom App Shortcuts",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    shortcutManager.resetToDefault()
                    profiles = shortcutManager.loadProfiles()
                    selectedProfileId = profiles.firstOrNull()?.id ?: ""
                }
            ) {
                Icon(Icons.Default.History, contentDescription = "Restore Defaults", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Profile Chip Selector Custom List
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles) { prof ->
                    val isSelected = prof.id == selectedProfileId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF00E676) else Color(0xFF1E1E24))
                            .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF2E2E38), RoundedCornerShape(20.dp))
                            .clickable { selectedProfileId = prof.id }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prof.appName,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Create Profile Action Code Button
            IconButton(
                onClick = { showAddProfileDialog = true },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(Color(0xFF2E2E3C), RoundedCornerShape(8.dp))
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile Slot", tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Profile Shortcuts Grid
        if (selectedProfile != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selectedProfile.appName} Presets",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row {
                    // Add Shortcut inside profile
                    TextButton(onClick = { showAddShortcutDialog = true }) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Key", color = Color(0xFF00E676), fontSize = 13.sp)
                    }

                    // Delete profile if not a system default profile
                    if (selectedProfile.id != "browser_profile" && selectedProfile.id != "vlc_profile" && selectedProfile.id != "presentation_profile") {
                        IconButton(
                            onClick = {
                                val listWithout = profiles.filter { it.id != selectedProfileId }
                                shortcutManager.saveProfiles(listWithout)
                                profiles = listWithout
                                selectedProfileId = listWithout.firstOrNull()?.id ?: ""
                            }
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Profile", tint = Color(0xFFCF6679))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedProfile.shortcuts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141419))
                        .border(1.dp, Color(0xFF22222E), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddBox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Custom Keys Here Yet", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(selectedProfile.shortcuts) { item ->
                        ShortcutCard(
                            item = item,
                            onTrigger = { triggerShortcut(item) },
                            onDelete = {
                                val updatedShortcuts = selectedProfile.shortcuts.filter { it.id != item.id }
                                val updatedProfiles = profiles.map {
                                    if (it.id == selectedProfile.id) it.copy(shortcuts = updatedShortcuts) else it
                                }
                                shortcutManager.saveProfiles(updatedProfiles)
                                profiles = updatedProfiles
                            }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Please select or create an app profile to begin.", color = Color.Gray)
            }
        }
    }

    // Modal adding profile Dialog
    if (showAddProfileDialog) {
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            containerColor = Color(0xFF1E1E24),
            title = { Text("New Desktop Application Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("Application Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            val newProf = ShortcutProfile(
                                id = UUID.randomUUID().toString(),
                                appName = newProfileName.trim(),
                                shortcuts = emptyList()
                            )
                            val newList = profiles + newProf
                            shortcutManager.saveProfiles(newList)
                            profiles = newList
                            selectedProfileId = newProf.id
                            newProfileName = ""
                            showAddProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Modal adding single key action Dialog inside profile
    if (showAddShortcutDialog) {
        AlertDialog(
            onDismissRequest = { showAddShortcutDialog = false },
            containerColor = Color(0xFF1E1E24),
            title = { Text("Add Macro Action Key", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = shortcutLabel,
                        onValueChange = { shortcutLabel = it },
                        label = { Text("Button Label (e.g., Undo)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = shortcutKey,
                        onValueChange = { shortcutKey = it },
                        label = { Text("Trigger Key (e.g., z, space, f11, down)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Active Modifiers:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = ctrlSelected, onCheckedChange = { ctrlSelected = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676)))
                            Text("Ctrl", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = altSelected, onCheckedChange = { altSelected = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676)))
                            Text("Alt", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = shiftSelected, onCheckedChange = { shiftSelected = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676)))
                            Text("Shift", color = Color.White, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = metaSelected, onCheckedChange = { metaSelected = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676)))
                            Text("Win", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shortcutLabel.isNotBlank() && shortcutKey.isNotBlank()) {
                            val newItem = ShortcutItem(
                                id = UUID.randomUUID().toString(),
                                label = shortcutLabel.trim(),
                                ctrl = ctrlSelected,
                                alt = altSelected,
                                shift = shiftSelected,
                                meta = metaSelected,
                                key = shortcutKey.trim().lowercase()
                            )
                            val updatedShortcuts = (selectedProfile?.shortcuts ?: emptyList()) + newItem
                            val updatedProfiles = profiles.map {
                                if (it.id == selectedProfileId) it.copy(shortcuts = updatedShortcuts) else it
                            }
                            shortcutManager.saveProfiles(updatedProfiles)
                            profiles = updatedProfiles
                            
                            // Reset inputs
                            shortcutLabel = ""
                            shortcutKey = ""
                            ctrlSelected = false
                            altSelected = false
                            shiftSelected = false
                            metaSelected = false
                            showAddShortcutDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
                ) {
                    Text("Add Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddShortcutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ShortcutCard(
    item: ShortcutItem,
    onTrigger: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable { onTrigger() }
            .testTag("shortcut_preset_${item.label.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
        border = BorderStroke(1.dp, Color(0xFF2E2E39))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                // Combined Modifiers Display Labels Inside Tiny Layout Keys
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    val mods = mutableListOf<String>()
                    if (item.ctrl) mods.add("Ctrl")
                    if (item.alt) mods.add("Alt")
                    if (item.shift) mods.add("Shift")
                    if (item.meta) mods.add("Win")
                    mods.add(item.key.uppercase())

                    mods.forEach { keyLabel ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF282833), RoundedCornerShape(3.dp))
                                .border(0.5.dp, Color(0xFF383849), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = keyLabel,
                                color = Color(0xFFDCDCE6),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Small Delete X Circle Button floating right
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete key mapping",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
