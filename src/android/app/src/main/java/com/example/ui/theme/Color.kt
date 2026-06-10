package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ── CYPHER THEME — Purple + Black ──
val CypherPurple      = Color(0xFFAB20F0)   // main accent
val CypherPurpleLight = Color(0xFFC060FF)   // light purple
val CypherPurpleDark  = Color(0xFF6A0DAD)   // dark purple
val CypherPurpleLow   = Color(0xFF1A0030)   // subtle purple bg
val CypherBlack       = Color(0xFF04000A)   // deep black bg
val CypherCardBg      = Color(0xFF0F001E)   // card background
val CypherText        = Color(0xFFE8D8FF)   // main text
val CypherTextDim     = Color(0xFF9A70C0)   // dim text
val CypherGreen       = Color(0xFF00FF88)   // connected / success
val CypherRed         = Color(0xFFFF3B3B)   // error / danger

// Legacy names kept so Theme.kt doesn't break
val Purple80          = CypherPurpleLight
val PurpleGrey80      = Color(0xFFCCC2DC)
val Pink80            = Color(0xFFEFB8C8)
val Purple40          = CypherPurple
val PurpleGrey40      = CypherPurpleDark
val Pink40            = Color(0xFF7D5260)
