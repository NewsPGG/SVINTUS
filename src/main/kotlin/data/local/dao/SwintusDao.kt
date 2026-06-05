package data.local.dao

import data.local.entities.PlayerStatsEntity
import data.local.entities.GameHistoryEntity
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SwintusDao {
    private val dbFile = File(System.getProperty("user.home"), ".swintus_game.db")

    private fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
    }

    init {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        playerId TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        gamesPlayed INTEGER NOT NULL,
                        wins INTEGER NOT NULL,
                        rating INTEGER NOT NULL
                    )
                """.trimIndent())

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS game_history (
                        gameId TEXT PRIMARY KEY,
                        endTime INTEGER NOT NULL,
                        winnerId TEXT NOT NULL,
                        winnerName TEXT NOT NULL,
                        totalTurns INTEGER NOT NULL,
                        playersList TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
    }

    fun getPlayerStats(playerId: String): PlayerStatsEntity? {
        return getConnection().use { conn ->
            val sql = "SELECT * FROM player_stats WHERE playerId = ?"
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, playerId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    PlayerStatsEntity(
                        playerId = rs.getString("playerId"),
                        username = rs.getString("username"),
                        gamesPlayed = rs.getInt("gamesPlayed"),
                        wins = rs.getInt("wins"),
                        rating = rs.getInt("rating")
                    )
                } else null
            }
        }
    }

    fun insertOrUpdatePlayer(stats: PlayerStatsEntity) {
        getConnection().use { conn ->
            val sql = """
                INSERT OR REPLACE INTO player_stats (playerId, username, gamesPlayed, wins, rating) 
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, stats.playerId)
                stmt.setString(2, stats.username)
                stmt.setInt(3, stats.gamesPlayed)
                stmt.setInt(4, stats.wins)
                stmt.setInt(5, stats.rating)
                stmt.executeUpdate()
            }
        }
    }

    fun getAllPlayersStats(): List<PlayerStatsEntity> {
        val list = mutableListOf<PlayerStatsEntity>()
        getConnection().use { conn ->
            val sql = "SELECT * FROM player_stats"
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    list.add(
                        PlayerStatsEntity(
                            playerId = rs.getString("playerId"),
                            username = rs.getString("username"),
                            gamesPlayed = rs.getInt("gamesPlayed"),
                            wins = rs.getInt("wins"),
                            rating = rs.getInt("rating")
                        )
                    )
                }
            }
        }
        return list
    }

    fun insertGameHistory(history: GameHistoryEntity) {
        getConnection().use { conn ->
            val sql = """
                INSERT INTO game_history (gameId, endTime, winnerId, winnerName, totalTurns, playersList) 
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, history.gameId)
                stmt.setLong(2, history.endTime)
                stmt.setString(3, history.winnerId)
                stmt.setString(4, history.winnerName)
                stmt.setInt(5, history.totalTurns)
                stmt.setString(6, history.playersList)
                stmt.executeUpdate()
            }
        }
    }

    fun getGameHistory(): List<GameHistoryEntity> {
        val list = mutableListOf<GameHistoryEntity>()
        getConnection().use { conn ->
            val sql = "SELECT * FROM game_history"
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    list.add(
                        GameHistoryEntity(
                            gameId = rs.getString("gameId"),
                            endTime = rs.getLong("endTime"),
                            winnerId = rs.getString("winnerId"),
                            winnerName = rs.getString("winnerName"),
                            totalTurns = rs.getInt("totalTurns"),
                            playersList = rs.getString("playersList")
                        )
                    )
                }
            }
        }
        return list
    }
}