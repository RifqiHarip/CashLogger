package com.rifqi.uangkas.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.rifqi.uangkas.R // Sesuaikan dengan package Anda
import com.rifqi.uangkas.ui.theme.KstcBackground

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // State untuk animasi ukuran (scale)
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(key1 = true) {
        // 1. Jalankan animasi membesar dengan efek pegas (bouncy)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // 2. Tahan di layar selama 1.5 detik agar logo sempat terlihat
        delay(1500L)
        // 3. Pindah ke halaman utama
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KstcBackground), // Latar belakang aplikasi
        contentAlignment = Alignment.Center
    ) {
        Image(
            // GANTI 'logo_kstc' SESUAI DENGAN NAMA FILE GAMBAR ANDA DI FOLDER DRAWABLE
            painter = painterResource(id = R.drawable.kstc_logo),
            contentDescription = "Logo Aplikasi",
            modifier = Modifier
                .size(200.dp) // Sesuaikan ukuran logo jika terlalu besar/kecil
                .scale(scale.value)
        )
    }
}