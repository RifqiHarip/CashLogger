package com.rifqi.uangkas.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun TennisScoreScreen(
    viewModel: TennisScoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    isDarkTheme: Boolean, // 👈 Parameter tema
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.scoreState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Lock Landscape Mode & Immersive Fullscreen (Hide Status Bar)
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val window = activity?.window

        // 1. Ubah orientasi ke Landscape
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // 2. Sembunyikan Status Bar (Baterai, Sinyal, Jam)
        window?.let { w ->
            val insetsController = WindowCompat.getInsetsController(w, w.decorView)
            // Sembunyikan status bar (atau ganti dengan systemBars() jika ingin menyembunyikan nav bar bawah juga)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            // Ijinkan swipe sementara dari pinggir layar untuk melihat jam/baterai jika diperlukan
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // Kembalikan orientasi dan tampilkan kembali Status Bar saat keluar layar
            activity?.requestedOrientation = originalOrientation
            window?.let { w ->
                val insetsController = WindowCompat.getInsetsController(w, w.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
    var editTeam1 by remember(state.player1Name) { mutableStateOf(state.player1Name) }
    var editTeam2 by remember(state.player2Name) { mutableStateOf(state.player2Name) }
    var editIsNoAd by remember(state.isNoAdMode) { mutableStateOf(state.isNoAdMode) }

    val screenBgColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF3F4F6)
    val pillBgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val iconColor = if (isDarkTheme) Color.White else Color.Black
    val undoBtnBg = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE5E7EB)
    val undoTextColor = if (isDarkTheme) Color.White else Color.Black

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = screenBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // BAR KONTROL RINGKAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = iconColor)
                    }

                    Surface(
                        onClick = { showSettingsDialog = true },
                        color = pillBgColor,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isTiebreak) "TIEBREAK" else if (state.isNoAdMode) "GOLDEN POINT" else "ADVANTAGE",
                                color = if (isDarkTheme) KstcGold else KstcDarkGreen,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, tint = if (isDarkTheme) KstcGold else KstcDarkGreen, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.resetMatch() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Skor", tint = Color.Gray)
                    }

                    Button(
                        onClick = { viewModel.undo() },
                        colors = ButtonDefaults.buttonColors(containerColor = undoBtnBg),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Undo", color = undoTextColor, fontSize = 11.sp)
                    }
                }
            }

            // AREA SKOR UTAMA
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LandscapeScoreCard(
                    teamName = state.player1Name,
                    pointText = state.p1PointText,
                    gamesCount = state.p1Games,
                    isWinning = state.p1Games > state.p2Games,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.addPoint(isPlayerOne = true) }
                )

                LandscapeScoreCard(
                    teamName = state.player2Name,
                    pointText = state.p2PointText,
                    gamesCount = state.p2Games,
                    isWinning = state.p2Games > state.p1Games,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.addPoint(isPlayerOne = false) }
                )
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Pengaturan Pertandingan", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTeam1,
                        onValueChange = { editTeam1 = it },
                        label = { Text("Nama Tim / Pemain 1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTeam2,
                        onValueChange = { editTeam2 = it },
                        label = { Text("Nama Tim / Pemain 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (editIsNoAd) "Golden Point (No-Ad)" else "Advantage Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = if (editIsNoAd) "Poin 40-40 langsung penentuan" else "Poin 40-40 butuh selisih 2 poin (AD)", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = editIsNoAd,
                            onCheckedChange = { editIsNoAd = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = KstcGold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMatchSettings(editTeam1, editTeam2, editIsNoAd)
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KstcDarkGreen)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun LandscapeScoreCard(
    teamName: String,
    pointText: String,
    gamesCount: Int,
    isWinning: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBgColor = if (isWinning) {
        KstcDarkGreen.copy(alpha = 0.95f)
    } else {
        if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    }

    val teamTextColor = if (isWinning) Color.White else (if (isDarkTheme) Color.White else Color(0xFF1F2937))
    val pointTextColor = if (isWinning) Color.White else (if (isDarkTheme) Color.White else KstcDarkGreen)
    val gamesPillBg = if (isWinning) Color.Black.copy(alpha = 0.4f) else (if (isDarkTheme) Color.Black.copy(alpha = 0.4f) else Color(0xFFF3F4F6))
    val gamesTextColor = if (isWinning) KstcGold else (if (isDarkTheme) KstcGold else KstcDarkGreen)

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = if (isWinning) androidx.compose.foundation.BorderStroke(2.5.dp, KstcGold) else (if (!isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val cardHeight = maxHeight

            val teamFontSize = (cardHeight.value * 0.09f).coerceIn(13f, 20f).sp
            val pointFontSize = (cardHeight.value * 0.38f).coerceIn(42f, 76f).sp
            val gamesFontSize = (cardHeight.value * 0.075f).coerceIn(10f, 14f).sp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = teamName,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = teamTextColor,
                    fontSize = teamFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = pointText,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = pointTextColor,
                    fontSize = pointFontSize,
                    maxLines = 1
                )

                Surface(
                    color = gamesPillBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GAMES: $gamesCount",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = gamesTextColor,
                        fontSize = gamesFontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}