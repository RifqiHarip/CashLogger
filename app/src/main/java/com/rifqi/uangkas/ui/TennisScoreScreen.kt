package com.rifqi.uangkas.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TennisScoreScreen(
    viewModel: TennisScoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    isDarkTheme: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.scoreState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val window = activity?.window

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        window?.let { w ->
            val insetsController = WindowCompat.getInsetsController(w, w.decorView)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            window?.let { w ->
                val insetsController = WindowCompat.getInsetsController(w, w.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            viewModel.saveCurrentMatch()
            showFinishDialog = true
        }
    }

    fun shareToWhatsApp(message: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Skor Match via")
        context.startActivity(shareIntent)
    }

    var editTeam1 by remember(state.player1Name) { mutableStateOf(state.player1Name) }
    var editTeam2 by remember(state.player2Name) { mutableStateOf(state.player2Name) }
    var editIsNoAd by remember(state.isNoAdMode) { mutableStateOf(state.isNoAdMode) }
    var editTargetGames by remember(state.targetGames) { mutableStateOf(state.targetGames.toString()) }

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

                    // Pill Header Menampilkan Target Game (misal: "FIRST TO 6 GAMES | GOLDEN POINT")
                    Surface(
                        onClick = { showSettingsDialog = true },
                        color = pillBgColor,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modeText = if (state.isTiebreak) "TIEBREAK" else if (state.isNoAdMode) "GOLDEN POINT" else "ADVANTAGE"
                            Text(
                                text = "FIRST TO ${state.targetGames} GAMES • $modeText",
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
                        onClick = {
                            if (state.p1Games > 0 || state.p2Games > 0) {
                                showFinishDialog = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan Skor", tint = KstcGold)
                    }

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

// DIALOG PENGATURAN PERTANDINGAN (RESPONSIF & SCROLLABLE LANDSCAPE)
    if (showSettingsDialog) {
        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Pengaturan Pertandingan",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState), // 👈 Mengaktifkan scroll vertikal pada dialog
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Input Nama Tim 1
                    OutlinedTextField(
                        value = editTeam1,
                        onValueChange = { editTeam1 = it },
                        label = { Text("Nama Tim / Pemain 1", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Input Nama Tim 2
                    OutlinedTextField(
                        value = editTeam2,
                        onValueChange = { editTeam2 = it },
                        label = { Text("Nama Tim / Pemain 2", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // SEKSI PEMILIHAN TARGET GAME
                    Text(
                        text = "Target Pemenang Game (First to N Games):",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(4, 6, 8).forEach { preset ->
                            FilterChip(
                                selected = editTargetGames == preset.toString(),
                                onClick = { editTargetGames = preset.toString() },
                                label = { Text("$preset G", fontSize = 11.sp) }
                            )
                        }
                        OutlinedTextField(
                            value = editTargetGames,
                            onValueChange = { editTargetGames = it.filter { char -> char.isDigit() } },
                            label = { Text("Custom", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

                    // SEKSI TOGGLE GOLDEN POINT / ADVANTAGE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editIsNoAd = !editIsNoAd },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editIsNoAd) "Golden Point (No-Ad)" else "Advantage Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (editIsNoAd) "Poin 40-40 langsung penentuan" else "Poin 40-40 butuh selisih 2 poin (AD)",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
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
                        val target = editTargetGames.toIntOrNull() ?: 6
                        viewModel.updateMatchSettings(editTeam1, editTeam2, editIsNoAd, target)
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

    if (showFinishDialog) {
        val currentRecord = MatchRecord(
            team1 = state.player1Name,
            team2 = state.player2Name,
            gamesTeam1 = state.p1Games,
            gamesTeam2 = state.p2Games,
            targetGames = state.targetGames,
            mode = if (state.isNoAdMode) "Golden Point (No-Ad)" else "Advantage Mode",
            dateText = "Pertandingan Ini"
        )

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(
                    text = if (state.isGameOver) "🏆 Pertandingan Selesai!" else "📊 Ringkasan Skor",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${state.player1Name} (${state.p1Games})  VS  (${state.p2Games}) ${state.player2Name}",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = KstcDarkGreen
                    )
                    Text(
                        text = "Ingin membagikan hasil pertandingan ini ke grup WhatsApp KSTC?",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val waText = viewModel.formatWhatsAppMessage(currentRecord)
                        shareToWhatsApp(waText)
                        showFinishDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("Kirim ke WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Tutup")
                }
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