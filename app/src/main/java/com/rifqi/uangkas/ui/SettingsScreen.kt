package com.rifqi.uangkas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pengaturan Aplikasi",
            color = if (isDarkTheme) Color.White else KstcDarkGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Seksi Tema
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode Gelap",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KstcGold,
                        checkedTrackColor = Color.DarkGray
                    )
                )
            }
        }

        // Seksi About Me
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tentang Aplikasi",
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFontFamily,
                    color = if (isDarkTheme) KstcGold else KstcDarkGreen,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Cash Logger (KSTC)",
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFontFamily,
                    color = if (isDarkTheme) Color.White else Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "Versi 1.3",
                    color = Color.Gray,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Aplikasi pencatatan uang kas komunitas KSTC secara real-time yang terhubung langsung dengan Google Sheets.",
                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    }
}