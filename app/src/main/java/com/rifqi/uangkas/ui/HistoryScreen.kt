package com.rifqi.uangkas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.uangkas.ui.theme.KstcDarkGreen
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
    val transactions by viewModel.recentTransactions.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.fetchTotalKas() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Riwayat Transaksi",
                color = if (isDarkTheme) Color.White else KstcDarkGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat transaksi",
                        color = Color.Gray,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    transactions.forEach { item ->
                        val isPemasukanItem = item.type.equals("pemasukan", ignoreCase = true)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        color = if (isDarkTheme) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    val subtitleText = listOfNotNull(
                                        item.weekNumber.takeIf { it.isNotBlank() },
                                        item.description.takeIf { !it.isNullOrBlank() }
                                    ).joinToString(" • ")

                                    if (subtitleText.isNotBlank()) {
                                        Text(
                                            text = subtitleText,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

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
                }
            }
        }
    }
}