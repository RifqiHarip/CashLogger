package com.rifqi.uangkas.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack
import kotlin.math.abs

// Model Data untuk Menyimpan Riwayat Pertandingan
data class MatchRecord(
    val id: Long = System.currentTimeMillis(),
    val team1: String,
    val team2: String,
    val gamesTeam1: Int,
    val gamesTeam2: Int,
    val targetGames: Int = 6,
    val mode: String,
    val dateText: String
)

data class ScoreState(
    val player1Name: String = "Tim A",
    val player2Name: String = "Tim B",
    val p1PointIndex: Int = 0, // 0=0, 1=15, 2=30, 3=40, 4=AD
    val p2PointIndex: Int = 0,
    val p1Games: Int = 0,
    val p2Games: Int = 0,
    val targetGames: Int = 6, // 👈 Target Game Dinamis (Default 6 Game)
    val isNoAdMode: Boolean = true, // true = Golden Point (No-Ad), false = Advantage (Standard Ad)
    val isTiebreak: Boolean = false,
    val p1TiebreakPoints: Int = 0,
    val p2TiebreakPoints: Int = 0,
    val isGameOver: Boolean = false,
    val winnerName: String? = null
) {
    val p1PointText: String
        get() = if (isTiebreak) p1TiebreakPoints.toString() else pointToString(p1PointIndex, p2PointIndex)

    val p2PointText: String
        get() = if (isTiebreak) p2TiebreakPoints.toString() else pointToString(p2PointIndex, p1PointIndex)

    private fun pointToString(myPoint: Int, opponentPoint: Int): String {
        if (myPoint >= 3 && opponentPoint >= 3) {
            return when {
                myPoint == opponentPoint -> "40" // Deuce
                myPoint > opponentPoint -> "AD"
                else -> "40"
            }
        }
        return when (myPoint) {
            0 -> "0"
            1 -> "15"
            2 -> "30"
            3 -> "40"
            else -> "40"
        }
    }
}

class TennisScoreViewModel : ViewModel() {
    private val _scoreState = MutableStateFlow(ScoreState())
    val scoreState: StateFlow<ScoreState> = _scoreState

    private val _matchHistory = MutableStateFlow<List<MatchRecord>>(emptyList())
    val matchHistory: StateFlow<List<MatchRecord>> = _matchHistory

    private val historyStack = Stack<ScoreState>()

    fun addPoint(isPlayerOne: Boolean) {
        val current = _scoreState.value
        if (current.isGameOver) return

        historyStack.push(current)

        _scoreState.update { state ->
            if (state.isTiebreak) {
                handleTiebreakPoint(state, isPlayerOne)
            } else {
                handleRegularPoint(state, isPlayerOne)
            }
        }
    }

    private fun handleRegularPoint(state: ScoreState, isPlayerOne: Boolean): ScoreState {
        var p1P = state.p1PointIndex
        var p2P = state.p2PointIndex
        var p1G = state.p1Games
        var p2G = state.p2Games

        if (isPlayerOne) p1P++ else p2P++

        if (state.isNoAdMode) {
            if (p1P >= 4 || p2P >= 4) {
                if (isPlayerOne) p1G++ else p2G++
                return checkSetStatus(state.copy(p1PointIndex = 0, p2PointIndex = 0, p1Games = p1G, p2Games = p2G))
            }
        } else {
            if (p1P >= 3 && p2P >= 3) {
                if (p1P == p2P) {
                    p1P = 3
                    p2P = 3
                } else if (abs(p1P - p2P) >= 2) {
                    if (p1P > p2P) p1G++ else p2G++
                    return checkSetStatus(state.copy(p1PointIndex = 0, p2PointIndex = 0, p1Games = p1G, p2Games = p2G))
                }
            } else if (p1P >= 4 || p2P >= 4) {
                if (isPlayerOne) p1G++ else p2G++
                return checkSetStatus(state.copy(p1PointIndex = 0, p2PointIndex = 0, p1Games = p1G, p2Games = p2G))
            }
        }

        return state.copy(p1PointIndex = p1P, p2PointIndex = p2P)
    }

