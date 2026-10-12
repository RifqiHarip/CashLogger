package com.rifqi.uangkas.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationTab(
    val title: String,
    val icon: ImageVector
) {
    HOME("Beranda", Icons.Default.Home),
    HISTORY("Riwayat", Icons.AutoMirrored.Filled.List),
    SETTINGS("Pengaturan", Icons.Default.Settings)
}