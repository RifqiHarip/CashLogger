package com.rifqi.uangkas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(viewModel: KasViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var weekNumber by remember { mutableStateOf("") }
    var isPemasukan by remember { mutableStateOf(true) }
    val totalKasDepan by viewModel.totalKas.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    // Clear Form saat status Success
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            name = ""
            amount = ""
            weekNumber = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash Logger", color = KstcGold,fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KstcDarkGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- KARTU TOTAL KAS PERMANEN DI ATAS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = KstcGold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                        // Tampilkan loading kecil jika data sedang ditarik saat awal buka
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
            // --- POPUP DIALOG BERHASIL ---
            if (uiState is UiState.Success) {
                val successState = uiState as UiState.Success

                AlertDialog(
                    onDismissRequest = { viewModel.resetState() }, // Tutup jika klik area luar
                    containerColor = Color.White,
                    title = {
                        Text("✅ Berhasil Disimpan", color = KstcDarkGreen,fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            // Rincian Input
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Rincian Input:", fontWeight = FontWeight.Bold, color = KstcDarkGreen,fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tipe: ${successState.submittedType.replaceFirstChar { it.uppercase() }}",fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Nama: ${successState.submittedName}",fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Nominal: ${formatRupiah.format(successState.submittedAmount)}",fontFamily = PlusJakartaSansFontFamily, fontSize = 14.sp, color = Color.DarkGray)
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
                                    Text("TOTAL KAS TERBARU", color = KstcDarkGreen, fontWeight = FontWeight.Bold,fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp)
                                    Text(formatRupiah.format(successState.totalKas), fontSize = 20.sp, color = Color.White,fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetState() }) {
                            Text("Tutup", color = KstcDarkGreen,fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // --- FORM INPUT ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Button(
                    onClick = { isPemasukan = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPemasukan) KstcDarkGreen else Color.LightGray),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Pemasukan", color = if (isPemasukan) Color.White else Color.DarkGray,fontFamily = PlusJakartaSansFontFamily,)
                }
                Button(
                    onClick = { isPemasukan = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isPemasukan) KstcDarkGreen else Color.LightGray),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Pengeluaran", color = if (!isPemasukan) Color.White else Color.DarkGray,fontFamily = PlusJakartaSansFontFamily,)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isPemasukan) "Nama Anggota" else "Keterangan Pengeluaran",fontFamily = PlusJakartaSansFontFamily) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Nominal (Rp.)",fontFamily = PlusJakartaSansFontFamily) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            if (isPemasukan) {
                OutlinedTextField(
                    value = weekNumber,
                    onValueChange = { weekNumber = it },
                    label = { Text("Minggu Ke- (Opsional)",fontFamily = PlusJakartaSansFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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
                onClick = { viewModel.submitData(name, amount, weekNumber, isPemasukan) },
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
        }
    }
}