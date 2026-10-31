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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TennisScoreScreen(
    viewModel: TennisScoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.scoreState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // --- LOGIKA OTOMATIS UBAH KE LANDSCAPE SAAT MASUK SCREEN ---
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // Paksa orientasi layar menjadi Landscape (mengikuti rotasi sensor HP)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            // Kembalikan ke orientasi asal (Portrait) saat layar ditutup
            activity?.requestedOrientation = originalOrientation
        }
    }

    var editTeam1 by remember(state.player1Name) { mutableStateOf(state.player1Name) }
    var editTeam2 by remember(state.player2Name) { mutableStateOf(state.player2Name) }
    var editIsNoAd by remember(state.isNoAdMode) { mutableStateOf(state.isNoAdMode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papan Skor Tenis", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Pertandingan", tint = KstcGold)
                    }
                    IconButton(onClick = { viewModel.resetMatch() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Skor", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KstcDarkGreen, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Baris Mode & Tombol Undo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { showSettingsDialog = true },
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.isTiebreak) "TIEBREAK" else if (state.isNoAdMode) "GOLDEN POINT (NO-AD)" else "ADVANTAGE MODE",
                            color = KstcGold,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = null, tint = KstcGold, modifier = Modifier.size(12.dp))
                    }
                }

                Button(
                    onClick = { viewModel.undo() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Undo Point", color = Color.White, fontSize = 11.sp)
                }
            }

            // AREA SKOR LANDSCAPE (LAYOUT ROW: TIM A DI KIRI, TIM B DI KANAN)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CARD TIM A (SISI KIRI LAYAR)
                LandscapeScoreCard(
                    teamName = state.player1Name,
                    pointText = state.p1PointText,
                    gamesCount = state.p1Games,
                    isWinning = state.p1Games > state.p2Games,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.addPoint(isPlayerOne = true) }
                )

                // CARD TIM B (SISI KANAN LAYAR)
                LandscapeScoreCard(
                    teamName = state.player2Name,
                    pointText = state.p2PointText,
                    gamesCount = state.p2Games,
                    isWinning = state.p2Games > state.p1Games,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.addPoint(isPlayerOne = false) }
                )
            }
        }
    }

    // DIALOG PENGATURAN
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

// COMPOSABLE CARD SKOR KHUSUS LANDSCAPE
@Composable
private fun LandscapeScoreCard(
    teamName: String,
    pointText: String,
    gamesCount: Int,
    isWinning: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isWinning) KstcDarkGreen.copy(alpha = 0.90f) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isWinning) androidx.compose.foundation.BorderStroke(2.5.dp, KstcGold) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Nama Tim
            Text(
                text = teamName,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 22.sp,
                maxLines = 1
            )

            // Poin Utama (Angka Raksasa 80sp)
            Text(
                text = pointText,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 80.sp
            )

            // Total Games yang dimenangkan
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "GAMES: $gamesCount",
                    fontFamily = PlusJakartaSansFontFamily,
                    color = KstcGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}