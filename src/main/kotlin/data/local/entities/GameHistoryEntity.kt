package data.local.entities

data class GameHistoryEntity(
    val gameId: String,
    val endTime: Long,
    val winnerId: String,
    val winnerName: String,
    val totalTurns: Int,
    val playersList: String
)