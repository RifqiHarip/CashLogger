package com.rifqi.uangkas.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack
import kotlin.math.abs

data class ScoreState(
    val player1Name: String = "Tim A",
    val player2Name: String = "Tim B",
    val p1PointIndex: Int = 0, // 0=0, 1=15, 2=30, 3=40, 4=AD
    val p2PointIndex: Int = 0,
    val p1Games: Int = 0,
    val p2Games: Int = 0,
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
            // MODE GOLDEN POINT (NO-AD): Saat 40-40, poin berikutnya langsung menang Game
            if (p1P >= 4 || p2P >= 4) {
                if (isPlayerOne) p1G++ else p2G++
                return checkSetStatus(state.copy(p1PointIndex = 0, p2PointIndex = 0, p1Games = p1G, p2Games = p2G))
            }
        } else {
            // MODE ADVANTAGE (STANDARD AD)
            if (p1P >= 3 && p2P >= 3) {
                if (p1P == p2P) {
                    // Jika poin sama setelah AD (kembali ke Deuce 40-40)
                    p1P = 3
                    p2P = 3
                } else if (abs(p1P - p2P) >= 2) {
                    // Menang Game jika selisih 2 poin setelah Deuce/AD
                    if (p1P > p2P) p1G++ else p2G++
                    return checkSetStatus(state.copy(p1PointIndex = 0, p2PointIndex = 0, p1Games = p1G, p2Games = p2G))
                }
            } else if (p1P >= 4 || p2P >= 4) {
                // Menang Game standar sebelum Deuce (misal 40-15 -> Win)
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

    private fun checkSetStatus(state: ScoreState): ScoreState {
        val p1G = state.p1Games
        val p2G = state.p2Games

        if (p1G == 6 && p2G == 6) {
            return state.copy(isTiebreak = true)
        }

        if ((p1G >= 6 && p1G - p2G >= 2) || (p1G == 7 && p2G == 6)) {
            return state.copy(isGameOver = true, winnerName = state.player1Name)
        }
        if ((p2G >= 6 && p2G - p1G >= 2) || (p2G == 7 && p1G == 6)) {
            return state.copy(isGameOver = true, winnerName = state.player2Name)
        }

        return state
    }

    // Ubah Nama Tim dan Mode Permainan
    fun updateMatchSettings(team1: String, team2: String, isNoAd: Boolean) {
        _scoreState.update {
            it.copy(
                player1Name = team1.ifBlank { "Tim A" },
                player2Name = team2.ifBlank { "Tim B" },
                isNoAdMode = isNoAd
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
            isNoAdMode = current.isNoAdMode
        )
    }
}