    private fun handleTiebreakPoint(state: ScoreState, isPlayerOne: Boolean): ScoreState {
        var p1Tb = state.p1TiebreakPoints
        var p2Tb = state.p2TiebreakPoints
        if (isPlayerOne) p1Tb++ else p2Tb++

        val tbWon = if (isPlayerOne) (p1Tb >= 7 && p1Tb - p2Tb >= 2) else (p2Tb >= 7 && p2Tb - p1Tb >= 2)

        return if (tbWon) {
            val p1G = if (isPlayerOne) state.p1Games + 1 else state.p1Games
            val p2G = if (!isPlayerOne) state.p2Games + 1 else state.p2Games
            checkSetStatus(state.copy(p1TiebreakPoints = 0, p2TiebreakPoints = 0, p1Games = p1G, p2Games = p2G, isTiebreak = false))
        } else {
            state.copy(p1TiebreakPoints = p1Tb, p2TiebreakPoints = p2Tb)
        }
    }

    // LOGIKA DINAMIS PENENTUAN PEMENANG SET
    private fun checkSetStatus(state: ScoreState): ScoreState {
        val p1G = state.p1Games
        val p2G = state.p2Games
        val target = state.targetGames

        // Masuk Tiebreak jika game sama di batas target (misal 6-6 atau 4-4)
        if (p1G == target && p2G == target) {
            return state.copy(isTiebreak = true)
        }

        // Pemenang set jika mencapai target selisih 2, atau menang tiebreak (target + 1)
        if ((p1G >= target && p1G - p2G >= 2) || (p1G == target + 1 && p2G == target)) {
            return state.copy(isGameOver = true, winnerName = state.player1Name)
        }
        if ((p2G >= target && p2G - p1G >= 2) || (p2G == target + 1 && p1G == target)) {
            return state.copy(isGameOver = true, winnerName = state.player2Name)
        }

        return state
    }

    fun saveCurrentMatch() {
        val current = _scoreState.value
        if (current.p1Games == 0 && current.p2Games == 0) return

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val record = MatchRecord(
            team1 = current.player1Name,
            team2 = current.player2Name,
            gamesTeam1 = current.p1Games,
            gamesTeam2 = current.p2Games,
            targetGames = current.targetGames,
            mode = if (current.isNoAdMode) "Golden Point (No-Ad)" else "Advantage Mode",
            dateText = dateFormat.format(Date())
        )

        _matchHistory.update { listOf(record) + it }
    }

    fun formatWhatsAppMessage(record: MatchRecord): String {
        val winner = when {
            record.gamesTeam1 > record.gamesTeam2 -> record.team1
            record.gamesTeam2 > record.gamesTeam1 -> record.team2
            else -> "Seri"
        }
        return """
            🎾 *KSTC TENNIS MATCH RESULT* 🎾
            
            🏆 *Pemenang:* $winner
            
            🥇 *${record.team1}*: ${record.gamesTeam1} Game(s)
            🥈 *${record.team2}*: ${record.gamesTeam2} Game(s)
            
            🎯 *Target Set:* ${record.targetGames} Games
            ⚙️ *Mode:* ${record.mode}
            📅 *Waktu:* ${record.dateText}
            
            _Recorded via Cash Logger KSTC_
        """.trimIndent()
    }

    // UBAH PENGATURAN PERTANDINGAN DENGAN TARGET GAME DINAMIS
    fun updateMatchSettings(team1: String, team2: String, isNoAd: Boolean, targetGames: Int) {
        val validTarget = targetGames.coerceAtLeast(1)
        _scoreState.update {
            it.copy(
                player1Name = team1.ifBlank { "Tim A" },
                player2Name = team2.ifBlank { "Tim B" },
                isNoAdMode = isNoAd,
                targetGames = validTarget
            )
        }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            _scoreState.value = historyStack.pop()
        }
    }

    fun resetMatch() {
        historyStack.clear()
        val current = _scoreState.value
        _scoreState.value = ScoreState(
            player1Name = current.player1Name,
            player2Name = current.player2Name,
            isNoAdMode = current.isNoAdMode,
            targetGames = current.targetGames
        )
    }
}