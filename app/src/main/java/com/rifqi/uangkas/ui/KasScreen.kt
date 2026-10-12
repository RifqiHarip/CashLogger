package com.rifqi.uangkas.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rifqi.uangkas.R
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    viewModel: KasViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    val uriHandler = LocalUriHandler.current
    val sheetUrl = "https://docs.google.com/spreadsheets/d/1qG-k1CiNxzZ-BO_n_JY4eKkWlccsTis5TXr8boMcBtE/edit?usp=sharing"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when(selectedTab) {
                            NavigationTab.HOME -> "Cash Logger"
                            NavigationTab.HISTORY -> "Riwayat Transaksi"
                            NavigationTab.SETTINGS -> "Pengaturan"
                        },
                        color = KstcGold,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.kstc_logo),
                        contentDescription = "Logo KSTC",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KstcDarkGreen
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                contentColor = KstcDarkGreen
            ) {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = tab.title,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KstcDarkGreen,
                            selectedTextColor = if (isDarkTheme) KstcGold else KstcDarkGreen,
                            indicatorColor = KstcGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { padding -> // 👈 INI ADALAH PARAMETER CONTENT YANG DIBUTUHKAN SCAFFOLD
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                NavigationTab.HOME -> HomeScreen(viewModel, isDarkTheme, sheetUrl, uriHandler)
                NavigationTab.HISTORY -> HistoryScreen(viewModel, isDarkTheme)
                NavigationTab.SETTINGS -> SettingsScreen(isDarkTheme, onThemeChange)
            }
        }
    } // 👈 Batas akhir Scaffold
}