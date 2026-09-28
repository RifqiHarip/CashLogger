package com.rifqi.uangkas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rifqi.uangkas.data.network.KasRequest
import com.rifqi.uangkas.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String, val totalKas: Double) : UiState()
    data class Error(val message: String) : UiState()
}

class KasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun submitData(name: String, amount: String, weekNumber: String, isPemasukan: Boolean) {
        val amountInt = amount.replace(Regex("[^0-9]"), "").toIntOrNull()
        if (name.isBlank() || amountInt == null) {
            _uiState.value = UiState.Error("Nama dan Nominal harus diisi dengan benar.")
            return
        }

        val type = if (isPemasukan) "pemasukan" else "pengeluaran"
        val week = if (isPemasukan) weekNumber.ifBlank { null } else null

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val request = KasRequest(name, amountInt, week, type)
                val response = RetrofitClient.apiService.submitKas(request)

                if (response.status == "success") {
                    _uiState.value = UiState.Success(
                        message = response.message,
                        totalKas = response.totalKasSekarang ?: 0.0
                    )
                } else {
                    _uiState.value = UiState.Error("Gagal: ${response.message}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Koneksi Gagal: ${e.localizedMessage}")
            }
        }
    }
}