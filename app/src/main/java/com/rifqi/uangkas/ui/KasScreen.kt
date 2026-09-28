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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(viewModel: KasViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var weekNumber by remember { mutableStateOf("") }
    var isPemasukan by remember { mutableStateOf(true) } // True = Pemasukan, False = Pengeluaran

    val uiState by viewModel.uiState.collectAsState()

    // Format Rupiah Indonesia
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KSTC Kas Logger", color = KstcGold, fontWeight = FontWeight.Bold) },
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

            // Dashboard Total Kas (Muncul jika ada response success)
            if (uiState is UiState.Success) {
                val total = (uiState as UiState.Success).totalKas
                Card(
                    colors = CardDefaults.cardColors(containerColor = KstcGold),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL KAS KOMUNITAS", color = KstcDarkGreen, fontWeight = FontWeight.Bold)
                        Text(formatRupiah.format(total), fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Toggle Tipe (Pemasukan / Pengeluaran)
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Button(
                    onClick = { isPemasukan = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPemasukan) KstcDarkGreen else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Pemasukan", color = if (isPemasukan) Color.White else Color.DarkGray)
                }
                Button(
                    onClick = { isPemasukan = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isPemasukan) KstcDarkGreen else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Pengeluaran", color = if (!isPemasukan) Color.White else Color.DarkGray)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isPemasukan) "Nama Anggota" else "Keterangan Pengeluaran") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            if (isPemasukan) {
                OutlinedTextField(
                    value = weekNumber,
                    onValueChange = { weekNumber = it },
                    label = { Text("Minggu Ke- (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Info (Loading / Error / Success Message)
            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(color = KstcGold)
                is UiState.Error -> Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold)
                is UiState.Success -> Text(state.message, color = KstcDarkGreen, fontWeight = FontWeight.Bold)
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Submit (Warna Emas)
            Button(
                onClick = { viewModel.submitData(name, amount, weekNumber, isPemasukan) },
                colors = ButtonDefaults.buttonColors(containerColor = KstcGold),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = uiState !is UiState.Loading
            ) {
                Text(
                    text = "SIMPAN KE GOOGLE SHEETS",
                    color = KstcDarkGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}