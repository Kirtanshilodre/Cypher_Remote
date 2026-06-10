package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ConnectionState
import com.example.bluetooth.PCConnectionManager

@SuppressLint("MissingPermission")
@Composable
fun ConnectScreen(
    connectionManager: PCConnectionManager,
    onNavigateToGuide: () -> Unit
) {
    val context = LocalContext.current
    val connectionState by connectionManager.connectionState.collectAsState()
    
    // Shared Preferences for persisting Wi-Fi IP & Port
    val prefs = remember { context.getSharedPreferences("wifi_settings", Context.MODE_PRIVATE) }
    var ipAddress by remember { mutableStateOf(prefs.getString("ip_address", "") ?: "") }
    var portString by remember { mutableStateOf(prefs.getString("port", "8000") ?: "8000") }

    var isBluetoothMode by remember { mutableStateOf(true) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var permissionGranted by remember { mutableStateOf(false) }

    // Required permissions depending on Android version
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionGranted = results.values.all { it }
        if (permissionGranted) {
            pairedDevices = connectionManager.getPairedDevices()
        }
    }

    // Check permission status at launch
    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        permissionGranted = allGranted
        if (allGranted) {
            pairedDevices = connectionManager.getPairedDevices()
        } else {
            launcher.launch(permissionsToRequest)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active Connection Header
        ConnectionHeaderCard(connectionState, connectionManager)

        Spacer(modifier = Modifier.height(16.dp))

        if (connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
            // Mode Select Toggle Buttons (BT vs Wi-Fi) with responsive pill layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E24))
                    .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isBluetoothMode = true }
                        .background(if (isBluetoothMode) Color(0xFF32323D) else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Profile",
                            tint = if (isBluetoothMode) Color(0xFF00E676) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bluetooth",
                            color = if (isBluetoothMode) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isBluetoothMode = false }
                        .background(if (!isBluetoothMode) Color(0xFF32323D) else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi Profile",
                            tint = if (!isBluetoothMode) Color(0xFF00E676) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Wi-Fi Network",
                            color = if (!isBluetoothMode) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isBluetoothMode) {
                // Bluetooth List section
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Paired PCs",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = {
                                if (permissionGranted) {
                                    pairedDevices = connectionManager.getPairedDevices()
                                } else {
                                    launcher.launch(permissionsToRequest)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Devices", tint = Color(0xFF00E676))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!permissionGranted) {
                        PermissionMissingWarning {
                            launcher.launch(permissionsToRequest)
                        }
                    } else if (connectionManager.bluetoothAdapter == null) {
                        DeviceUnsupportedWarning()
                    } else if (!connectionManager.bluetoothAdapter!!.isEnabled) {
                        BluetoothDisabledWarning(context)
                    } else if (pairedDevices.isEmpty()) {
                        EmptyPairedDevicesWarning()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pairedDevices) { device ->
                                DeviceListItem(device) {
                                    connectionManager.connectBluetooth(device)
                                }
                            }
                        }
                    }
                }
            } else {
                // Wi-Fi Connection Settings
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Connect via Network (Local Wi-Fi)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("PC IP Address", color = Color.Gray) },
                        placeholder = { Text("e.g. 192.168.1.10", color = Color(0xFF555562)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF3E3E4B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ip_address_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = portString,
                        onValueChange = { portString = it },
                        label = { Text("Port", color = Color.Gray) },
                        placeholder = { Text("Default: 8000", color = Color(0xFF555562)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF3E3E4B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("port_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val port = portString.toIntOrNull() ?: 8000
                            // Save IP and Port
                            prefs.edit()
                                .putString("ip_address", ipAddress)
                                .putString("port", portString)
                                .apply()
                            
                            connectionManager.connectWifi(ipAddress.trim(), port)
                        },
                        enabled = ipAddress.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF233529)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("wifi_connect_button")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect PC Socket", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                        border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onNavigateToGuide() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Need the PC Receiver code?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Click here to grab the terminal companion script & run instructions.",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.ArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Already connected!
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF0E2F1D), CircleShape)
                        .border(2.dp, Color(0xFF00E676), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowUp,
                        contentDescription = "Connected",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "System Online & Linked",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Transmit mouse actions, keys and media signals directly dynamically.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { connectionManager.disconnect() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCF6679),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .height(48.dp)
                        .testTag("disconnect_button")
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disconnect", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConnectionHeaderCard(
    state: ConnectionState,
    manager: PCConnectionManager
) {
    val (statusText, statusColor, descText) = when (state) {
        is ConnectionState.Disconnected -> Triple("Disconnected", Color(0xFFCF6679), "No pairing target active")
        is ConnectionState.Connecting -> Triple("Linking...", Color(0xFFFFD54F), "Establishing hardware streams")
        is ConnectionState.Connected -> Triple("Connected", Color(0xFF00E676), "${state.name} (${state.type})")
        is ConnectionState.Error -> Triple("Link Error", Color(0xFFCF6679), state.message)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
        border = BorderStroke(1.dp, if (state is ConnectionState.Connected) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFF2E2E38))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = descText,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            if (state is ConnectionState.Connected) {
                IconButton(onClick = { manager.disconnect() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PermissionMissingWarning(onRequest: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1C1C)),
        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bluetooth Permissions Needed", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("The app needs Bluetooth permissions to list and connect to your PC.", color = Color.LightGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Allow Access", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeviceUnsupportedWarning() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1C1C)),
        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bluetooth Unavailable", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your phone doesn't seem to support Bluetooth hardware natively. Run via Wi-Fi network instead.", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun BluetoothDisabledWarning(context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A241C)),
        border = BorderStroke(1.dp, Color(0xFFED380).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bluetooth is Off", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Turn on Bluetooth inside system settings to locate nearby desktop pairings.", color = Color.LightGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (e: Exception) {
                        // ignore
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D), contentColor = Color.Black)
            ) {
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyPairedDevicesWarning() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
        border = BorderStroke(1.dp, Color(0xFF2E2E38)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Bonded PCs Found", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("To proceed over Bluetooth first pair your computer inside Android Bluetooth settings, then reload this menu.", color = Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceListItem(device: BluetoothDevice, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A22)),
        border = BorderStroke(1.dp, Color(0xFF2D2D3D))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LaptopMac,
                contentDescription = "Computer icon",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Host PC",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = device.address,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
