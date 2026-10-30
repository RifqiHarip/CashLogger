package com.rifqi.uangkas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
import com.rifqi.uangkas.ui.theme.KstcGold
import com.rifqi.uangkas.ui.theme.PlusJakartaSansFontFamily
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: KasViewModel,
    isDarkTheme: Boolean
) {
    // Collect state dari ViewModel
    val transactions by viewModel.recentTransactions.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    // State untuk Filter & Search
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Coaching", "Mabar"

    // Format Rupiah
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    // Logika Filter Data
    val filteredTransactions = transactions.filter { item ->
        // Cek tipe kas
        val matchesCashType = if (selectedFilter == "Semua") {
            true
        } else {
            item.cashType.equals(selectedFilter, ignoreCase = true)
        }

        // Cek pencarian (bisa cari nama, tanggal, atau periode)
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.timestamp.contains(searchQuery, ignoreCase = true) ||
                item.weekNumber.contains(searchQuery, ignoreCase = true)

        matchesCashType && matchesSearch
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.fetchTotalKas() },
        modifier = Modifier.fillMaxSize()
    ) {
        // Hapus verticalScroll di sini karena kita akan pakai LazyColumn di bawah
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Riwayat Transaksi",
                color = if (isDarkTheme) Color.White else KstcDarkGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // --- SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, tgl, atau periode...", fontFamily = PlusJakartaSansFontFamily, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KstcGold,
                    unfocusedBorderColor = Color.Gray
                )
            )

            // --- FILTER CHIPS (Semua / Coaching / Mabar) ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                listOf("Semua", "Coaching", "Mabar").forEach { filterOpt ->
                    val isSelected = selectedFilter == filterOpt
                    Button(
                        onClick = { selectedFilter = filterOpt },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) KstcDarkGreen else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = filterOpt,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // --- LIST TRANSAKSI ---
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada transaksi ditemukan",
                        color = Color.Gray,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Menggunakan LazyColumn agar list panjang tidak lag
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { item ->
                        val isPemasukanItem = item.type.equals("pemasukan", ignoreCase = true)
                        val isMabar = item.cashType.equals("mabar", ignoreCase = true)

                        // Menentukan warna badge kas
                        val badgeColor = if (isMabar) Color(0xFFDDA0DD) else Color(0xFF87CEEB) // Ungu vs Biru

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    // Header: Nama & Badge Kas
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            color = if (isDarkTheme) Color.White else Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Badge Coaching/Mabar
                                        Box(
                                            modifier = Modifier
                                                .background(color = badgeColor, shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.cashType.uppercase(),
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Subtitle: Tgl, Periode, Deskripsi
                                    val subtitleText = listOfNotNull(
                                        item.timestamp.takeIf { it.isNotBlank() },
                                        item.weekNumber.takeIf { it.isNotBlank() },
                                        item.description.takeIf { !it.isNullOrBlank() }
                                    ).joinToString(" • ")

                                    if (subtitleText.isNotBlank()) {
                                        Text(
                                            text = subtitleText,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                // Nominal Uang
                                Text(
                                    text = "${if (isPemasukanItem) "+" else "-"} ${formatRupiah.format(item.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    color = if (isPemasukanItem) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    if (hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(color = KstcGold, modifier = Modifier.size(24.dp))
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.loadMoreTransactions() },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isDarkTheme) Color.LightGray else KstcDarkGreen
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Muat Lebih Banyak",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}