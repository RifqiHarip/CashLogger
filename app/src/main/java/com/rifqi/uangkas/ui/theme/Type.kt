package com.rifqi.uangkas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// Pastikan import R ini sesuai dengan package aplikasi Anda
import com.rifqi.uangkas.R

// 1. Daftarkan Font Poppins yang baru saja kita masukkan
val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold)
)

// 2. Timpa pengaturan teks bawaan Material Design untuk menggunakan Poppins
val Typography = Typography(
    // Teks biasa (seperti di dalam TextField atau text deskripsi)
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Teks besar (seperti Judul TopBar atau Angka Total Kas)
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Teks tombol
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)