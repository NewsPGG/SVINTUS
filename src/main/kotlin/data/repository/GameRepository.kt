package data.repository

import Application.PlayerProfile
import data.local.dao.SwintusDao
import data.local.entities.GameHistoryEntity
import data.local.entities.PlayerStatsEntity
import java.util.UUID

class GameRepository(private val dao: SwintusDao) {

    suspend fun saveGameResult(winnerId: UUID, winnerName: String, totalTurns: Int, players: List<PlayerProfile>) {
        val gameHistory = GameHistoryEntity(
            gameId = UUID.randomUUID().toString(),
            endTime = System.currentTimeMillis(),
            winnerId = winnerId.toString(),
            winnerName = winnerName,
            totalTurns = totalTurns,
            playersList = players.joinToString(",") { it.id.toString() }
        )
        dao.insertGameHistory(gameHistory)

        for (player in players) {
            val currentStats = dao.getPlayerStats(player.id.toString())
                ?: PlayerStatsEntity(player.id.toString(), player.username, 0, 0, 0)

            val isWinner = player.id == winnerId
            val newWins = if (isWinner) currentStats.wins + 1 else currentStats.wins
            val newRating = if (isWinner) currentStats.rating + 5 else maxOf(0, currentStats.rating - 2)

            dao.insertOrUpdatePlayer(
                PlayerStatsEntity(
                    playerId = player.id.toString(),
                    username = player.username,
                    gamesPlayed = currentStats.gamesPlayed + 1,
                    wins = newWins,
                    rating = newRating
                )
            )
        }
    }

    suspend fun loadLeaderboard(): List<PlayerProfile> {
        return dao.getAllPlayersStats().map {
            PlayerProfile(
                id = java.util.UUID.fromString(it.playerId),
                username = it.username,
                rating = it.rating,
                gamesPlayed = it.gamesPlayed,
                wins = it.wins
            )
        }
    }
}