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

// Model Data Catatan Poin Setiap Game Selesai
data class GameLog(
    val gameNumber: Int,
    val setNumber: Int,
    val winnerName: String,
    val pointSequence: List<String> // Catatan urutan poin (contoh: ["15-0", "15-15", "15-30", "15-40", "30-40", "Game Tim B"])
)

// Model Data Skor per Set
data class SetScore(
    val setNumber: Int,
    val gamesTeam1: Int,
    val gamesTeam2: Int
)

// Model Data Riwayat Pertandingan Lengkap
data class MatchRecord(
    val id: Long = System.currentTimeMillis(),
    val team1: String,
    val team2: String,
    val setsTeam1: Int,
    val setsTeam2: Int,
    val completedSets: List<SetScore>,
    val gameLogs: List<GameLog> = emptyList(), // 👈 Rincian Poin per Game
    val targetGames: Int = 6,
    val targetSets: Int = 1,
    val mode: String,
    val dateText: String
) {
    val setScoresDetail: String
        get() = if (completedSets.isNotEmpty()) {
            completedSets.joinToString(" | ") { "Set ${it.setNumber}: ${it.gamesTeam1}-${it.gamesTeam2}" }
        } else {
            "Set 1: $setsTeam1-$setsTeam2"
        }
}

data class ScoreState(
    val player1Name: String = "Tim A",
    val player2Name: String = "Tim B",
    val p1PointIndex: Int = 0,
    val p2PointIndex: Int = 0,
    val p1Games: Int = 0,
    val p2Games: Int = 0,
    val p1Sets: Int = 0,
    val p2Sets: Int = 0,
    val currentGamePoints: List<String> = emptyList(), // 👈 Tracker Poin Game Berjalan (e.g. ["15-0", "15-15", ...])
    val gameLogs: List<GameLog> = emptyList(), // 👈 Daftar Log Semua Game
    val completedSets: List<SetScore> = emptyList(),
    val targetGames: Int = 6,
    val targetSets: Int = 1,
    val isNoAdMode: Boolean = true,
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

    fun pointToString(myPoint: Int, opponentPoint: Int): String {
        if (myPoint >= 3 && opponentPoint >= 3) {
            return when {
                myPoint == opponentPoint -> "40"
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

        // Catat Poin Saat Ini ke Urutan Game Berjalan
        val currentScoreDisplay = "${state.pointToString(p1P, p2P)}-${state.pointToString(p2P, p1P)}"
        val updatedCurrentGamePoints = state.currentGamePoints + currentScoreDisplay

        var gameWon = false
        var gameWinner = ""

        if (state.isNoAdMode) {
            if (p1P >= 4 || p2P >= 4) {
                gameWon = true
                if (isPlayerOne) { p1G++; gameWinner = state.player1Name } else { p2G++; gameWinner = state.player2Name }
            }
        } else {
            if (p1P >= 3 && p2P >= 3) {
                if (p1P == p2P) {
                    p1P = 3
                    p2P = 3
                } else if (abs(p1P - p2P) >= 2) {
                    gameWon = true
                    if (p1P > p2P) { p1G++; gameWinner = state.player1Name } else { p2G++; gameWinner = state.player2Name }
                }
            } else if (p1P >= 4 || p2P >= 4) {
                gameWon = true
                if (isPlayerOne) { p1G++; gameWinner = state.player1Name } else { p2G++; gameWinner = state.player2Name }
            }
        }

        return if (gameWon) {
            // Rekam Log Game ke Riwayat
            val newGameLog = GameLog(
                gameNumber = state.p1Games + state.p2Games + 1,
                setNumber = state.completedSets.size + 1,
                winnerName = gameWinner,
                pointSequence = updatedCurrentGamePoints
            )

            checkSetStatus(
                state.copy(
                    p1PointIndex = 0,
                    p2PointIndex = 0,
                    p1Games = p1G,
                    p2Games = p2G,
                    currentGamePoints = emptyList(),
                    gameLogs = state.gameLogs + newGameLog
                )
            )
        } else {
            state.copy(p1PointIndex = p1P, p2PointIndex = p2P, currentGamePoints = updatedCurrentGamePoints)
        }
    }

    private fun handleTiebreakPoint(state: ScoreState, isPlayerOne: Boolean): ScoreState {
        var p1Tb = state.p1TiebreakPoints
        var p2Tb = state.p2TiebreakPoints
        if (isPlayerOne) p1Tb++ else p2Tb++

        val tbScoreDisplay = "TB $p1Tb-$p2Tb"
        val updatedCurrentGamePoints = state.currentGamePoints + tbScoreDisplay

        val tbWon = if (isPlayerOne) (p1Tb >= 7 && p1Tb - p2Tb >= 2) else (p2Tb >= 7 && p2Tb - p1Tb >= 2)

        return if (tbWon) {
            val p1G = if (isPlayerOne) state.p1Games + 1 else state.p1Games
            val p2G = if (!isPlayerOne) state.p2Games + 1 else state.p2Games
            val gameWinner = if (isPlayerOne) state.player1Name else state.player2Name

            val newGameLog = GameLog(
                gameNumber = state.p1Games + state.p2Games + 1,
                setNumber = state.completedSets.size + 1,
                winnerName = gameWinner,
                pointSequence = updatedCurrentGamePoints
            )

            checkSetStatus(
                state.copy(
                    p1TiebreakPoints = 0,
                    p2TiebreakPoints = 0,
                    p1Games = p1G,
                    p2Games = p2G,
                    isTiebreak = false,
                    currentGamePoints = emptyList(),
                    gameLogs = state.gameLogs + newGameLog
                )
            )
        } else {
            state.copy(p1TiebreakPoints = p1Tb, p2TiebreakPoints = p2Tb, currentGamePoints = updatedCurrentGamePoints)
        }
    }

    private fun checkSetStatus(state: ScoreState): ScoreState {
        val p1G = state.p1Games
        val p2G = state.p2Games
        val target = state.targetGames

        if (p1G == target && p2G == target) {
            return state.copy(isTiebreak = true)
        }

        val p1SetWon = (p1G >= target && p1G - p2G >= 2) || (p1G == target + 1 && p2G == target)
        val p2SetWon = (p2G >= target && p2G - p1G >= 2) || (p2G == target + 1 && p2G == target)

        if (p1SetWon || p2SetWon) {
            val newP1Sets = if (p1SetWon) state.p1Sets + 1 else state.p1Sets
            val newP2Sets = if (p2SetWon) state.p2Sets + 1 else state.p2Sets
            val newSetRecord = SetScore(
                setNumber = state.completedSets.size + 1,
                gamesTeam1 = p1G,
                gamesTeam2 = p2G
            )
            val updatedCompletedSets = state.completedSets + newSetRecord

            val setsNeededToWin = if (state.targetSets > 1) (state.targetSets / 2) + 1 else 1
            val isMatchOver = newP1Sets >= setsNeededToWin || newP2Sets >= setsNeededToWin

            return state.copy(
                p1Games = 0,
                p2Games = 0,
                p1PointIndex = 0,
                p2PointIndex = 0,
                p1Sets = newP1Sets,
                p2Sets = newP2Sets,
                completedSets = updatedCompletedSets,
                isGameOver = isMatchOver,
                winnerName = if (isMatchOver) (if (newP1Sets > newP2Sets) state.player1Name else state.player2Name) else null
            )
        }

        return state
    }

    fun saveCurrentMatch() {
        val current = _scoreState.value
        if (current.p1Games == 0 && current.p2Games == 0 && current.completedSets.isEmpty()) return

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val setsList = if (current.completedSets.isEmpty()) {
            listOf(SetScore(1, current.p1Games, current.p2Games))
        } else {
            current.completedSets
        }

        val record = MatchRecord(
            team1 = current.player1Name,
            team2 = current.player2Name,
            setsTeam1 = current.p1Sets,
            setsTeam2 = current.p2Sets,
            completedSets = setsList,
            gameLogs = current.gameLogs,
            targetGames = current.targetGames,
            targetSets = current.targetSets,
            mode = if (current.isNoAdMode) "Golden Point (No-Ad)" else "Advantage Mode",
            dateText = dateFormat.format(Date())
        )

        _matchHistory.update { listOf(record) + it }
    }

    // FORMAT PESAN WHATSAPP RINCIAN SKOR LENGKAP BERSAMA ALUR POIN
    fun formatWhatsAppMessage(record: MatchRecord): String {
        val winner = when {
            record.setsTeam1 > record.setsTeam2 -> record.team1
            record.setsTeam2 > record.setsTeam1 -> record.team2
            else -> "Seri"
        }

        val gameLogsDetail = if (record.gameLogs.isNotEmpty()) {
            "\n📝 *Rincian Per Game:*\n" + record.gameLogs.joinToString("\n") { log ->
                "• Game ${log.gameNumber} (Set ${log.setNumber}) [${log.winnerName}]: ${log.pointSequence.joinToString(" ➔ ")}"
            }
        } else ""

        return """
            🎾 *KSTC TENNIS MATCH RESULT* 🎾
            
            🏆 *Pemenang:* $winner
            
            🥇 *${record.team1}*: ${record.setsTeam1} Set
            🥈 *${record.team2}*: ${record.setsTeam2} Set
            
            📊 *Skor Set:* ${record.setScoresDetail}$gameLogsDetail
            
            ⚙️ *Mode:* ${record.mode}
            📅 *Waktu:* ${record.dateText}
            
            _Recorded via Cash Logger KSTC_
        """.trimIndent()
    }

    fun updateMatchSettings(team1: String, team2: String, isNoAd: Boolean, targetGames: Int, targetSets: Int) {
        val validTargetG = targetGames.coerceAtLeast(1)
        val validTargetS = targetSets.coerceAtLeast(1)
        _scoreState.update {
            it.copy(
                player1Name = team1.ifBlank { "Tim A" },
                player2Name = team2.ifBlank { "Tim B" },
                isNoAdMode = isNoAd,
                targetGames = validTargetG,
                targetSets = validTargetS
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
            targetGames = current.targetGames,
            targetSets = current.targetSets
        )
    }
}