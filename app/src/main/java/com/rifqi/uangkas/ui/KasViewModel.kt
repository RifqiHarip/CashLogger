package com.rifqi.uangkas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rifqi.uangkas.data.network.KasRequest
import com.rifqi.uangkas.data.network.RetrofitClient
import com.rifqi.uangkas.data.network.TransactionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val message: String,
        val totalKas: Double,
        val totalKasMabar: Double,
        val submittedName: String,
        val submittedAmount: Long,
        val submittedType: String,
        val submittedDesc: String?,
        val submittedCashType: String?
    ) : UiState()
    data class Error(val message: String) : UiState()
}

class KasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // --- STATE TOTAL KAS DASHBOARD ---
    private val _totalKas = MutableStateFlow<Double?>(null) // Kas Coaching
    private val _totalKasMabar = MutableStateFlow<Double?>(null) // Kas Mabar

    val totalKas: StateFlow<Double?> = _totalKas
    val totalKasMabar: StateFlow<Double?> = _totalKasMabar

    // --- STATE RIWAYAT TRANSAKSI ---
    private val _recentTransactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val recentTransactions: StateFlow<List<TransactionItem>> = _recentTransactions

    // --- STATE REFRESH & PAGINASI ---
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    // Variabel Paginasi
    private var currentOffset = 0
    private val PAGE_LIMIT = 10

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                fetchTotalKas()
                delay(300_000) // Auto-refresh setiap 5 menit
            }
        }
    }

    // Fetch Halaman Pertama (Reset Offset)
    fun fetchTotalKas() {
        currentOffset = 0
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = RetrofitClient.apiService.getTotalKas(limit = PAGE_LIMIT, offset = currentOffset)
                if (response.status == "success") {
                    _totalKas.value = response.totalKasCoaching
                    _totalKasMabar.value = response.totalKasMabar
                    _recentTransactions.value = response.recentTransactions ?: emptyList()
                    _hasMore.value = response.hasMore ?: false
                }
            } catch (e: Exception) {
                // Pertahankan state lama jika koneksi gagal
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Fetch Halaman Berikutnya (Load More Append)
    fun loadMoreTransactions() {
        if (_isLoadingMore.value || !_hasMore.value) return

        _isLoadingMore.value = true
        currentOffset += PAGE_LIMIT

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTotalKas(limit = PAGE_LIMIT, offset = currentOffset)
                if (response.status == "success") {
                    val newItems = response.recentTransactions ?: emptyList()
                    _recentTransactions.value = _recentTransactions.value + newItems
                    _hasMore.value = response.hasMore ?: false
                } else {
                    currentOffset -= PAGE_LIMIT // Rollback offset jika gagal
                }
            } catch (e: Exception) {
                currentOffset -= PAGE_LIMIT // Rollback offset jika exception
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun submitData(
        name: String,
        amount: String,
        weekNumber: String,
        description: String,
        isPemasukan: Boolean,
        cashType: String
    ) {
        val amountLong = amount.replace(Regex("[^0-9]"), "").toLongOrNull()
        if (name.isBlank() || amountLong == null) {
            _uiState.value = UiState.Error("Nama dan Nominal harus diisi dengan benar.")
            return
        }

        val type = if (isPemasukan) "pemasukan" else "pengeluaran"
        val week = if (isPemasukan) weekNumber.ifBlank { null } else null
        val desc = description.ifBlank { null }
        val cash = cashType.ifBlank { null }

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val request = KasRequest(name, amountLong , week, type, desc, cash)
                val response = RetrofitClient.apiService.submitKas(request)

                if (response.status == "success") {
                    _totalKas.value = response.totalKasCoaching
                    _totalKasMabar.value = response.totalKasMabar

                    _uiState.value = UiState.Success(
                        message = response.message,
                        totalKas = response.totalKasCoaching ?: 0.0,
                        totalKasMabar = response.totalKasMabar ?: 0.0,
                        submittedName = name,
                        submittedAmount = amountLong,
                        submittedType = type,
                        submittedCashType = cash,
                        submittedDesc = desc
                    )

                    // Refresh daftar transaksi agar transaksi baru langsung muncul di baris paling atas
                    fetchTotalKas()
                } else {
                    _uiState.value = UiState.Error("Gagal: ${response.message}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Koneksi Gagal: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}