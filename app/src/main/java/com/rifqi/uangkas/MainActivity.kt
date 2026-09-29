package com.rifqi.uangkas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rifqi.uangkas.ui.KasScreen
import com.rifqi.uangkas.ui.SplashScreen
import com.rifqi.uangkas.ui.theme.UangKasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Deteksi tema bawaan HP (True jika Dark, False jika Light)
            val systemTheme = isSystemInDarkTheme()

            // 2. State tema yang mengikuti sistem HP sebagai default,
            // tapi bisa diubah manual lewat slider di KasScreen
            var isDarkTheme by remember(systemTheme) { mutableStateOf(systemTheme) }

            // 3. Masukkan variable isDarkTheme ke parameter UangKasTheme
            UangKasTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Logika perpindahan layar (Navigasi sederhana)
                    var showSplashScreen by remember { mutableStateOf(true) }

                    if (showSplashScreen) {
                        SplashScreen(
                            onTimeout = {
                                showSplashScreen = false
                            }
                        )
                    } else {
                        // 4. Teruskan state tema ke KasScreen agar slider berfungsi
                        KasScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it }
                        )
                    }
                }
            }
        }
    }
}