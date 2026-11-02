package com.rifqi.uangkas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToTennisScore: () -> Unit // 👈 Callback baru untuk Papan Skor Tenis
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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

        // --- SEKSI TAMPILAN ---
        Text(
            text = "TAMPILAN",
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
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

        // --- SEKSI FITUR TAMBAHAN ---
        Text(
            text = "FITUR TAMBAHAN",
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        // 1. CARD KOMPAS KIBLAT
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { onNavigateToQibla() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_compass),
                    contentDescription = "Ikon Kompas",
                    tint = if (isDarkTheme) KstcGold else KstcDarkGreen,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kompas Arah Kiblat",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Cari arah dengan getaran (haptic)",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Buka",
                    tint = Color.Gray
                )
            }
        }

        // 2. CARD BARU: PAPAN SKOR TENIS
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { onNavigateToTennisScore() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = "Ikon Tenis",
                    tint = if (isDarkTheme) KstcGold else KstcDarkGreen,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Papan Skor Tenis",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Hitung poin mabar (No-Ad & Tiebreak)",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Buka",
                    tint = Color.Gray
                )
            }
        }

        // --- SEKSI TENTANG APLIKASI ---
        Text(
            text = "TENTANG",
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

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
                    text = "Versi 1.4.3.1",
                    color = Color.Gray,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Aplikasi pencatatan uang kas komunitas KSTC secara real-time yang dilengkapi fitur pendukung mabar.",
                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    }
}