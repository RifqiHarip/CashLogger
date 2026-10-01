package com.rifqi.uangkas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.Image
import com.rifqi.uangkas.R
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.platform.LocalUriHandler

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    viewModel: KasViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var weekNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPemasukan by remember { mutableStateOf(true) }

    val totalKasDepan by viewModel.totalKas.collectAsState()
    val transactions by viewModel.recentTransactions.collectAsState()
    val scrollState = rememberScrollState() // 1. Buat state scroll

    val isRefreshing by viewModel.isRefreshing.collectAsState() // State animasi refresh
    val uiState by viewModel.uiState.collectAsState()

    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val uriHandler = LocalUriHandler.current
    val sheetUrl = "https://docs.google.com/spreadsheets/d/1qG-k1CiNxzZ-BO_n_JY4eKkWlccsTis5TXr8boMcBtE/edit?usp=sharing"

    // Clear Form saat status Success
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            name = ""
            amount = ""
            weekNumber = ""
            description = ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cash Logger",
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
                // PARAMETER BARU: 'actions' untuk menaruh item di pojok kanan atas
                actions = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange,
                        modifier = Modifier.padding(end = 12.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KstcGold,        // Warna tombol bundar saat aktif
                            checkedTrackColor = Color.DarkGray,  // Warna jalur saat aktif
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.White
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KstcDarkGreen
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- KARTU TOTAL KAS PERMANEN DI ATAS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = KstcGold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                // Menggunakan Box agar teks tetap di tengah, dan tombol di kanan
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    // Teks Total Kas di tengah
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL KAS KOMUNITAS",
                            color = KstcDarkGreen,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalKasDepan != null) {
                            Text(
                                text = formatRupiah.format(totalKasDepan),
                                fontSize = 28.sp,
                                color = Color.White,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            // Loading saat awal buka aplikasi
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    // Tombol Refresh di ujung kanan
                    IconButton(
                        onClick = { viewModel.fetchTotalKas() },
                        enabled = !isRefreshing,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = KstcDarkGreen,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Kas",
                                tint = KstcDarkGreen
                            )
                        }
                    }
                }
            }

            // --- POPUP DIALOG BERHASIL ---
            // --- POPUP DIALOG BERHASIL ---
            if (uiState is UiState.Success) {
                val successState = uiState as UiState.Success

                AlertDialog(
                    onDismissRequest = { viewModel.resetState() },
                    // Warna popup menyesuaikan tema
                    containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                    title = {
                        Text("✅ Berhasil Disimpan", color = if (isDarkTheme) KstcGold else KstcDarkGreen, fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            // Rincian Input
                            Card(
                                // Warna kartu rincian: Hijau pudar gelap (Dark Mode) vs Hijau muda (Light Mode)
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2D3730) else Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val textColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color.DarkGray

                                    Text("Rincian Input:", fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else KstcDarkGreen, fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tipe: ${successState.submittedType.replaceFirstChar { it.uppercase() }}", fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = textColor)
                                    Text("Nama: ${successState.submittedName}", fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = textColor)
                                    Text("Nominal: ${formatRupiah.format(successState.submittedAmount)}", fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = textColor)
                                    Text("Keterangan: ${successState.submittedDesc}", fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = textColor)
                                }
                            }

                            // Total Kas Baru
                            Card(
                                colors = CardDefaults.cardColors(containerColor = KstcGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Teks menggunakan Dark Green agar terlihat sangat jelas di atas background Gold
                                    Text("TOTAL KAS TERBARU", color = KstcDarkGreen, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp)
                                    Text(formatRupiah.format(successState.totalKas), fontSize = 20.sp, color = KstcDarkGreen, fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetState() }) {
                            Text("Tutup", color = if (isDarkTheme) KstcGold else KstcDarkGreen, fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // --- FORM INPUT ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Button(
                    onClick = { isPemasukan = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPemasukan) KstcDarkGreen else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Pemasukan", color = if (isPemasukan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,fontFamily = PlusJakartaSansFontFamily,)
                }
                Button(
                    onClick = { isPemasukan = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isPemasukan) KstcDarkGreen else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Pengeluaran", color = if (!isPemasukan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,fontFamily = PlusJakartaSansFontFamily,)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Anggota",fontFamily = PlusJakartaSansFontFamily) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Nominal (Rp.)",fontFamily = PlusJakartaSansFontFamily) },
                prefix = { Text("Rp ", fontFamily = PlusJakartaSansFontFamily) },
                visualTransformation = RupiahVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Keterangan Tambahan") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            if (isPemasukan) {
                OutlinedTextField(
                    value = weekNumber,
                    onValueChange = { weekNumber = it },
                    label = { Text("Periode") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(color = KstcGold)
                is UiState.Error -> Text(state.message, color = Color.Red,fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.submitData(name, amount, weekNumber, description, isPemasukan) },
                colors = ButtonDefaults.buttonColors(containerColor = KstcGold),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = uiState !is UiState.Loading
            ) {
                Text(
                    text = "SIMPAN DATA",
                    color = KstcDarkGreen,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            TextButton(
                onClick = {
                    // Ini akan otomatis membuka browser atau aplikasi Google Sheets di HP
                    uriHandler.openUri(sheetUrl)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Buka Google Sheets",
                    color = if (isDarkTheme) Color.LightGray else Color.Gray,
                    fontFamily = PlusJakartaSansFontFamily,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline // Memberikan efek garis bawah
                )
            }
            // ... di dalam Column utama KasScreen, di bagian bawah setelah tombol Google Sheets ...

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Riwayat Transaksi Terakhir",
                color = if (isDarkTheme) Color.White else KstcDarkGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Asumsi Anda menyimpan list transaksi di ViewModel (misal: viewModel.recentTransactions)

            if (transactions.isEmpty()) {
                Text(
                    text = "Belum ada riwayat transaksi",
                    color = Color.Gray,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp
                )
            } else {
                // Tampilkan list card kecil untuk setiap transaksi
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    transactions.forEach { item ->
                        val isPemasukan = item.type.equals("pemasukan", ignoreCase = true)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        color = if (isDarkTheme) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${item.weekNumber} • ${item.description}",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }

                                // Nominal dengan warna hijau untuk pemasukan, merah/pink untuk pengeluaran
                                Text(
                                    text = "${if (isPemasukan) "+" else "-"} ${formatRupiah.format(item.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    color = if (isPemasukan) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        // Format angka dengan titik setiap 3 digit
        val formatted = original.reversed().chunked(3).joinToString(".").reversed()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= original.length) return formatted.length

                var transformedOffset = 0
                var originalCharCount = 0
                for (char in formatted) {
                    if (originalCharCount == offset) break
                    if (char != '.') {
                        originalCharCount++
                    }
                    transformedOffset++
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= formatted.length) return original.length

                var originalOffset = 0
                var transformedCharCount = 0
                for (char in formatted) {
                    if (transformedCharCount == offset) break
                    if (char != '.') {
                        originalOffset++
                    }
                    transformedCharCount++
                }
                return originalOffset
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}