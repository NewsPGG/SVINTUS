classDiagram
    direction TB

    class GameRepository {
        -SwintusDao dao
        +saveGameResult(winnerId, winnerName, totalTurns, profiles) List~PlayerProfile~
        +loadLeaderboard() List~PlayerProfile~
    }

    class SwintusDao {
        -Connection connection
        +getAllPlayersStats() List~PlayerStatsEntity~
        +insertPlayerStats(entity PlayerStatsEntity)
        +updatePlayerStats(entity PlayerStatsEntity)
        +insertGameHistory(entity GameHistoryEntity)
    }

    class PlayerStatsEntity {
        +UUID playerId
        +String username
        +int rating
        +int gamesPlayed
        +int wins
    }

    class GameHistoryEntity {
        +UUID gameId
        +UUID winnerId
        +int totalTurns
        +String dateTime
    }

    class SQLiteDatabase {
        <<Database>>
        +swintus.db
        -- Tables --
        +player_stats
        +game_history
    }

    GameRepository o-- SwintusDao
    SwintusDao --> SQLiteDatabase
    SwintusDao ..> PlayerStatsEntity
    SwintusDao ..> GameHistoryEntity   