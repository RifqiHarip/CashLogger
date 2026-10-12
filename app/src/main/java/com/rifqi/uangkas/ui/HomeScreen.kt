package com.rifqi.uangkas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.platform.LocalContext
import android.content.Context
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: KasViewModel,
    isDarkTheme: Boolean,
    sheetUrl: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("kstc_prefs", Context.MODE_PRIVATE)
    }

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var weekNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPemasukan by remember { mutableStateOf(true) }

    var expanded by remember { mutableStateOf(false) }

    // Memuat data member yang tersimpan di HP, atau menggunakan daftar default baru jika belum ada
    val memberList = remember {
        val defaultMembers = setOf("Rifqi", "A. Ridho", "Adi", "Carlo", "Iskandar", "Garry")
        val savedMembers = sharedPreferences.getStringSet("saved_member_list", defaultMembers) ?: defaultMembers
        mutableStateListOf<String>().apply {
            addAll(savedMembers.sortedWith(String.CASE_INSENSITIVE_ORDER))
        }
    }

    // Filter daftar anggota dan urutkan hasilnya secara alfabetis
    val filteredOptions = memberList.filter { it.contains(name, ignoreCase = true) }.sortedWith(String.CASE_INSENSITIVE_ORDER)
    val totalKasDepan by viewModel.totalKas.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            name = ""
            amount = ""
            weekNumber = ""
            description = ""
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.fetchTotalKas() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- KARTU TOTAL KAS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = KstcGold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // --- POPUP DIALOG BERHASIL ---
            if (uiState is UiState.Success) {
                val successState = uiState as UiState.Success

                AlertDialog(
                    onDismissRequest = { viewModel.resetState() },
                    containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                    title = {
                        Text("✅ Berhasil Disimpan", color = if (isDarkTheme) KstcGold else KstcDarkGreen, fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            Card(
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

                            Card(
                                colors = CardDefaults.cardColors(containerColor = KstcGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
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
                    Text("Pemasukan", color = if (isPemasukan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = PlusJakartaSansFontFamily)
                }
                Button(
                    onClick = { isPemasukan = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isPemasukan) KstcDarkGreen else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Pengeluaran", color = if (!isPemasukan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = PlusJakartaSansFontFamily)
                }
            }

            // --- AUTOCOMPLETE DROPDOWN DENGAN DEFAULT BARU & PENYIMPANAN PERMANEN ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        expanded = true
                    },
                    label = { Text("Nama Anggota", fontFamily = PlusJakartaSansFontFamily) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option, fontFamily = PlusJakartaSansFontFamily) },
                            onClick = {
                                name = option
                                expanded = false
                            }
                        )
                    }

                    if (name.isNotBlank() && !memberList.any { it.equals(name, ignoreCase = true) }) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "➕ Tambahkan \"$name\"",
                                    color = if (isDarkTheme) KstcGold else KstcDarkGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFontFamily
                                )
                            },
                            onClick = {
                                memberList.add(name)
                                // Simpan perubahan secara permanen ke SharedPreferences HP
                                sharedPreferences.edit()
                                    .putStringSet("saved_member_list", memberList.toSet())
                                    .apply()

                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Nominal", fontFamily = PlusJakartaSansFontFamily) },
                prefix = { Text("Rp ", fontFamily = PlusJakartaSansFontFamily) },
                visualTransformation = RupiahVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Keterangan Tambahan", fontFamily = PlusJakartaSansFontFamily) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            if (isPemasukan) {
                OutlinedTextField(
                    value = weekNumber,
                    onValueChange = { weekNumber = it },
                    label = { Text("Periode", fontFamily = PlusJakartaSansFontFamily) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(color = KstcGold)
                is UiState.Error -> Text(state.message, color = Color.Red, fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold)
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitData(name, amount, weekNumber, description, isPemasukan) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KstcGold,
                    contentColor = KstcDarkGreen,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && amount.isNotBlank() && (amount.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L) > 0L && uiState !is UiState.Loading
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(color = KstcDarkGreen, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "SIMPAN DATA",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            TextButton(
                onClick = { uriHandler.openUri(sheetUrl) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Buka Google Sheets",
                    color = if (isDarkTheme) Color.LightGray else Color.Gray,
                    fontFamily = PlusJakartaSansFontFamily,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
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