package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.PCConnectionManager
import com.example.shortcuts.ShortcutManager
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material3.ExperimentalMaterial3Api

// ── GUIDE tab removed ──
enum class RemoteTab(val label: String, val icon: ImageVector) {
    CONNECT("Sync",      Icons.Default.Link),
    MOUSE("Mouse",       Icons.Default.Mouse),
    KEYBOARD("Keyboard", Icons.Default.Keyboard),
    MEDIA("Media",       Icons.Default.PlayCircle),
    SHORTCUTS("Shortcuts", Icons.Default.SettingsApplications)
}

// ── Cypher purple + black colors ──
private val NavBg       = Color(0xFF0F001E)
private val NavSelected = Color(0xFFAB20F0)
private val AppBg       = Color(0xFF04000A)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var pcConnectionManager: PCConnectionManager
    private lateinit var shortcutManager: ShortcutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pcConnectionManager = PCConnectionManager(applicationContext)
        shortcutManager     = ShortcutManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                var currentTab by remember { mutableStateOf(RemoteTab.CONNECT) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AppBg,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "CYPHER",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp,
                                    color = Color(0xFFAB20F0)
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = NavBg,
                                titleContentColor = Color(0xFFAB20F0)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = NavBg,
                            contentColor = Color(0xFF9A70C0),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            RemoteTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = if (isSelected) Color.Black else Color(0xFF9A70C0)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color(0xFFAB20F0) else Color(0xFF9A70C0)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = NavSelected
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(AppBg)
                    ) {
                        when (currentTab) {
                            RemoteTab.CONNECT -> ConnectScreen(
                                connectionManager = pcConnectionManager,
                                onNavigateToGuide = { /* GUIDE removed */ }
                            )
                            RemoteTab.MOUSE     -> TrackpadScreen(connectionManager = pcConnectionManager)
                            RemoteTab.KEYBOARD  -> KeyboardScreen(connectionManager = pcConnectionManager)
                            RemoteTab.MEDIA     -> MediaScreen(connectionManager = pcConnectionManager)
                            RemoteTab.SHORTCUTS -> ShortcutsScreen(
                                connectionManager = pcConnectionManager,
                                shortcutManager   = shortcutManager
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        pcConnectionManager.disconnect()
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F001E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Hello $name!", color = Color(0xFFAB20F0), fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "System Ready. Remote Service Online.", color = Color(0xFF9A70C0), fontSize = 14.sp)
        }
    }
}
